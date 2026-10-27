/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockGetter
 *  net.minecraft.AirBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3
 *  net.minecraft.BlockState
 *  net.minecraft.LocalPlayer
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.BlockGetter;
import net.minecraft.AirBlock;
import net.minecraft.BlockPos;
import net.minecraft.Vec3i;
import net.minecraft.Vec3;
import net.minecraft.BlockState;
import net.minecraft.LocalPlayer;

public class AutoMine
extends Module {
    private final SliderSetting minY = this.add(new SliderSetting("MinY", -64.0, -64.0, 40.0, 1.0));
    private final SliderSetting reach = this.add(new SliderSetting("Reach", 4.5, 2.0, 6.0, 0.1));
    private final BooleanSetting avoidContainers = this.add(new BooleanSetting("AvoidContainers", true));
    private final BooleanSetting skipExposedOnly = this.add(new BooleanSetting("SkipBuried", true));
    private int retargetCooldown;

    public AutoMine() {
        super("AutoMine", Module.Category.Player);
        this.setChinese("\u533a\u5757\u6316\u6398");
    }

    @Override
    public void onEnable() {
        this.retargetCooldown = 0;
        if (PacketMine.INSTANCE != null && PacketMine.INSTANCE.isOff()) {
            PacketMine.INSTANCE.enable();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (AutoMine.nullCheck()) {
            return;
        }
        if (PacketMine.getBreakPos() != null && !AutoMine.mc.level.isEmptyBlock(PacketMine.getBreakPos())) {
            return;
        }
        if (this.retargetCooldown > 0) {
            --this.retargetCooldown;
            return;
        }
        BlockPos scanTarget = this.scanTarget();
        if (scanTarget == null) {
            this.retargetCooldown = 10;
            return;
        }
        PacketMine.INSTANCE.mine(scanTarget);
    }

    private BlockPos scanTarget() {
        LocalPlayer player = AutoMine.mc.player;
        BlockPos getVelocityAffectingPos = player.getBlockPosBelowThatAffectsMyMovement();
        int getDefaultName = getVelocityAffectingPos.method_10264();
        int valueInt = this.minY.getValueInt();
        int n = (int)Math.ceil(this.reach.getValueFloat());
        for (int i = getDefaultName; i >= valueInt; --i) {
            BlockPos BlockPos = null;
            double n2 = Double.MAX_VALUE;
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    double getX;
                    BlockPos class_23392 = new BlockPos(getVelocityAffectingPos.method_10263() + j, i, getVelocityAffectingPos.getZ() + k);
                    BlockState offsetX = AutoMine.mc.level.method_8320(class_23392);
                    if (offsetX.method_26215() || !(offsetX.method_26210((BlockGetter)AutoMine.mc.level, class_23392) >= 0.0f) || !offsetX.getFluidState().method_15771() || PacketMine.unbreakable(class_23392) || this.avoidContainers.getValue() && offsetX.method_26204() instanceof AirBlock || this.skipExposedOnly.getValue() && BlockUtil.getClickSideStrict(class_23392) == null || !((getX = player.getEyePosition().method_1022(Vec3.method_24953((Vec3i)class_23392))) <= (double)this.reach.getValueFloat()) || !(getX < n2)) continue;
                    n2 = getX;
                    BlockPos = class_23392;
                }
            }
            if (BlockPos == null) continue;
            return BlockPos;
        }
        return null;
    }
}

