package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import ru.hogoshi.Animation;
import im.expensive.Expensive;
import im.expensive.command.staffs.StaffStorage;
import im.expensive.events.EventDisplay;
import im.expensive.events.EventUpdate;
import im.expensive.functions.impl.render.HUD;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.display.ElementUpdater;
import im.expensive.ui.styles.Style;
import im.expensive.utils.drag.Dragging;
import im.expensive.utils.math.Vector4i;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffListRenderer implements ElementRenderer, ElementUpdater {

    final Dragging dragging;
    final ResourceLocation logo = new ResourceLocation("expensive/images/HUD2/staff.png");
    final float iconSize = 10;

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");
    private final Animation animation = new Animation();

    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6.5f;
        MatrixStack ms = eventDisplay.getMatrixStack();
        ITextComponent name = GradientUtil.gradient("StaffList");


        Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

        //DisplayUtils.drawShadow(posX, posY, width, height, 10, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
        Vector4i colors = new Vector4i(HUD.getColor(0, 1), HUD.getColor(90, 1), HUD.getColor(180, 1), HUD.getColor(270, 1));
        drawStyledRect(posX, posY, width, height, 4);
        DisplayUtils.drawImage(logo, posX + padding, posY + padding, iconSize, iconSize, colors);
        Fonts.sfui.drawCenteredText(ms, name, posX + width / 2, posY + padding + 1f, fontSize);

        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        DisplayUtils.drawRectHorizontalW(posX + 0.5f, posY, width - 1, 2.5f, 3, ColorUtils.rgba(0, 0, 0, (int) (255 * 0.25f)));
        DisplayUtils.drawRectVerticalW(posX + 20.0f, posY - 13.5f, 1, 14.0f, 3, ColorUtils.rgba(0, 0, 0, (int) (255 * 0.75f)));
        posY += 3.5f;
        for (StaffListRenderer.Staff f : staffPlayers) {

            ITextComponent prefix = f.getPrefix();
            float prefixWidth = Fonts.sfMedium.getWidth(prefix, fontSize);
            String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
            float nameWidth = Fonts.sfMedium.getWidth(staff, fontSize);


            float localWidth = prefixWidth + nameWidth + Fonts.sfMedium.getWidth(f.getStatus().string, fontSize) + padding * 3;

            Fonts.sfMedium.drawText(ms, prefix, posX + padding, posY, fontSize, 255);
            Fonts.sfMedium.drawText(ms, staff, posX + padding + prefixWidth, posY, ColorUtils.rgb(255,255,255), fontSize);
            Fonts.sfMedium.drawText(ms, f.getStatus().string, posX + width - padding - Fonts.sfMedium.getWidth(f.getStatus().string, fontSize), posY, f.getStatus().color, fontSize);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding;
            localHeight += fontSize + padding;
        }

        width = Math.max(maxWidth, 80);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", -1),
        VANISHED("V", ColorUtils.rgb(254, 68, 68));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {

        //DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.getColor(0)); // outline
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(0, 0, 0, 215));
    }
}
