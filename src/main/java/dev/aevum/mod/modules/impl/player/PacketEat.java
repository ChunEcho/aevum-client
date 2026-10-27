/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.DataComponents
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.Packet;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.DataComponents;

public class PacketEat
extends Module {
    public static PacketEat INSTANCE;
    private final BooleanSetting deSync = this.add(new BooleanSetting("DeSync", false));
    private final BooleanSetting noRelease = this.add(new BooleanSetting("NoRelease", true));

    public PacketEat() {
        super("PacketEat", Module.Category.Player);
        this.setChinese("\u53d1\u5305\u8fdb\u98df");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.deSync.getValue() && PacketEat.mc.player.isUsingItem() && PacketEat.mc.player.getUseItem().method_7909().components().method_57832(DataComponents.FOOD)) {
            Module.sendSequencedPacket(id -> new ServerboundUseItemPacket(PacketEat.mc.player.getUsedItemHand(), id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        ServerboundPlayerActionPacket packet;
        Packet<?> packet2;
        if (this.noRelease.getValue() && (packet2 = event.getPacket()) instanceof ServerboundPlayerActionPacket && (packet = (ServerboundPlayerActionPacket)packet2).getAction() == ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.RELEASE_USE_ITEM && PacketEat.mc.player.getUseItem().method_7909().components().method_57832(DataComponents.FOOD)) {
            event.cancel();
        }
    }
}

