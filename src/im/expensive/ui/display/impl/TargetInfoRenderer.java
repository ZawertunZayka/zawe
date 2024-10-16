package im.expensive.ui.display.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.functions.impl.render.HUD;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.animations.Animation;
import im.expensive.utils.animations.Direction;
import im.expensive.utils.animations.impl.EaseBackIn;
import im.expensive.utils.client.ClientUtil;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.math.MathUtil;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.math.Vector4i;
import im.expensive.utils.render.*;
import im.expensive.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetInfoRenderer implements ElementRenderer {
    final StopWatch stopWatch = new StopWatch();
    final Dragging drag;
    LivingEntity entity = null;
    boolean allow;
    final Animation animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;
    @Override
    public void render(EventDisplay eventDisplay) {
        entity = getTarget(entity);

        float rounding = 6;
        boolean out = !allow || stopWatch.isReached(1000);
        animation.setDuration(out ? 400 : 300);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            String name = entity.getName().getString();

            float posX = drag.getX();
            float posY = drag.getY();

            float headSize = 34.25f;
            float spacing = 5;

            float width = 172 / 1.5f;
            float height = 59 / 1.5f;
            drag.setWidth(width);
            drag.setHeight(height);
            float shrinking = 1.5f;
            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));


            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();


            healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathUtil.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);


            float animationValue = (float) animation.getOutput();

            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = posX + (width * halfAnimationValueRest);
            float testY = posY + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;
            int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());

            GlStateManager.pushMatrix();
            Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

            sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());

            //глов
            DisplayUtils.drawShadow(posX, posY, 1, 1, 11, Color.BLACK.getRGB());


            drawStyledRect(posX, posY, width, height, rounding, 255);
            //рендерится голова
            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(posX + 2.5f, posY + 2.60f , headSize, headSize,6, style.getSecondColor().getRGB() );
            Stencil.readStencilBuffer(1);
            drawTargetHead(entity, posX + 2.5f , posY + 2.5f , headSize, headSize);
            Stencil.uninitStencilBuffer();
            Vector4i colors = new Vector4i(HUD.getColor(0, 1), HUD.getColor(90, 1), HUD.getColor(180, 1), HUD.getColor(270, 1));
            Scissor.push();
            Scissor.setFromComponentCoordinates(testX, testY, testW - 6, testH);
            Fonts.sfui.drawText(eventDisplay.getMatrixStack(), entity.getName().getString(), posX + 29.0f + spacing + spacing, posY + spacing + 1, ColorUtils.rgb(255,255,255), 8);
            Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(),"HP: " + ((int) hp + ".0"), posX + 29.0f + spacing + spacing, posY + spacing + 1 + spacing + spacing,ColorUtils.rgb(255,255,255), 7);

            Scissor.unset();
            Scissor.pop();

            Vector4i vector4i = new Vector4i(Color.GREEN.getRGB(), Color.GREEN.getRGB(), Color.GREEN.getRGB(), Color.GREEN.getRGB()); //style.getFirstColor().getRGB(), //style.getSecondColor().getRGB(), //style.getSecondColor().getRGB());

            // Определение цвета в зависимости от процента здоровья
            int healthColor;
            if (hp / maxHp > 0.5) {
                // Зеленый, если здоровье более 50%
                healthColor = ColorUtils.rgb(0, 255, 0);
            } else if (hp / maxHp > 0.2) {
                // Желтый, если здоровье более 20%
                healthColor = ColorUtils.rgb(255, 255, 0);
            } else {
                // Красный, если здоровье менее 20%
                healthColor = ColorUtils.rgb(255, 0, 0);
            }

            // Использование определенного цвета при рисовании полоски здоровья
            DisplayUtils.drawRoundedRect(posX + 29.75f + spacing + spacing, posY + height - spacing * 1.80f - 3, (width - 44) * healthAnimation, 8, new Vector4f(4, 4, 4, 4), healthColor);
            GlStateManager.popMatrix();
        }
    }


    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Expensive.getInstance().getFunctionRegistry().getKillAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            drawFace(rendererManager.getEntityTexture(entity), x, y, 8F, 8F, 8F, 8F, width, height, 64F, 64F, entity);
        }
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawFace(ResourceLocation res, float d,
                         float y,
                         float u,
                         float v,
                         float uWidth,
                         float vHeight,
                         float width,
                         float height,
                         float tileWidth,
                         float tileHeight,
                         LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = (target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
        GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius, int alpha) {
        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 21, 21, 215));
    }
}