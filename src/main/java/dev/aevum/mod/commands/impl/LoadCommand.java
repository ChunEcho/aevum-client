/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.Aevum;
import dev.aevum.core.Manager;
import dev.aevum.core.impl.ConfigManager;
import dev.aevum.mod.commands.Command;
import dev.aevum.mod.modules.impl.client.Fonts;
import java.io.File;
import java.util.List;

public class LoadCommand
extends Command {
    public LoadCommand() {
        super("load", "[config]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        this.sendChatMessage("\u00a7fLoading..");
        ConfigManager.options = Manager.getFile("cfg" + File.separator + parameters[0] + ".cfg");
        Aevum.CONFIG = new ConfigManager();
        Aevum.CONFIG.load();
        ConfigManager.options = Manager.getFile("options.txt");
        Aevum.save();
        Fonts.INSTANCE.refresh();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

