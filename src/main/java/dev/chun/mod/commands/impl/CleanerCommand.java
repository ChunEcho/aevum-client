/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 */
package dev.chun.mod.commands.impl;

import dev.chun.Aevum;
import dev.chun.Aevum;
import dev.chun.core.impl.PlayerManager;
import dev.chun.mod.commands.Command;
import dev.chun.mod.gui.windows.WindowsScreen;
import dev.chun.mod.gui.windows.impl.ItemSelectWindow;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;

public class CleanerCommand
extends Command {
    public CleanerCommand() {
        super("cleaner", "[\"\"/name/reset/clear/list] | [add/remove] [name]");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (parameters.length == 0) {
            PlayerManager.screenToOpen = new WindowsScreen(new ItemSelectWindow(Aevum.CLEANER));
            return;
        }
        switch (parameters[0]) {
            case "reset": {
                Aevum.CLEANER.clear();
                Aevum.CLEANER.add(Items.NETHERITE_SWORD.getTranslationKey());
                Aevum.CLEANER.add(Items.NETHERITE_PICKAXE.getTranslationKey());
                Aevum.CLEANER.add(Items.NETHERITE_HELMET.getTranslationKey());
                Aevum.CLEANER.add(Items.NETHERITE_CHESTPLATE.getTranslationKey());
                Aevum.CLEANER.add(Items.NETHERITE_LEGGINGS.getTranslationKey());
                Aevum.CLEANER.add(Items.NETHERITE_BOOTS.getTranslationKey());
                Aevum.CLEANER.add(Items.OBSIDIAN.getTranslationKey());
                Aevum.CLEANER.add(Items.ENDER_CHEST.getTranslationKey());
                Aevum.CLEANER.add(Items.ENDER_PEARL.getTranslationKey());
                Aevum.CLEANER.add(Items.ENCHANTED_GOLDEN_APPLE.getTranslationKey());
                Aevum.CLEANER.add(Items.EXPERIENCE_BOTTLE.getTranslationKey());
                Aevum.CLEANER.add(Items.COBWEB.getTranslationKey());
                Aevum.CLEANER.add(Items.POTION.getTranslationKey());
                Aevum.CLEANER.add(Items.SPLASH_POTION.getTranslationKey());
                Aevum.CLEANER.add(Items.TOTEM_OF_UNDYING.getTranslationKey());
                Aevum.CLEANER.add(Items.END_CRYSTAL.getTranslationKey());
                Aevum.CLEANER.add(Items.ELYTRA.getTranslationKey());
                Aevum.CLEANER.add(Items.FLINT_AND_STEEL.getTranslationKey());
                Aevum.CLEANER.add(Items.PISTON.getTranslationKey());
                Aevum.CLEANER.add(Items.STICKY_PISTON.getTranslationKey());
                Aevum.CLEANER.add(Items.REDSTONE_BLOCK.getTranslationKey());
                Aevum.CLEANER.add(Items.GLOWSTONE.getTranslationKey());
                Aevum.CLEANER.add(Items.RESPAWN_ANCHOR.getTranslationKey());
                Aevum.CLEANER.add(Items.ANVIL.getTranslationKey());
                this.sendChatMessage("\u00a7fItems list got reset");
                return;
            }
            case "clear": {
                Aevum.CLEANER.getList().clear();
                this.sendChatMessage("\u00a7fItems list got clear");
                return;
            }
            case "list": {
                if (Aevum.CLEANER.getList().isEmpty()) {
                    this.sendChatMessage("\u00a7fItems list is empty");
                    return;
                }
                for (String name : Aevum.CLEANER.getList()) {
                    this.sendChatMessage("\u00a7a" + name);
                }
                return;
            }
            case "add": {
                if (parameters.length == 2) {
                    Aevum.CLEANER.add(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Aevum.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
            case "remove": {
                if (parameters.length == 2) {
                    Aevum.CLEANER.remove(parameters[1]);
                    this.sendChatMessage("\u00a7f" + parameters[1] + (Aevum.CLEANER.inList(parameters[1]) ? " \u00a7ahas been added" : " \u00a7chas been removed"));
                    return;
                }
                this.sendUsage();
                return;
            }
        }
        if (parameters.length == 1) {
            this.sendChatMessage("\u00a7f" + parameters[0] + (Aevum.CLEANER.inList(parameters[0]) ? " \u00a7ais in whitelist" : " \u00a7cisn't in whitelist"));
            return;
        }
        this.sendUsage();
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        if (count == 1) {
            String input = seperated.getLast().toLowerCase();
            ArrayList<String> correct = new ArrayList<String>();
            List<String> list = List.of("add", "remove", "list", "reset", "clear");
            for (String x : list) {
                if (!input.equalsIgnoreCase(Aevum.getPrefix() + "cleaner") && !x.toLowerCase().startsWith(input)) continue;
                correct.add(x);
            }
            int numCmds = correct.size();
            String[] commands = new String[numCmds];
            int i = 0;
            for (String x : correct) {
                commands[i++] = x;
            }
            return commands;
        }
        return null;
    }
}

