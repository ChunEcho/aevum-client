/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundSystemChatPacket
 */
package dev.aevum.mod.commands.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.mod.commands.Command;
import java.util.List;
import net.minecraft.Packet;
import net.minecraft.ClientboundSystemChatPacket;

public class PingCommand
extends Command {
    private long sendTime;

    public PingCommand() {
        super("ping", "");
    }

    @Override
    public void runCommand(String[] parameters) {
        this.sendTime = System.currentTimeMillis();
        mc.getConnection().method_45730("chat ");
        Aevum.EVENT_BUS.subscribe(this);
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return null;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive e) {
        ClientboundSystemChatPacket packet;
        Packet<?> packet2 = e.getPacket();
        if (packet2 instanceof ClientboundSystemChatPacket && ((packet = (ClientboundSystemChatPacket)packet2).comp_763().getString().contains("chat.use") || packet.comp_763().getString().contains("\u547d\u4ee4") || packet.comp_763().getString().contains("Bad command") || packet.comp_763().getString().contains("No such command") || packet.comp_763().getString().contains("<--[HERE]") || packet.comp_763().getString().contains("Unknown") || packet.comp_763().getString().contains("\u5e2e\u52a9") || packet.comp_763().getString().contains("\u6267\u884c\u9519\u8bef"))) {
            this.sendChatMessage("ping: " + (System.currentTimeMillis() - this.sendTime) + "ms");
            Aevum.EVENT_BUS.unsubscribe(this);
        }
    }
}

