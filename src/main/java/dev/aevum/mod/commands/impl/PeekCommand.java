/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.mod.commands.Command;
import dev.aevum.mod.modules.impl.misc.ShulkerViewer;
import java.util.List;
import net.minecraft.ItemStack;

public class PeekCommand
extends Command {
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public PeekCommand() {
        super("peek", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        ShulkerViewer.openContainer(PeekCommand.mc.player.getMainHandItem(), ITEMS, true);
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

