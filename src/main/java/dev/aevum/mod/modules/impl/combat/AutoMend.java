/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.NonNullList
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.ItemEnchantments
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.events.impl.UpdateRotateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.NonNullList;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.ItemEnchantments;

public class AutoMend
extends Module {
    public static AutoMend INSTANCE;
    public final BooleanSetting rotation = this.add(new BooleanSetting("Rotation", true).setParent());
    private final BooleanSetting instant = this.add(new BooleanSetting("Instant", false, this.rotation::isOpen));
    public final BooleanSetting onlyBroken = this.add(new BooleanSetting("OnlyBroken", true));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 3, 0, 5));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    public final EnumSetting<SwingSide> interactSwing = this.add(new EnumSetting<SwingSide>("InteractSwing", SwingSide.All));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true));
    public final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final Timer delayTimer = new Timer();
    boolean lookDown = false;
    int exp = 0;
    private boolean throwing = false;

    public AutoMend() {
        super("AutoMend", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u7ecf\u9a8c\u74f6");
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        this.throwing = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        this.throwing = this.checkThrow();
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.lookDown && this.isThrow() && this.delayTimer.passed((long)this.delay.getValueInt() * 20L) && (!this.onlyGround.getValue() || AutoMend.mc.player.onGround())) {
            this.exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
            if (this.rotation.getValue() && this.instant.getValue()) {
                Aevum.ROTATION.snapAt(Aevum.ROTATION.rotationYaw, 88.0f);
            }
            this.throwExp();
            if (this.rotation.getValue() && this.instant.getValue()) {
                Aevum.ROTATION.snapBack();
            }
        }
        if (this.autoDisable.getValue() && !this.isThrow()) {
            this.disable();
        }
    }

    @Override
    public void onEnable() {
        this.lookDown = !this.rotation.getValue() || this.instant.getValue();
        boolean bl = this.lookDown;
        if (AutoMend.nullCheck()) {
            this.disable();
            return;
        }
        this.exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public String getInfo() {
        return String.valueOf(this.exp);
    }

    public void throwExp() {
        int newSlot;
        int oldSlot = AutoMend.mc.player.getInventory().selected;
        if (this.inventory.getValue() && (newSlot = InventoryUtil.findItemInventorySlotFromZero(Items.EXPERIENCE_BOTTLE)) != -1) {
            InventoryUtil.inventorySwap(newSlot, AutoMend.mc.player.getInventory().selected);
            AutoMend.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
            EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.interactSwing.getValue());
            InventoryUtil.inventorySwap(newSlot, AutoMend.mc.player.getInventory().selected);
            EntityUtil.syncInventory();
            this.delayTimer.reset();
        } else {
            newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE);
            if (newSlot != -1) {
                InventoryUtil.switchToSlot(newSlot);
                AutoMend.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.interactSwing.getValue());
                InventoryUtil.switchToSlot(oldSlot);
                this.delayTimer.reset();
            }
        }
    }

    @EventListener(priority=-200)
    public void RotateEvent(UpdateRotateEvent event) {
        if (!this.rotation.getValue() || this.instant.getValue()) {
            return;
        }
        if (this.isThrow()) {
            event.setPitch(88.0f);
            this.lookDown = true;
        }
    }

    public boolean isThrow() {
        return this.throwing;
    }

    public boolean checkThrow() {
        if (this.isOff()) {
            return false;
        }
        if (AutoMend.mc.screen != null) {
            return false;
        }
        if (this.usingPause.getValue() && AutoMend.mc.player.isUsingItem()) {
            return false;
        }
        if (!(InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) != -1 || this.inventory.getValue() && InventoryUtil.findItemInventorySlotFromZero(Items.EXPERIENCE_BOTTLE) != -1)) {
            return false;
        }
        if (this.onlyBroken.getValue()) {
            NonNullList armors = AutoMend.mc.player.getInventory().armor;
            for (ItemStack armor : armors) {
                if (armor.method_7960() || EntityUtil.getDamagePercent(armor) >= 100) continue;
                ItemEnchantments enchants = EnchantmentHelper.method_57532((ItemStack)armor);
                return enchants.method_57534().contains(AutoMend.mc.level.method_30349().method_30530(Enchantments.MENDING.method_58273()).method_40264(Enchantments.MENDING).get());
            }
        } else {
            return true;
        }
        return false;
    }
}

