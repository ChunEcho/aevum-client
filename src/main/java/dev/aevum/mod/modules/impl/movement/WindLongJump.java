/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Items
 *  net.minecraft.Direction
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.LocalPlayer
 */
package dev.aevum.mod.modules.impl.movement;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Items;
import net.minecraft.Direction;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.LocalPlayer;

public class WindLongJump
extends Module {
    public static WindLongJump INSTANCE;
    private final SliderSetting throwDelay = this.add(new SliderSetting("ThrowDelay", 70.0, 0.0, 1000.0, 10.0).setSuffix("ms"));
    private final SliderSetting upSpeed = this.add(new SliderSetting("UpSpeed", 5.0, 0.1, 10.0, 0.1));
    private final SliderSetting forwardSpeed = this.add(new SliderSetting("ForwardSpeed", 3.0, 0.5, 20.0, 0.1));
    private final SliderSetting maintainTime = this.add(new SliderSetting("MaintainTime", 50.0, 10.0, 500.0, 10.0).setSuffix("ms"));
    private final BooleanSetting antiKick = this.add(new BooleanSetting("AntiKick", true));
    private final Timer stopTimer = new Timer();
    private long boostStartTime;
    private boolean thrown;
    private boolean jumped;
    private boolean stopping;

    public WindLongJump() {
        super("HighJump", "Wind charge boost", Module.Category.Movement);
        this.setChinese("\u98ce\u5f39\u9ad8\u8df3");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (WindLongJump.nullCheck()) {
            return;
        }
        this.thrown = false;
        this.jumped = false;
        this.boostStartTime = 0L;
        this.stopping = false;
        if (!this.throwCharge()) {
            this.disable();
            return;
        }
        this.thrown = true;
        this.boostStartTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        this.thrown = false;
        this.boostStartTime = 0L;
        this.stopping = false;
        if (WindLongJump.mc.player != null) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (WindLongJump.nullCheck()) {
            return;
        }
        if (this.stopping) {
            if (this.antiKick.getValue() && this.stopTimer.passed(500L)) {
                BlockUtil.clickBlock(WindLongJump.mc.player.method_24515().method_10074(), Direction.DOWN, false, InteractionHand.MAIN_HAND);
                this.disable();
            } else if (!this.antiKick.getValue()) {
                this.disable();
            }
            return;
        }
        if (!this.thrown || this.boostStartTime == 0L) {
            return;
        }
        long n = System.currentTimeMillis() - this.boostStartTime;
        long n2 = (long)this.throwDelay.getValue();
        long n3 = n2 + (long)this.maintainTime.getValue();
        if (n >= n2 && n < n3) {
            if (!this.jumped) {
                if (WindLongJump.mc.player.onGround()) {
                    WindLongJump.mc.player.jumpFromGround();
                }
                MovementUtil.setMotionY(this.upSpeed.getValue());
                this.jumped = true;
            }
            double radians = Math.toRadians(Aevum.ROTATION.rotationYaw);
            double value = this.forwardSpeed.getValue();
            MovementUtil.setMotionX(-Math.sin(radians) * value);
            MovementUtil.setMotionZ(Math.cos(radians) * value);
        } else if (n >= n3) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
            this.stopping = true;
            this.stopTimer.reset();
        }
    }

    private boolean throwCharge() {
        int n;
        boolean b = false;
        LocalPlayer player = WindLongJump.mc.player;
        if (player.getMainHandItem().method_7909() == Items.WIND_CHARGE) {
            n = player.getInventory().selected;
        } else {
            n = InventoryUtil.findItem(Items.WIND_CHARGE);
            if (n == -1 && (n = InventoryUtil.findItemInventorySlotFromZero(Items.WIND_CHARGE)) != -1) {
                b = true;
            }
        }
        if (n == -1) {
            return false;
        }
        float rotationYaw = Aevum.ROTATION.rotationYaw;
        Aevum.ROTATION.snapBack();
        Aevum.ROTATION.snapAt(rotationYaw, 90.0f);
        if (player.getMainHandItem().method_7909() == Items.WIND_CHARGE) {
            Module.sendSequencedPacket(n2 -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n2, rotationYaw, 90.0f));
            player.method_6104(InteractionHand.MAIN_HAND);
        } else if (b) {
            InventoryUtil.inventorySwap(n, player.getInventory().selected);
            Module.sendSequencedPacket(n2 -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n2, rotationYaw, 90.0f));
            player.method_6104(InteractionHand.MAIN_HAND);
            InventoryUtil.inventorySwap(n, player.getInventory().selected);
            EntityUtil.syncInventory();
        } else {
            int field_1725 = player.getInventory().selected;
            InventoryUtil.switchToSlot(n);
            Module.sendSequencedPacket(n2 -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, n2, rotationYaw, 90.0f));
            player.method_6104(InteractionHand.MAIN_HAND);
            InventoryUtil.switchToSlot(field_1725);
        }
        Aevum.ROTATION.snapBack();
        return true;
    }
}

