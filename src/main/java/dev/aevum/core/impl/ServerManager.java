/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundSetTimePacket
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.ClientboundPlayerChatPacket
 *  net.minecraft.ClientboundSystemChatPacket
 */
package dev.aevum.core.impl;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.GameLeftEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.Fonts;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.impl.misc.AutoLog;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import net.minecraft.Packet;
import net.minecraft.ClientboundSetTimePacket;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.ClientboundPlayerChatPacket;
import net.minecraft.ClientboundSystemChatPacket;

public class ServerManager
implements Wrapper {
    public final Timer playerNull = new Timer();
    public int currentSlot = -1;
    private final ArrayDeque<Float> tpsResult = new ArrayDeque(20);
    boolean worldNull = true;
    private long time;
    private long tickTime;
    private float tps;
    int lastSlot;

    public ServerManager() {
        Aevum.EVENT_BUS.subscribe(this);
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventListener(priority=-200)
    public void onPacket(PacketEvent.Send event) {
        if (AntiCheat.INSTANCE.attackCDFix.getValue()) {
            ServerboundSetCarriedItemPacket packet2;
            if (event.isCancelled()) {
                return;
            }
            Packet<?> packet = event.getPacket();
            if (packet instanceof ServerboundSwingPacket || packet instanceof ServerboundInteractPacket && Criticals.getInteractType((ServerboundInteractPacket)packet) == ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK) {
                ServerManager.mc.player.resetAttackStrengthTicker();
            } else if (packet instanceof ServerboundSetCarriedItemPacket && this.lastSlot != (packet2 = (ServerboundSetCarriedItemPacket)packet).getSlot()) {
                this.lastSlot = packet2.getSlot();
                ServerManager.mc.player.resetAttackStrengthTicker();
            }
        }
    }

    @EventListener
    public void onLeft(GameLeftEvent event) {
        this.currentSlot = -1;
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundSetCarriedItemPacket) {
            ServerboundSetCarriedItemPacket packet2 = (ServerboundSetCarriedItemPacket)packet;
            int packetSlot = packet2.getSlot();
            if (AntiCheat.INSTANCE.noBadSlot.getValue() && packetSlot == this.currentSlot) {
                event.cancel();
                return;
            }
            this.currentSlot = packetSlot;
        }
    }

    public float getTPS() {
        return ServerManager.round2(this.tps);
    }

    public float getCurrentTPS() {
        return ServerManager.round2(20.0f * ((float)this.tickTime / 1000.0f));
    }

    public float getTPSFactor() {
        return this.getTPS() / 20.0f;
    }

    @EventListener(priority=999)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            if (this.time != 0L) {
                this.tickTime = System.currentTimeMillis() - this.time;
                if (this.tpsResult.size() > 20) {
                    this.tpsResult.poll();
                }
                this.tpsResult.add(Float.valueOf(20.0f * (1000.0f / (float)this.tickTime)));
                float average = 0.0f;
                for (Float value : this.tpsResult) {
                    average += MathUtil.clamp(value.floatValue(), 0.0f, 20.0f);
                }
                this.tps = average / (float)this.tpsResult.size();
            }
            this.time = System.currentTimeMillis();
        }
    }

    @EventListener
    private void PacketReceive(PacketEvent.Receive event) {
        if (ServerManager.mc.player == null || ServerManager.mc.level == null) {
            return;
        }
        String s = null;
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket packet2 = (ClientboundSystemChatPacket)packet;
            if (packet2.comp_763() != null) {
                s = packet2.comp_763().getString();
            }
        } else {
            packet = event.getPacket();
            if (packet instanceof ClientboundPlayerChatPacket) {
                ClientboundPlayerChatPacket packet3 = (ClientboundPlayerChatPacket)packet;
                String string = s = packet3.comp_1103() != null ? packet3.comp_1103().getString() : packet3.comp_1102().comp_1090();
            }
        }
        if (s != null) {
            // empty if block
        }
    }

    public void onUpdate() {
        if (ServerManager.mc.player == null) {
            this.playerNull.reset();
        }
        if (this.worldNull && ServerManager.mc.level != null) {
            Fonts.INSTANCE.refresh();
            AutoLog.loggedOut = false;
            Aevum.MODULE.onLogin();
            this.worldNull = false;
        } else if (!this.worldNull && ServerManager.mc.level == null) {
            Aevum.save();
            Aevum.MODULE.onLogout();
            this.worldNull = true;
        }
    }
}

