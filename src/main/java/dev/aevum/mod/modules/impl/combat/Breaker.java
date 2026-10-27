/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.PickaxeItem
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Position
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.AntiCrawl;
import dev.aevum.mod.modules.impl.combat.CevBreaker;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.PickaxeItem;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Position;

public class Breaker
extends Module {
    public static Breaker INSTANCE;
    public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    public final SliderSetting range = this.add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
    private final BooleanSetting burrow = this.add(new BooleanSetting("Burrow", true));
    private final BooleanSetting head = this.add(new BooleanSetting("Head", true));
    private final BooleanSetting face = this.add(new BooleanSetting("Face", true));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
    private final BooleanSetting surround = this.add(new BooleanSetting("Surround", true));
    private final BooleanSetting cevPause = this.add(new BooleanSetting("CevPause", true));
    private final BooleanSetting forceDouble = this.add(new BooleanSetting("ForceDouble", false));
    public static final List<Block> hard;

    public Breaker() {
        super("Breaker", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u6316\u6398");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (CevBreaker.INSTANCE.isOn() && this.cevPause.getValue()) {
            return;
        }
        if (AntiCrawl.INSTANCE.work) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        Player player = CombatUtil.getClosestEnemy(this.targetRange.getValue());
        if (player == null) {
            return;
        }
        this.doBreak(player);
    }

    private void doBreak(Player player) {
        BlockPos pos = EntityUtil.getEntityPos((Entity)player, true);
        if (PacketMine.getBreakPos() != null && !PacketMine.getBreakPos().equals((Object)PacketMine.secondPos) && PacketMine.secondPos != null && !Breaker.mc.level.isEmptyBlock(PacketMine.secondPos) && this.forceDouble.getValue()) {
            return;
        }
        double[] dArray = new double[]{-0.8, 0.3, 1.1};
        double[] xzOffset = new double[]{0.3, -0.3};
        for (Player playerEntity : CombatUtil.getEnemies(this.targetRange.getValue())) {
            for (double y : dArray) {
                for (double x : xzOffset) {
                    for (double z : xzOffset) {
                        BlockPosX offsetPos = new BlockPosX(playerEntity.getX() + x, playerEntity.getY() + y, playerEntity.getZ() + z);
                        if (!this.canBreak(offsetPos) || !offsetPos.equals(PacketMine.getBreakPos())) continue;
                        return;
                    }
                }
            }
        }
        ArrayList<Float> yList = new ArrayList<Float>();
        if (this.down.getValue()) {
            yList.add(Float.valueOf(-0.8f));
        }
        if (this.head.getValue()) {
            yList.add(Float.valueOf(2.3f));
        }
        if (this.burrow.getValue()) {
            yList.add(Float.valueOf(0.3f));
        }
        if (this.face.getValue()) {
            yList.add(Float.valueOf(1.1f));
        }
        Iterator iterator = yList.iterator();
        while (iterator.hasNext()) {
            double y = ((Float)iterator.next()).floatValue();
            for (double offset : xzOffset) {
                BlockPosX offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
                if (!this.canBreak(offsetPos)) continue;
                PacketMine.INSTANCE.mine(offsetPos);
                return;
            }
        }
        Iterator iterator2 = yList.iterator();
        while (iterator2.hasNext()) {
            double y = ((Float)iterator2.next()).floatValue();
            for (double offset : xzOffset) {
                for (double offset2 : xzOffset) {
                    BlockPosX offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                    if (!this.canBreak(offsetPos)) continue;
                    PacketMine.INSTANCE.mine(offsetPos);
                    return;
                }
            }
        }
        if (this.surround.getValue()) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN || Math.sqrt(Breaker.mc.player.getEyePosition().method_1025(pos.method_10093(direction).method_46558())) > this.range.getValue() || !Breaker.mc.level.isEmptyBlock(pos.method_10093(direction)) && !pos.method_10093(direction).equals((Object)PacketMine.getBreakPos()) || !this.canPlaceCrystal(pos.method_10093(direction), false) || pos.method_10093(direction).equals((Object)PacketMine.secondPos)) continue;
                return;
            }
            ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
            for (Direction i : Direction.values()) {
                if (i == Direction.UP || i == Direction.DOWN || Math.sqrt(Breaker.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558())) > this.range.getValue() || !this.canBreak(pos.method_10093(i)) || !this.canPlaceCrystal(pos.method_10093(i), true) || this.isSurroundPos(pos.method_10093(i))) continue;
                arrayList.add(pos.method_10093(i));
            }
            if (!arrayList.isEmpty()) {
                PacketMine.INSTANCE.mine(arrayList.stream().min(Comparator.comparingDouble(E -> E.method_19770((Position)Breaker.mc.player.getEyePosition()))).get());
            } else {
                for (Direction i : Direction.values()) {
                    if (i == Direction.UP || i == Direction.DOWN || Math.sqrt(Breaker.mc.player.getEyePosition().method_1025(pos.method_10093(i).method_46558())) > this.range.getValue() || !this.canBreak(pos.method_10093(i)) || !this.canPlaceCrystal(pos.method_10093(i), false)) continue;
                    arrayList.add(pos.method_10093(i));
                }
                if (!arrayList.isEmpty()) {
                    PacketMine.INSTANCE.mine(arrayList.stream().min(Comparator.comparingDouble(E -> E.method_19770((Position)Breaker.mc.player.getEyePosition()))).get());
                }
            }
        }
    }

    private boolean isSurroundPos(BlockPos pos) {
        for (Direction i : Direction.values()) {
            BlockPos self;
            if (i == Direction.UP || i == Direction.DOWN || !(self = EntityUtil.getPlayerPos(true)).method_10093(i).equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean block) {
        BlockPos obsPos = pos.method_10074();
        BlockPos boost = obsPos.method_10084();
        return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN || !block) && BlockUtil.noEntityBlockCrystal(boost, true, true) && BlockUtil.noEntityBlockCrystal(boost.method_10084(), true, true);
    }

    private boolean isObsidian(BlockPos pos) {
        return Breaker.mc.player.getEyePosition().method_1022(pos.method_46558()) <= PacketMine.INSTANCE.range.getValue() && hard.contains(BlockUtil.getBlock(pos)) && BlockUtil.getClickSideStrict(pos) != null;
    }

    private boolean canBreak(BlockPos pos) {
        return this.isObsidian(pos) && (BlockUtil.getClickSideStrict(pos) != null || pos.equals((Object)PacketMine.getBreakPos())) && (!pos.equals((Object)PacketMine.secondPos) || !(Breaker.mc.player.getMainHandItem().method_7909() instanceof PickaxeItem) && !PacketMine.INSTANCE.autoSwitch.getValue() && !PacketMine.INSTANCE.noGhostHand.getValue());
    }

    static {
        hard = Arrays.asList(Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.NETHERITE_BLOCK, Blocks.CRYING_OBSIDIAN, Blocks.RESPAWN_ANCHOR, Blocks.ANCIENT_DEBRIS, Blocks.ANVIL);
    }
}

