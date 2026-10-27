/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 *  net.minecraft.Component
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.HUD;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.Vec3;
import net.minecraft.Component;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.Mth;
import net.minecraft.PoseStack;

public class DamageNumber
extends Module {
    private final EnumSetting<ColorMode> colorMode = this.add(new EnumSetting<ColorMode>("ColorMode", ColorMode.Sync));
    private final ColorSetting startColor = this.add(new ColorSetting("StartColor", new Color(189, 153, 255, 255), () -> this.colorMode.getValue() == ColorMode.Custom));
    private final ColorSetting endColor = this.add(new ColorSetting("EndColor", new Color(189, 153, 255, 40), () -> this.colorMode.getValue() == ColorMode.Custom));
    private final BooleanSetting onlyPlayers = this.add(new BooleanSetting("OnlyPlayers", true));
    private final SliderSetting lifeTime = this.add(new SliderSetting("LifeTime", 900.0, 200.0, 3000.0, 50.0));
    private final SliderSetting scale = this.add(new SliderSetting("Scale", 1.1, 0.4, 3.0, 0.05));
    private final SliderSetting rise = this.add(new SliderSetting("Rise", 0.9, 0.1, 2.5, 0.05));
    private final SliderSetting random = this.add(new SliderSetting("RandomOffset", 0.18, 0.0, 0.6, 0.01));
    private final SliderSetting range = this.add(new SliderSetting("TrackRange", 12.0, 2.0, 24.0, 0.5));
    private final Map<Integer, TargetHealth> trackedTargets = new HashMap<Integer, TargetHealth>();
    private final List<DamagePopup> popups = new ArrayList<DamagePopup>();
    private final DecimalFormat format = new DecimalFormat("0.0");

    public DamageNumber() {
        super("DamageNumber", Module.Category.Render);
        this.setChinese("\u4f24\u5bb3\u6570\u5b57");
    }

    @Override
    public void onEnable() {
        this.trackedTargets.clear();
        this.popups.clear();
    }

    @Override
    public void onDisable() {
        this.trackedTargets.clear();
        this.popups.clear();
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send send) {
        Packet<?> packet;
        if (Module.nullCheck() || !((packet = send.getPacket()) instanceof ServerboundInteractPacket)) {
            return;
        }
        ServerboundInteractPacket PlayerInteractEntityC2SPacket = (ServerboundInteractPacket)packet;
        if (Criticals.getInteractType(PlayerInteractEntityC2SPacket) != ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK) {
            return;
        }
        Entity entity = Criticals.getEntity(PlayerInteractEntityC2SPacket);
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity class_2825 = (LivingEntity)entity;
        if (this.onlyPlayers.getValue() && !(class_2825 instanceof Player)) {
            return;
        }
        this.trackedTargets.put(class_2825.method_5628(), new TargetHealth(class_2825, this.getHealth(class_2825), new Timer()));
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (DamageNumber.nullCheck()) {
            return;
        }
        this.popups.removeIf(damagePopup -> damagePopup.timer.passedMs((long)this.lifeTime.getValue()));
        Iterator<Map.Entry<Integer, TargetHealth>> iterator = this.trackedTargets.entrySet().iterator();
        while (iterator.hasNext()) {
            TargetHealth targetHealth = iterator.next().getValue();
            LivingEntity entity = targetHealth.entity;
            if (entity == null || !entity.isAlive() || entity.isRemoved() || (double)DamageNumber.mc.player.method_5739((Entity)entity) > this.range.getValue()) {
                iterator.remove();
                continue;
            }
            if (targetHealth.timer.passedMs(1400.0)) {
                iterator.remove();
                continue;
            }
            float health = this.getHealth(entity);
            float damage = targetHealth.lastHealth - health;
            if (damage > 0.05f) {
                this.popups.add(new DamagePopup(this.getPopupPos(entity), damage, new Timer()));
                targetHealth.lastHealth = health;
                targetHealth.timer.reset();
                continue;
            }
            if (health <= targetHealth.lastHealth) continue;
            targetHealth.lastHealth = health;
            targetHealth.timer.reset();
        }
    }

    @Override
    public void onRender3D(PoseStack MatrixStack) {
        if (Module.nullCheck() || this.popups.isEmpty()) {
            return;
        }
        long n = (long)this.lifeTime.getValue();
        for (DamagePopup damagePopup : this.popups) {
            float clamp = Mth.method_15363((float)((float)damagePopup.timer.getMs() / (float)n), (float)0.0f, (float)1.0f);
            Vec3 method_15364 = damagePopup.pos.method_1031(0.0, this.rise.getValue() * (double)clamp, 0.0);
            Render3DUtil.drawText3D(Component.method_30163((String)this.format.format(damagePopup.damage)), method_15364.x, method_15364.y, method_15364.z, 0.0, 0.0, this.scale.getValue(), this.getPopupColor(clamp).getRGB());
        }
    }

    private Vec3 getPopupPos(LivingEntity LivingEntity) {
        return new Vec3(LivingEntity.getX() + (Math.random() - 0.5) * this.random.getValue(), LivingEntity.getY() + (double)LivingEntity.getBbHeight() + 0.3, LivingEntity.getZ() + (Math.random() - 0.5) * this.random.getValue());
    }

    private float getHealth(LivingEntity LivingEntity) {
        return LivingEntity.getHealth() + LivingEntity.getAbsorptionAmount();
    }

    private Color getPopupColor(float n) {
        return ColorUtil.fadeColor(this.colorMode.getValue() == ColorMode.Sync ? this.getHudColor(0) : this.startColor.getValue(), this.colorMode.getValue() == ColorMode.Sync ? this.getHudColor(180) : this.endColor.getValue(), n);
    }

    private Color getHudColor(int n) {
        if (HUD.INSTANCE != null) {
            return new Color(HUD.INSTANCE.getColor(n), true);
        }
        return Color.WHITE;
    }

    private static enum ColorMode {
        Sync,
        Custom;

    }

    private static class TargetHealth {
        private final LivingEntity entity;
        private float lastHealth;
        private final Timer timer;

        private TargetHealth(LivingEntity entity, float lastHealth, Timer timer) {
            this.entity = entity;
            this.lastHealth = lastHealth;
            this.timer = timer;
        }
    }

    record DamagePopup(Vec3 pos, float damage, Timer timer) {
    }
}

