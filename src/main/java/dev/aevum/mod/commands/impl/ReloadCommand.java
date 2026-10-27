/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.Aevum;
import dev.aevum.core.impl.ConfigManager;
import dev.aevum.mod.commands.Command;
import java.util.List;

public class ReloadCommand
extends Command {
    public ReloadCommand() {
        super("reload", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        this.sendChatMessage("\u00a7fReloading..");
        Aevum.CONFIG = new ConfigManager();
        Aevum.CONFIG.load();
        Aevum.CLEANER.read();
        Aevum.XRAY.read();
        Aevum.TRADE.read();
        Aevum.FRIEND.read();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

