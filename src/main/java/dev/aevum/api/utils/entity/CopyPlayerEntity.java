/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.MobEffectInstance
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Level
 */
package dev.aevum.api.utils.entity;

import com.mojang.authlib.GameProfile;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.PredictUtil;
import dev.aevum.asm.accessors.ILivingEntity;
import java.util.ArrayList;
import net.minecraft.MobEffectInstance;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Level;

public class CopyPlayerEntity
extends Player {
    private final boolean onGround;

    public CopyPlayerEntity(Player player) {
        this(player, false, 0.0, 0, 0, false, false, false, false);
    }

    public CopyPlayerEntity(Player player, boolean effect) {
        super((Level)Wrapper.mc.level, player.method_24515(), player.getYRot(), new GameProfile(player.getGameProfile().getId(), player.getGameProfile().getName()));
        this.method_5719((Entity)player);
        this.xo = player.xo;
        this.zo = player.zo;
        this.yo = player.yo;
        this.yBodyRot = player.yBodyRot;
        this.yHeadRot = player.yHeadRot;
        this.attackAnim = player.attackAnim;
        this.swingTime = player.swingTime;
        this.walkAnimation.method_48567(player.walkAnimation.method_48566());
        this.walkAnimation.position = player.walkAnimation.method_48569();
        ((ILivingEntity)((Object)this)).setLeaningPitch(((ILivingEntity)player).getLeaningPitch());
        ((ILivingEntity)((Object)this)).setLastLeaningPitch(((ILivingEntity)player).getLeaningPitch());
        this.wasTouchingWater = player.isInWater();
        this.method_5660(player.isShiftKeyDown());
        this.method_18380(player.getPose());
        this.method_5729(7, player.isFallFlying());
        this.onGround = player.onGround();
        this.method_24830(this.onGround);
        this.method_18799(player.getDeltaMovement());
        this.getInventory().method_7377(player.getInventory());
        if (effect) {
            for (MobEffectInstance se : new ArrayList(player.getActiveEffects())) {
                this.method_6092(se);
            }
        }
        this.method_52544(player.getAbsorptionAmount());
        this.method_6033(player.getHealth());
        this.method_5857(player.method_5829());
    }

    public CopyPlayerEntity(Player player, boolean effect, double maxMotionY, int ticks, int simulation, boolean step, boolean doubleStep, boolean jump, boolean inBlockPause) {
        this(player, effect);
        if (ticks > 0) {
            this.method_33574(PredictUtil.getPos(player, maxMotionY, ticks, simulation, step, doubleStep, jump, inBlockPause));
        }
    }

    public boolean onGround() {
        return this.onGround;
    }

    public boolean isSpectator() {
        return false;
    }

    public boolean method_7337() {
        return false;
    }
}

