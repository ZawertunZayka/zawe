package im.expensive.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.functions.impl.render.HUD;
import im.expensive.utils.client.ClientUtil;
import im.expensive.utils.client.IMinecraft;
import im.expensive.utils.client.Vec2i;
import im.expensive.utils.math.MathUtil;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.KawaseBlur;
import im.expensive.utils.render.font.Fonts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MainScreen extends Screen implements IMinecraft {
    private static final ResourceLocation BACKMENU = new ResourceLocation("expensive/images/backmenu.png");
    private final List<Button> buttons = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();

    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        setupButtons(width, height);
    }

    private void setupButtons(int width, int height) {
        float buttonWidth = 200; // Ширина кнопки
        float buttonHeight = 30; // Высота кнопки

        // Центрирование кнопок
        float x = (width - buttonWidth) / 2;
        float y = (height - (buttonHeight * 4 + 10 * 3)) / 2; // Корректировка для расстановки кнопок

        buttons.clear();
        // Добавляем кнопки
        buttons.add(new Button(x, y, buttonWidth, buttonHeight, "Одиночная игра", () -> mc.displayGuiScreen(new WorldSelectionScreen(this))));
        buttons.add(new Button(x, y += buttonHeight + 10, buttonWidth, buttonHeight, "Сетевая игра", () -> mc.displayGuiScreen(new MultiplayerScreen(this))));
        buttons.add(new Button(x, y += buttonHeight + 10, buttonWidth, buttonHeight, "Настройки", () -> mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings))));
        buttons.add(new Button(x, y += buttonHeight + 10, buttonWidth, buttonHeight, "Выйти", () -> mc.shutdown()));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Обновляем частицы
        if (stopWatch.isReached(100)) {
            particles.add(new Particle());
            stopWatch.reset();
        }

        MainWindow mainWindow = mc.getMainWindow();
        int windowWidth = ClientUtil.calc(mainWindow.getScaledWidth());
        int windowHeight = ClientUtil.calc(mainWindow.getScaledHeight());

        // Рисуем фон
        DisplayUtils.drawImage(BACKMENU, 0, 0, windowWidth, windowHeight, -1);

        // Позиционируем логотип
        int logoWidth = 960;  // Подгонка размера
        int logoHeight = 540; // Подгонка размера
        int xLogo = (windowWidth - logoWidth) / 2;
        int yLogo = (windowHeight - logoHeight) / 2 + 50; // Смещение для логотипа

        // Рисуем заголовок
        drawTitle(matrixStack, windowWidth, yLogo);
        drawButtons(matrixStack, mouseX, mouseY, partialTicks);

        Expensive.getInstance().getAltWidget().render(matrixStack, mouseX, mouseY);
    }

    private void drawTitle(MatrixStack matrixStack, int windowWidth, int yLogo) {
        float titleX = (windowWidth / 2f); // Центр по горизонтали
        float titleY = yLogo + 110; // Вертикальная позиция заголовка
        Fonts.sfbold.drawCenteredText(matrixStack, "zawe", titleX, titleY, HUD.getColor(0), 30F);
        // Можно добавить подзаголовок, если нужно
        Fonts.sfui.drawCenteredText(matrixStack, "", titleX, yLogo + 300, -1, 15F);
    }

    private void drawButtons(MatrixStack stack, int mouseX, int mouseY, float pt) {
        buttons.forEach(button -> button.render(stack, mouseX, mouseY, pt));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        Expensive.getInstance().getAltWidget().click(fixed.getX(), fixed.getY(), button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        Expensive.getInstance().getAltWidget().onChar(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Expensive.getInstance().getAltWidget().onKey(keyCode);
        return false;
    }

    // Класс кнопок с функцией клика и рендеринга
    @AllArgsConstructor
    private class Button {
        @Getter private final float x, y, width, height;
        private final String text;
        private final Runnable action;

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            // Фон кнопки
            int buttonColor = ColorUtils.rgba(21, 24, 40, 255);
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                buttonColor = HUD.getColor(1, 15);
            }
            DisplayUtils.drawRoundedRect(x, y, width, height, 5, buttonColor);

            // Рендеринг текста
            float textX = x + (width / 2f);
            float textY = y + (height / 2f) - 8.5f; // Центрируем текст по вертикали
            Fonts.montserrat.drawCenteredText(stack, text, textX, textY, ColorUtils.rgb(255, 255, 255), 17F);
        }

        public void click(int mX, int mY, int button) {
            if (MathUtil.isHovered(mX, mY, x, y, width, height) && button == 0) {
                action.run();
            }
        }
    }

    private static final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Particle {
        private float x, y;
        private float velocityX, velocityY;
        private int life;

        public Particle() {
            this.x = ThreadLocalRandom.current().nextFloat() * 1920; // Ширина экрана
            this.y = ThreadLocalRandom.current().nextFloat() * 1080; // Высота экрана
            this.velocityX = ThreadLocalRandom.current().nextFloat() * 2 - 1; // Случайная скорость по X
            this.velocityY = ThreadLocalRandom.current().nextFloat() * 2 - 1; // Случайная скорость по Y
            this.life = ThreadLocalRandom.current().nextInt(60, 120); // Время жизни частицы
        }

        public void update() {
            this.x += velocityX;
            this.y += velocityY;
            this.life--;
        }

        public boolean isAlive() {
            return this.life > 0;
        }
    }
}
