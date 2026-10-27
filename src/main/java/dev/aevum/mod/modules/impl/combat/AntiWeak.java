/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MobEffects
 *  net.minecraft.EndCrystal
 *  net.minecraft.SwordItem
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.MobEffects;
import net.minecraft.EndCrystal;
import net.minecraft.SwordItem;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;

public class AntiWeak
extends Module {
    private final EnumSetting<SwapMode> swapMode = this.add(new EnumSetting<SwapMode>("SwapMode", SwapMode.Inventory));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500).setSuffix("ms"));
    private final BooleanSetting onlyCrystal = this.add(new BooleanSetting("OnlyCrystal", true));
    private final Timer delayTimer = new Timer();
    boolean ignore = false;
    private ServerboundInteractPacket lastPacket = null;

    public AntiWeak() {
        super("AntiWeak", Module.Category.Combat);
        this.setChinese("\u53cd\u865a\u5f31");
    }

    @Override
    public String getInfo() {
        return this.swapMode.getValue().name();
    }

    @EventListener(priority=200)
    public void onPacketSend(PacketEvent.Send event) {
        ServerboundInteractPacket packet;
        if (AntiWeak.nullCheck()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (this.ignore) {
            return;
        }
        if (AntiWeak.mc.player.method_6112(MobEffects.WEAKNESS) == null) {
            return;
        }
        if (AntiWeak.mc.player.getMainHandItem().method_7909() instanceof SwordItem) {
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundInteractPacket && Criticals.getInteractType(packet = (ServerboundInteractPacket)packet2) == ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK) {
            if (this.onlyCrystal.getValue() && !(Criticals.getEntity(packet) instanceof EndCrystal)) {
                return;
            }
            this.lastPacket = packet;
            this.delayTimer.reset();
            this.ignore = true;
            this.doAnti();
            this.ignore = false;
            event.cancel();
        }
    }

    private void doAnti() {
        int strong;
        if (this.lastPacket == null) {
            return;
        }
        int n = strong = this.swapMode.getValue() != SwapMode.Inventory ? InventoryUtil.findClass(SwordItem.class) : InventoryUtil.findClassInventorySlot(SwordItem.class);
        if (strong == -1) {
            return;
        }
        int old = AntiWeak.mc.player.getInventory().selected;
        if (this.swapMode.getValue() != SwapMode.Inventory) {
            InventoryUtil.switchToSlot(strong);
        } else {
            InventoryUtil.inventorySwap(strong, AntiWeak.mc.player.getInventory().selected);
        }
        mc.getConnection().method_52787((Packet)this.lastPacket);
        if (this.swapMode.getValue() != SwapMode.Inventory) {
            if (this.swapMode.getValue() != SwapMode.Normal) {
                InventoryUtil.switchToSlot(old);
            }
        } else {
            InventoryUtil.inventorySwap(strong, AntiWeak.mc.player.getInventory().selected);
            EntityUtil.syncInventory();
        }
    }

    public static enum SwapMode {
        Normal,
        Silent,
        Inventory;

    }
}

