/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Level
 *  net.minecraft.AABB
 *  net.minecraft.HitResult
 *  net.minecraft.Vec3
 *  net.minecraft.PacketListener
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundMoveEntityPacket
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.ClientboundTeleportEntityPacket
 *  net.minecraft.EntityHitResult
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.asm.accessors.IEntity;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Aura;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.Entity;
import net.minecraft.Level;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.PacketListener;
import net.minecraft.Packet;
import net.minecraft.ClientboundMoveEntityPacket;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.ClientboundTeleportEntityPacket;
import net.minecraft.EntityHitResult;
import net.minecraft.PoseStack;

public class BackTrack
extends Module {
    private final CopyOnWriteArrayList<QueuedPacket> packets = new CopyOnWriteArrayList();
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100.0, 0.0, 1000.0, 1.0).setSuffix("ms"));
    private final SliderSetting maxQueued = this.add(new SliderSetting("MaxQueued", 200.0, 0.0, 1000.0, 1.0));
    private final BooleanSetting render = this.add(new BooleanSetting("Render", true));
    private final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(189, 153, 255, 30), () -> this.render.getValue()).injectBoolean(true));
    private final ColorSetting outline = this.add(new ColorSetting("Outline", new Color(189, 153, 255, 120), () -> this.render.getValue()).injectBoolean(true));
    private Vec3 serverPos;

    public BackTrack() {
        super("BackTrack", Module.Category.Exploit);
        this.setChinese("\u6253\u6ed1\u56de\u6eaf");
    }

    @Override
    public String getInfo() {
        return this.delay.getValueInt() + "ms";
    }

    @Override
    public void onDisable() {
        this.flush();
    }

    @Override
    public void onLogout() {
        this.packets.clear();
        this.serverPos = null;
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (BackTrack.nullCheck()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            this.flush();
            return;
        }
        Entity target = this.getTarget();
        if (target == null) {
            return;
        }
        if (!(packet instanceof ClientboundMoveEntityPacket)) {
            if (packet instanceof ClientboundTeleportEntityPacket) {
                ClientboundTeleportEntityPacket entityPacket = (ClientboundTeleportEntityPacket)packet;
                if (entityPacket.getId() != target.method_5628()) {
                    return;
                }
                this.updateServerPos(entityPacket);
                this.queue(packet);
                event.cancel();
            }
            return;
        }
        ClientboundMoveEntityPacket entityPacket2 = (ClientboundMoveEntityPacket)packet;
        Entity entity = entityPacket2.method_11645((Level)BackTrack.mc.level);
        if (entity == null || entity != target) {
            return;
        }
        this.updateServerPos(entityPacket2, target);
        this.queue(packet);
        event.cancel();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.update();
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        this.update();
        if (!this.render.getValue() || Module.nullCheck()) {
            return;
        }
        Entity target = this.getTarget();
        if (target == null || this.serverPos == null) {
            return;
        }
        AABB box = ((IEntity)target).getDimensions().method_30757(this.serverPos).method_1009(0.0, 0.1, 0.0);
        Render3DUtil.draw3DBox(matrixStack, box, this.fill.getValue(), this.outline.getValue(), this.outline.booleanValue, this.fill.booleanValue);
    }

    private void update() {
        if (BackTrack.nullCheck()) {
            this.packets.clear();
            this.serverPos = null;
            return;
        }
        long now = System.currentTimeMillis();
        this.packets.removeIf(p -> p.trySend(now));
    }

    private void flush() {
        if (BackTrack.nullCheck()) {
            this.packets.clear();
            this.serverPos = null;
            return;
        }
        for (QueuedPacket p : this.packets) {
            p.apply();
        }
        this.packets.clear();
        this.serverPos = null;
    }

    private void queue(Packet packet) {
        int max = this.maxQueued.getValueInt();
        if (max <= 0) {
            return;
        }
        this.packets.add(new QueuedPacket(packet, System.currentTimeMillis() + (long)this.delay.getValueInt()));
        while (this.packets.size() > max) {
            QueuedPacket overflow = this.packets.remove(0);
            overflow.apply();
        }
    }

    private Entity getTarget() {
        if (Aura.target != null) {
            return Aura.target;
        }
        HitResult target = BackTrack.mc.hitResult;
        if (target instanceof EntityHitResult) {
            EntityHitResult EntityHitResult = (EntityHitResult)target;
            return EntityHitResult.getEntity();
        }
        return null;
    }

    private void updateServerPos(ClientboundTeleportEntityPacket packet) {
        this.serverPos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    }

    private void updateServerPos(ClientboundMoveEntityPacket packet, Entity entity) {
        if (this.serverPos == null) {
            this.serverPos = entity.position();
        }
        double dx = (double)packet.getXa() / 4096.0;
        double dy = (double)packet.getYa() / 4096.0;
        double dz = (double)packet.getZa() / 4096.0;
        this.serverPos = this.serverPos.method_1031(dx, dy, dz);
    }

    private static class QueuedPacket {
        private final Packet packet;
        private final long sendAt;

        public QueuedPacket(Packet packet, long sendAt) {
            this.packet = packet;
            this.sendAt = sendAt;
        }

        public boolean trySend(long now) {
            if (now >= this.sendAt) {
                this.apply();
                return true;
            }
            return false;
        }

        public void apply() {
            if (this.packet != null) {
                this.packet.method_11054((PacketListener)Wrapper.mc.getConnection());
            }
        }
    }
}

