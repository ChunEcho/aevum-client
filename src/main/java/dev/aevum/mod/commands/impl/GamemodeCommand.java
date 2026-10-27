/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GameType
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.mod.commands.Command;
import java.util.List;
import net.minecraft.GameType;

public class GamemodeCommand
extends Command {
    public GamemodeCommand() {
        super("gamemode", "[gamemode]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            this.sendUsage();
            return;
        }
        String moduleName = parameters[0];
        if (moduleName.equalsIgnoreCase("survival")) {
            GamemodeCommand.mc.gameMode.method_2907(GameType.SURVIVAL);
        } else if (moduleName.equalsIgnoreCase("creative")) {
            GamemodeCommand.mc.gameMode.method_2907(GameType.CREATIVE);
        } else if (moduleName.equalsIgnoreCase("adventure")) {
            GamemodeCommand.mc.gameMode.method_2907(GameType.ADVENTURE);
        } else if (moduleName.equalsIgnoreCase("spectator")) {
            GamemodeCommand.mc.gameMode.method_2907(GameType.SPECTATOR);
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            return new String[]{"survival", "creative", "adventure", "spectator"};
        }
        return null;
    }
}

