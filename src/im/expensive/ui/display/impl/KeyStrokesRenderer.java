package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import im.expensive.events.EventDisplay;
import im.expensive.functions.impl.render.HUD;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.math.Vector4i;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.Scissor;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeyStrokesRenderer implements ElementRenderer {
    private StopWatch time = new StopWatch();
    private final Dragging dragging;
    private float width;
    private float height;

    public void render(EventDisplay e) {
        MatrixStack mv = e.getMatrixStack();
        float posX = this.dragging.getX();
        float posY = this.dragging.getY();
        float padding = 3.0F;
        float fontSize = 8.0F;
        float textPadding = 23.0F;
        ITextComponent w = GradientUtil.gradient("W");
        ITextComponent a = GradientUtil.gradient("A");
        ITextComponent s = GradientUtil.gradient("S");
        ITextComponent d = GradientUtil.gradient("D");
        ITextComponent pr = GradientUtil.gradient("SPACE");
        this.drawRect(posX, posY, this.width - 2.0F, this.height);
        Fonts.sfMedium.drawCenteredText(mv, w, posX + 43.0F, posY + padding - 25.0F, fontSize);
        Fonts.sfMedium.drawCenteredText(mv, a, posX - textPadding + 35.0F, posY + padding + 6.0F, fontSize);
        Fonts.sfMedium.drawText(mv, pr, posX + 27.0F, posY + 38.5F, fontSize, 255);
        Fonts.sfMedium.drawCenteredText(mv, s, posX + 43.0F, posY + padding + 6.0F, fontSize);
        Fonts.sfMedium.drawCenteredText(mv, d, posX + textPadding + 50.0F, posY + padding + 6.0F, fontSize);
        this.onClick(posX, posY, this.width - 2.0F, this.height);
        float maxWidth = Fonts.sfMedium.getWidth(w, fontSize) + padding * 2.0F;
        float localHeight = fontSize + padding * 2.0F;
        Scissor.push();
        Scissor.setFromComponentCoordinates((double)posX, (double)posY, (double)(this.width - 2.0F), (double)this.height);
        Scissor.unset();
        Scissor.pop();
        this.width = Math.max(maxWidth, 80.0F);
        this.height = localHeight + 2.5F;
        this.dragging.setWidth(this.width);
        this.dragging.setHeight(this.height);

    }

    public void drawRect(float x, float y, float width, float height) {
        final Vector4i vector4i = new Vector4i(HUD.getColor(0, 1), HUD.getColor(0, 1), HUD.getColor(90, 1), HUD.getColor(90, 1));
        width = 28.0F;
        DisplayUtils.drawRoundedRect(x + 30, y, width, height + 10,4, ColorUtils.rgba(0, 0, 0, 215));//S

        DisplayUtils.drawRoundedRect(x + 30, y - 30, width, height + 10,4, ColorUtils.rgba(0, 0, 0, 215));//W

        DisplayUtils.drawRoundedRect(x, y + 30, width + 60, height + 7,4, ColorUtils.rgba(0, 0, 0, 215));//space

        DisplayUtils.drawRoundedRect(x, y, width, height + 10, 4, ColorUtils.rgba(0, 0, 0, 215));//A

        DisplayUtils.drawRoundedRect(x + 60, y, width, height + 10,4, ColorUtils.rgba(0, 0, 0, 215));//D


        DisplayUtils.drawShadow(x, y + 55, width + 60, height - 10, 7, HUD.getColor(1), HUD.getColor(0));

        DisplayUtils.drawRoundedRect(x, y + 55, width + 60, height - 10, new Vector4f(4.0f, 4.0f, 4.0f, 4.0f), vector4i);//GRADIENT LINE
    }

    public void onClick(float x, float y, float width, float height) {
        width = 28.0F;
        if (mc.gameSettings.keyBindBack.pressed) {
            DisplayUtils.drawRoundedRect(x + 30,y,width,height + 10,4,ColorUtils.rgba(225,225,225,150));//S
        }

        if (mc.gameSettings.keyBindJump.pressed) {
            DisplayUtils.drawRoundedRect(x,y + 30,width + 60,height + 7,4,ColorUtils.rgba(225,225,225,150));//space
        }

        if (mc.gameSettings.keyBindForward.pressed) {
            DisplayUtils.drawRoundedRect(x + 30,y - 30,width,height + 10,4,ColorUtils.rgba(225,225,225,150));//W
        }

        if (mc.gameSettings.keyBindLeft.pressed) {
            DisplayUtils.drawRoundedRect(x,y,width,height + 10,4,ColorUtils.rgba(225,225,225,150));//A
        }

        if (mc.gameSettings.keyBindRight.pressed) {
            DisplayUtils.drawRoundedRect(x + 60,y,width,height + 10,4,ColorUtils.rgba(225,225,225,150));//D
        }

    }

    public KeyStrokesRenderer(Dragging dragging) {
        this.dragging = dragging;
    }
}