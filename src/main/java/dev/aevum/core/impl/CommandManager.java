/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Component
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.interfaces.IChatHudHook;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.commands.Command;
import dev.aevum.mod.commands.impl.AimCommand;
import dev.aevum.mod.commands.impl.BindCommand;
import dev.aevum.mod.commands.impl.BindsCommand;
import dev.aevum.mod.commands.impl.CleanerCommand;
import dev.aevum.mod.commands.impl.ClipCommand;
import dev.aevum.mod.commands.impl.EsuCommand;
import dev.aevum.mod.commands.impl.FakePlayerCommand;
import dev.aevum.mod.commands.impl.FriendCommand;
import dev.aevum.mod.commands.impl.GamemodeCommand;
import dev.aevum.mod.commands.impl.GcCommand;
import dev.aevum.mod.commands.impl.KitCommand;
import dev.aevum.mod.commands.impl.LoadCommand;
import dev.aevum.mod.commands.impl.PeekCommand;
import dev.aevum.mod.commands.impl.PingCommand;
import dev.aevum.mod.commands.impl.PrefixCommand;
import dev.aevum.mod.commands.impl.RejoinCommand;
import dev.aevum.mod.commands.impl.ReloadCommand;
import dev.aevum.mod.commands.impl.SaveCommand;
import dev.aevum.mod.commands.impl.TCommand;
import dev.aevum.mod.commands.impl.TeleportCommand;
import dev.aevum.mod.commands.impl.ToggleCommand;
import dev.aevum.mod.commands.impl.TradeCommand;
import dev.aevum.mod.commands.impl.WatermarkCommand;
import dev.aevum.mod.commands.impl.XrayCommand;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import java.util.HashMap;
import net.minecraft.Component;

public class CommandManager
implements Wrapper {
    private final HashMap<String, Command> commands = new HashMap();

    public CommandManager() {
        this.init();
    }

    public void init() {
        this.registerCommand(new AimCommand());
        this.registerCommand(new BindCommand());
        this.registerCommand(new BindsCommand());
        this.registerCommand(new CleanerCommand());
        this.registerCommand(new ClipCommand());
        this.registerCommand(new EsuCommand());
        this.registerCommand(new FakePlayerCommand());
        this.registerCommand(new FriendCommand());
        this.registerCommand(new XrayCommand());
        this.registerCommand(new GamemodeCommand());
        this.registerCommand(new KitCommand());
        this.registerCommand(new LoadCommand());
        this.registerCommand(new PingCommand());
        this.registerCommand(new PrefixCommand());
        this.registerCommand(new RejoinCommand());
        this.registerCommand(new ReloadCommand());
        this.registerCommand(new SaveCommand());
        this.registerCommand(new TeleportCommand());
        this.registerCommand(new TCommand());
        this.registerCommand(new ToggleCommand());
        this.registerCommand(new TradeCommand());
        this.registerCommand(new WatermarkCommand());
        this.registerCommand(new PeekCommand());
        this.registerCommand(new GcCommand());
    }

    public static void sendMessage(String message) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Earth) {
                CommandManager.mc.gui.method_1743().method_1812(Component.method_30163((String)message));
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.mc.gui.method_1743().method_1812(Component.method_30163((String)("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] " + message)));
                return;
            }
            ((IChatHudHook)CommandManager.mc.gui.method_1743()).aevumClient$addMessage(Component.method_30163((String)(ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f " + message)));
        });
    }

    public static void sendMessageId(String message, int id) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Earth) {
                ((IChatHudHook)CommandManager.mc.gui.method_1743()).aevumClient$addMessage(Component.method_30163((String)message), id);
                return;
            }
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                ((IChatHudHook)CommandManager.mc.gui.method_1743()).aevumClient$addMessage(Component.method_30163((String)("\u00a7f[\u00a7b" + ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f] " + message)), id);
                return;
            }
            ((IChatHudHook)CommandManager.mc.gui.method_1743()).aevumClient$addMessage(Component.method_30163((String)(ClientSetting.INSTANCE.hackName.getValue() + "\u00a7f " + message)), id);
        });
    }

    public static void sendChatMessageWidthIdNoSync(String message, int id) {
        mc.execute(() -> {
            if (Module.nullCheck()) {
                return;
            }
            ((IChatHudHook)CommandManager.mc.gui.method_1743()).aevumClient$addMessageOutSync(Component.method_30163((String)("\u00a7f" + message)), id);
        });
    }

    private void registerCommand(Command command) {
        this.commands.put(command.getName(), command);
    }

    public Command getCommandBySyntax(String string) {
        return this.commands.get(string);
    }

    public HashMap<String, Command> getCommands() {
        return this.commands;
    }

    public void command(String[] commandIn) {
        Command command = this.commands.get(commandIn[0].substring(Aevum.getPrefix().length()).toLowerCase());
        if (command == null) {
            CommandManager.sendMessage("\u00a74Invalid Command!");
        } else {
            String[] parameterList = new String[commandIn.length - 1];
            System.arraycopy(commandIn, 1, parameterList, 0, commandIn.length - 1);
            if (parameterList.length == 1 && parameterList[0].equals("help")) {
                command.sendUsage();
                return;
            }
            command.runCommand(parameterList);
        }
    }
}

