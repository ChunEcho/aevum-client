/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Component
 *  net.minecraft.ClientboundDisconnectPacket
 *  net.minecraft.Minecraft
 *  net.minecraft.ConnectScreen
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerData
 *  net.minecraft.ClientCommonPacketListener
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.mod.commands.Command;
import java.util.List;
import net.minecraft.Component;
import net.minecraft.ClientboundDisconnectPacket;
import net.minecraft.Minecraft;
import net.minecraft.ConnectScreen;
import net.minecraft.Screen;
import net.minecraft.TitleScreen;
import net.minecraft.ServerAddress;
import net.minecraft.ServerData;
import net.minecraft.ClientCommonPacketListener;

public class RejoinCommand
extends Command {
    public RejoinCommand() {
        super("rejoin", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        mc.doRunTask(() -> {
            if (RejoinCommand.mc.level != null && mc.getCurrentServer() != null) {
                ServerData lastestServerEntry = mc.getCurrentServer();
                new ClientboundDisconnectPacket(Component.method_30163((String)"Self kick")).method_11467((ClientCommonPacketListener)mc.getConnection());
                ConnectScreen.method_36877((Screen)new TitleScreen(), (Minecraft)mc, (ServerAddress)ServerAddress.method_2950((String)lastestServerEntry.ip), (ServerData)lastestServerEntry, (boolean)false, null);
            }
        });
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }
}

