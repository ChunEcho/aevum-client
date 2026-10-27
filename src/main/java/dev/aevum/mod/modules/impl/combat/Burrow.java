/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.ExperienceOrb
 *  net.minecraft.EndCrystal
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.Arrow
 *  net.minecraft.ThrownExperienceBottle
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 *  net.minecraft.Mth
 *  net.minecraft.BlockHitResult
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.core.impl.RotationManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.ExperienceOrb;
import net.minecraft.EndCrystal;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.Arrow;
import net.minecraft.ThrownExperienceBottle;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;
import net.minecraft.Mth;
import net.minecraft.BlockHitResult;

public class Burrow
extends Module {
    public static Burrow INSTANCE;
    private final EnumSetting<RotateMode> rotate = this.add(new EnumSetting<RotateMode>("RotateMode", RotateMode.Bypass));
    private final EnumSetting<LagBackMode> lagMode = this.add(new EnumSetting<LagBackMode>("LagMode", LagBackMode.TrollHack));
    private final EnumSetting<LagBackMode> aboveLagMode = this.add(new EnumSetting<LagBackMode>("MoveLagMode", LagBackMode.Smart));
    private final List<BlockPos> placePos = new ArrayList<BlockPos>();
    private final Timer timer = new Timer();
    private final Timer webTimer = new Timer();
    private final BooleanSetting disable = this.add(new BooleanSetting("Disable", true));
    private final BooleanSetting jumpDisable = this.add(new BooleanSetting("JumpDisable", true, () -> !this.disable.getValue()));
    private final SliderSetting delay = this.add(new SliderSetting("Delay", 500, 0, 1000, () -> !this.disable.getValue()));
    private final SliderSetting webTime = this.add(new SliderSetting("WebTime", 0, 0, 500));
    private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true));
    private final BooleanSetting antiLag = this.add(new BooleanSetting("AntiLag", false));
    private final BooleanSetting single = this.add(new BooleanSetting("SingleBlock", false));
    private final BooleanSetting detectMine = this.add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting headFill = this.add(new BooleanSetting("HeadFill", false));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", false));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", true));
    private final BooleanSetting noSelfPos = this.add(new BooleanSetting("NoSelfPos", false));
    private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting sound = this.add(new BooleanSetting("Sound", true));
    private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 4.0, 1.0, 4.0, 1.0));
    private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true));
    private final BooleanSetting wait = this.add(new BooleanSetting("Wait", true, this.disable::getValue));
    private final BooleanSetting fakeMove = this.add(new BooleanSetting("FakeMove", true).setParent());
    private final BooleanSetting center = this.add(new BooleanSetting("AllowCenter", true, this.fakeMove::isOpen));
    private final SliderSetting preCorrect = this.add(new SliderSetting("PreCorrect", 0.25, 0.0, 1.0, 0.001, this.fakeMove::isOpen));
    private final SliderSetting moveDis = this.add(new SliderSetting("MoveDis", 0.25, 0.0, 1.0, 0.001, this.fakeMove::isOpen));
    private final SliderSetting moveDis2 = this.add(new SliderSetting("MoveDis2", 0.25, 0.0, 1.0, 0.001, this.fakeMove::isOpen));
    private final SliderSetting moveCorrect2 = this.add(new SliderSetting("Correct", 0.25, 0.0, 1.0, 0.001, this.fakeMove::isOpen));
    private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 0.01, 0.0, 1.0, 0.001, this.fakeMove::isOpen));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
    private final SliderSetting smartX = this.add(new SliderSetting("SmartXZ", 3.0, 0.0, 10.0, 0.1, () -> this.lagMode.getValue() == LagBackMode.Smart || this.aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartUp = this.add(new SliderSetting("SmartUp", 3.0, 0.0, 10.0, 0.1, () -> this.lagMode.getValue() == LagBackMode.Smart || this.aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDown = this.add(new SliderSetting("SmartDown", 3.0, 0.0, 10.0, 0.1, () -> this.lagMode.getValue() == LagBackMode.Smart || this.aboveLagMode.getValue() == LagBackMode.Smart));
    private final SliderSetting smartDistance = this.add(new SliderSetting("SmartDistance", 2.0, 0.0, 10.0, 0.1, () -> this.lagMode.getValue() == LagBackMode.Smart || this.aboveLagMode.getValue() == LagBackMode.Smart));
    private int progress = 0;
    private Vec3 currentPos;

    public Burrow() {
        super("Burrow", Module.Category.Combat);
        this.setChinese("\u5361\u9ed1\u66dc\u77f3");
        INSTANCE = this;
    }

    @EventListener
    public void onUpdate(ClientTickEvent event) {
        if (this.inventory.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (event.isPost()) {
            if (!this.disable.getValue() && this.jumpDisable.getValue() && Burrow.mc.player.input.jumping) {
                this.disable();
                return;
            }
            if (Aevum.PLAYER.isInWeb((Player)Burrow.mc.player)) {
                this.webTimer.reset();
                return;
            }
            if (this.usingPause.getValue() && Burrow.mc.player.isUsingItem()) {
                return;
            }
            if (!this.webTimer.passedMs(this.webTime.getValue())) {
                return;
            }
            if (!this.disable.getValue() && !this.timer.passedMs(this.delay.getValue())) {
                return;
            }
            if (!Burrow.mc.player.onGround()) {
                return;
            }
            if (this.antiLag.getValue() && !BlockUtil.canCollide((Entity)Burrow.mc.player, new AABB(EntityUtil.getPlayerPos(true).method_10074()))) {
                return;
            }
            if (this.single.getValue() && EntityUtil.isInsideBlock()) {
                if (this.disable.getValue()) {
                    this.disable();
                }
                return;
            }
            if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
                return;
            }
            int oldSlot = Burrow.mc.player.getInventory().selected;
            int block = this.getBlock();
            if (block == -1) {
                CommandManager.sendMessageId("\u00a74No block found.", this.hashCode() - 1);
                this.disable();
                return;
            }
            this.progress = 0;
            this.placePos.clear();
            double offset = this.single.getValue() ? 0.0 : AntiCheat.getOffset();
            BlockPosX pos1 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() + 0.5, Burrow.mc.player.getZ() + offset);
            BlockPosX pos2 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() + 0.5, Burrow.mc.player.getZ() + offset);
            BlockPosX pos3 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() + 0.5, Burrow.mc.player.getZ() - offset);
            BlockPosX pos4 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() + 0.5, Burrow.mc.player.getZ() - offset);
            BlockPosX pos5 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() + 1.5, Burrow.mc.player.getZ() + offset);
            BlockPosX pos6 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() + 1.5, Burrow.mc.player.getZ() + offset);
            BlockPosX pos7 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() + 1.5, Burrow.mc.player.getZ() - offset);
            BlockPosX pos8 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() + 1.5, Burrow.mc.player.getZ() - offset);
            BlockPosX pos9 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() - 1.0, Burrow.mc.player.getZ() + offset);
            BlockPosX pos10 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() - 1.0, Burrow.mc.player.getZ() + offset);
            BlockPosX pos11 = new BlockPosX(Burrow.mc.player.getX() + offset, Burrow.mc.player.getY() - 1.0, Burrow.mc.player.getZ() - offset);
            BlockPosX pos12 = new BlockPosX(Burrow.mc.player.getX() - offset, Burrow.mc.player.getY() - 1.0, Burrow.mc.player.getZ() - offset);
            BlockPos playerPos = EntityUtil.getPlayerPos(true);
            boolean headFill = false;
            if (!(this.canPlace(pos1) || this.canPlace(pos2) || this.canPlace(pos3) || this.canPlace(pos4))) {
                boolean cantHeadFill = !this.headFill.getValue() || !this.canPlace(pos5) && !this.canPlace(pos6) && !this.canPlace(pos7) && !this.canPlace(pos8);
                boolean cantDown = !this.down.getValue() || !this.canPlace(pos9) && !this.canPlace(pos10) && !this.canPlace(pos11) && !this.canPlace(pos12);
                boolean bl = cantDown;
                if (cantHeadFill) {
                    if (cantDown) {
                        if (!this.wait.getValue() && this.disable.getValue()) {
                            this.disable();
                        }
                        return;
                    }
                } else {
                    headFill = true;
                }
            }
            boolean above = false;
            BlockPos headPos = EntityUtil.getPlayerPos(true).method_10086(2);
            boolean rotate = this.rotate.getValue() == RotateMode.Normal;
            CombatUtil.attackCrystal(pos1, rotate, false);
            CombatUtil.attackCrystal(pos2, rotate, false);
            CombatUtil.attackCrystal(pos3, rotate, false);
            CombatUtil.attackCrystal(pos4, rotate, false);
            if (headFill || Burrow.mc.player.isVisuallyCrawling() || this.trapped(headPos) || this.trapped(headPos.method_10069(1, 0, 0)) || this.trapped(headPos.method_10069(-1, 0, 0)) || this.trapped(headPos.method_10069(0, 0, 1)) || this.trapped(headPos.method_10069(0, 0, -1)) || this.trapped(headPos.method_10069(1, 0, -1)) || this.trapped(headPos.method_10069(-1, 0, -1)) || this.trapped(headPos.method_10069(1, 0, 1)) || this.trapped(headPos.method_10069(-1, 0, 1))) {
                above = true;
                if (!this.fakeMove.getValue()) {
                    if (!this.wait.getValue() && this.disable.getValue()) {
                        this.disable();
                    }
                    return;
                }
                if (!this.fakeMove()) {
                    return;
                }
            } else {
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 0.4199999868869781, Burrow.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 0.7531999805212017, Burrow.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 0.9999957640154541, Burrow.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.1661092609382138, Burrow.mc.player.getZ(), false));
                this.currentPos = new Vec3(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.1661092609382138, Burrow.mc.player.getZ());
            }
            this.timer.reset();
            this.doSwap(block);
            if (this.rotate.getValue() == RotateMode.Bypass) {
                if (above) {
                    float[] angle = RotationManager.getRotation(this.currentPos.method_1031(0.0, (double)Burrow.mc.player.method_18381(Burrow.mc.player.getPose()), 0.0), Burrow.mc.player.position());
                    Aevum.ROTATION.snapAt(angle[0], angle[1]);
                } else {
                    Aevum.ROTATION.snapAt(Aevum.ROTATION.rotationYaw, 90.0f);
                }
            }
            this.placeBlock(playerPos, rotate);
            this.placeBlock(pos1, rotate);
            this.placeBlock(pos2, rotate);
            this.placeBlock(pos3, rotate);
            this.placeBlock(pos4, rotate);
            if (this.down.getValue()) {
                this.placeBlock(pos9, rotate);
                this.placeBlock(pos10, rotate);
                this.placeBlock(pos11, rotate);
                this.placeBlock(pos12, rotate);
            }
            if (this.headFill.getValue() && above) {
                this.placeBlock(pos5, rotate);
                this.placeBlock(pos6, rotate);
                this.placeBlock(pos7, rotate);
                this.placeBlock(pos8, rotate);
            }
            if (this.inventory.getValue()) {
                this.doSwap(block);
                EntityUtil.syncInventory();
            } else {
                this.doSwap(oldSlot);
            }
            switch ((above ? this.aboveLagMode.getValue() : this.lagMode.getValue()).ordinal()) {
                case 0: {
                    ArrayList<BlockPosX> list = new ArrayList<BlockPosX>();
                    for (double x = Burrow.mc.player.position().method_10216() - this.smartX.getValue(); x < Burrow.mc.player.position().method_10216() + this.smartX.getValue(); x += 1.0) {
                        for (double z = Burrow.mc.player.position().method_10215() - this.smartX.getValue(); z < Burrow.mc.player.position().method_10215() + this.smartX.getValue(); z += 1.0) {
                            for (double d = Burrow.mc.player.position().method_10214() - this.smartDown.getValue(); d < Burrow.mc.player.position().method_10214() + this.smartUp.getValue(); d += 1.0) {
                                list.add(new BlockPosX(x, d, z));
                            }
                        }
                    }
                    double getDistance = 0.0;
                    BlockPos bestPos = null;
                    for (BlockPos class_23382 : list) {
                        if (!this.canMove(class_23382) || (double)Mth.method_15355((float)((float)Burrow.mc.player.method_5707(class_23382.method_46558().method_1031(0.0, -0.5, 0.0)))) < this.smartDistance.getValue() || bestPos != null && !(Burrow.mc.player.method_5707(class_23382.method_46558()) < getDistance)) continue;
                        bestPos = class_23382;
                        getDistance = Burrow.mc.player.method_5707(class_23382.method_46558());
                    }
                    if (bestPos == null) break;
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos((double)bestPos.method_10263() + 0.5, (double)bestPos.method_10264(), (double)bestPos.getZ() + 0.5, false));
                    break;
                }
                case 1: {
                    for (int i = 0; i < 20; ++i) {
                        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1337.0, Burrow.mc.player.getZ(), false));
                    }
                    break;
                }
                case 7: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.16610926093821, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.170005801788139, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.2426308013947485, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 2.3400880035762786, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 2.640088003576279, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 8: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.1001, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.0605, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.0802, Burrow.mc.player.getZ(), false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.1127, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 2: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 2.3400880035762786, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 5: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), Burrow.mc.player.getY() + 1.9, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 3: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), -70.0, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 4: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Burrow.mc.player.getX(), -7.0, Burrow.mc.player.getZ(), false));
                    break;
                }
                case 6: {
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot(-180.0f, -90.0f, false));
                    mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot(180.0f, 90.0f, false));
                }
            }
            if (this.disable.getValue()) {
                this.disable();
            }
        }
    }

    private boolean fakeMove() {
        double[] offsets = new double[]{1.0, 0.0, -1.0};
        ArrayList<BlockPosX> offList = new ArrayList<BlockPosX>();
        BlockPos playerPos = Burrow.mc.player.method_24515();
        for (double d : offsets) {
            for (double z : offsets) {
                offList.add(new BlockPosX(Burrow.mc.player.getX() + d, Burrow.mc.player.getY(), Burrow.mc.player.getZ() + z));
            }
        }
        for (BlockPos class_23382 : offList) {
            if (!this.checkSelf(class_23382) || BlockUtil.canReplace(class_23382) || this.headFill.getValue() && BlockUtil.canReplace(class_23382.method_10084())) continue;
            this.gotoPos(class_23382);
            return true;
        }
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (BlockPos class_23383 : offList) {
            if (playerPos.equals((Object)class_23383) || !this.checkSelf(class_23383) || !this.canMove(class_23383)) continue;
            arrayList.add(class_23383);
        }
        if (!arrayList.isEmpty()) {
            double dis = 10.0;
            BlockPos target = null;
            Object object = arrayList.iterator();
            while (object.hasNext()) {
                BlockPos p = (BlockPos)object.next();
                double getDistance = Burrow.mc.player.position().method_1022(p.method_46558().method_1031(0.0, -0.5, 0.0));
                if (!(getDistance < dis) && target != null) continue;
                target = p;
                dis = getDistance;
            }
            this.gotoPos(target);
            return true;
        }
        for (BlockPos class_23384 : offList) {
            if (playerPos.equals((Object)class_23384) || !this.checkSelf(class_23384)) continue;
            arrayList.add(class_23384);
        }
        if (!arrayList.isEmpty()) {
            double dis = 10.0;
            BlockPos target = null;
            for (BlockPos p : arrayList) {
                double getDistance = Burrow.mc.player.position().method_1022(p.method_46558().method_1031(0.0, -0.5, 0.0));
                if (!(getDistance < dis) && target != null) continue;
                target = p;
                dis = getDistance;
            }
            this.gotoPos(target);
            return true;
        }
        if (!this.center.getValue()) {
            return false;
        }
        offList.clear();
        offList.add(new BlockPosX(Burrow.mc.player.getX() + 1.0, Burrow.mc.player.getY(), Burrow.mc.player.getZ()));
        offList.add(new BlockPosX(Burrow.mc.player.getX() - 1.0, Burrow.mc.player.getY(), Burrow.mc.player.getZ()));
        offList.add(new BlockPosX(Burrow.mc.player.getX(), Burrow.mc.player.getY(), Burrow.mc.player.getZ() - 1.0));
        offList.add(new BlockPosX(Burrow.mc.player.getX(), Burrow.mc.player.getY(), Burrow.mc.player.getZ() + 1.0));
        for (BlockPos class_23385 : offList) {
            if (!this.canMove(class_23385)) continue;
            this.gotoPos(class_23385);
            return true;
        }
        if (!this.wait.getValue() && this.disable.getValue()) {
            this.disable();
        }
        return false;
    }

    private void placeBlock(BlockPos pos, boolean rotate) {
        if (this.canPlace(pos) && !this.placePos.contains(pos) && this.progress < this.blocksPer.getValueInt()) {
            Direction side;
            this.placePos.add(pos);
            if (BlockUtil.allowAirPlace()) {
                ++this.progress;
                BlockUtil.placedPos.add(pos);
                if (this.sound.getValue()) {
                    Burrow.mc.level.method_8396((Player)Burrow.mc.player, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
                }
                this.clickBlock(pos, Direction.DOWN, rotate, InteractionHand.MAIN_HAND, this.packetPlace.getValue());
            }
            if ((side = BlockUtil.getPlaceSide(pos)) == null) {
                return;
            }
            ++this.progress;
            BlockUtil.placedPos.add(pos);
            if (this.sound.getValue()) {
                Burrow.mc.level.method_8396((Player)Burrow.mc.player, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
            }
            this.clickBlock(pos.method_10093(side), side.getOpposite(), rotate, InteractionHand.MAIN_HAND, this.packetPlace.getValue());
        }
    }

    public void clickBlock(BlockPos pos, Direction side, boolean rotate, InteractionHand hand, boolean packet) {
        Vec3 directionVec = new Vec3((double)pos.method_10263() + 0.5 + (double)side.getNormal().method_10263() * 0.5, (double)pos.method_10264() + 0.5 + (double)side.getNormal().method_10264() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getNormal().getZ() * 0.5);
        if (rotate) {
            float[] angle = RotationManager.getRotation(this.currentPos.method_1031(0.0, (double)Burrow.mc.player.method_18381(Burrow.mc.player.getPose()), 0.0), directionVec);
            Aevum.ROTATION.snapAt(angle[0], angle[1]);
        }
        EntityUtil.swingHand(hand, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        if (packet) {
            Module.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(hand, result, id));
        } else {
            Burrow.mc.gameMode.method_2896(Burrow.mc.player, hand, result);
        }
        if (rotate) {
            Aevum.ROTATION.snapBack();
        }
    }

    private void doSwap(int slot) {
        if (this.inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, Burrow.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private void gotoPos(BlockPos offPos) {
        double d;
        double d2;
        double targetX = (double)offPos.method_10263() + 0.5;
        double targetZ = (double)offPos.getZ() + 0.5;
        double x = Burrow.mc.player.getX();
        double z = Burrow.mc.player.getZ();
        double y = Burrow.mc.player.getY() + this.yOffset.getValue();
        double xDiff = Math.abs(x - targetX);
        double zDiff = Math.abs(z - targetZ);
        double moveDis = this.preCorrect.getValue();
        if (moveDis > 0.0) {
            if (xDiff >= moveDis) {
                x = x > targetX ? (x = x - moveDis) : (x = x + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
            }
            if (zDiff >= moveDis) {
                z = z > targetZ ? (z = z - moveDis) : (z = z + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
            }
        }
        xDiff = Math.abs(x - targetX);
        zDiff = Math.abs(z - targetZ);
        moveDis = this.moveDis.getValue();
        if (moveDis > 0.0) {
            while (xDiff > moveDis) {
                x = x > targetX ? (x = x - moveDis) : (x = x + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
                xDiff = Math.abs(x - targetX);
            }
            while (zDiff > moveDis) {
                z = z > targetZ ? (z = z - moveDis) : (z = z + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
                zDiff = Math.abs(z - targetZ);
            }
        }
        moveDis = this.moveDis2.getValue();
        if (d2 > 0.0) {
            while (xDiff > moveDis) {
                x = x > targetX ? (x = x - moveDis) : (x = x + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
                xDiff = Math.abs(x - targetX);
            }
            while (zDiff > moveDis) {
                z = z > targetZ ? (z = z - moveDis) : (z = z + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
                zDiff = Math.abs(z - targetZ);
            }
        }
        moveDis = this.moveCorrect2.getValue();
        if (d > 0.0) {
            if (xDiff >= moveDis) {
                x = x > targetX ? (x = x - moveDis) : (x = x + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
            }
            if (zDiff >= moveDis) {
                z = z > targetZ ? (z = z - moveDis) : (z = z + moveDis);
                Burrow.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y, z, false));
            }
        }
        this.currentPos = new Vec3(x, y, z);
    }

    private boolean canMove(BlockPos pos) {
        return Burrow.mc.level.isEmptyBlock(pos) && Burrow.mc.level.isEmptyBlock(pos.method_10084());
    }

    private boolean canPlace(BlockPos pos) {
        if (this.noSelfPos.getValue() && pos.equals((Object)EntityUtil.getPlayerPos(true))) {
            return false;
        }
        if (!BlockUtil.allowAirPlace() && BlockUtil.getPlaceSide(pos) == null) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (this.detectMine.getValue() && Aevum.BREAK.isMining(pos)) {
            return false;
        }
        return !this.hasEntity(pos);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new AABB(pos))) {
            if (entity == Burrow.mc.player || !entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof ThrownExperienceBottle || entity instanceof Arrow || entity instanceof EndCrystal && this.breakCrystal.getValue()) continue;
            return true;
        }
        return false;
    }

    private boolean checkSelf(BlockPos pos) {
        return Burrow.mc.player.method_5829().method_994(new AABB(pos));
    }

    private boolean trapped(BlockPos pos) {
        return (BlockUtil.canCollide((Entity)Burrow.mc.player, new AABB(pos)) || BlockUtil.getBlock(pos) == Blocks.COBWEB) && this.checkSelf(pos.method_10087(2));
    }

    private int getBlock() {
        if (this.inventory.getValue()) {
            if (InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) != -1 || !this.enderChest.getValue()) {
                return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST);
        }
        if (InventoryUtil.findBlock(Blocks.OBSIDIAN) != -1 || !this.enderChest.getValue()) {
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
        return InventoryUtil.findBlock(Blocks.ENDER_CHEST);
    }

    private static enum RotateMode {
        Bypass,
        Normal,
        None;

    }

    private static enum LagBackMode {
        Smart,
        Invalid,
        TrollHack,
        ToVoid,
        ToVoid2,
        Normal,
        Rotation,
        Fly,
        Glide;

    }
}

