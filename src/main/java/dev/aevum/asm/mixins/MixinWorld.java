/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Level
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.BlockState
 *  net.minecraft.LevelChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.SelfTrap;
import dev.aevum.mod.modules.impl.player.InteractTweaks;
import net.minecraft.Level;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.BlockState;
import net.minecraft.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Level.class})
public abstract class MixinWorld {
    @Inject(method={"getBlockState"}, at={@At(value="HEAD")}, cancellable=true)
    public void blockStateHook(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (Wrapper.mc.level != null && Wrapper.mc.level.isInWorldBounds(pos)) {
            if (SelfTrap.airList.contains(pos)) {
                cir.setReturnValue((Object)Blocks.AIR.defaultBlockState());
                return;
            }
            if (!ClientSetting.INSTANCE.mioCompatible.getValue()) {
                LevelChunk worldChunk;
                BlockState tempState;
                boolean terrainIgnore = CombatUtil.terrainIgnore;
                BlockPos modifyPos = CombatUtil.modifyPos;
                BlockState modifyBlockState = CombatUtil.modifyBlockState;
                if (terrainIgnore || modifyPos != null) {
                    LevelChunk worldChunk2 = Wrapper.mc.level.method_8497(pos.method_10263() >> 4, pos.getZ() >> 4);
                    BlockState tempState2 = worldChunk2.method_8320(pos);
                    if (modifyPos != null && modifyBlockState != null && pos.equals((Object)modifyPos)) {
                        cir.setReturnValue((Object)modifyBlockState);
                        return;
                    }
                    if (terrainIgnore) {
                        if (Aevum.HOLE.isHard(tempState2.method_26204())) {
                            return;
                        }
                        cir.setReturnValue((Object)Blocks.AIR.defaultBlockState());
                    }
                } else if (InteractTweaks.INSTANCE.isActive && (tempState = (worldChunk = Wrapper.mc.level.method_8497(pos.method_10263() >> 4, pos.getZ() >> 4)).method_8320(pos)).method_26204() == Blocks.BEDROCK) {
                    cir.setReturnValue((Object)Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}

