/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Items
 *  net.minecraft.PickaxeItem
 *  net.minecraft.SwordItem
 *  net.minecraft.AnvilBlock
 *  net.minecraft.Block
 *  net.minecraft.ChestBlock
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.DeathScreen
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Items;
import net.minecraft.PickaxeItem;
import net.minecraft.SwordItem;
import net.minecraft.AnvilBlock;
import net.minecraft.Block;
import net.minecraft.ChestBlock;
import net.minecraft.EnderChestBlock;
import net.minecraft.Packet;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.DeathScreen;

public class InteractTweaks
extends Module {
    public static InteractTweaks INSTANCE;
    public final BooleanSetting noEntityTrace = this.add(new BooleanSetting("NoEntityTrace", true).setParent());
    public final BooleanSetting onlyPickaxe = this.add(new BooleanSetting("OnlyPickaxe", true, this.noEntityTrace::isOpen));
    public final BooleanSetting multiTask = this.add(new BooleanSetting("MultiTask", true));
    public final BooleanSetting respawn = this.add(new BooleanSetting("Respawn", true));
    public final BooleanSetting ghostHand = this.add(new BooleanSetting("IgnoreBedrock", false));
    private final BooleanSetting noAbort = this.add(new BooleanSetting("NoMineAbort", false));
    private final BooleanSetting noReset = this.add(new BooleanSetting("NoMineReset", false));
    private final BooleanSetting noDelay = this.add(new BooleanSetting("NoMineDelay", false));
    private final BooleanSetting noInteract = this.add(new BooleanSetting("NoInteract", false));
    private final BooleanSetting pickaxeSwitch = this.add(new BooleanSetting("SwitchEat", false).setParent());
    private final BooleanSetting allowSword = this.add(new BooleanSetting("Sword", true, this.pickaxeSwitch::isOpen));
    private final BooleanSetting allowPickaxe = this.add(new BooleanSetting("Pickaxe", true, this.pickaxeSwitch::isOpen));
    private final BooleanSetting reach = this.add(new BooleanSetting("Reach", false));
    public final SliderSetting blockRange = this.add(new SliderSetting("BlockRange", 5.0, 0.0, 15.0, 0.1, this.reach::getValue));
    public final SliderSetting entityRange = this.add(new SliderSetting("EntityRange", 5.0, 0.0, 15.0, 0.1, this.reach::getValue));
    private final SliderSetting delay = this.add(new SliderSetting("UseDelay", 4.0, 0.0, 4.0, 1.0));
    public boolean isActive;
    boolean swapped = false;
    int lastSlot = 0;

    public InteractTweaks() {
        super("InteractTweaks", Module.Category.Player);
        this.setChinese("\u4ea4\u4e92\u8c03\u6574");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.respawn.getValue() && InteractTweaks.mc.screen instanceof DeathScreen) {
            InteractTweaks.mc.player.respawn();
            mc.method_1507(null);
        }
        if (InteractTweaks.mc.rightClickDelay <= 4 - this.delay.getValueInt()) {
            InteractTweaks.mc.rightClickDelay = 0;
        }
        if (this.pickaxeSwitch.getValue()) {
            if (!(InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof PickaxeItem && this.allowPickaxe.getValue() || InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof SwordItem && this.allowSword.getValue() || InteractTweaks.mc.player.getMainHandItem().method_7909() == Items.ENCHANTED_GOLDEN_APPLE || InteractTweaks.mc.player.getMainHandItem().method_7909() == Items.GOLDEN_APPLE)) {
                this.swapped = false;
                return;
            }
            int gapple = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
            if (gapple == -1) {
                gapple = InventoryUtil.findItem(Items.GOLDEN_APPLE);
            }
            if (gapple == -1) {
                if (this.swapped) {
                    InventoryUtil.switchToSlot(this.lastSlot);
                    this.swapped = false;
                }
                return;
            }
            if (InteractTweaks.mc.options.keyUse.isDown()) {
                if ((InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof PickaxeItem && this.allowPickaxe.getValue() || InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof SwordItem && this.allowSword.getValue()) && InteractTweaks.mc.player.getOffhandItem().method_7909() != Items.ENCHANTED_GOLDEN_APPLE && InteractTweaks.mc.player.getMainHandItem().method_7909() != Items.GOLDEN_APPLE) {
                    this.lastSlot = InteractTweaks.mc.player.getInventory().selected;
                    InventoryUtil.switchToSlot(gapple);
                    this.swapped = true;
                }
            } else if (this.swapped) {
                InventoryUtil.switchToSlot(this.lastSlot);
                this.swapped = false;
            }
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet;
        if (InteractTweaks.nullCheck() || !this.noInteract.getValue() || !((packet = event.getPacket()) instanceof ServerboundUseItemOnPacket)) {
            return;
        }
        ServerboundUseItemOnPacket packet2 = (ServerboundUseItemOnPacket)packet;
        Block block = InteractTweaks.mc.level.method_8320(packet2.getHitResult().method_17777()).method_26204();
        if (!InteractTweaks.mc.player.isShiftKeyDown() && (block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AnvilBlock)) {
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        this.isActive = false;
    }

    public boolean reach() {
        return this.isOn() && this.reach.getValue();
    }

    public boolean noAbort() {
        return this.isOn() && this.noAbort.getValue() && !InteractTweaks.mc.options.keyUse.isDown();
    }

    public boolean noReset() {
        return this.isOn() && this.noReset.getValue();
    }

    public boolean noDelay() {
        return this.isOn() && this.noDelay.getValue();
    }

    public boolean multiTask() {
        return this.isOn() && this.multiTask.getValue();
    }

    public boolean noEntityTrace() {
        if (this.isOff() || !this.noEntityTrace.getValue()) {
            return false;
        }
        if (this.onlyPickaxe.getValue()) {
            return InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof PickaxeItem || InteractTweaks.mc.player.isUsingItem() && !(InteractTweaks.mc.player.getMainHandItem().method_7909() instanceof SwordItem);
        }
        return true;
    }

    public boolean ghostHand() {
        return this.isOn() && this.ghostHand.getValue() && !InteractTweaks.mc.options.keyUse.isDown() && !InteractTweaks.mc.options.keyShift.isDown();
    }
}

