/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Player
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.ServerboundUseItemOnPacket
 *  net.minecraft.BlockHitResult
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.PredictUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.combat.AutoAnchor;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.movement.Velocity;
import dev.aevum.mod.modules.settings.enums.Timing;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import net.minecraft.InteractionHand;
import net.minecraft.Player;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.ServerboundUseItemOnPacket;
import net.minecraft.BlockHitResult;

public class AutoWeb
extends Module {
    public static AutoWeb INSTANCE;
    public static boolean force;
    public static boolean ignore;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, () -> this.page.getValue() == Page.General));
    public final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 2, 1, 10, () -> this.page.getValue() == Page.General));
    public final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 2.0, 0.0, 50.0, 1.0, () -> this.page.getValue() == Page.General));
    public final SliderSetting maxWebs = this.add(new SliderSetting("MaxWebs", 2.0, 1.0, 8.0, 1.0, () -> this.page.getValue() == Page.General));
    public final SliderSetting offset = this.add(new SliderSetting("Offset", 0.25, 0.0, 0.3, 0.01, () -> this.page.getValue() == Page.General));
    public final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0, 0.1, () -> this.page.getValue() == Page.General));
    public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0, 0.0, 8.0, 0.1, () -> this.page.getValue() == Page.General));
    final ArrayList<BlockPos> pos = new ArrayList();
    private final BooleanSetting preferAnchor = this.add(new BooleanSetting("PreferAnchor", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.getValue() == Page.General));
    private final BooleanSetting feet = this.add(new BooleanSetting("Feet", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting feetExtend = this.add(new BooleanSetting("FeetExtend", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting face = this.add(new BooleanSetting("Face", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting down = this.add(new BooleanSetting("Down", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.3, 0.1, 1.0, 0.01, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.checkFov.getValue() && this.page.getValue() == Page.Rotate));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final Timer timer = new Timer();
    public Vec3 directionVec = null;
    int progress = 0;

    public AutoWeb() {
        super("AutoWeb", Module.Category.Combat);
        this.setChinese("\u8718\u86db\u7f51\u5149\u73af");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        if (this.pos.isEmpty()) {
            return null;
        }
        return "Working";
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (AutoWeb.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.rotate.getValue() && this.shouldYawStep() && this.directionVec != null) {
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (AutoWeb.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (force) {
            ignore = true;
        }
        this.update();
        ignore = false;
    }

    @Override
    public void onDisable() {
        force = false;
    }

    private void update() {
        if (!this.timer.passed(this.placeDelay.getValueInt())) {
            return;
        }
        if (this.inventorySwap.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        this.pos.clear();
        this.progress = 0;
        this.directionVec = null;
        if (this.preferAnchor.getValue() && AutoAnchor.INSTANCE.currentPos != null) {
            return;
        }
        if (this.getWebSlot() == -1) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.usingPause.getValue() && AutoWeb.mc.player.isUsingItem()) {
            return;
        }
        block0: for (Player player : CombatUtil.getEnemies(this.targetRange.getValue())) {
            Vec3 playerPos = this.predictTicks.getValue() > 0.0 ? PredictUtil.getPos(player, this.predictTicks.getValueInt()) : player.position();
            int webs = 0;
            if (this.feet.getValue() && this.placeWeb(new BlockPosX(playerPos.method_10216(), playerPos.method_10214(), playerPos.method_10215()))) {
                ++webs;
            }
            if (this.down.getValue()) {
                this.placeWeb(new BlockPosX(playerPos.method_10216(), playerPos.method_10214() - 0.8, playerPos.method_10215()));
            }
            ArrayList<BlockPosX> list = new ArrayList<BlockPosX>();
            for (float x : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                for (float z : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                    for (float y : new float[]{0.0f, 1.0f, -1.0f}) {
                        BlockPosX pos = new BlockPosX(playerPos.method_10216() + (double)x, playerPos.method_10214() + (double)y, playerPos.method_10215() + (double)z);
                        if (list.contains((Object)pos)) continue;
                        list.add(pos);
                        if (!this.isTargetHere(pos, player) || AutoWeb.mc.level.method_8320((BlockPos)pos).method_26204() != Blocks.COBWEB || Aevum.BREAK.isMining(pos)) continue;
                        ++webs;
                    }
                }
            }
            if ((float)webs >= this.maxWebs.getValueFloat() && !ignore) continue;
            boolean skip = false;
            if (this.feetExtend.getValue()) {
                block4: for (float x : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                    for (float z : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                        BlockPosX pos = new BlockPosX(playerPos.method_10216() + (double)x, playerPos.method_10214(), playerPos.method_10215() + (double)z);
                        if (!this.isTargetHere(pos, player) || !this.placeWeb(pos) || !((float)(++webs) >= this.maxWebs.getValueFloat())) continue;
                        skip = true;
                        break block4;
                    }
                }
            }
            if (skip || !this.face.getValue()) continue;
            for (float x : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                for (float z : new float[]{0.0f, this.offset.getValueFloat(), -this.offset.getValueFloat()}) {
                    BlockPosX pos = new BlockPosX(playerPos.method_10216() + (double)x, playerPos.method_10214() + 1.1, playerPos.method_10215() + (double)z);
                    if (this.isTargetHere(pos, player) && this.placeWeb(pos) && (float)(++webs) >= this.maxWebs.getValueFloat()) continue block0;
                }
            }
        }
    }

    private boolean isTargetHere(BlockPos pos, Player target) {
        return new AABB(pos).method_994(target.method_5829());
    }

    private boolean placeWeb(BlockPos pos) {
        if (this.pos.contains(pos)) {
            return false;
        }
        this.pos.add(pos);
        if (this.progress >= this.blocksPer.getValueInt()) {
            return false;
        }
        if (this.getWebSlot() == -1) {
            return false;
        }
        if (this.detectMining.getValue() && Aevum.BREAK.isMining(pos)) {
            return false;
        }
        if (BlockUtil.getPlaceSide(pos, this.placeRange.getValue()) != null && (AutoWeb.mc.level.isEmptyBlock(pos) || ignore && BlockUtil.getBlock(pos) == Blocks.COBWEB) && pos.method_10264() < 320) {
            int oldSlot = AutoWeb.mc.player.getInventory().selected;
            int webSlot = this.getWebSlot();
            if (!this.placeBlock(pos, this.rotate.getValue(), webSlot)) {
                return false;
            }
            BlockUtil.placedPos.add(pos);
            ++this.progress;
            if (this.inventorySwap.getValue()) {
                this.doSwap(webSlot);
                EntityUtil.syncInventory();
            } else {
                this.doSwap(oldSlot);
            }
            force = false;
            this.timer.reset();
            return true;
        }
        return false;
    }

    public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {
        Direction side = BlockUtil.getPlaceSide(pos);
        if (side == null) {
            if (BlockUtil.allowAirPlace()) {
                return this.clickBlock(pos, Direction.DOWN, rotate, slot);
            }
            return false;
        }
        return this.clickBlock(pos.method_10093(side), side.getOpposite(), rotate, slot);
    }

    public boolean clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
        Vec3 directionVec = new Vec3((double)pos.method_10263() + 0.5 + (double)side.getNormal().method_10263() * 0.5, (double)pos.method_10264() + 0.5 + (double)side.getNormal().method_10264() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getNormal().getZ() * 0.5);
        if (rotate && !this.faceVector(directionVec)) {
            return false;
        }
        this.doSwap(slot);
        EntityUtil.swingHand(InteractionHand.MAIN_HAND, AntiCheat.INSTANCE.interactSwing.getValue());
        BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
        Module.sendSequencedPacket(id -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, id));
        if (rotate && !this.shouldYawStep()) {
            Aevum.ROTATION.snapBack();
        }
        return true;
    }

    private boolean faceVector(Vec3 directionVec) {
        if (!this.shouldYawStep()) {
            Aevum.ROTATION.lookAt(directionVec);
            return true;
        }
        this.directionVec = directionVec;
        if (Aevum.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
            return true;
        }
        return !this.checkFov.getValue();
    }

    private void doSwap(int slot) {
        if (this.inventorySwap.getValue()) {
            InventoryUtil.inventorySwap(slot, AutoWeb.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getWebSlot() {
        if (this.inventorySwap.getValue()) {
            return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
        }
        return InventoryUtil.findBlock(Blocks.COBWEB);
    }

    static {
        force = false;
        ignore = false;
    }

    public static enum Page {
        General,
        Rotate;

    }
}

