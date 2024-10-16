package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.functions.api.Function;
import im.expensive.functions.impl.render.HUD;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.client.KeyStorage;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.math.MathUtil;
import im.expensive.utils.math.Vector4i;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;

public class KeyBindRenderer implements ElementRenderer
{
    private final Dragging dragging;
    private float animation;
    private float width;
    private float height;
    final ResourceLocation bind1 = new ResourceLocation("expensive/images/HUD2/hotkeys.png");
    final float iconSize = 10;

    public void render(final EventDisplay eventDisplay) {
        final MatrixStack ms = eventDisplay.getMatrixStack();
        final float posX = this.dragging.getX();
        float posY = this.dragging.getY();
        final float fontSize = 6.5f;
        final float padding = 5.0f;
        final ITextComponent name = GradientUtil.gradient("Hotkeys");
        float maxWidth = Fonts.sfbold.getWidth(name, fontSize) + padding * 2.0f;
        final Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();


        DisplayUtils.drawRoundedRect(posX, posY + 1.0f, this.animation, 18.0f, 4.0f, ColorUtils.rgba(0, 0, 0, 215));
        Fonts.sfui.drawCenteredText(ms, name, posX + this.animation / 1.9f - 5, posY + padding + 1.5f, 6.5f);
        Vector4i colors = new Vector4i(HUD.getColor(0, 1), HUD.getColor(90, 1), HUD.getColor(180, 1), HUD.getColor(270, 1));
        DisplayUtils.drawImage(bind1, posX + padding, posY + padding, iconSize, iconSize, colors);
        DisplayUtils.drawRectVerticalW(posX + 20.0f, posY + 3.0f, width - 1, 14.0f, 3, ColorUtils.rgba(0, 0, 0, (int) (255 * 0.75f)));
        posY += fontSize + padding * 2.0f;
        float localHeight = fontSize + padding * 2.0f;
        posY += 3.0f;
        for (Function f : Expensive.getInstance().getFunctionRegistry().getFunctions()) {
            f.getAnimation().update();
            if (f.getAnimation().getValue() > 0.0) {
                if (f.getBind() == 0) {
                    continue;
                }
                final String nameText = f.getName();
                final float nameWidth = Fonts.sfbold.getWidth(nameText, fontSize);
                final String bindText = "[" + KeyStorage.getKey(f.getBind()) + "]";
                final float bindWidth = Fonts.sfbold.getWidth(bindText, fontSize);
                final float localWidth = nameWidth + bindWidth + padding * 3.0f;
                DisplayUtils.drawRoundedRect(posX, posY, this.animation, 12.0f, 3.0f, ColorUtils.rgba(0, 0, 0, (int)(215.0 * f.getAnimation().getValue())));
                Fonts.sfui.drawText(ms, nameText, posX + 4.0f, posY + 2.5f, ColorUtils.rgba(255, 255, 255, (int)(255.0 * f.getAnimation().getValue())), fontSize);
                Fonts.sfui.drawText(ms, bindText, posX + this.animation - 4.0f - bindWidth, posY + 2.5f, ColorUtils.rgba(255, 255, 255, (int)(255.0 * f.getAnimation().getValue())), fontSize);
                //DisplayUtils.drawRectVerticalW(posX + 63.0f, posY + 2.0f, width - 1, 8.0f, 3, ColorUtils.rgba(0, 0, 0, (int) (255 * 0.75f)));
                if (localWidth > maxWidth) {
                    maxWidth = localWidth;
                }
                posY += (float)((7.5f + padding) * f.getAnimation().getValue());
                localHeight += (float)((fontSize + padding) * f.getAnimation().getValue());

            }
        }
        this.animation = MathUtil.lerp(this.animation, Math.max(maxWidth, 80.0f), 10.0f);
        this.height = localHeight + 2.5f;
        this.dragging.setWidth(this.animation);
        this.dragging.setHeight(this.height);
    }

    public static void sizeAnimation(final double width, final double height, final double scale) {
        GlStateManager.translated(width, height, 0.0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0.0);
    }

    public KeyBindRenderer(final Dragging dragging) {
        this.dragging = dragging;
    }
}
