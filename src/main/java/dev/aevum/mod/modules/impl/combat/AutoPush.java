/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.DirectionalBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.PistonBaseBlock
 *  net.minecraft.BlockState
 *  net.minecraft.Property
 *  net.minecraft.Minecraft
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.misc.ClientUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.core.impl.RotationManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.DirectionalBlock;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.PistonBaseBlock;
import net.minecraft.BlockState;
import net.minecraft.Property;
import net.minecraft.Minecraft;

public class AutoPush
extends Module {
    public static AutoPush INSTANCE;
    private final BooleanSetting torch = this.add(new BooleanSetting("Torch", false));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotation", true));
    private final BooleanSetting yawDeceive = this.add(new BooleanSetting("YawDeceive", true));
    private final BooleanSetting pistonPacket = this.add(new BooleanSetting("PistonPacket", false));
    private final BooleanSetting powerPacket = this.add(new BooleanSetting("PowerPacket", true));
    private final BooleanSetting noEating = this.add(new BooleanSetting("EatingPause", true));
    private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
    private final BooleanSetting allowWeb = this.add(new BooleanSetting("AllowWeb", true));
    private final SliderSetting updateDelay = this.add(new SliderSetting("Delay", 100, 0, 1000));
    private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
    private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0));
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0));
    private final SliderSetting surroundCheck = this.add(new SliderSetting("SurroundCheck", 2, 0, 4));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final Timer timer = new Timer();

    public AutoPush() {
        super("AutoPush", Module.Category.Combat);
        this.setChinese("\u6d3b\u585e\u63a8\u4eba");
        INSTANCE = this;
    }

    public static void pistonFacing(Direction direction) {
        if (direction == Direction.EAST) {
            Aevum.ROTATION.snapAt(-90.0f, 5.0f);
        } else if (direction == Direction.WEST) {
            Aevum.ROTATION.snapAt(90.0f, 5.0f);
        } else if (direction == Direction.NORTH) {
            Aevum.ROTATION.snapAt(180.0f, 5.0f);
        } else if (direction == Direction.SOUTH) {
            Aevum.ROTATION.snapAt(0.0f, 5.0f);
        }
    }

    @Override
    public void onEnable() {
        AutoCrystal.INSTANCE.lastBreakTimer.reset();
    }

    boolean isTargetHere(BlockPos blockPos, Entity entity) {
        return new AABB(blockPos).method_994(entity.method_5829());
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        if (!this.NV1586N0().passedMs(this.Ux8s().getValue())) {
            return;
        }
        if (this.VRwl().getValue() && !AutoPush.mc.player.onGround()) {
            return;
        }
        if (this.findBlock(this.getBlockType()) == -1 || this.findClass(PistonBaseBlock.class) == -1) {
            if (this.autoDisableAccess().getValue()) {
                this.disable();
            }
            return;
        }
        if (this.noEatingAccess().getValue() && AutoPush.mc.player.isUsingItem()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        block0: for (Player playerEntity : CombatUtil.getEnemies(this.getRange().getValue())) {
            if (!this.canPush(playerEntity).booleanValue()) continue;
            float[] array2 = new float[]{-0.25f, 0.0f, 0.25f};
            float[] array = array2;
            int length = array2.length;
            int n = ("\u79f0\ub8d9\u3224\uce2b\ufd3b\u900e\u59ce\u487e\u39ee\u941a\ueb9d\ufcfb\u204d\u832f\uc64b\uc9c9\u9af2\ucee9\udc27\u9011\ua9fc\uc093\u074f\u51ea\uc1d2\u994b\ubf87\ue03b\u02e5\ub7a7\u758c\u86e7\ue050\u7d2a\u6435\u2ecc\u5262\u7dcf\u8738\uf964\u747b\u5e4e\u6bc4\u4bf4\ua2c6\uc5c8\ue17e\u82e9\u81a4\u3f6e\u6cee\ufab5\u8649\u0d53\ud0d0\udf23\uc7c8\u4792\u7438\u0258\u270e\u931b\u9c90\u45d3\u3404\u1002\u4887\u8a2e\u9a08\ude05\u376b\u24a2\u31a6\uc6ad\u5516\u35a9\u48ea\ua7ea\u93fb\u2987\uce3a\ua4b6\uaf94\u9d62\ua0c7\ub416\ud610\ue070\ud00a\u2c47\ud3f5\u0ff9\ue6ae\u786e\u2d7e\u393b\ue354\u834f\u2096\udb8c\u9f97\uf8ac\ud199\ud397\ucfb4\u1f83\u0ea2\u8cbe\u5406\u8dd5\uf340\uabbf\u2d08\u2dc3\ue325\uf2a3\u1afc\u673b\uae4e\uae9c\u8984\u06ef\ubed6\u6d23\u4c05\u28e7\u8b33\uf52f\u4411\u77a3\u20f8\ub160\ub378\u59e0\ua828\u49ff\u6afc\uf309\u825b\ue95c\u6916\ub0ed\u0846\u5e54\u6f19\u4f52\ua7d7\ud7e9\u2b14\udaee\ue4bb\u4e67\ua49a\u438c\u7a62\u4558\u7475\u0cfb\u88dc\u5ca6\u7cb7\ud49a\u5ee3\uc703\u1c5e\u838b\ud23f\u6a5d\u9121\u253b\uc666\uf7cd\u0754\u74d2\u77f9\u608e\ub74b\uccfa\u586f\u80dd\udaef\u650c\uc5d9\u604d\u972c\u4d55\ud1fa\u3b02\ue0aa\ud557\ubc17\ubc79\ue000\ue54f\u3db7\uaf19\u4e21\u1ec5\uca22\u2a20\u3dd3\u2563\u5c85\u6dff\u03c3\u2888\u877e\u4e9c\u8cfd\u597b\u47b4\uf12b\u9b54\u3cb1\u0a5b\u6943\uca0e\u5675\u15ae\u2465\u3c1c\u2f87\u62db\u847c\ub5bd\u5de6\u7319\u6ab3\uefa2\ud582\u516e\uaf8a\u6db7\ued6e\u0fa7\ua37b\u270c\u8449\ufeff\u2967\u5ca1\u4f5c\u908b\udac2\uaa7c\u2195\ub340\uacc5\u2527\ua379\uee08\uaa29\ufa28\uc6f2\uabbe\u9820\uec17\u6d8b\u258d\ufe6b\u9892\u57af\u206e\u811a\u937e\ub491\u7f7c\uef58\u99bc\u794f\u3b89\u5c9c\u6d9f\ub85e\u79b7\ued51\u024a\u32c3\ude55\u0c83\u5372\u3805\u4ac9\ue327\uf79c\u3e39\u79f6\ua26e\u5927\u3e4a\uc184\u0896\ue761\ubccc\u1d13\u0d74\u40bb\ua932\ubd8c\ueb61\u2656\u225e\u5ff0\u2738\u6881\u59a9\uaace\u88a2\ubeef\u7b11\uc7eb\uad99\u9833\u7174\ude40\ua279\u5a66\uc3d7\u6103\ua9f9\ud4ec\u0293\u13e3\ue54b\ub660\ub381\u0572\u9c0c\u6816\u1c75\u2eb4\u1ae9\u4599\u38f2\udc9c\u0d6b\u9e28\u524a\ua427\u3fa2\ue45b\u3a95\u9f8d\u90d3\u44e9\u7b48\u41db\u4f94\u79c2\u62f9\u5c09\u5b14\u091d\u57a9\ue4b5\u927a\u3571\u279e\u4fe3\ude16\u48c0\ucfe7\u181f\ubf80\u0267\u6c11\u9cd1\ua04a\u9d5d\ub48e\u8d91\udf6a\uec2f\uae8f\u528c\u68bb\u9bb2\uaeb4\u560f\u5bdb\u9dd0\ueefa\uca4e\u7202\u3ed0\u3e92\u018f\u05fe\u17cb\u7a73\u9645\u0868\uc91f\u3d71\u63cc\u962a\u32d8\ud0b2\ucbcc\u6c47\u6f8b\uc9bb\u5f31\u14f0\u6706\ud15f\u8c3a\ue66f\u333a\ud6fa\ucac3\u8294\u6163\u4309\u6cf8\u5eb1\u24e6\ufc32\u0861\u191b\uc558\u8b66\u38c6\u5e99\u4bdd\u25c8\ud226\u9101\uab29\u1be6\u1771\u533c\uae35\u9d3e\u0792\ud6d5\ua515\u9966\u6852\u393c\ub590\u39dc\u71ea\ud7c2\ub1a9\u1a8a\ub82e\u897d\uecbd\u570b\u96c6\uebd2\uff5d\u6e0a\u7f1a\u0fc3\ua10e\uf472\udb8b\u11d2\u60e7\u86ef\u4c05\ua34b\u288f\u9ac0\u24f4\u4caf\u6308\u4e20\ud029\u6d30\u619d\u1607\uf6da\u6702\uf17c\u375d\uc1da\ud495\u46d1\u2e13\u13f9\uc7dd".charAt(414) << 16 | "\u79f0\ub8d9\u3224\uce2b\ufd3b\u900e\u59ce\u487e\u39ee\u941a\ueb9d\ufcfb\u204d\u832f\uc64b\uc9c9\u9af2\ucee9\udc27\u9011\ua9fc\uc093\u074f\u51ea\uc1d2\u994b\ubf87\ue03b\u02e5\ub7a7\u758c\u86e7\ue050\u7d2a\u6435\u2ecc\u5262\u7dcf\u8738\uf964\u747b\u5e4e\u6bc4\u4bf4\ua2c6\uc5c8\ue17e\u82e9\u81a4\u3f6e\u6cee\ufab5\u8649\u0d53\ud0d0\udf23\uc7c8\u4792\u7438\u0258\u270e\u931b\u9c90\u45d3\u3404\u1002\u4887\u8a2e\u9a08\ude05\u376b\u24a2\u31a6\uc6ad\u5516\u35a9\u48ea\ua7ea\u93fb\u2987\uce3a\ua4b6\uaf94\u9d62\ua0c7\ub416\ud610\ue070\ud00a\u2c47\ud3f5\u0ff9\ue6ae\u786e\u2d7e\u393b\ue354\u834f\u2096\udb8c\u9f97\uf8ac\ud199\ud397\ucfb4\u1f83\u0ea2\u8cbe\u5406\u8dd5\uf340\uabbf\u2d08\u2dc3\ue325\uf2a3\u1afc\u673b\uae4e\uae9c\u8984\u06ef\ubed6\u6d23\u4c05\u28e7\u8b33\uf52f\u4411\u77a3\u20f8\ub160\ub378\u59e0\ua828\u49ff\u6afc\uf309\u825b\ue95c\u6916\ub0ed\u0846\u5e54\u6f19\u4f52\ua7d7\ud7e9\u2b14\udaee\ue4bb\u4e67\ua49a\u438c\u7a62\u4558\u7475\u0cfb\u88dc\u5ca6\u7cb7\ud49a\u5ee3\uc703\u1c5e\u838b\ud23f\u6a5d\u9121\u253b\uc666\uf7cd\u0754\u74d2\u77f9\u608e\ub74b\uccfa\u586f\u80dd\udaef\u650c\uc5d9\u604d\u972c\u4d55\ud1fa\u3b02\ue0aa\ud557\ubc17\ubc79\ue000\ue54f\u3db7\uaf19\u4e21\u1ec5\uca22\u2a20\u3dd3\u2563\u5c85\u6dff\u03c3\u2888\u877e\u4e9c\u8cfd\u597b\u47b4\uf12b\u9b54\u3cb1\u0a5b\u6943\uca0e\u5675\u15ae\u2465\u3c1c\u2f87\u62db\u847c\ub5bd\u5de6\u7319\u6ab3\uefa2\ud582\u516e\uaf8a\u6db7\ued6e\u0fa7\ua37b\u270c\u8449\ufeff\u2967\u5ca1\u4f5c\u908b\udac2\uaa7c\u2195\ub340\uacc5\u2527\ua379\uee08\uaa29\ufa28\uc6f2\uabbe\u9820\uec17\u6d8b\u258d\ufe6b\u9892\u57af\u206e\u811a\u937e\ub491\u7f7c\uef58\u99bc\u794f\u3b89\u5c9c\u6d9f\ub85e\u79b7\ued51\u024a\u32c3\ude55\u0c83\u5372\u3805\u4ac9\ue327\uf79c\u3e39\u79f6\ua26e\u5927\u3e4a\uc184\u0896\ue761\ubccc\u1d13\u0d74\u40bb\ua932\ubd8c\ueb61\u2656\u225e\u5ff0\u2738\u6881\u59a9\uaace\u88a2\ubeef\u7b11\uc7eb\uad99\u9833\u7174\ude40\ua279\u5a66\uc3d7\u6103\ua9f9\ud4ec\u0293\u13e3\ue54b\ub660\ub381\u0572\u9c0c\u6816\u1c75\u2eb4\u1ae9\u4599\u38f2\udc9c\u0d6b\u9e28\u524a\ua427\u3fa2\ue45b\u3a95\u9f8d\u90d3\u44e9\u7b48\u41db\u4f94\u79c2\u62f9\u5c09\u5b14\u091d\u57a9\ue4b5\u927a\u3571\u279e\u4fe3\ude16\u48c0\ucfe7\u181f\ubf80\u0267\u6c11\u9cd1\ua04a\u9d5d\ub48e\u8d91\udf6a\uec2f\uae8f\u528c\u68bb\u9bb2\uaeb4\u560f\u5bdb\u9dd0\ueefa\uca4e\u7202\u3ed0\u3e92\u018f\u05fe\u17cb\u7a73\u9645\u0868\uc91f\u3d71\u63cc\u962a\u32d8\ud0b2\ucbcc\u6c47\u6f8b\uc9bb\u5f31\u14f0\u6706\ud15f\u8c3a\ue66f\u333a\ud6fa\ucac3\u8294\u6163\u4309\u6cf8\u5eb1\u24e6\ufc32\u0861\u191b\uc558\u8b66\u38c6\u5e99\u4bdd\u25c8\ud226\u9101\uab29\u1be6\u1771\u533c\uae35\u9d3e\u0792\ud6d5\ua515\u9966\u6852\u393c\ub590\u39dc\u71ea\ud7c2\ub1a9\u1a8a\ub82e\u897d\uecbd\u570b\u96c6\uebd2\uff5d\u6e0a\u7f1a\u0fc3\ua10e\uf472\udb8b\u11d2\u60e7\u86ef\u4c05\ua34b\u288f\u9ac0\u24f4\u4caf\u6308\u4e20\ud029\u6d30\u619d\u1607\uf6da\u6702\uf17c\u375d\uc1da\ud495\u46d1\u2e13\u13f9\uc7dd".charAt(415)) - 1400428399;
            if (((length | 1) & 1) == 0) {
                // empty if block
            }
            int n2 = n ^ 0x197F8742;
            while (true) {
                int n3 = ++n2;
                if (((n2 | 1) & 1) == 0) {
                    // empty if block
                }
                if (n3 >= length) {
                    int block;
                    if (!AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX(), playerEntity.getY() + 2.5, playerEntity.getZ())))) {
                        for (Direction direction : Direction.values()) {
                            if (direction == Direction.UP || direction == Direction.DOWN) continue;
                            BlockPos offset = EntityUtil.getEntityPos((Entity)playerEntity).method_10093(direction);
                            AABB offset2 = playerEntity.method_5829().method_997(new Vec3((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ()));
                            if (this.getBlock(offset.method_10084()) == Blocks.PISTON_HEAD || AutoPush.mc.level.method_39454((Entity)playerEntity, offset2.method_989(0.0, 1.0, 0.0)) || this.isTargetHere(offset, (Entity)playerEntity)) continue;
                            if (this.tryPush(EntityUtil.getEntityPos((Entity)playerEntity).method_10093(direction.getOpposite()).method_10084(), direction)) {
                                this.NV1586N0().reset();
                                return;
                            }
                            if (!this.tryPush(EntityUtil.getEntityPos((Entity)playerEntity).method_10093(direction.getOpposite()), direction)) continue;
                            this.NV1586N0().reset();
                            return;
                        }
                    }
                    for (float n4 : array) {
                        float[] array4 = array;
                        int length4 = array4.length;
                        for (int k = 0; k < length4; ++k) {
                            BlockPosX blockPosX = new BlockPosX(playerEntity.getX() + (double)n4, playerEntity.getY() + 0.5, playerEntity.getZ() + (double)array4[k]);
                            for (Direction direction2 : Direction.values()) {
                                if (direction2 == Direction.UP || direction2 == Direction.DOWN || !this.isTargetHere(blockPosX.method_10093(direction2), (Entity)playerEntity)) continue;
                                if (this.tryPush(blockPosX.method_10093(direction2.getOpposite()).method_10084(), direction2)) {
                                    this.NV1586N0().reset();
                                    return;
                                }
                                if (!this.tryPush(blockPosX.method_10093(direction2.getOpposite()), direction2)) continue;
                                this.NV1586N0().reset();
                                return;
                            }
                        }
                    }
                    BlockPosX blockPosX = new BlockPosX(playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ());
                    if (AutoPush.mc.level.isEmptyBlock((BlockPos)blockPosX) || (block = this.findBlock(this.getBlockType())) == -1) continue block0;
                    for (Direction direction3 : Direction.values()) {
                        BlockPos offset3;
                        if (direction3 == Direction.UP || direction3 == Direction.DOWN || !AutoPush.mc.level.isEmptyBlock(offset3 = blockPosX.method_10093(direction3)) || !BlockUtil.canPlace(offset3, this.placeRangeAccess().getValue())) continue;
                        int selectedSlot = AutoPush.mc.player.getInventory().selected;
                        this.doSwap(block);
                        ClientUtil.m_1z8dKi(offset3, this.rotateAccess().getValue(), this.xmP7().getValue());
                        if (this.yTKN().getValue()) {
                            this.doSwap(block);
                            ClientUtil.Njn83();
                        } else {
                            this.doSwap(selectedSlot);
                        }
                        this.NV1586N0().reset();
                        return;
                    }
                    continue block0;
                }
                float f = array2[n2];
                float[] array5 = array;
                int length7 = array5.length;
                for (int n7 = 0; n7 < length7; ++n7) {
                    BlockPosX blockPosX3 = new BlockPosX(playerEntity.getX() + (double)f, playerEntity.getY() + 0.5, playerEntity.getZ() + (double)array5[n7]);
                    for (Direction direction4 : Direction.values()) {
                        BlockPos offset4;
                        if (direction4 == Direction.UP || direction4 == Direction.DOWN || !this.isTargetHere(offset4 = blockPosX3.method_10093(direction4), (Entity)playerEntity) || !AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB(offset4))) continue;
                        if (this.tryPush(blockPosX3.method_10093(direction4.getOpposite()), direction4)) {
                            this.NV1586N0().reset();
                            return;
                        }
                        if (!this.tryPush(blockPosX3.method_10093(direction4.getOpposite()).method_10084(), direction4)) continue;
                        this.NV1586N0().reset();
                        return;
                    }
                }
            }
        }
    }

    private boolean tryPush(BlockPos modifyPos, Direction direction) {
        if (!AutoPush.mc.level.isEmptyBlock(modifyPos.method_10093(direction))) {
            return false;
        }
        if (this.isTrueFacing(modifyPos, direction) && this.facingCheck(modifyPos) && BlockUtil.clientCanPlace(modifyPos, false)) {
            boolean n = false;
            double value = this.placeRangeAccess().getValue();
            if (!((n | true) & true)) {
                // empty if block
            }
            if (BlockUtil.getPlaceSide(modifyPos, value) != null) {
                CombatUtil.modifyPos = modifyPos;
                CombatUtil.modifyBlockState = Blocks.PISTON.defaultBlockState();
                Direction[] values = Direction.values();
                int length = values.length;
                for (int i = 0; i < length; ++i) {
                    if (this.getBlock(modifyPos.method_10093(values[i])) != this.getBlockType()) continue;
                    n = true;
                    break;
                }
                for (Direction class_23502 : Direction.values()) {
                    if (n) break;
                    if (!BlockUtil.canPlace(modifyPos.method_10093(class_23502), this.placeRangeAccess().getValue())) continue;
                    n = true;
                }
                CombatUtil.modifyPos = null;
                if (n) {
                    int class1 = this.findClass(PistonBaseBlock.class);
                    Direction placeSide = BlockUtil.getPlaceSide(modifyPos);
                    if (placeSide != null) {
                        if (this.rotateAccess().getValue()) {
                            Aevum.ROTATION.lookAt(modifyPos.method_10093(placeSide), placeSide.getOpposite());
                        }
                        if (this.yawDeceiveAccess().getValue()) {
                            AutoPush.pistonFacing(direction.getOpposite());
                        }
                        int selectedSlot = AutoPush.mc.player.getInventory().selected;
                        this.doSwap(class1);
                        ClientUtil.m_1z8dKi(modifyPos, false, this.pistonPacketAccess().getValue());
                        if (this.yTKN().getValue()) {
                            this.doSwap(class1);
                            ClientUtil.Njn83();
                        } else {
                            this.doSwap(selectedSlot);
                        }
                        if (this.rotateAccess().getValue() && this.yawDeceiveAccess().getValue()) {
                            Aevum.ROTATION.lookAt(modifyPos.method_10093(placeSide), placeSide.getOpposite());
                        }
                        if (this.rotateAccess().getValue()) {
                            Aevum.ROTATION.snapBack();
                        }
                        for (Direction direction3 : Direction.values()) {
                            if (this.getBlock(modifyPos.method_10093(direction3)) != this.getBlockType()) continue;
                            if (this.XjkNja().getValue()) {
                                PacketMine.INSTANCE.mine(modifyPos.method_10093(direction3));
                            }
                            if (this.autoDisableAccess().getValue()) {
                                this.disable();
                            }
                            return true;
                        }
                        for (Direction direction4 : Direction.values()) {
                            if (direction4 == Direction.UP && this.torchAccess().getValue() || !BlockUtil.canPlace(modifyPos.method_10093(direction4), this.placeRangeAccess().getValue())) continue;
                            int selectedSlot2 = AutoPush.mc.player.getInventory().selected;
                            int block = this.findBlock(this.getBlockType());
                            this.doSwap(block);
                            ClientUtil.m_1z8dKi(modifyPos.method_10093(direction4), this.rotateAccess().getValue(), this.xmP7().getValue());
                            if (this.yTKN().getValue()) {
                                this.doSwap(block);
                                ClientUtil.Njn83();
                            } else {
                                this.doSwap(selectedSlot2);
                            }
                            if (this.XjkNja().getValue()) {
                                PacketMine.INSTANCE.mine(modifyPos.method_10093(direction4));
                            }
                            return true;
                        }
                        return true;
                    }
                }
            } else {
                Direction direction5 = null;
                for (Direction class_23503 : Direction.values()) {
                    if (class_23503 == Direction.UP && this.torchAccess().getValue()) continue;
                    if (direction5 != null) break;
                    CombatUtil.modifyPos = modifyPos.method_10093(class_23503);
                    CombatUtil.modifyBlockState = this.getBlockType().defaultBlockState();
                    if (BlockUtil.getPlaceSide(modifyPos) != null) {
                        direction5 = class_23503;
                    }
                    CombatUtil.modifyPos = null;
                    if (direction5 == null || BlockUtil.canPlace(modifyPos.method_10093(direction5))) continue;
                    direction5 = null;
                }
                if (direction5 != null) {
                    int selectedSlot3 = AutoPush.mc.player.getInventory().selected;
                    int block2 = this.findBlock(this.getBlockType());
                    this.doSwap(block2);
                    ClientUtil.m_1z8dKi(modifyPos.method_10093(direction5), this.rotateAccess().getValue(), this.xmP7().getValue());
                    if (this.yTKN().getValue()) {
                        this.doSwap(block2);
                        ClientUtil.Njn83();
                    } else {
                        this.doSwap(selectedSlot3);
                    }
                    CombatUtil.modifyPos = modifyPos.method_10093(direction5);
                    CombatUtil.modifyBlockState = this.getBlockType().defaultBlockState();
                    int class2 = this.findClass(PistonBaseBlock.class);
                    Direction class_23504 = BlockUtil.getPlaceSide(modifyPos);
                    if (class_23504 != null) {
                        if (this.rotateAccess().getValue()) {
                            Aevum.ROTATION.lookAt(modifyPos.method_10093(class_23504), class_23504.getOpposite());
                        }
                        if (this.yawDeceiveAccess().getValue()) {
                            AutoPush.pistonFacing(direction.getOpposite());
                        }
                        int selectedSlot4 = AutoPush.mc.player.getInventory().selected;
                        this.doSwap(class2);
                        ClientUtil.m_1z8dKi(modifyPos, false, this.pistonPacketAccess().getValue());
                        if (this.yTKN().getValue()) {
                            this.doSwap(class2);
                            ClientUtil.Njn83();
                        } else {
                            this.doSwap(selectedSlot4);
                        }
                        if (this.rotateAccess().getValue() && this.yawDeceiveAccess().getValue()) {
                            Aevum.ROTATION.lookAt(modifyPos.method_10093(class_23504), class_23504.getOpposite());
                        }
                        if (this.rotateAccess().getValue()) {
                            Aevum.ROTATION.snapBack();
                        }
                    }
                    CombatUtil.modifyPos = null;
                    return true;
                }
            }
        }
        if (AutoPush.mc.level.method_8320(modifyPos).method_26204() instanceof PistonBaseBlock && this.getBlockState(modifyPos).method_11654((Property)DirectionalBlock.FACING) == direction) {
            Direction[] values6 = Direction.values();
            int length6 = values6.length;
            for (int n3 = 0; n3 < length6; ++n3) {
                if (this.getBlock(modifyPos.method_10093(values6[n3])) != this.getBlockType()) {
                    continue;
                }
                if (this.autoDisableAccess().getValue()) {
                    this.disable();
                    return true;
                }
                return false;
            }
            for (Direction direction7 : Direction.values()) {
                if (direction7 == Direction.UP && this.torchAccess().getValue() || !BlockUtil.canPlace(modifyPos.method_10093(direction7), this.placeRangeAccess().getValue())) continue;
                int n = AutoPush.mc.player.getInventory().selected;
                int block3 = this.findBlock(this.getBlockType());
                this.doSwap(block3);
                ClientUtil.m_1z8dKi(modifyPos.method_10093(direction7), this.rotateAccess().getValue(), this.xmP7().getValue());
                if (this.yTKN().getValue()) {
                    this.doSwap(block3);
                    ClientUtil.Njn83();
                } else {
                    this.doSwap(n);
                }
                return true;
            }
        }
        return false;
    }

    private boolean facingCheck(BlockPos blockPos) {
        boolean b = false;
        if (ClientSetting.INSTANCE.lowVersion.getValue()) {
            Minecraft ntagHp7U8 = mc;
            Direction directionFromEntityLiving = MathUtil.getDirectionFromEntityLiving(blockPos, (LivingEntity)ntagHp7U8.player);
            return directionFromEntityLiving != Direction.UP && directionFromEntityLiving != Direction.DOWN;
        }
        return true;
    }

    private boolean isTrueFacing(BlockPos blockPos, Direction direction) {
        if (this.yawDeceiveAccess().getValue()) {
            return true;
        }
        Direction placeSide = BlockUtil.getPlaceSide(blockPos);
        if (placeSide == null) {
            return false;
        }
        float[] rotation = RotationManager.getRotation(new Vec3((double)blockPos.method_10263() + 0.5 + (double)placeSide.getNormal().method_10263() * 0.5, (double)blockPos.method_10264() + 0.5 + (double)placeSide.getNormal().method_10264() * 0.5, (double)blockPos.getZ() + 0.5 + (double)placeSide.getNormal().getZ() * 0.5));
        return MathUtil.getFacingOrder(rotation[0], rotation[1]).getOpposite() == direction;
    }

    private void doSwap(int n) {
        if (((n | 1) & 1) == 0) {
            // empty if block
        }
        if (this.yTKN().getValue()) {
            ClientUtil.FgmR(n, AutoPush.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(n);
        }
    }

    public int findBlock(Block block) {
        if (this.yTKN().getValue()) {
            return InventoryUtil.findBlockInventorySlot(block);
        }
        return InventoryUtil.findBlock(block);
    }

    public int findClass(Class<?> clazz) {
        if (this.yTKN().getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        }
        return InventoryUtil.findClass(clazz);
    }

    private Boolean canPush(Player playerEntity) {
        if (this.m_9q9cp().getValue() && !playerEntity.onGround()) {
            return false;
        }
        if (!this.getAllowWeb().getValue() && Aevum.PLAYER.isInWeb(playerEntity)) {
            return false;
        }
        float[] array = new float[]{-0.25f, 0.0f, 0.25f};
        int n = 0;
        if (AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX() + 1.0, playerEntity.getY() + 0.5, playerEntity.getZ())))) {
            ++n;
        }
        if (AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX() - 1.0, playerEntity.getY() + 0.5, playerEntity.getZ())))) {
            ++n;
        }
        if (AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 1.0)))) {
            ++n;
        }
        if (AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() - 1.0)))) {
            ++n;
        }
        for (float n2 : array) {
            float[] array3 = array;
            int length2 = array3.length;
            for (int j = 0; j < length2; ++j) {
                BlockPosX blockPosX = new BlockPosX(playerEntity.getX() + (double)n2, playerEntity.getY() + 0.5, playerEntity.getZ() + (double)array3[j]);
                for (Direction direction : Direction.values()) {
                    BlockPos offset;
                    if (direction == Direction.UP || direction == Direction.DOWN || !this.isTargetHere(offset = blockPosX.method_10093(direction), (Entity)playerEntity)) continue;
                    if (AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB(offset))) {
                        return true;
                    }
                    if (!((double)n > this.G773AM().getValue() - 1.0)) continue;
                    return true;
                }
            }
        }
        if (!AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX(), playerEntity.getY() + 2.5, playerEntity.getZ())))) {
            for (Direction direction2 : Direction.values()) {
                if (direction2 == Direction.UP || direction2 == Direction.DOWN) continue;
                BlockPos offset2 = EntityUtil.getEntityPos((Entity)playerEntity).method_10093(direction2);
                AABB offset3 = playerEntity.method_5829().method_997(new Vec3((double)direction2.getStepX(), (double)direction2.getStepY(), (double)direction2.getStepZ()));
                if (this.getBlock(offset2.method_10084()) == Blocks.PISTON_HEAD || AutoPush.mc.level.method_39454((Entity)playerEntity, offset3.method_989(0.0, 1.0, 0.0)) || this.isTargetHere(offset2, (Entity)playerEntity) || !AutoPush.mc.level.method_39454((Entity)playerEntity, new AABB((BlockPos)new BlockPosX(playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ())))) continue;
                return true;
            }
        }
        return (double)n > this.G773AM().getValue() - 1.0 || Aevum.HOLE.isHole(new BlockPosX(playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ()));
    }

    private Block getBlock(BlockPos blockPos) {
        return AutoPush.mc.level.method_8320(blockPos).method_26204();
    }

    private Block getBlockType() {
        if (this.torchAccess().getValue()) {
            return Blocks.REDSTONE_TORCH;
        }
        return Blocks.REDSTONE_BLOCK;
    }

    private BlockState getBlockState(BlockPos blockPos) {
        return AutoPush.mc.level.method_8320(blockPos);
    }

    private BooleanSetting torchAccess() {
        return this.torch;
    }

    private BooleanSetting rotateAccess() {
        return this.rotate;
    }

    private BooleanSetting yawDeceiveAccess() {
        return this.yawDeceive;
    }

    private BooleanSetting pistonPacketAccess() {
        return this.pistonPacket;
    }

    private BooleanSetting xmP7() {
        return this.powerPacket;
    }

    private BooleanSetting noEatingAccess() {
        return this.noEating;
    }

    private BooleanSetting XjkNja() {
        return this.mine;
    }

    private BooleanSetting getAllowWeb() {
        return this.allowWeb;
    }

    private SliderSetting Ux8s() {
        return this.updateDelay;
    }

    private BooleanSetting VRwl() {
        return this.selfGround;
    }

    private BooleanSetting m_9q9cp() {
        return this.onlyGround;
    }

    private BooleanSetting autoDisableAccess() {
        return this.autoDisable;
    }

    private SliderSetting getRange() {
        return this.range;
    }

    private SliderSetting placeRangeAccess() {
        return this.placeRange;
    }

    private SliderSetting G773AM() {
        return this.surroundCheck;
    }

    private BooleanSetting yTKN() {
        return this.inventory;
    }

    private Timer NV1586N0() {
        return this.timer;
    }
}

