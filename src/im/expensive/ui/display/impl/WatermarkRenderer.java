package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.functions.impl.render.HUD;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.client.ServerTPS;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;
import im.expensive.utils.player.MoveUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

    final Minecraft mc = Minecraft.getInstance();


    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float posX = 5;
        float posY = 4;
        float posX12 = 18;
        float posY12 = 4;
        float padding = 5;
        float fontSize = 6.5f;
        float iconSize = 10;
        float posX1 = 65;
        float posY1 = 4;

        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();
        String fps1 = String.valueOf(mc.debugFPS);


        drawStyledRect(posX, posY, iconSize + padding * 13.5f, iconSize + padding * 1.3f, 4);
//        DisplayUtils.drawImage(logo, posX + padding, posY - 2.0f + padding, iconSize, iconSize , ColorUtils.rgb(255, 255, 255));
        DisplayUtils.drawRectVerticalW(posX + 17.5f, posY + 3.5f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 115), ColorUtils.rgba(70, 70, 70, 115));
       //CLIENT NAME
        Fonts.sfMedium.drawText(ms, "Zawe Client", posX + 19.5f, posY + 3.5f, HUD.getColor(0), fontSize);
        Fonts.icons2.drawText(ms, "B", posX + 4.0f, posY + 2f, HUD.getColor(0), fontSize + 3.5f);

        int fps = mc.getDebugFPS();
        ITextComponent fpsText = GradientUtil.gradient(String.valueOf(fps));
        float fpsTextWidth = Fonts.sfMedium.getWidth(fpsText, fontSize);
        float fpsPosX = posX + iconSize + padding * 3;
        if (fps >= 100 && fps <= 999) {
            posX1 += 5;
        }

        String bps = (int) mc.player.getPosX() + " " + (int) mc.player.getPosY() + " " + (int) mc.player.getPosZ();

        String name = mc.player.getName().getString();
        String username = "User";
        float namewidht = Fonts.sfMedium.getWidth(username, fontSize);


        float fpsTextWidth1 = Fonts.sfMedium.getWidth(bps, fontSize);

        float fpsWidth = Fonts.sfMedium.getWidth(String.valueOf(fps), fontSize);

        drawStyledRect(posX + 0.1f, posY + 15, iconSize + padding * 3.2f + fpsTextWidth1, iconSize + padding * 1.3f, 4);
        //COORDS
        DisplayUtils.drawRectVerticalW(posX + 13.9f, posY + 18.0f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 215), ColorUtils.rgba(70, 70, 70, 215));
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), bps, posX + 18, posY + 18.8f, ColorUtils.rgb(210, 210, 210), fontSize, 0.05f);
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "S", posX + 4, posY + 18.1f, HUD.getColor(0), fontSize + 1, 0.05f);

        drawStyledRect(posX + 23 + fpsTextWidth1, posY + 15, iconSize + padding * 10.5f, iconSize + padding * 1.3f, 4);
        //TPS
        DisplayUtils.drawRectVerticalW(posX + 37 + fpsTextWidth1, posY + 18.0f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 215), ColorUtils.rgba(70, 70, 70, 215));
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), " Ticks", posX + 40 + fpsTextWidth1, posY + 18.8f, ColorUtils.rgb(210, 210, 210), fontSize - 0.4f, 0.05f);
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "V", posX + 27 + fpsTextWidth1, posY + 18.1f, HUD.getColor(0), fontSize + 1, 0.05f);

        drawStyledRect(posX + 83 + fpsTextWidth1, posY + 15, iconSize + padding * 9.2f, iconSize + padding * 1.3f, 4);
        //BPS
        DisplayUtils.drawRectVerticalW(posX + 95.5f + fpsTextWidth1, posY + 18.0f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 255), ColorUtils.rgba(70, 70, 70, 255));
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), " BPS", posX + 98 + fpsTextWidth1, posY + 18.8f, ColorUtils.rgb(210, 210, 210), fontSize - 0.2f, 0.05f);
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "M", posX + 86 + fpsTextWidth1, posY + 18.1f, HUD.getColor(0), fontSize + 1, 0.05f);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(new Date());
        String time = timeString;
        float timeTextWidth = Fonts.sfui.getWidth(time, fontSize);
        drawStyledRect(posX12 + 62.1f, posY + 0, iconSize + padding + 52 + namewidht + fpsWidth + timeTextWidth, iconSize + padding * 1.3f, 4);
//POLOSKA fps / time
        DisplayUtils.drawRectVerticalW(posX12 + 106.9f + +namewidht + fpsWidth, posY + 3.0f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 215), ColorUtils.rgba(70, 70, 70, 215));
//POLOSKA name / fps
        DisplayUtils.drawRectVerticalW(posX12 + 76.9f + +namewidht, posY + 3.0f, 0.5f, 7, ColorUtils.rgba(70, 70, 70, 215), ColorUtils.rgba(70, 70, 70, 215));
//NAME
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), username, posX + 87, posY + 3.5f, ColorUtils.rgb(210, 210, 210), fontSize, 0.05f);
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "O", posX12 + 65.5f, posY + 3.1f, HUD.getColor(0), fontSize + 1, 0.05f);
//FPS
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), fps + " FPS", posX12 + 89 + namewidht, posY + 3.5f, ColorUtils.rgba(210, 210, 210, 255), fontSize + 0, 0.05f);
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "P", posX12 + 79 + namewidht, posY + 3.1f, HUD.getColor(0), fontSize + 1, 0.05f);
//TIME
        Fonts.wasafont.drawText(eventDisplay.getMatrixStack(), "N", posX12 + 109 + namewidht + fpsWidth, posY + 3.1f, HUD.getColor(0), fontSize + 1, 0.05f);
        Fonts.sfMedium.drawText(eventDisplay.getMatrixStack(), time, posX12 + 119 + namewidht + fpsWidth, posY + 3.5f, ColorUtils.rgba(210, 210, 210, 255), fontSize + 0, 0.05f);


    }


    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawRoundedRect(x, y, width + -4, height + -3, radius, ColorUtils.rgba(0, 0, 0, 150));
        DisplayUtils.drawRoundedRect(x, y, width + -4, height + -3, radius, ColorUtils.rgba(0, 0, 0, 150));
    }
}