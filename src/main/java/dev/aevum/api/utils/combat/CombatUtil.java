/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.Player
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.AbstractClientPlayer
 */
package dev.aevum.api.utils.combat;

import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.Player;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.BlockState;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.AbstractClientPlayer;

public class CombatUtil
implements Wrapper {
    public static final Timer breakTimer = new Timer();
    public static boolean terrainIgnore = false;
    public static BlockPos modifyPos;
    public static BlockState modifyBlockState;

    public static List<Player> getEnemies(double range) {
        ArrayList<Player> list = new ArrayList<Player>();
        for (AbstractClientPlayer player : Aevum.THREAD.getPlayers()) {
            if (!CombatUtil.isValid((Entity)player, range)) continue;
            list.add((Player)player);
        }
        return list;
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        CombatUtil.attackCrystal(new AABB(pos), rotate, eatingPause);
    }

    public static void attackCrystal(AABB box, boolean rotate, boolean eatingPause) {
        for (EndCrystal entity : BlockUtil.getEndCrystals(box)) {
            CombatUtil.attackWithDelay((Entity)entity, rotate, eatingPause);
        }
    }

    public static void attackWithDelay(Entity entity, boolean rotate, boolean usingPause) {
        if (!breakTimer.passed((long)(AntiCheat.INSTANCE.attackDelay.getValue() * 1000.0))) {
            return;
        }
        if (usingPause && CombatUtil.mc.player.isUsingItem()) {
            return;
        }
        CombatUtil.attack(entity, rotate);
    }

    public static void attack(Entity entity, boolean rotate) {
        if (entity != null) {
            Vec3 attackVec = MathUtil.getClosestPointToBox(CombatUtil.mc.player.getEyePosition(), entity.method_5829());
            if (CombatUtil.mc.player.getEyePosition().method_1022(attackVec) > AntiCheat.INSTANCE.ieRange.getValue()) {
                return;
            }
            breakTimer.reset();
            if (rotate && AntiCheat.INSTANCE.attackRotate.getValue()) {
                Aevum.ROTATION.lookAt(attackVec);
            }
            mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)entity, (boolean)CombatUtil.mc.player.isShiftKeyDown()));
            CombatUtil.mc.player.resetAttackStrengthTicker();
            EntityUtil.swingHand(InteractionHand.MAIN_HAND, AntiCheat.INSTANCE.attackSwing.getValue());
            if (rotate && AntiCheat.INSTANCE.attackRotate.getValue()) {
                Aevum.ROTATION.snapBack();
            }
        }
    }

    public static boolean isntValid(Entity entity, double range) {
        return !CombatUtil.isValid(entity, range);
    }

    public static boolean isValid(Entity entity, double range) {
        Player player;
        boolean invalid = entity == null || !entity.isAlive() || entity.equals((Object)CombatUtil.mc.player) || entity instanceof Player && Aevum.FRIEND.isFriend(player = (Player)entity) || CombatUtil.mc.player.position().method_1022(entity.position()) > range;
        return !invalid;
    }

    public static boolean isValid(Entity entity) {
        Player player;
        boolean invalid = entity == null || !entity.isAlive() || entity.equals((Object)CombatUtil.mc.player) || entity instanceof Player && Aevum.FRIEND.isFriend(player = (Player)entity);
        return !invalid;
    }

    public static Player getClosestEnemy(double getDistance) {
        Player closest = null;
        for (Player player : CombatUtil.getEnemies(getDistance)) {
            if (closest == null) {
                closest = player;
                continue;
            }
            if (!(CombatUtil.mc.player.method_5707(player.position()) < CombatUtil.mc.player.method_5858((Entity)closest))) continue;
            closest = player;
        }
        return closest;
    }

    static {
        modifyBlockState = Blocks.AIR.defaultBlockState();
    }
}

