/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.Player
 *  net.minecraft.Item
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.DirectionalBlock
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.PistonBaseBlock
 *  net.minecraft.Property
 *  net.minecraft.Mth
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.misc.ClientUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.combat.AutoPush;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.Player;
import net.minecraft.Item;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.DirectionalBlock;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.PistonBaseBlock;
import net.minecraft.Property;
import net.minecraft.Mth;

public class PistonCrystal
extends Module {
    public static PistonCrystal INSTANCE;
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
    private final BooleanSetting pistonPacket = this.add(new BooleanSetting("PistonPacket", false));
    private final BooleanSetting noEating = this.add(new BooleanSetting("NoEating", true));
    private final BooleanSetting eatingBreak = this.add(new BooleanSetting("EatingBreak", false));
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 1.0, 8.0));
    private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 1.0, 8.0));
    private final BooleanSetting fire = this.add(new BooleanSetting("Fire", true));
    private final BooleanSetting switchPos = this.add(new BooleanSetting("Switch", false));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("SelfGround", true));
    private final BooleanSetting onlyStatic = this.add(new BooleanSetting("MovingPause", true));
    private final SliderSetting updateDelay = this.add(new SliderSetting("PlaceDelay", 100, 0, 500));
    private final SliderSetting posUpdateDelay = this.add(new SliderSetting("PosUpdateDelay", 500, 0, 1000));
    private final SliderSetting stageSetting = this.add(new SliderSetting("Stage", 4, 1, 10));
    private final SliderSetting pistonStage = this.add(new SliderSetting("PistonStage", 1, 1, 10));
    private final SliderSetting pistonMaxStage = this.add(new SliderSetting("PistonMaxStage", 1, 1, 10));
    private final SliderSetting powerStage = this.add(new SliderSetting("PowerStage", 3, 1, 10));
    private final SliderSetting powerMaxStage = this.add(new SliderSetting("PowerMaxStage", 3, 1, 10));
    private final SliderSetting crystalStage = this.add(new SliderSetting("CrystalStage", 4, 1, 10));
    private final SliderSetting crystalMaxStage = this.add(new SliderSetting("CrystalMaxStage", 4, 1, 10));
    private final SliderSetting fireStage = this.add(new SliderSetting("FireStage", 2, 1, 10));
    private final SliderSetting fireMaxStage = this.add(new SliderSetting("FireMaxStage", 2, 1, 10));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting debug = this.add(new BooleanSetting("Debug", false));
    private final Timer timer = new Timer();
    private final Timer crystalTimer = new Timer();
    public BlockPos bestPos = null;
    public BlockPos bestOPos = null;
    public Direction bestFacing = null;
    public double getDistance = 100.0;
    public boolean getPos = false;
    public int stage = 1;
    private Player target;
    private boolean isPiston;

    public PistonCrystal() {
        super("PistonCrystal", Module.Category.Combat);
        this.setTarget(null);
        this.m_01Axx(false);
        this.setChinese("\u6d3b\u585e\u6c34\u6676");
        INSTANCE = this;
    }

    private static boolean canFire(BlockPos blockPos) {
        if (BlockUtil.canReplace(blockPos.method_10074())) {
            return (("\u4de1\u7fad\u0d35\uc030\ued3c\u85fd\ub68a\ueb8f\ue40f\ufbd7\u0616\u547a\u0c3d\ud35d\u9909\ufd8b\ue4de\u183d\u0192\u43d6\ub3ab\ud12c\ubc44\ub4c4\u0d10\u4cea\u447e\ud8e1\ucce1\u0b5b\ua33a\u1529\u96a7\ubd74\uf292\ue326\u6d76\u9868\ud1f2\u088c\u11a9\u46a2\u16c3\uabc1\uc4b0\u6d9e\u6e96\u5905\udc52\ua635\u6594\u3563\u1540\u563d\uc768\u8010\u4b74\u41a0\uc047\ub8d0\u1e7d\ufce2\ub8f6\u3e40\u08bb\uc64d\u4b99\u9c67\u4ccf\u15d0\ucf98\u2124\u2ba3\uf684\u0f37\u8f70\u490a\u22a9\u695a\ub51a\ubf58\udcd8\u1abb\uf1ed\ua178\u57e3\ufc5d\u8d38\u98a3\u6d96\u2052\u2794\u0f12\u3577\ub91c\u7ba9\u6922\u308a\u3b35\uecf3\u3193\u4cd2\uf867\u391d\ua2ea\uf643\u99b7\u64a0\uacc5\uabbb\u1116\u625c\u03a0\u5619\ue2c4\u42d3\u1a59\uc910\udb76\u87c6\u69b5\u6d98\u93f8\u1421\ub3fb\u1961\ue599\ua275\u4c79\ue8b9\u5592\u6e5d\u677d\u60f9\uab93\uc45f\u1536\u0fed\u1b5b\u4546\u095d\udccf\u2531\u1bdf\u5586\u62d7\u44d4\u13c7\u5170\udd98\ucea7\u99fc\u2743\u503b\u5af1\u2ba0\uf4e7\u19e8\u4679\u0a6a\u950c\u8f80\u5202\udf92\u31b5\ub289\ud9b2\uccbb\uf348\u1c05\u1a4f\ud39a\u640b\u248a\uea25\u907a\u394e\ub6df\u3a84\ua1d6\u125b\u11d5\u3750\u1369\uf47b\u6de1\uf5ef\ueb33\u042e\u6b70\u61a1\u76ee\u26cd\u33dc\u11cf\u478c\ub202\ud668\u923c\u4677\uf2be\u1373\u9c27\ue032\u1c65\u4a03\ufab6\u8b3d\u2a2d\ue64b\uae7b\u736d\u0806\u748a\u3b71\ud310\uae69\ua358\u7ff5\u4b73\uebf4\u5b60\u7f32\ud8ff\ue5b3\u8559\u7c34\ue0eb\u5e98\uece3\u3124\ua234\ubeee\u9604\ua3e1\u6460\u5d97\u711f\uc6e3\u40dd\u5391\u1e81\udae2\uc6f7\ub0c7\u1786\u3cba\u501b\u1236\uc2a7\u43e5\u6d84\u4d59\u340a\u262f\u553a\u0787\u346c\u5e4a\u6be4\u01ec\u2bc0\u04b2\ud004\u18d1\u888f\u0949\u3813\ub100\uf014\u68c9\u57ad\u2529\ub623\u2db4\u41e8\uc3ef\ufd02\u9bdd\u7e6d\u138e\ud6a2\u17d8\uf481\u517b\u706c\u2e29\u3fd9\u35fe\u6d4b\ud2ee\ube4c\u7979\u9492\u1355\u5e62\u81d0\ud61f\u88b8\u67ca\u6878\u4d43\u4a21\ueb8d\u8fe0\u4e19\uba05\u2692\uf1ea\u9466\u52d9\ueb83\ubf48\u4b9f\ub501\u792f\u6d7a\u3357\u1aec\u2d46\u5dd9\ube14\u77b4\ubca8\u7e66\ucb7c\u8ded\u57aai\ua3d2\u027c\u5911\u0aee\u4661\u16ac\u45ce\u139b\u3c9e\ue501\uea42\uac79\ufbdc\u330a\ua163\uac0f\u56ff\u4278\uac98\u38cd\ub690\ub9d8\ufd18\u841b\ud25a\u879f\u4718\ud160\u3582\ucc37\u4ce9\u3ebc\u6fd5\uf7e3\ud7d2\uad7c\ud3d9\ua32b\u9439\ub7ea\u1abe\u0187\ub65a\ub74b\u7c90\uf3a1\uad33\u5b2c\u3524\u7d69\u69bc\u57b3\u59ec\u7eb8\u33b0\ua935\u9c7a\u3e49\ufdf9\uc260\ud521\u431d\ub3e8\u86a6\uae86\u46f8\ub1ab\ucad2\u3abd\ud64c\u8778\u35e3\ue467\u08d3\ua1a7\u019f\u553a\u922c\ua371\u3f4c\u7d11\u2d9f\u879f\u3ee3\u9ec4\ue906\u578d\u2fba\u3c22\u205f\u3dd6\u939a\u75c1\u7e30\u0a20\ua753\uaf8d\u140d\ue407\ude03\u9079\ubda7\uba8e\u3408\u4ed5\u00a6\u5235\uddc1\ubd3d\u82b5\ubc1d\u44d4\u5b08\u1d51\u1ecb\ua017\u34d4\uda16\u8e77\uaf4b\udc9c\u7593\u1d0a\u69ee\u4046\u8ff2\u5870\ucf66\u7843\u344e\u7d45\ue40d\u3154\ucd0e\ufd8b\ua870\u235e\u5bb6\u676d\u2d11\udb21\uf1df\u53a9\ud170\u2e0c\uea3b\uf2a8\uaead\u5647\u4905\u0f41\u9a0f\u1299\u21c3\u3193\u9e4f\u7369\u4649\u04fc\ua6f3\u8e18\u9e72\u06ad\u2017\u9287\uaf40\uc1c9\ufaf6\u5417\uff8e\u596c\u45d6\u843f\u3c0c\u51b2\u2109\u860c\u9a5c\uceda\u503e\uefc2\udb83\ue707\u14c2\ubd29\u698a\ubac9\u82e2\u1a8a\u0af2\udfea\ud351\u1cae\u24ef\udcd8\u7ba7\u522a\u2af6\u1f7d\u1212\u2645\u8e04\ufd15\u0508\u69b5\u5347\ub0cf\ue87a\uce77\u4662\ubf59\u0c3c\ucf2e\u67ca\u23bd\u52ba\u400b\uc35a\uce3e\uf651\uccc0\u7cce\uc13e\uac01\u5823\u38b5\ucedc\uebb0\uf1a5\u4724\u1c1a\u1e8c\u09bb\ud562\u1515\u5a74\u0b69\u08d7\u7045\u7ce1\uccde\u812e\u07e0\uca92\ubb75\u7590\u2143\u375c\uf2bb\u7e06L\uba77\u94d8\ucdb6\ufa83\uaa37\u9100\u898e\ucb4a\ub491\u50fc\uc101\u7964\u1a26\u587e\u869c\u93eb\u934e\u27f8\u38f9\uc542\u82be\u6caa\u1307\ua582\u59d1\u95ac\u883b\ua969\ub42f\u6231\u95ff\u5df8\uc337\ub541\u877c\u46f3\u122c\u7897\u7e03\ua005\u8185\ubaa0\u4340\u7288\u8b81\ude5c\u8b7d\u56a3\u3b5e\ue1f4\ub301\uca65\u3117\u052a\ub815\uac14\u16b1\u4374\uf46d\ueb0c\u9f8e\u77fe\u22b0\u3ab9\u44b1\ud4e2\u3e69\u9a00\uc44e\ucbe1\ub51f\ua1b2\u0755\u64f5\ub4ac\u3721\uda4b\ub54c\ufb40\ub2f3\u550c\u2da5\uae21\uee5c\u0e0f\ubff2\ucc8d\u8eb2\u9490\ud2e0\ufa1c\uf59e\u9cfb\u2e81\uca87\ud89b\u59ed\ub6bc\u7c13\ud2d7\u6296\u86ba\u9702\u6860\uf576\u3475\u5a8e\u45e5\u49fe\u8e21\uf032\ucb2c\ua405\u4476\u8201\ud844\u2327\u2ad7\uee99\u1679\u19ee\ua6d4\ub70a\uee26\ub16c\uedb6\u4920\u8157\u897c\u8de3\u9bb7\u7997\ua1c2\u727a\ud279\u2aee\ubc13\u865c\u8b95\u9e38\u8173\u9e68\u489f\u151a\u7ed2\u5d70\u119d\ua663\u3a94\uc9e0\uef3f\ub2f8\u516b\uf054\uf636\uede3\u7844\u722e\ue6f9\ue097\ueb65\u76bb\u97fd\u3d66\u3f07\ue34f\ue7de\u49b0\u92a8\u1fb1\u6d30\u4d86\u3e57\ud1f1\u0c10\ua118\uef0e\ud9aa\u3477\u6d6a\ue37c\u6352\u1584\uccfe\ud854\ud05a\u1506\u53de\u7624\u4988\u8068\u92ba\ufcaa\ufa46\u8cce\u72ae\uf105p\u083d\ue1bc\u4043\u811d\uf424\ub4fd\u610d\u3c4e\u532a\uc7cd\ud2f6\udb88\u75ed\u2015\ubfc8\u60cc\ue047\ubc9a\u9d27\ub32e\u7915\u25dd\u041b\ubfa4\u9095\ue5c4\u3da2\ue057\u48d9\u4d46\u5fe9\ua05e\uaa7e\u9aa8\uaf33\u415d\u0d70\u9944\ubf61\u9d5d\uce06\u7fc2\uf98b\u5afb\u8167\uceba\u80cd\u76d3\u2e26\u2cf2\u71c1\u412c\uacf8\u7d02\u5e02\u6df1\u9c71\ufc14\u7c8a\u5a8e\udbff\u1cfa".charAt(666) << 16 | 0x8EB2) + 918211305 ^ 0x348599B) != 0;
        }
        return PistonCrystal.mc.level.isEmptyBlock(blockPos) && BlockUtil.canClick(blockPos.method_10093(Direction.DOWN)) && BlockUtil.isStrictDirection(blockPos.method_10074(), Direction.UP);
    }

    public void onTick() {
        if (this.pistonStageAccess().getValue() > this.K2hz().getValue()) {
            this.pistonStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.fireStageAccess().getValue() > this.K2hz().getValue()) {
            this.fireStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.powerStageAccess().getValue() > this.K2hz().getValue()) {
            this.powerStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.crystalStageAccess().getValue() > this.K2hz().getValue()) {
            this.crystalStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.pistonMaxStageAccess().getValue() > this.K2hz().getValue()) {
            this.pistonMaxStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.fireMaxStageAccess().getValue() > this.K2hz().getValue()) {
            this.fireMaxStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.getPowerMaxStage().getValue() > this.K2hz().getValue()) {
            this.getPowerMaxStage().setValue(this.K2hz().getValue());
        }
        if (this.crystalMaxStageAccess().getValue() > this.K2hz().getValue()) {
            this.crystalMaxStageAccess().setValue(this.K2hz().getValue());
        }
        if (this.crystalMaxStageAccess().getValue() < this.crystalStageAccess().getValue()) {
            this.crystalStageAccess().setValue(this.crystalMaxStageAccess().getValue());
        }
        if (this.getPowerMaxStage().getValue() < this.powerStageAccess().getValue()) {
            this.powerStageAccess().setValue(this.getPowerMaxStage().getValue());
        }
        if (this.pistonMaxStageAccess().getValue() < this.pistonStageAccess().getValue()) {
            this.pistonStageAccess().setValue(this.pistonMaxStageAccess().getValue());
        }
        if (this.fireMaxStageAccess().getValue() < this.fireStageAccess().getValue()) {
            this.fireStageAccess().setValue(this.fireMaxStageAccess().getValue());
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent updateEvent) {
        PistonCrystal pistonCrystal;
        boolean b4;
        boolean b3;
        boolean b = false;
        this.onTick();
        this.setTarget(CombatUtil.getClosestEnemy(this.jz83().getValue()));
        if (this.targetAccess() == null) {
            return;
        }
        if (this.noEatingAccess().getValue() && PistonCrystal.mc.player.isUsingItem()) {
            return;
        }
        boolean value = this.CQYxr().getValue();
        if (!PistonCrystal.mc.player.onGround()) {
            boolean b2 = value;
            b3 = true;
            b4 = b2;
            pistonCrystal = this;
        } else {
            boolean b5 = value;
            b3 = false;
            b4 = b5;
            pistonCrystal = this;
        }
        if (ClientUtil.m_7lzNX(pistonCrystal, b4, b3, this.onlyGroundAccess().getValue())) {
            return;
        }
        BlockPos entityPos = EntityUtil.getEntityPos((Entity)this.targetAccess(), true);
        if (!PistonCrystal.mc.player.isUsingItem() || this.eatingBreakAccess().getValue()) {
            if (this.checkCrystal(entityPos.method_10086(0))) {
                ClientUtil.attack(entityPos.method_10086(0), this.rotateAccess().getValue(), true);
            }
            if (this.checkCrystal(entityPos.method_10086(1))) {
                ClientUtil.attack(entityPos.method_10086(1), this.rotateAccess().getValue(), true);
            }
            if (this.checkCrystal(entityPos.method_10086(2))) {
                ClientUtil.attack(entityPos.method_10086(2), this.rotateAccess().getValue(), true);
            }
        }
        if (this.bestPos != null && PistonCrystal.mc.level.method_8320(this.bestPos).method_26204() instanceof PistonBaseBlock) {
            this.m_01Axx(true);
        } else if (this.isPistonAccess()) {
            this.m_01Axx(false);
            this.crystalTimerAccess().reset();
            this.bestPos = null;
        }
        if (this.crystalTimerAccess().passedMs(this.t4Gq().getValueInt())) {
            this.stage = 0;
            this.getDistance = 100.0;
            this.getPos = false;
            this.getBestPos(entityPos.method_10086(2));
            this.getBestPos(entityPos.method_10084());
        }
        if (!this.m_1appl().passedMs(this.yG5D().getValueInt())) {
            return;
        }
        if (this.getPos && this.bestPos != null) {
            this.m_1appl().reset();
            if (this.debugAccess().getValue()) {
                this.sendMessage("[Debug] PistonPos:" + String.valueOf(String.valueOf(this.bestPos)) + " Facing:" + String.valueOf(String.valueOf(this.bestFacing)) + " CrystalPos:" + String.valueOf(String.valueOf(this.bestOPos.method_10093(this.bestFacing))));
            }
            this.doPistonAura(this.bestPos, this.bestFacing, this.bestOPos);
        }
    }

    public boolean check(boolean b, boolean b2, boolean b3) {
        if (ClientUtil.isMoving() && b) {
            return true;
        }
        if (b2 && b3) {
            return true;
        }
        return this.findBlock(Blocks.REDSTONE_BLOCK) == -1 || this.findClass(PistonBaseBlock.class) == -1 || this.findItem(Items.END_CRYSTAL) == -1;
    }

    private boolean checkCrystal(BlockPos blockPos) {
        for (Entity entity : BlockUtil.getEntities(new AABB(blockPos))) {
            if (!(entity instanceof EndCrystal) || AutoCrystal.INSTANCE.calculateDamage(entity.position(), this.targetAccess(), this.targetAccess()) <= 7.0f) continue;
            return true;
        }
        return false;
    }

    private boolean findTarget(BlockPos blockPos) {
        for (Entity entity : BlockUtil.getEntities(new AABB(blockPos))) {
            if (!(entity instanceof EndCrystal) || !EntityUtil.getEntityPos(entity).equals((Object)blockPos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getInfo() {
        if (this.targetAccess() != null) {
            return this.targetAccess().method_5477().getString();
        }
        return null;
    }

    private void getBestPos(BlockPos blockPos) {
        int n = 0;
        Direction[] values = Direction.values();
        int length = values.length;
        if (n < length) {
            Direction direction = values[n];
            if (direction != Direction.DOWN && direction != Direction.UP) {
                this.getPos(blockPos, direction);
            }
            ++n;
        }
    }

    private void getPos(BlockPos blockPos, Direction direction) {
        if (!BlockUtil.canPlaceCrystal(blockPos.method_10093(direction)) && !this.findTarget(blockPos.method_10093(direction))) {
            return;
        }
        this.getPos(blockPos.method_10079(direction, 3), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 3).method_10084(), direction, blockPos);
        int n = blockPos.method_10093(direction).method_10263() - blockPos.method_10263();
        int n2 = blockPos.method_10093(direction).getZ() - blockPos.getZ();
        this.getPos(blockPos.method_10079(direction, 3).method_10069(n2, 0, n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 3).method_10069(-n2, 0, -n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 3).method_10069(n2, 1, n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 3).method_10069(-n2, 1, -n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2).method_10084(), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2).method_10069(n2, 0, n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2).method_10069(-n2, 0, -n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2).method_10069(n2, 1, n), direction, blockPos);
        this.getPos(blockPos.method_10079(direction, 2).method_10069(-n2, 1, -n), direction, blockPos);
    }

    private void getPos(BlockPos bestPos, Direction bestFacing, BlockPos bestOPos) {
        if (this.switchPosAccess().getValue() && this.bestPos != null && this.bestPos.equals((Object)bestPos) && PistonCrystal.mc.level.isEmptyBlock(this.bestPos)) {
            return;
        }
        if (!BlockUtil.canPlace(bestPos, this.SrlvX().getValue()) && !(this.getBlock(bestPos) instanceof PistonBaseBlock)) {
            return;
        }
        if (this.findClass(PistonBaseBlock.class) == -1) {
            return;
        }
        if (!(!ClientSetting.INSTANCE.lowVersion.getValue() || this.getBlock(bestPos) instanceof PistonBaseBlock || PistonCrystal.mc.player.getY() - (double)bestPos.method_10264() > -2.0 && PistonCrystal.mc.player.getY() - (double)bestPos.method_10264() < 3.0 || !(BlockUtil.distanceToXZ((double)bestPos.method_10263() + 0.5, (double)bestPos.getZ() + 0.5) < 2.6))) {
            return;
        }
        if (PistonCrystal.mc.level.isEmptyBlock(bestPos.method_10079(bestFacing, -1)) && PistonCrystal.mc.level.method_8320(bestPos.method_10079(bestFacing, -1)).method_26204() != Blocks.FIRE) {
            if (this.getBlock(bestPos.method_10093(bestFacing.getOpposite())) == Blocks.MOVING_PISTON && !this.findTarget(bestPos.method_10093(bestFacing.getOpposite()))) {
                return;
            }
            if (!BlockUtil.canPlace(bestPos, this.SrlvX().getValue()) && !this.isPiston(bestPos, bestFacing)) {
                return;
            }
            if ((double)Mth.method_15355((float)((float)PistonCrystal.mc.player.getEyePosition().method_1025(bestPos.method_46558()))) >= this.getDistance && this.bestPos != null) {
                return;
            }
            this.bestPos = bestPos;
            this.bestOPos = bestOPos;
            this.bestFacing = bestFacing;
            this.getDistance = Mth.method_15355((float)((float)PistonCrystal.mc.player.getEyePosition().method_1025(bestPos.method_46558())));
            this.getPos = true;
            this.crystalTimerAccess().reset();
        }
    }

    private void doPistonAura(BlockPos blockPos, Direction direction, BlockPos blockPos2) {
        if ((double)this.stage >= this.K2hz().getValue()) {
            this.stage = 0;
        }
        ++this.stage;
        if (PistonCrystal.mc.level.isEmptyBlock(blockPos)) {
            if (!BlockUtil.canPlace(blockPos)) {
                return;
            }
            if ((double)this.stage >= this.pistonStageAccess().getValue() && (double)this.stage <= this.pistonMaxStageAccess().getValue()) {
                Direction placeSide = BlockUtil.getPlaceSide(blockPos);
                if (placeSide == null) {
                    return;
                }
                int selectedSlot = PistonCrystal.mc.player.getInventory().selected;
                AutoPush.pistonFacing(direction);
                int class1 = this.findClass(PistonBaseBlock.class);
                this.doSwap(class1);
                ClientUtil.m_1z8dKi(blockPos, false, this.getPistonPacket().getValue());
                if (this.m_0lwz().getValue()) {
                    this.doSwap(class1);
                    ClientUtil.Njn83();
                } else {
                    this.doSwap(selectedSlot);
                }
                BlockPos offset = blockPos.method_10093(placeSide);
                Direction opposite = placeSide.getOpposite();
                if (this.rotateAccess().getValue()) {
                    Aevum.ROTATION.lookAt(offset, opposite);
                }
            }
        }
        if ((double)this.stage >= this.powerStageAccess().getValue() && (double)this.stage <= this.getPowerMaxStage().getValue()) {
            this.doRedStone(blockPos, direction, blockPos2.method_10093(direction));
        }
        if ((double)this.stage >= this.crystalStageAccess().getValue() && (double)this.stage <= this.crystalMaxStageAccess().getValue()) {
            this.placeCrystal(blockPos2, direction);
        }
        if ((double)this.stage >= this.fireStageAccess().getValue() && (double)this.stage <= this.fireMaxStageAccess().getValue()) {
            this.doFire(blockPos2, direction);
        }
    }

    private void placeCrystal(BlockPos blockPos, Direction direction) {
        if (!BlockUtil.canPlaceCrystal(blockPos.method_10093(direction))) {
            return;
        }
        int item = this.findItem(Items.END_CRYSTAL);
        if (item == -1) {
            return;
        }
        int selectedSlot = PistonCrystal.mc.player.getInventory().selected;
        this.doSwap(item);
        BlockUtil.placeCrystal(blockPos.method_10093(direction), true);
        if (this.m_0lwz().getValue()) {
            this.doSwap(item);
            ClientUtil.Njn83();
        } else {
            this.doSwap(selectedSlot);
        }
    }

    private boolean isPiston(BlockPos blockPos, Direction direction) {
        boolean b = false;
        if (!(PistonCrystal.mc.level.method_8320(blockPos).method_26204() instanceof PistonBaseBlock)) {
            String s = "\u4de1\u7fad\u0d35\uc030\ued3c\u85fd\ub68a\ueb8f\ue40f\ufbd7\u0616\u547a\u0c3d\ud35d\u9909\ufd8b\ue4de\u183d\u0192\u43d6\ub3ab\ud12c\ubc44\ub4c4\u0d10\u4cea\u447e\ud8e1\ucce1\u0b5b\ua33a\u1529\u96a7\ubd74\uf292\ue326\u6d76\u9868\ud1f2\u088c\u11a9\u46a2\u16c3\uabc1\uc4b0\u6d9e\u6e96\u5905\udc52\ua635\u6594\u3563\u1540\u563d\uc768\u8010\u4b74\u41a0\uc047\ub8d0\u1e7d\ufce2\ub8f6\u3e40\u08bb\uc64d\u4b99\u9c67\u4ccf\u15d0\ucf98\u2124\u2ba3\uf684\u0f37\u8f70\u490a\u22a9\u695a\ub51a\ubf58\udcd8\u1abb\uf1ed\ua178\u57e3\ufc5d\u8d38\u98a3\u6d96\u2052\u2794\u0f12\u3577\ub91c\u7ba9\u6922\u308a\u3b35\uecf3\u3193\u4cd2\uf867\u391d\ua2ea\uf643\u99b7\u64a0\uacc5\uabbb\u1116\u625c\u03a0\u5619\ue2c4\u42d3\u1a59\uc910\udb76\u87c6\u69b5\u6d98\u93f8\u1421\ub3fb\u1961\ue599\ua275\u4c79\ue8b9\u5592\u6e5d\u677d\u60f9\uab93\uc45f\u1536\u0fed\u1b5b\u4546\u095d\udccf\u2531\u1bdf\u5586\u62d7\u44d4\u13c7\u5170\udd98\ucea7\u99fc\u2743\u503b\u5af1\u2ba0\uf4e7\u19e8\u4679\u0a6a\u950c\u8f80\u5202\udf92\u31b5\ub289\ud9b2\uccbb\uf348\u1c05\u1a4f\ud39a\u640b\u248a\uea25\u907a\u394e\ub6df\u3a84\ua1d6\u125b\u11d5\u3750\u1369\uf47b\u6de1\uf5ef\ueb33\u042e\u6b70\u61a1\u76ee\u26cd\u33dc\u11cf\u478c\ub202\ud668\u923c\u4677\uf2be\u1373\u9c27\ue032\u1c65\u4a03\ufab6\u8b3d\u2a2d\ue64b\uae7b\u736d\u0806\u748a\u3b71\ud310\uae69\ua358\u7ff5\u4b73\uebf4\u5b60\u7f32\ud8ff\ue5b3\u8559\u7c34\ue0eb\u5e98\uece3\u3124\ua234\ubeee\u9604\ua3e1\u6460\u5d97\u711f\uc6e3\u40dd\u5391\u1e81\udae2\uc6f7\ub0c7\u1786\u3cba\u501b\u1236\uc2a7\u43e5\u6d84\u4d59\u340a\u262f\u553a\u0787\u346c\u5e4a\u6be4\u01ec\u2bc0\u04b2\ud004\u18d1\u888f\u0949\u3813\ub100\uf014\u68c9\u57ad\u2529\ub623\u2db4\u41e8\uc3ef\ufd02\u9bdd\u7e6d\u138e\ud6a2\u17d8\uf481\u517b\u706c\u2e29\u3fd9\u35fe\u6d4b\ud2ee\ube4c\u7979\u9492\u1355\u5e62\u81d0\ud61f\u88b8\u67ca\u6878\u4d43\u4a21\ueb8d\u8fe0\u4e19\uba05\u2692\uf1ea\u9466\u52d9\ueb83\ubf48\u4b9f\ub501\u792f\u6d7a\u3357\u1aec\u2d46\u5dd9\ube14\u77b4\ubca8\u7e66\ucb7c\u8ded\u57aai\ua3d2\u027c\u5911\u0aee\u4661\u16ac\u45ce\u139b\u3c9e\ue501\uea42\uac79\ufbdc\u330a\ua163\uac0f\u56ff\u4278\uac98\u38cd\ub690\ub9d8\ufd18\u841b\ud25a\u879f\u4718\ud160\u3582\ucc37\u4ce9\u3ebc\u6fd5\uf7e3\ud7d2\uad7c\ud3d9\ua32b\u9439\ub7ea\u1abe\u0187\ub65a\ub74b\u7c90\uf3a1\uad33\u5b2c\u3524\u7d69\u69bc\u57b3\u59ec\u7eb8\u33b0\ua935\u9c7a\u3e49\ufdf9\uc260\ud521\u431d\ub3e8\u86a6\uae86\u46f8\ub1ab\ucad2\u3abd\ud64c\u8778\u35e3\ue467\u08d3\ua1a7\u019f\u553a\u922c\ua371\u3f4c\u7d11\u2d9f\u879f\u3ee3\u9ec4\ue906\u578d\u2fba\u3c22\u205f\u3dd6\u939a\u75c1\u7e30\u0a20\ua753\uaf8d\u140d\ue407\ude03\u9079\ubda7\uba8e\u3408\u4ed5\u00a6\u5235\uddc1\ubd3d\u82b5\ubc1d\u44d4\u5b08\u1d51\u1ecb\ua017\u34d4\uda16\u8e77\uaf4b\udc9c\u7593\u1d0a\u69ee\u4046\u8ff2\u5870\ucf66\u7843\u344e\u7d45\ue40d\u3154\ucd0e\ufd8b\ua870\u235e\u5bb6\u676d\u2d11\udb21\uf1df\u53a9\ud170\u2e0c\uea3b\uf2a8\uaead\u5647\u4905\u0f41\u9a0f\u1299\u21c3\u3193\u9e4f\u7369\u4649\u04fc\ua6f3\u8e18\u9e72\u06ad\u2017\u9287\uaf40\uc1c9\ufaf6\u5417\uff8e\u596c\u45d6\u843f\u3c0c\u51b2\u2109\u860c\u9a5c\uceda\u503e\uefc2\udb83\ue707\u14c2\ubd29\u698a\ubac9\u82e2\u1a8a\u0af2\udfea\ud351\u1cae\u24ef\udcd8\u7ba7\u522a\u2af6\u1f7d\u1212\u2645\u8e04\ufd15\u0508\u69b5\u5347\ub0cf\ue87a\uce77\u4662\ubf59\u0c3c\ucf2e\u67ca\u23bd\u52ba\u400b\uc35a\uce3e\uf651\uccc0\u7cce\uc13e\uac01\u5823\u38b5\ucedc\uebb0\uf1a5\u4724\u1c1a\u1e8c\u09bb\ud562\u1515\u5a74\u0b69\u08d7\u7045\u7ce1\uccde\u812e\u07e0\uca92\ubb75\u7590\u2143\u375c\uf2bb\u7e06L\uba77\u94d8\ucdb6\ufa83\uaa37\u9100\u898e\ucb4a\ub491\u50fc\uc101\u7964\u1a26\u587e\u869c\u93eb\u934e\u27f8\u38f9\uc542\u82be\u6caa\u1307\ua582\u59d1\u95ac\u883b\ua969\ub42f\u6231\u95ff\u5df8\uc337\ub541\u877c\u46f3\u122c\u7897\u7e03\ua005\u8185\ubaa0\u4340\u7288\u8b81\ude5c\u8b7d\u56a3\u3b5e\ue1f4\ub301\uca65\u3117\u052a\ub815\uac14\u16b1\u4374\uf46d\ueb0c\u9f8e\u77fe\u22b0\u3ab9\u44b1\ud4e2\u3e69\u9a00\uc44e\ucbe1\ub51f\ua1b2\u0755\u64f5\ub4ac\u3721\uda4b\ub54c\ufb40\ub2f3\u550c\u2da5\uae21\uee5c\u0e0f\ubff2\ucc8d\u8eb2\u9490\ud2e0\ufa1c\uf59e\u9cfb\u2e81\uca87\ud89b\u59ed\ub6bc\u7c13\ud2d7\u6296\u86ba\u9702\u6860\uf576\u3475\u5a8e\u45e5\u49fe\u8e21\uf032\ucb2c\ua405\u4476\u8201\ud844\u2327\u2ad7\uee99\u1679\u19ee\ua6d4\ub70a\uee26\ub16c\uedb6\u4920\u8157\u897c\u8de3\u9bb7\u7997\ua1c2\u727a\ud279\u2aee\ubc13\u865c\u8b95\u9e38\u8173\u9e68\u489f\u151a\u7ed2\u5d70\u119d\ua663\u3a94\uc9e0\uef3f\ub2f8\u516b\uf054\uf636\uede3\u7844\u722e\ue6f9\ue097\ueb65\u76bb\u97fd\u3d66\u3f07\ue34f\ue7de\u49b0\u92a8\u1fb1\u6d30\u4d86\u3e57\ud1f1\u0c10\ua118\uef0e\ud9aa\u3477\u6d6a\ue37c\u6352\u1584\uccfe\ud854\ud05a\u1506\u53de\u7624\u4988\u8068\u92ba\ufcaa\ufa46\u8cce\u72ae\uf105p\u083d\ue1bc\u4043\u811d\uf424\ub4fd\u610d\u3c4e\u532a\uc7cd\ud2f6\udb88\u75ed\u2015\ubfc8\u60cc\ue047\ubc9a\u9d27\ub32e\u7915\u25dd\u041b\ubfa4\u9095\ue5c4\u3da2\ue057\u48d9\u4d46\u5fe9\ua05e\uaa7e\u9aa8\uaf33\u415d\u0d70\u9944\ubf61\u9d5d\uce06\u7fc2\uf98b\u5afb\u8167\uceba\u80cd\u76d3\u2e26\u2cf2\u71c1\u412c\uacf8\u7d02\u5e02\u6df1\u9c71\ufc14\u7c8a\u5a8e\udbff\u1cfa";
            return (("\u4de1\u7fad\u0d35\uc030\ued3c\u85fd\ub68a\ueb8f\ue40f\ufbd7\u0616\u547a\u0c3d\ud35d\u9909\ufd8b\ue4de\u183d\u0192\u43d6\ub3ab\ud12c\ubc44\ub4c4\u0d10\u4cea\u447e\ud8e1\ucce1\u0b5b\ua33a\u1529\u96a7\ubd74\uf292\ue326\u6d76\u9868\ud1f2\u088c\u11a9\u46a2\u16c3\uabc1\uc4b0\u6d9e\u6e96\u5905\udc52\ua635\u6594\u3563\u1540\u563d\uc768\u8010\u4b74\u41a0\uc047\ub8d0\u1e7d\ufce2\ub8f6\u3e40\u08bb\uc64d\u4b99\u9c67\u4ccf\u15d0\ucf98\u2124\u2ba3\uf684\u0f37\u8f70\u490a\u22a9\u695a\ub51a\ubf58\udcd8\u1abb\uf1ed\ua178\u57e3\ufc5d\u8d38\u98a3\u6d96\u2052\u2794\u0f12\u3577\ub91c\u7ba9\u6922\u308a\u3b35\uecf3\u3193\u4cd2\uf867\u391d\ua2ea\uf643\u99b7\u64a0\uacc5\uabbb\u1116\u625c\u03a0\u5619\ue2c4\u42d3\u1a59\uc910\udb76\u87c6\u69b5\u6d98\u93f8\u1421\ub3fb\u1961\ue599\ua275\u4c79\ue8b9\u5592\u6e5d\u677d\u60f9\uab93\uc45f\u1536\u0fed\u1b5b\u4546\u095d\udccf\u2531\u1bdf\u5586\u62d7\u44d4\u13c7\u5170\udd98\ucea7\u99fc\u2743\u503b\u5af1\u2ba0\uf4e7\u19e8\u4679\u0a6a\u950c\u8f80\u5202\udf92\u31b5\ub289\ud9b2\uccbb\uf348\u1c05\u1a4f\ud39a\u640b\u248a\uea25\u907a\u394e\ub6df\u3a84\ua1d6\u125b\u11d5\u3750\u1369\uf47b\u6de1\uf5ef\ueb33\u042e\u6b70\u61a1\u76ee\u26cd\u33dc\u11cf\u478c\ub202\ud668\u923c\u4677\uf2be\u1373\u9c27\ue032\u1c65\u4a03\ufab6\u8b3d\u2a2d\ue64b\uae7b\u736d\u0806\u748a\u3b71\ud310\uae69\ua358\u7ff5\u4b73\uebf4\u5b60\u7f32\ud8ff\ue5b3\u8559\u7c34\ue0eb\u5e98\uece3\u3124\ua234\ubeee\u9604\ua3e1\u6460\u5d97\u711f\uc6e3\u40dd\u5391\u1e81\udae2\uc6f7\ub0c7\u1786\u3cba\u501b\u1236\uc2a7\u43e5\u6d84\u4d59\u340a\u262f\u553a\u0787\u346c\u5e4a\u6be4\u01ec\u2bc0\u04b2\ud004\u18d1\u888f\u0949\u3813\ub100\uf014\u68c9\u57ad\u2529\ub623\u2db4\u41e8\uc3ef\ufd02\u9bdd\u7e6d\u138e\ud6a2\u17d8\uf481\u517b\u706c\u2e29\u3fd9\u35fe\u6d4b\ud2ee\ube4c\u7979\u9492\u1355\u5e62\u81d0\ud61f\u88b8\u67ca\u6878\u4d43\u4a21\ueb8d\u8fe0\u4e19\uba05\u2692\uf1ea\u9466\u52d9\ueb83\ubf48\u4b9f\ub501\u792f\u6d7a\u3357\u1aec\u2d46\u5dd9\ube14\u77b4\ubca8\u7e66\ucb7c\u8ded\u57aai\ua3d2\u027c\u5911\u0aee\u4661\u16ac\u45ce\u139b\u3c9e\ue501\uea42\uac79\ufbdc\u330a\ua163\uac0f\u56ff\u4278\uac98\u38cd\ub690\ub9d8\ufd18\u841b\ud25a\u879f\u4718\ud160\u3582\ucc37\u4ce9\u3ebc\u6fd5\uf7e3\ud7d2\uad7c\ud3d9\ua32b\u9439\ub7ea\u1abe\u0187\ub65a\ub74b\u7c90\uf3a1\uad33\u5b2c\u3524\u7d69\u69bc\u57b3\u59ec\u7eb8\u33b0\ua935\u9c7a\u3e49\ufdf9\uc260\ud521\u431d\ub3e8\u86a6\uae86\u46f8\ub1ab\ucad2\u3abd\ud64c\u8778\u35e3\ue467\u08d3\ua1a7\u019f\u553a\u922c\ua371\u3f4c\u7d11\u2d9f\u879f\u3ee3\u9ec4\ue906\u578d\u2fba\u3c22\u205f\u3dd6\u939a\u75c1\u7e30\u0a20\ua753\uaf8d\u140d\ue407\ude03\u9079\ubda7\uba8e\u3408\u4ed5\u00a6\u5235\uddc1\ubd3d\u82b5\ubc1d\u44d4\u5b08\u1d51\u1ecb\ua017\u34d4\uda16\u8e77\uaf4b\udc9c\u7593\u1d0a\u69ee\u4046\u8ff2\u5870\ucf66\u7843\u344e\u7d45\ue40d\u3154\ucd0e\ufd8b\ua870\u235e\u5bb6\u676d\u2d11\udb21\uf1df\u53a9\ud170\u2e0c\uea3b\uf2a8\uaead\u5647\u4905\u0f41\u9a0f\u1299\u21c3\u3193\u9e4f\u7369\u4649\u04fc\ua6f3\u8e18\u9e72\u06ad\u2017\u9287\uaf40\uc1c9\ufaf6\u5417\uff8e\u596c\u45d6\u843f\u3c0c\u51b2\u2109\u860c\u9a5c\uceda\u503e\uefc2\udb83\ue707\u14c2\ubd29\u698a\ubac9\u82e2\u1a8a\u0af2\udfea\ud351\u1cae\u24ef\udcd8\u7ba7\u522a\u2af6\u1f7d\u1212\u2645\u8e04\ufd15\u0508\u69b5\u5347\ub0cf\ue87a\uce77\u4662\ubf59\u0c3c\ucf2e\u67ca\u23bd\u52ba\u400b\uc35a\uce3e\uf651\uccc0\u7cce\uc13e\uac01\u5823\u38b5\ucedc\uebb0\uf1a5\u4724\u1c1a\u1e8c\u09bb\ud562\u1515\u5a74\u0b69\u08d7\u7045\u7ce1\uccde\u812e\u07e0\uca92\ubb75\u7590\u2143\u375c\uf2bb\u7e06L\uba77\u94d8\ucdb6\ufa83\uaa37\u9100\u898e\ucb4a\ub491\u50fc\uc101\u7964\u1a26\u587e\u869c\u93eb\u934e\u27f8\u38f9\uc542\u82be\u6caa\u1307\ua582\u59d1\u95ac\u883b\ua969\ub42f\u6231\u95ff\u5df8\uc337\ub541\u877c\u46f3\u122c\u7897\u7e03\ua005\u8185\ubaa0\u4340\u7288\u8b81\ude5c\u8b7d\u56a3\u3b5e\ue1f4\ub301\uca65\u3117\u052a\ub815\uac14\u16b1\u4374\uf46d\ueb0c\u9f8e\u77fe\u22b0\u3ab9\u44b1\ud4e2\u3e69\u9a00\uc44e\ucbe1\ub51f\ua1b2\u0755\u64f5\ub4ac\u3721\uda4b\ub54c\ufb40\ub2f3\u550c\u2da5\uae21\uee5c\u0e0f\ubff2\ucc8d\u8eb2\u9490\ud2e0\ufa1c\uf59e\u9cfb\u2e81\uca87\ud89b\u59ed\ub6bc\u7c13\ud2d7\u6296\u86ba\u9702\u6860\uf576\u3475\u5a8e\u45e5\u49fe\u8e21\uf032\ucb2c\ua405\u4476\u8201\ud844\u2327\u2ad7\uee99\u1679\u19ee\ua6d4\ub70a\uee26\ub16c\uedb6\u4920\u8157\u897c\u8de3\u9bb7\u7997\ua1c2\u727a\ud279\u2aee\ubc13\u865c\u8b95\u9e38\u8173\u9e68\u489f\u151a\u7ed2\u5d70\u119d\ua663\u3a94\uc9e0\uef3f\ub2f8\u516b\uf054\uf636\uede3\u7844\u722e\ue6f9\ue097\ueb65\u76bb\u97fd\u3d66\u3f07\ue34f\ue7de\u49b0\u92a8\u1fb1\u6d30\u4d86\u3e57\ud1f1\u0c10\ua118\uef0e\ud9aa\u3477\u6d6a\ue37c\u6352\u1584\uccfe\ud854\ud05a\u1506\u53de\u7624\u4988\u8068\u92ba\ufcaa\ufa46\u8cce\u72ae\uf105p\u083d\ue1bc\u4043\u811d\uf424\ub4fd\u610d\u3c4e\u532a\uc7cd\ud2f6\udb88\u75ed\u2015\ubfc8\u60cc\ue047\ubc9a\u9d27\ub32e\u7915\u25dd\u041b\ubfa4\u9095\ue5c4\u3da2\ue057\u48d9\u4d46\u5fe9\ua05e\uaa7e\u9aa8\uaf33\u415d\u0d70\u9944\ubf61\u9d5d\uce06\u7fc2\uf98b\u5afb\u8167\uceba\u80cd\u76d3\u2e26\u2cf2\u71c1\u412c\uacf8\u7d02\u5e02\u6df1\u9c71\ufc14\u7c8a\u5a8e\udbff\u1cfa".charAt(794) << 16 | 0xBC9A) - 441537691 ^ 0xC5F667FF) != 0;
        }
        if (((Direction)PistonCrystal.mc.level.method_8320(blockPos).method_11654((Property)DirectionalBlock.FACING)).getOpposite() != direction) {
            return false;
        }
        return PistonCrystal.mc.level.isEmptyBlock(blockPos.method_10079(direction, -1)) || this.getBlock(blockPos.method_10079(direction, -1)) == Blocks.FIRE || this.getBlock(blockPos.method_10093(direction.getOpposite())) == Blocks.MOVING_PISTON;
    }

    private void doFire(BlockPos blockPos, Direction direction) {
        if (!this.fireAccess().getValue()) {
            return;
        }
        int item = this.findItem(Items.FLINT_AND_STEEL);
        if (item == -1) {
            return;
        }
        int selectedSlot = PistonCrystal.mc.player.getInventory().selected;
        int[] array = new int[]{0, direction.getStepZ(), -direction.getStepZ()};
        int[] array2 = new int[]{0, 1};
        int[] array3 = new int[]{0, direction.getStepX(), -direction.getStepX()};
        for (int n : array) {
            for (int n2 : array2) {
                int[] array6 = array3;
                int length3 = array6.length;
                for (int k = 0; k < length3; ++k) {
                    if (this.getBlock(blockPos.method_10069(n, n2, array6[k])) != Blocks.FIRE) continue;
                    return;
                }
            }
        }
        for (int n3 : array) {
            for (int n5 : array2) {
                for (int n7 : array3) {
                    if (!PistonCrystal.canFire(blockPos.method_10069(n3, n5, n7))) continue;
                    this.doSwap(item);
                    this.placeFire(blockPos.method_10069(n3, n5, n7));
                    if (this.m_0lwz().getValue()) {
                        this.doSwap(item);
                        ClientUtil.Njn83();
                    } else {
                        this.doSwap(selectedSlot);
                    }
                    return;
                }
            }
        }
    }

    public void placeFire(BlockPos blockPos) {
        BlockUtil.clickBlock(blockPos.method_10093(Direction.DOWN), Direction.UP, this.rotateAccess().getValue());
    }

    private void doRedStone(BlockPos blockPos, Direction direction, BlockPos blockPos2) {
        if (!PistonCrystal.mc.level.isEmptyBlock(blockPos.method_10079(direction, -1)) && this.getBlock(blockPos.method_10079(direction, -1)) != Blocks.FIRE && this.getBlock(blockPos.method_10093(direction.getOpposite())) != Blocks.MOVING_PISTON) {
            return;
        }
        Direction[] values = Direction.values();
        int length = values.length;
        int n = 32973;
        int n2 = 16;
        if (((length | 1) & 1) == 0) {
            // empty if block
        }
        for (int i = 0; i < length; ++i) {
            Direction[] array = values;
            if (((i | 1) & 1) == 0) {
                // empty if block
            }
            if (this.getBlock(blockPos.method_10093(array[i])) != Blocks.REDSTONE_BLOCK) continue;
            return;
        }
        int block = this.findBlock(Blocks.REDSTONE_BLOCK);
        if (block == -1) {
            return;
        }
        int selectedSlot = PistonCrystal.mc.player.getInventory().selected;
        Direction bestNeighboring = BlockUtil.getBestNeighboring(blockPos, direction);
        if (bestNeighboring != null && bestNeighboring != direction.getOpposite() && BlockUtil.canPlace(blockPos.method_10093(bestNeighboring), this.SrlvX().getValue()) && !blockPos.method_10093(bestNeighboring).equals((Object)blockPos2)) {
            this.doSwap(block);
            BlockUtil.placeBlock(blockPos.method_10093(bestNeighboring), this.rotateAccess().getValue());
            if (this.m_0lwz().getValue()) {
                this.doSwap(block);
                ClientUtil.Njn83();
            } else {
                this.doSwap(selectedSlot);
            }
            return;
        }
        for (Direction direction2 : Direction.values()) {
            if (!BlockUtil.canPlace(blockPos.method_10093(direction2), this.SrlvX().getValue()) || blockPos.method_10093(direction2).equals((Object)blockPos2) || direction2 == direction.getOpposite()) continue;
            this.doSwap(block);
            BlockUtil.placeBlock(blockPos.method_10093(direction2), this.rotateAccess().getValue());
            if (this.m_0lwz().getValue()) {
                this.doSwap(block);
                ClientUtil.Njn83();
            } else {
                this.doSwap(selectedSlot);
            }
            return;
        }
    }

    private void doSwap(int n) {
        if (this.m_0lwz().getValue()) {
            ClientUtil.FgmR(n, PistonCrystal.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(n);
        }
    }

    public int findItem(Item item) {
        if (this.m_0lwz().getValue()) {
            return InventoryUtil.findItemInventorySlot(item);
        }
        return InventoryUtil.findItem(item);
    }

    public int findBlock(Block block) {
        if (this.m_0lwz().getValue()) {
            return InventoryUtil.findBlockInventorySlot(block);
        }
        return InventoryUtil.findBlock(block);
    }

    public int findClass(Class<?> clazz) {
        if (this.m_0lwz().getValue()) {
            return InventoryUtil.findClassInventorySlot(clazz);
        }
        return InventoryUtil.findClass(clazz);
    }

    private Block getBlock(BlockPos blockPos) {
        return PistonCrystal.mc.level.method_8320(blockPos).method_26204();
    }

    private BooleanSetting rotateAccess() {
        return this.rotate;
    }

    private BooleanSetting getPistonPacket() {
        return this.pistonPacket;
    }

    private BooleanSetting noEatingAccess() {
        return this.noEating;
    }

    private BooleanSetting eatingBreakAccess() {
        return this.eatingBreak;
    }

    private SliderSetting SrlvX() {
        return this.placeRange;
    }

    private SliderSetting jz83() {
        return this.range;
    }

    private BooleanSetting fireAccess() {
        return this.fire;
    }

    private BooleanSetting switchPosAccess() {
        return this.switchPos;
    }

    private BooleanSetting onlyGroundAccess() {
        return this.onlyGround;
    }

    private BooleanSetting CQYxr() {
        return this.onlyStatic;
    }

    private SliderSetting yG5D() {
        return this.updateDelay;
    }

    private SliderSetting t4Gq() {
        return this.posUpdateDelay;
    }

    private SliderSetting K2hz() {
        return this.stageSetting;
    }

    private SliderSetting pistonStageAccess() {
        return this.pistonStage;
    }

    private SliderSetting pistonMaxStageAccess() {
        return this.pistonMaxStage;
    }

    private SliderSetting powerStageAccess() {
        return this.powerStage;
    }

    private SliderSetting getPowerMaxStage() {
        return this.powerMaxStage;
    }

    private SliderSetting crystalStageAccess() {
        return this.crystalStage;
    }

    private SliderSetting crystalMaxStageAccess() {
        return this.crystalMaxStage;
    }

    private SliderSetting fireStageAccess() {
        return this.fireStage;
    }

    private SliderSetting fireMaxStageAccess() {
        return this.fireMaxStage;
    }

    private BooleanSetting m_0lwz() {
        return this.inventory;
    }

    private BooleanSetting debugAccess() {
        return this.debug;
    }

    private Timer m_1appl() {
        return this.timer;
    }

    private Timer crystalTimerAccess() {
        return this.crystalTimer;
    }

    private Player targetAccess() {
        return this.target;
    }

    private void setTarget(Player target) {
        this.target = target;
    }

    private boolean isPistonAccess() {
        return this.isPiston;
    }

    private void m_01Axx(boolean isPiston) {
        if (!((isPiston | true) & true)) {
            // empty if block
        }
        this.isPiston = isPiston;
    }
}

