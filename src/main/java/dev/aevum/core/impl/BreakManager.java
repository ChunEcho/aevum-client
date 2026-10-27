/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.BlockPos
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundBlockDestructionPacket
 *  net.minecraft.Mth
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.BlockBreakingProgressEvent;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.ServerConnectBeginEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.FadeUtils;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.impl.render.BreakESP;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.BlockPos;
import net.minecraft.Packet;
import net.minecraft.ClientboundBlockDestructionPacket;
import net.minecraft.Mth;

public class BreakManager
implements Wrapper {
    public final ConcurrentHashMap<Integer, BreakData> breakMap = new ConcurrentHashMap();
    public final ConcurrentHashMap<Integer, BreakData> doubleMap = new ConcurrentHashMap();

    public BreakManager() {
        Aevum.EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onServerConnectBegin(ServerConnectBeginEvent event) {
        this.breakMap.clear();
        this.doubleMap.clear();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Module.nullCheck()) {
            return;
        }
        if (AntiCheat.INSTANCE.detectDouble.getValue()) {
            Iterator iterator = ((ConcurrentHashMap.KeySetView)Aevum.BREAK.doubleMap.keySet()).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)iterator.next();
                BreakData breakData = Aevum.BREAK.doubleMap.get(i);
                if (breakData != null && breakData.getEntity() != null && !BreakManager.mc.level.isEmptyBlock(breakData.pos) && !breakData.timer.passedMs(Math.max(AntiCheat.INSTANCE.minTimeout.getValue() * 1000.0, breakData.breakTime * AntiCheat.INSTANCE.doubleMineTimeout.getValue()))) continue;
                Aevum.BREAK.doubleMap.remove(i);
            }
        }
        for (BreakData breakData : this.breakMap.values()) {
            breakData.breakTime = Math.max(BreakESP.getBreakTime(breakData.pos, false), 50.0);
            if (PacketMine.unbreakable(breakData.pos)) {
                breakData.fade.setLength(0L);
                breakData.complete = false;
                breakData.failed = true;
                continue;
            }
            if (BreakManager.mc.level.isEmptyBlock(breakData.pos)) {
                breakData.fade.setLength(0L);
                breakData.complete = true;
                breakData.failed = false;
                continue;
            }
            if (!breakData.complete && breakData.timer.passedMs(breakData.breakTime * AntiCheat.INSTANCE.breakTimeout.getValue())) {
                breakData.fade.setLength(0L);
                breakData.failed = true;
                continue;
            }
            breakData.fade.setLength((long)breakData.breakTime);
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (Module.nullCheck()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundBlockDestructionPacket) {
            BreakData current;
            ClientboundBlockDestructionPacket packet2 = (ClientboundBlockDestructionPacket)packet;
            if (packet2.getPos() == null) {
                return;
            }
            BreakData breakData = new BreakData(packet2.getPos(), packet2.getId(), false);
            if (breakData.getEntity() == null) {
                return;
            }
            if (Mth.method_15355((float)((float)breakData.getEntity().getEyePosition().method_1025(packet2.getPos().method_46558()))) > 8.0f) {
                return;
            }
            if (AntiCheat.INSTANCE.detectDouble.getValue() && packet2.getProgress() != 255) {
                if (packet2.getProgress() != 0) {
                    BreakData doublePos = this.doubleMap.get(packet2.getId());
                    if (doublePos != null) {
                        doublePos.pos = packet2.getPos();
                        doublePos.timer.reset();
                    } else if (!PacketMine.unbreakable(packet2.getPos())) {
                        this.doubleMap.put(packet2.getId(), new BreakData(packet2.getPos(), packet2.getId(), true));
                    }
                    return;
                }
                BreakData doublePos = this.doubleMap.get(packet2.getId());
                if (doublePos != null && doublePos.pos.equals((Object)packet2.getPos()) && !doublePos.timer.passedS(150.0)) {
                    return;
                }
            }
            if ((current = this.breakMap.get(packet2.getId())) != null && !current.failed && current.pos.equals((Object)packet2.getPos())) {
                return;
            }
            this.breakMap.put(packet2.getId(), breakData);
            Aevum.EVENT_BUS.post(BlockBreakingProgressEvent.get(packet2.getPos(), packet2.getId(), packet2.getProgress()));
            if (AntiCheat.INSTANCE.detectDouble.getValue() && !this.doubleMap.containsKey(packet2.getId()) && !PacketMine.unbreakable(packet2.getPos())) {
                this.doubleMap.put(packet2.getId(), new BreakData(packet2.getPos(), packet2.getId(), true));
            }
        }
    }

    public boolean isMining(BlockPos pos) {
        return this.isMining(pos, true);
    }

    public boolean isMining(BlockPos pos, boolean self) {
        if (self && PacketMine.getBreakPos() != null && PacketMine.getBreakPos().equals((Object)pos)) {
            return true;
        }
        for (BreakData breakData : this.breakMap.values()) {
            if (breakData.getEntity() == null || breakData.getEntity().getEyePosition().method_1022(pos.method_46558()) > 7.0 || breakData.failed || !breakData.pos.equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    public static class BreakData {
        public BlockPos pos;
        private final int entityId;
        public final FadeUtils fade;
        public final Timer timer;
        public double breakTime;
        public boolean failed = false;
        public boolean complete = false;

        public BreakData(BlockPos pos, int entityId, boolean extraBreak) {
            this.pos = pos;
            this.entityId = entityId;
            this.breakTime = Math.max(BreakESP.getBreakTime(pos, extraBreak), 50.0);
            this.fade = new FadeUtils((long)this.breakTime);
            this.timer = new Timer();
        }

        public Entity getEntity() {
            if (Wrapper.mc.level == null) {
                return null;
            }
            Entity entity = Wrapper.mc.level.method_8469(this.entityId);
            if (entity instanceof Player) {
                return entity;
            }
            return null;
        }
    }
}

