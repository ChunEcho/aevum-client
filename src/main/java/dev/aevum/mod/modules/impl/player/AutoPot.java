/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.MobEffect
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.MobEffects
 *  net.minecraft.Player
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionContents
 *  net.minecraft.BlockPos
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.DataComponents
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.MobEffect;
import net.minecraft.MobEffectInstance;
import net.minecraft.MobEffects;
import net.minecraft.Player;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContents;
import net.minecraft.BlockPos;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.DataComponents;

public class AutoPot
extends Module {
    public static AutoPot INSTANCE;
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 5.0, 0.0, 10.0, 0.1).setSuffix("s"));
    private final BooleanSetting speed = this.add(new BooleanSetting("Speed", false));
    private final BooleanSetting resistance = this.add(new BooleanSetting("Resistance", false));
    private final BooleanSetting strength = this.add(new BooleanSetting("Strength", false));
    private final BooleanSetting slowFalling = this.add(new BooleanSetting("SlowFalling", false));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BindSetting speedKey = this.add(new BindSetting("SpeedKey", -1));
    private final BindSetting strengthKey = this.add(new BindSetting("StrengthKey", -1));
    private final BindSetting resistanceKey = this.add(new BindSetting("ResistanceKey", -1));
    private final Timer delayTimer = new Timer();
    private boolean throwing = false;
    private boolean turtlePress;
    private boolean speedPress;
    private boolean strengthPress;

    public AutoPot() {
        super("AutoPot", Module.Category.Player);
        this.setChinese("\u81ea\u52a8\u836f\u6c34");
        INSTANCE = this;
        Aevum.EVENT_BUS.subscribe(new AutoPotTick());
    }

    public static int findPotionInventorySlot(MobEffect targetEffect) {
        for (int i = 35; i >= 0; --i) {
            ItemStack itemStack = AutoPot.mc.player.getInventory().method_5438(i);
            if (Item.method_7880((Item)itemStack.method_7909()) != Item.method_7880((Item)Items.SPLASH_POTION)) continue;
            PotionContents potionContentsComponent = (PotionContents)itemStack.method_57825(DataComponents.POTION_CONTENTS, (Object)PotionContents.EMPTY);
            for (MobEffectInstance effect : potionContentsComponent.method_57397()) {
                if (effect.method_5579().comp_349() != targetEffect) continue;
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    public static int findPotion(MobEffect targetEffect) {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = AutoPot.mc.player.getInventory().method_5438(i);
            if (Item.method_7880((Item)itemStack.method_7909()) != Item.method_7880((Item)Items.SPLASH_POTION)) continue;
            PotionContents potionContentsComponent = (PotionContents)itemStack.method_57825(DataComponents.POTION_CONTENTS, (Object)PotionContents.EMPTY);
            for (MobEffectInstance effect : potionContentsComponent.method_57397()) {
                if (effect.method_5579().comp_349() != targetEffect) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        this.throwing = false;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.getValue() * 1000.0)) {
            return;
        }
        if (!this.onlyGround.getValue() || (AutoPot.mc.player.onGround() || Aevum.PLAYER.isInWeb((Player)AutoPot.mc.player)) && !AutoPot.mc.level.isEmptyBlock((BlockPos)new BlockPosX(AutoPot.mc.player.position().method_1031(0.0, -1.0, 0.0)))) {
            if (this.resistance.getValue() && (!AutoPot.mc.player.method_6059(MobEffects.DAMAGE_RESISTANCE) || AutoPot.mc.player.method_6112(MobEffects.DAMAGE_RESISTANCE).method_5578() < 2)) {
                this.throwing = this.checkThrow((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349());
                if (this.isThrow()) {
                    this.throwPotion((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349());
                    return;
                }
            }
            if (this.speed.getValue() && !AutoPot.mc.player.method_6059(MobEffects.MOVEMENT_SPEED)) {
                this.throwing = this.checkThrow((MobEffect)MobEffects.MOVEMENT_SPEED.comp_349());
                if (this.isThrow()) {
                    this.throwPotion((MobEffect)MobEffects.MOVEMENT_SPEED.comp_349());
                    return;
                }
            }
            if (this.strength.getValue() && !AutoPot.mc.player.method_6059(MobEffects.DAMAGE_BOOST)) {
                this.throwing = this.checkThrow((MobEffect)MobEffects.DAMAGE_BOOST.comp_349());
                if (this.isThrow()) {
                    this.throwPotion((MobEffect)MobEffects.DAMAGE_BOOST.comp_349());
                    return;
                }
            }
            if (this.slowFalling.getValue() && !AutoPot.mc.player.method_6059(MobEffects.SLOW_FALLING)) {
                this.throwing = this.checkThrow((MobEffect)MobEffects.SLOW_FALLING.comp_349());
                if (this.isThrow()) {
                    this.throwPotion((MobEffect)MobEffects.SLOW_FALLING.comp_349());
                }
            }
        }
    }

    public void throwPotion(MobEffect targetEffect) {
        int newSlot;
        int oldSlot = AutoPot.mc.player.getInventory().selected;
        if (this.inventory.getValue() && (newSlot = AutoPot.findPotionInventorySlot(targetEffect)) != -1) {
            Aevum.ROTATION.snapAt(Aevum.ROTATION.rotationYaw, 90.0f);
            InventoryUtil.inventorySwap(newSlot, AutoPot.mc.player.getInventory().selected);
            AutoPot.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
            InventoryUtil.inventorySwap(newSlot, AutoPot.mc.player.getInventory().selected);
            EntityUtil.syncInventory();
            Aevum.ROTATION.snapBack();
            this.delayTimer.reset();
        } else {
            newSlot = AutoPot.findPotion(targetEffect);
            if (newSlot != -1) {
                Aevum.ROTATION.snapAt(Aevum.ROTATION.rotationYaw, 90.0f);
                InventoryUtil.switchToSlot(newSlot);
                AutoPot.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, Aevum.ROTATION.getLastYaw(), Aevum.ROTATION.getLastPitch()));
                InventoryUtil.switchToSlot(oldSlot);
                Aevum.ROTATION.snapBack();
                this.delayTimer.reset();
            }
        }
    }

    public boolean isThrow() {
        return this.throwing;
    }

    public boolean checkThrow(MobEffect targetEffect) {
        if (!EntityUtil.inInventory()) {
            return false;
        }
        if (this.usingPause.getValue() && AutoPot.mc.player.isUsingItem()) {
            return false;
        }
        return AutoPot.findPotion(targetEffect) != -1 || this.inventory.getValue() && AutoPot.findPotionInventorySlot(targetEffect) != -1;
    }

    public class AutoPotTick {
        @EventListener
        public void onTick(ClientTickEvent event) {
            if (Module.nullCheck() || event.isPost()) {
                return;
            }
            if (AutoPot.this.inventory.getValue() && !EntityUtil.inInventory()) {
                return;
            }
            if (Wrapper.mc.screen == null) {
                if (AutoPot.this.resistanceKey.isPressed()) {
                    if (!AutoPot.this.turtlePress && AutoPot.this.checkThrow((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349())) {
                        AutoPot.this.throwPotion((MobEffect)MobEffects.DAMAGE_RESISTANCE.comp_349());
                        AutoPot.this.turtlePress = true;
                        return;
                    }
                } else {
                    AutoPot.this.turtlePress = false;
                }
                if (AutoPot.this.strengthKey.isPressed()) {
                    if (!AutoPot.this.strengthPress && AutoPot.this.checkThrow((MobEffect)MobEffects.DAMAGE_BOOST.comp_349())) {
                        AutoPot.this.throwPotion((MobEffect)MobEffects.DAMAGE_BOOST.comp_349());
                        AutoPot.this.strengthPress = true;
                        return;
                    }
                } else {
                    AutoPot.this.strengthPress = false;
                }
                if (AutoPot.this.speedKey.isPressed()) {
                    if (!AutoPot.this.speedPress && AutoPot.this.checkThrow((MobEffect)MobEffects.MOVEMENT_SPEED.comp_349())) {
                        AutoPot.this.throwPotion((MobEffect)MobEffects.MOVEMENT_SPEED.comp_349());
                        AutoPot.this.speedPress = true;
                    }
                } else {
                    AutoPot.this.speedPress = false;
                }
            } else {
                AutoPot.this.speedPress = false;
                AutoPot.this.turtlePress = false;
                AutoPot.this.strengthPress = false;
            }
        }
    }
}

