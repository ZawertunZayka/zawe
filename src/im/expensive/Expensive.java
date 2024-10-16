package im.expensive;

import com.google.common.eventbus.EventBus;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import im.expensive.command.*;
import im.expensive.command.friends.FriendStorage;
import im.expensive.command.impl.*;
import im.expensive.command.impl.feature.*;
import im.expensive.command.staffs.StaffStorage;
import im.expensive.config.ConfigStorage;
import im.expensive.events.EventKey;
import im.expensive.functions.api.Function;
import im.expensive.functions.api.FunctionRegistry;
import im.expensive.scripts.client.ScriptManager;
import im.expensive.ui.ab.factory.ItemFactory;
import im.expensive.ui.ab.factory.ItemFactoryImpl;
import im.expensive.ui.ab.logic.ActivationLogic;
import im.expensive.ui.ab.model.IItem;
import im.expensive.ui.ab.model.ItemStorage;
import im.expensive.ui.ab.render.Window;
import im.expensive.ui.autobuy.AutoBuyConfig;
import im.expensive.ui.autobuy.AutoBuyHandler;
import im.expensive.ui.dropdown.DropDown;
import im.expensive.ui.mainmenu.AltConfig;
import im.expensive.ui.mainmenu.AltWidget;
import im.expensive.ui.styles.Style;
import im.expensive.ui.styles.StyleFactory;
import im.expensive.ui.styles.StyleFactoryImpl;
import im.expensive.ui.styles.StyleManager;
import im.expensive.utils.TPSCalc;
import im.expensive.utils.client.ServerTPS;
import im.expensive.utils.drag.DragManager;
import im.expensive.utils.drag.Dragging;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import via.ViaMCP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Я просто делаю свой клиент, я свободный человек, пасщю что хочу
 * Газ (А-а)
 * Бро, я напастил авто тотем уже в три лет
 * Не ври, что ты не пастер, это не писаный клиент
 * Деус — модель, уши размером с живот макавто
 * Ты нервничаешь, бесишь сам себя, когда сурсов нет
 * У, у, у, у, у, я больших мазиков съедатель (А-а)
 * Больших уши уничтожитель, мазикавыпиватель (А-а)
 * Пастирских читов игратель, тебя выключатель (У-у)
 * Белый, но во мне краситель, к пастингу привыкатель (Е-е)
 * Бро, это shit skidding, я им рассказыватель (отвечаю)
 * Большие бидоны мазика — силой слова я их поедатель (У)
 * У меня большой живот — я его показыватель
 * Бро реал скидет, зовёт ся falok(Fals3r)
 * Эй, чит восьми из десяти mc.player — говно, я таким сру утром
 * Четыре бегина делят дерьмо — это на двух клиентах
 * Мой любимый кодер стал пастером — уже не авто тотем макслоло
 * Мазик рушит стены моего желудка
 * Она долго запускаться, как будто лоудер говно
 * Кодеры купаются в мазике, бля, они срут в коде
 * У меня есть шкильники качки, да, они жрут мазик
 * Они сразу поломают кисть, они не жмут руку
 * Ты пастишь реже ,чем dedinside — тя там не видно (Ха)
 * Я сейчас official, иногда ворую с клиентов (У-у)
 * Бро откинулся в Актобе — щас он летит к нам (У-у)
 * Кто этот ебаный Пастер, чё он нам пиздит там? (У-у)
 * Я могу потыкать ему этим Кодом прям в бошку (Гр-ра)
 * Подлетай к моему аирДропу — я залутаю его (Ха, макавто)
 * Я могу пастить даже спиной к монику, ты не сможешь так (Не сможешь)
 * Пока мне делали glow, у тебя пастили под носом, е, а-а
 * [Аутро]
 * У, у, у, у, у, я больших мазиков съедатель
 * Больших уши уничтожитель, мазикавыпиватель
 * Пастирских читов игратель, тебя выключатель
 * Давай спастим wintware, короче
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expensive {

    public static UserData userData;
    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "expensive solutions";

    // Экземпляр Expensive
    @Getter
    private static Expensive instance;

    // Менеджеры
    private FunctionRegistry functionRegistry;
    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private StyleManager styleManager;

    // Менеджер событий и скриптов
    private final EventBus eventBus = new EventBus();
    private final ScriptManager scriptManager = new ScriptManager();

    // Директории
    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\expensive");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\expensive\\files");

    // Элементы интерфейса
    private AltWidget altWidget;
    private AltConfig altConfig;
    private DropDown dropDown;
    private Window autoBuyUI;

    // Конфигурация и обработчики
    private AutoBuyConfig autoBuyConfig = new AutoBuyConfig();
    private AutoBuyHandler autoBuyHandler;
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;
    private ActivationLogic activationLogic;
    private ItemStorage itemStorage;

    public Expensive() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
    }



    public Dragging createDrag(Function module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    private void clientLoad() {
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        functionRegistry = new FunctionRegistry();
        macroManager = new MacroManager();
        configStorage = new ConfigStorage();
        functionRegistry.init();
        initCommands();
        initStyles();
        altWidget = new AltWidget();
        altConfig = new AltConfig();
        tpsCalc = new TPSCalc();


        try {
            autoBuyConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configStorage.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        dropDown = new DropDown(new StringTextComponent(""));
        initAutoBuy();
        autoBuyUI = new Window(new StringTextComponent(""), itemStorage);
        //autoBuyUI = new AutoBuyUI(new StringTextComponent("A"));
        autoBuyHandler = new AutoBuyHandler();
        autoBuyConfig = new AutoBuyConfig();

        eventBus.register(this);
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (functionRegistry.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(dropDown);
        }
        if (this.functionRegistry.getAutoBuyUI().isState() && this.functionRegistry.getAutoBuyUI().setting.get() == key) {
            Minecraft.getInstance().displayGuiScreen(autoBuyUI);
        }


    }

    private void initAutoBuy() {
        ItemFactory itemFactory = new ItemFactoryImpl();
        CopyOnWriteArrayList<IItem> items = new CopyOnWriteArrayList<>();
        itemStorage = new ItemStorage(items, itemFactory);

        activationLogic = new ActivationLogic(itemStorage, eventBus);
    }

    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

    private void initStyles() {
        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        // Существующие стили
        styles.add(styleFactory.createStyle("Морской", new Color(5, 63, 111), new Color(133, 183, 246)));
        styles.add(styleFactory.createStyle("Малиновый", new Color(109, 10, 40), new Color(239, 96, 136)));
        styles.add(styleFactory.createStyle("Черничный", new Color(78, 5, 127), new Color(193, 140, 234)));
        styles.add(styleFactory.createStyle("Необычный", new Color(243, 160, 232), new Color(171, 250, 243)));
        styles.add(styleFactory.createStyle("Огненный", new Color(194, 21, 0), new Color(255, 197, 0)));
        styles.add(styleFactory.createStyle("Металлический", new Color(40, 39, 39), new Color(178, 178, 178)));
        styles.add(styleFactory.createStyle("Прикольный", new Color(82, 241, 171), new Color(66, 172, 245)));
        styles.add(styleFactory.createStyle("Новогодний", new Color(190, 5, 60), new Color(255, 255, 255)));

        // Новые стили
        styles.add(styleFactory.createStyle("Закатный", new Color(255, 102, 0), new Color(255, 204, 0)));
        styles.add(styleFactory.createStyle("Летний", new Color(255, 255, 102), new Color(0, 204, 102)));
        styles.add(styleFactory.createStyle("Космический", new Color(25, 25, 112), new Color(255, 105, 180)));
        styles.add(styleFactory.createStyle("Теплый шоколад", new Color(153, 101, 21), new Color(255, 204, 153)));
        styles.add(styleFactory.createStyle("Фиолетовый дождь", new Color(148, 0, 211), new Color(255, 0, 255)));
        styles.add(styleFactory.createStyle("Тихий океан", new Color(0, 102, 204), new Color(0, 204, 255)));
        styles.add(styleFactory.createStyle("Лаванда", new Color(230, 230, 250), new Color(138, 43, 226)));
        styles.add(styleFactory.createStyle("Туманный", new Color(173, 216, 230), new Color(224, 255, 255)));
        styles.add(styleFactory.createStyle("Радужный", new Color(255, 0, 0), new Color(0, 255, 0)));
        styles.add(styleFactory.createStyle("Пастельный", new Color(255, 182, 193), new Color(255, 218, 185)));
        styles.add(styleFactory.createStyle("Солнечный", new Color(255, 255, 0), new Color(255, 228, 196)));
        styles.add(styleFactory.createStyle("Электрик", new Color(0, 0, 255), new Color(0, 255, 255)));
        styles.add(styleFactory.createStyle("Графит", new Color(50, 50, 50), new Color(169, 169, 169)));
        styles.add(styleFactory.createStyle("Карамельный", new Color(255, 204, 153), new Color(255, 140, 0)));
        styles.add(styleFactory.createStyle("Ягодный", new Color(255, 20, 147), new Color(221, 160, 221)));
        styles.add(styleFactory.createStyle("Ледяной", new Color(173, 216, 230), new Color(135, 206, 250)));
        styles.add(styleFactory.createStyle("Тропический", new Color(255, 105, 180), new Color(75, 0, 130)));
        styles.add(styleFactory.createStyle("Винтажный", new Color(205, 133, 63), new Color(255, 160, 122)));
        styles.add(styleFactory.createStyle("Лесной", new Color(34, 139, 34), new Color(124, 252, 0)));
        styles.add(styleFactory.createStyle("Морозный", new Color(240, 248, 255), new Color(176, 224, 230)));
        styles.add(styleFactory.createStyle("Ретро", new Color(255, 228, 181), new Color(139, 69, 19)));

        styleManager = new StyleManager(styles, styles.get(0));
    }



    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserData {
        final String user;
        final int uid;
    }

}
