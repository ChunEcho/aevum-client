/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.AxeItem
 *  net.minecraft.ItemStack
 *  net.minecraft.SwordItem
 *  net.minecraft.TridentItem
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundContainerClosePacket
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ChatScreen
 *  net.minecraft.OptionsScreen
 *  net.minecraft.PauseScreen
 *  net.minecraft.OptionsSubScreen
 *  net.minecraft.InventoryScreen
 *  net.minecraft.MaceItem
 */
package dev.aevum.api.utils.player;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.gui.ClickGuiScreen;
import dev.aevum.mod.gui.PeekScreen;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.AxeItem;
import net.minecraft.ItemStack;
import net.minecraft.SwordItem;
import net.minecraft.TridentItem;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundContainerClosePacket;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;
import net.minecraft.ChatScreen;
import net.minecraft.OptionsScreen;
import net.minecraft.PauseScreen;
import net.minecraft.OptionsSubScreen;
import net.minecraft.InventoryScreen;
import net.minecraft.MaceItem;

public class EntityUtil
implements Wrapper {
    public static boolean inInventory() {
        return EntityUtil.mc.screen == null || EntityUtil.mc.screen instanceof OptionsSubScreen || EntityUtil.mc.screen instanceof OptionsScreen || EntityUtil.mc.screen instanceof PeekScreen || EntityUtil.mc.screen instanceof ChatScreen || EntityUtil.mc.screen instanceof InventoryScreen || EntityUtil.mc.screen instanceof ClickGuiScreen || EntityUtil.mc.screen instanceof PauseScreen;
    }

    public static boolean isHoldingWeapon(Player player) {
        return player.getMainHandItem().method_7909() instanceof SwordItem || player.getMainHandItem().method_7909() instanceof AxeItem || player.getMainHandItem().method_7909() instanceof MaceItem || player.getMainHandItem().method_7909() instanceof TridentItem;
    }

    public static boolean isInsideBlock(Player player) {
        return BlockUtil.canCollide((Entity)player, player.method_5829());
    }

    public static boolean isInsideBlock() {
        return EntityUtil.isInsideBlock((Player)EntityUtil.mc.player);
    }

    public static int getDamagePercent(ItemStack stack) {
        if (stack.getDamageValue() == stack.getMaxDamage()) {
            return 100;
        }
        return (int)((double)(stack.getMaxDamage() - stack.getDamageValue()) / Math.max(0.1, (double)stack.getMaxDamage()) * 100.0);
    }

    public static boolean isArmorLow(Player player, int durability) {
        for (ItemStack piece : player.method_5661()) {
            if (piece == null || piece.method_7960()) {
                return true;
            }
            if (EntityUtil.getDamagePercent(piece) >= durability) continue;
            return true;
        }
        return false;
    }

    public static float getHealth(Entity entity) {
        if (entity.showVehicleHealth()) {
            LivingEntity livingBase = (LivingEntity)entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }

    public static BlockPos getEntityPos(Entity entity) {
        return new BlockPosX(entity.position());
    }

    public static BlockPos getPlayerPos(boolean fix) {
        return new BlockPosX(EntityUtil.mc.player.position(), fix);
    }

    public static BlockPos getEntityPos(Entity entity, boolean fix) {
        return new BlockPosX(entity.position(), fix);
    }

    public static boolean canSee(BlockPos pos, Direction side) {
        Vec3 testVec = pos.method_46558().method_1031((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5);
        BlockHitResult result = EntityUtil.mc.level.method_17742(new ClipContext(EntityUtil.mc.player.getEyePosition(), testVec, ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.NONE, (Entity)EntityUtil.mc.player));
        return result == null || result.method_17783() == HitResult.HitResult$Type.MISS;
    }

    public static void swingHand(InteractionHand hand, SwingSide side) {
        switch (side) {
            case All: {
                EntityUtil.mc.player.method_6104(hand);
                break;
            }
            case Client: {
                EntityUtil.mc.player.method_23667(hand, false);
                break;
            }
            case Server: {
                mc.getConnection().method_52787((Packet)new ServerboundSwingPacket(hand));
            }
        }
    }

    public static void syncInventory() {
        if (AntiCheat.INSTANCE.closeScreen.getValue()) {
            mc.getConnection().method_52787((Packet)new ServerboundContainerClosePacket(EntityUtil.mc.player.containerMenu.containerId));
        }
    }
}

