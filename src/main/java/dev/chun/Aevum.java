package dev.chun;

import dev.chun.api.events.eventbus.EventBus;
import dev.chun.api.events.impl.InitEvent;
import dev.chun.core.impl.BlurManager;
import dev.chun.core.impl.BreakManager;
import dev.chun.core.impl.CleanerManager;
import dev.chun.core.impl.CommandManager;
import dev.chun.core.impl.ConfigManager;
import dev.chun.core.impl.FPSManager;
import dev.chun.core.impl.FriendManager;
import dev.chun.core.impl.HoleManager;
import dev.chun.core.impl.ModuleManager;
import dev.chun.core.impl.PlayerManager;
import dev.chun.core.impl.PopManager;
import dev.chun.core.impl.RotationManager;
import dev.chun.core.impl.ServerManager;
import dev.chun.core.impl.ShaderManager;
import dev.chun.core.impl.ThreadManager;
import dev.chun.core.impl.TimerManager;
import dev.chun.core.impl.TradeManager;
import dev.chun.core.impl.XrayManager;
import dev.chun.mod.modules.impl.client.ClientSetting;
import java.io.File;
import java.lang.invoke.MethodHandles;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;

public class Aevum implements ModInitializer {
    public static final String NAME = "Aevum Client";
    public static final String VERSION = "1.02-beta";
    public static final String CONFIG_DIR = "aevum";
    public static final EventBus EVENT_BUS = new EventBus();
    public static HoleManager HOLE;
    public static PlayerManager PLAYER;
    public static TradeManager TRADE;
    public static CleanerManager CLEANER;
    public static XrayManager XRAY;
    public static ModuleManager MODULE;
    public static CommandManager COMMAND;
    public static ConfigManager CONFIG;
    public static RotationManager ROTATION;
    public static BreakManager BREAK;
    public static PopManager POP;
    public static FriendManager FRIEND;
    public static TimerManager TIMER;
    public static ShaderManager SHADER;
    public static BlurManager BLUR;
    public static FPSManager FPS;
    public static ServerManager SERVER;
    public static ThreadManager THREAD;
    public static boolean loaded;
    public static long initTime;
    public static String userId;

    public static String getPrefix() {
        return ClientSetting.INSTANCE.prefix.getValue();
    }

    public static void save() {
        CONFIG.save();
        CLEANER.save();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[Aevum Client] Saved");
    }

    private void register() {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loaded) {
                Aevum.save();
            }
        }));
    }

    public void onInitialize() {
        this.register();
        CONFIG = new ConfigManager();
        MODULE = new ModuleManager();
        HOLE = new HoleManager();
        COMMAND = new CommandManager();
        FRIEND = new FriendManager();
        XRAY = new XrayManager();
        CLEANER = new CleanerManager();
        TRADE = new TradeManager();
        ROTATION = new RotationManager();
        BREAK = new BreakManager();
        PLAYER = new PlayerManager();
        POP = new PopManager();
        TIMER = new TimerManager();
        SHADER = new ShaderManager();
        BLUR = new BlurManager();
        FPS = new FPSManager();
        SERVER = new ServerManager();
        CONFIG.load();
        THREAD = new ThreadManager();

        initTime = System.currentTimeMillis();
        loaded = true;
        EVENT_BUS.post(new InitEvent());
        File folder = new File(MinecraftClient.getInstance().runDirectory.getPath() + File.separator + CONFIG_DIR + File.separator + "cfg");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    static {
        loaded = false;
    }
}

