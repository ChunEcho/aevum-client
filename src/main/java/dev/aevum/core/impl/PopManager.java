/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Level
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundEntityEventPacket
 *  net.minecraft.AbstractClientPlayer
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.DeathEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.TotemEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Level;
import net.minecraft.Packet;
import net.minecraft.ClientboundEntityEventPacket;
import net.minecraft.AbstractClientPlayer;

public class PopManager
implements Wrapper {
    public final HashMap<String, Integer> popContainer = new HashMap();
    private final List<Player> deadPlayer = new ArrayList<Player>();

    public PopManager() {
        this.init();
    }

    public void init() {
        Aevum.EVENT_BUS.subscribe(this);
    }

    public int getPop(String s) {
        return this.popContainer.getOrDefault(s, 0);
    }

    public int getPop(Player player) {
        return this.getPop(player.method_5477().getString());
    }

    public void onUpdate() {
        if (Module.nullCheck()) {
            return;
        }
        for (AbstractClientPlayer player : Aevum.THREAD.getPlayers()) {
            if (player == null || !player.isDeadOrDying()) {
                this.deadPlayer.remove(player);
                continue;
            }
            if (this.deadPlayer.contains(player)) continue;
            Aevum.EVENT_BUS.post(DeathEvent.get((Player)player));
            this.onDeath((Player)player);
            this.deadPlayer.add((Player)player);
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        Entity entity;
        ClientboundEntityEventPacket packet;
        if (Module.nullCheck()) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ClientboundEntityEventPacket && (packet = (ClientboundEntityEventPacket)packet2).getEventId() == 35 && (entity = packet.method_11469((Level)PopManager.mc.level)) instanceof Player) {
            Player player = (Player)entity;
            this.onTotemPop(player);
        }
    }

    public void onDeath(Player player) {
        this.popContainer.remove(player.method_5477().getString());
    }

    public void onTotemPop(Player player) {
        int l_Count = 1;
        if (this.popContainer.containsKey(player.method_5477().getString())) {
            l_Count = this.popContainer.get(player.method_5477().getString());
            this.popContainer.put(player.method_5477().getString(), ++l_Count);
        } else {
            this.popContainer.put(player.method_5477().getString(), l_Count);
        }
        Aevum.EVENT_BUS.post(TotemEvent.get(player));
    }
}

