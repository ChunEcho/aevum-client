/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.ArmorStand
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.AirItem
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.EnchantmentHelper
 *  net.minecraft.Enchantments
 *  net.minecraft.BlockGetter
 *  net.minecraft.Level
 *  net.minecraft.AirBlock
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.AABB
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundPlayerActionPacket
 *  net.minecraft.ServerboundPlayerActionPacket$Action
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.RespawnAnchorBlock
 *  net.minecraft.Attributes
 *  net.minecraft.Holder
 */
package dev.aevum.mod.modules.impl.player;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClickBlockEvent;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.math.FadeUtils;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.IPlayerMoveC2SPacket;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoAnchor;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.combat.CevBreaker;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.movement.Velocity;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.enums.Timing;
import dev.aevum.mod.modules.settings.impl.BindSetting;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.InteractionHand;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.ArmorStand;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.AirItem;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantments;
import net.minecraft.BlockGetter;
import net.minecraft.Level;
import net.minecraft.AirBlock;
import net.minecraft.Blocks;
import net.minecraft.Block;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.AABB;
import net.minecraft.Vec3i;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.BlockState;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundPlayerActionPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.RespawnAnchorBlock;
import net.minecraft.Attributes;
import net.minecraft.Holder;

public class PacketMine
extends Module {
    public static PacketMine INSTANCE;
    public static BlockPos secondPos;
    public static double progress;
    private final FadeUtils animationTime = new FadeUtils(1000L);
    private final FadeUtils secondAnim = new FadeUtils(1000L);
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    private final SliderSetting stopDelay = this.add(new SliderSetting("StopDelay", 50.0, 0.0, 500.0, 1.0, () -> this.page.is(Page.General)));
    private final SliderSetting startDelay = this.add(new SliderSetting("StartDelay", 200.0, 0.0, 500.0, 1.0, () -> this.page.is(Page.General)));
    private final SliderSetting damage = this.add(new SliderSetting("Damage", 0.7f, 0.0, 2.0, 0.01, () -> this.page.is(Page.General)));
    private final SliderSetting maxBreak = this.add(new SliderSetting("MaxBreak", 3.0, 0.0, 20.0, 1.0, () -> this.page.is(Page.General)));
    public final BooleanSetting noGhostHand = this.add(new BooleanSetting("1.21", false, () -> this.page.is(Page.General)));
    public final BooleanSetting noCollide = this.add(new BooleanSetting("NoCollide", true, () -> this.page.is(Page.General)));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.getValue() == Page.General));
    private final BooleanSetting grimDisabler = this.add(new BooleanSetting("GrimDisabler", false, () -> this.page.is(Page.General)));
    private final BooleanSetting instant = this.add(new BooleanSetting("Instant", false, () -> this.page.is(Page.General)));
    private final BooleanSetting wait = this.add(new BooleanSetting("Wait", true, () -> !this.instant.getValue() && this.page.is(Page.General)));
    private final BooleanSetting mineAir = this.add(new BooleanSetting("MineAir", true, () -> this.wait.getValue() && !this.instant.getValue() && this.page.is(Page.General)));
    private final BooleanSetting hotBar = this.add(new BooleanSetting("HotbarSwap", false, () -> this.page.is(Page.General)));
    private final BooleanSetting doubleBreak = this.add(new BooleanSetting("DoubleBreak", true, () -> this.page.is(Page.General))).setParent();
    public final BooleanSetting autoSwitch = this.add(new BooleanSetting("AutoSwitch", true, () -> this.page.is(Page.General) && this.doubleBreak.isOpen()));
    private final SliderSetting start = this.add(new SliderSetting("Start", 0.9f, 0.0, 2.0, 0.01, () -> this.page.is(Page.General) && this.doubleBreak.isOpen()));
    private final SliderSetting timeOut = this.add(new SliderSetting("TimeOut", 1.2f, 0.0, 2.0, 0.01, () -> this.page.is(Page.General) && this.doubleBreak.isOpen()));
    private final BooleanSetting setAir = this.add(new BooleanSetting("SetAir", false, () -> this.page.is(Page.General)));
    private final BooleanSetting swing = this.add(new BooleanSetting("Swing", true, () -> this.page.is(Page.General)));
    private final BooleanSetting endSwing = this.add(new BooleanSetting("EndSwing", false, () -> this.page.is(Page.General)));
    public final SliderSetting range = this.add(new SliderSetting("Range", 6.0, 3.0, 10.0, 0.1, () -> this.page.is(Page.General)));
    private final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting<SwingSide>("SwingSide", SwingSide.All, () -> this.page.is(Page.General)));
    private final BooleanSetting unbreakableCancel = this.add(new BooleanSetting("UnbreakableCancel", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting switchReset = this.add(new BooleanSetting("SwitchReset", false, () -> this.page.is(Page.Check)));
    private final BooleanSetting preferWeb = this.add(new BooleanSetting("PreferWeb", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting preferHead = this.add(new BooleanSetting("PreferHead", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting farCancel = this.add(new BooleanSetting("FarCancel", false, () -> this.page.is(Page.Check)));
    private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting checkWeb = this.add(new BooleanSetting("CheckWeb", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting checkGround = this.add(new BooleanSetting("CheckGround", true, () -> this.page.is(Page.Check)));
    private final BooleanSetting smart = this.add(new BooleanSetting("Smart", true, () -> this.page.is(Page.Check) && this.checkGround.getValue()));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", false, () -> this.page.is(Page.Check)).setParent());
    private final BooleanSetting allowOffhand = this.add(new BooleanSetting("AllowOffhand", true, () -> this.page.is(Page.Check) && this.usingPause.isOpen()));
    private final BooleanSetting bypassGround = this.add(new BooleanSetting("BypassGround", true, () -> this.page.is(Page.Check)));
    private final SliderSetting bypassTime = this.add(new SliderSetting("BypassTime", 400, 0, 2000, () -> this.bypassGround.getValue() && this.page.is(Page.Check)));
    private final BindSetting pause = this.add(new BindSetting("Pause", -1, () -> this.page.is(Page.Check)));
    private final BooleanSetting rotate = this.add(new BooleanSetting("StartRotate", true, () -> this.page.is(Page.Rotation)));
    private final BooleanSetting endRotate = this.add(new BooleanSetting("EndRotate", false, () -> this.page.is(Page.Rotation)));
    private final SliderSetting syncTime = this.add(new SliderSetting("Sync", 300, 0, 1000, () -> this.page.is(Page.Rotation)));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.page.is(Page.Rotation)).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.page.is(Page.Rotation) && this.yawStep.isOpen()));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.page.is(Page.Rotation) && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.page.is(Page.Rotation) && this.yawStep.isOpen()));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.page.is(Page.Rotation) && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.page.is(Page.Rotation) && this.yawStep.isOpen()));
    public final BooleanSetting crystal = this.add(new BooleanSetting("Crystal", false, () -> this.page.is(Page.Place)).setParent());
    private final BooleanSetting onlyHeadBomber = this.add(new BooleanSetting("OnlyCev", true, () -> this.page.is(Page.Place) && this.crystal.isOpen()));
    private final BooleanSetting waitPlace = this.add(new BooleanSetting("WaitPlace", true, () -> this.page.is(Page.Place) && this.crystal.isOpen()));
    private final BooleanSetting spamPlace = this.add(new BooleanSetting("SpamPlace", false, () -> this.page.is(Page.Place) && this.crystal.isOpen()));
    private final BooleanSetting afterBreak = this.add(new BooleanSetting("AfterBreak", true, () -> this.page.is(Page.Place) && this.crystal.isOpen()));
    private final BooleanSetting checkDamage = this.add(new BooleanSetting("DetectProgress", true, () -> this.page.is(Page.Place) && this.crystal.isOpen()));
    private final SliderSetting crystalDamage = this.add(new SliderSetting("Progress", 0.9f, 0.0, 1.0, 0.01, () -> this.page.is(Page.Place) && this.crystal.isOpen() && this.checkDamage.getValue()));
    public final BindSetting obsidian = this.add(new BindSetting("Obsidian", -1, () -> this.page.is(Page.Place)));
    private final BindSetting enderChest = this.add(new BindSetting("EnderChest", -1, () -> this.page.is(Page.Place)));
    private final BooleanSetting placeRotate = this.add(new BooleanSetting("PlaceRotate", true, () -> this.page.is(Page.Place)));
    private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.is(Page.Place)));
    private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 100, 0, 1000, () -> this.page.is(Page.Place)));
    private final BooleanSetting checkDouble = this.add(new BooleanSetting("CheckDouble", false, () -> this.page.is(Page.Render)));
    private final EnumSetting<Animation> animation = this.add(new EnumSetting<Animation>("Animation", Animation.Up, () -> this.page.is(Page.Render)));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut, () -> this.page.is(Page.Render)));
    private final EnumSetting<Easing> fadeEase = this.add(new EnumSetting<Easing>("FadeEase", Easing.CubicInOut, () -> this.page.is(Page.Render)));
    private final SliderSetting expandLine = this.add(new SliderSetting("ExpandLine", 0.0, 0.0, 1.0, () -> this.page.is(Page.Render)));
    private final ColorSetting startColor = this.add(new ColorSetting("StartFill", new Color(255, 255, 255, 100), () -> this.page.is(Page.Render)));
    private final ColorSetting startOutlineColor = this.add(new ColorSetting("StartOutline", new Color(255, 255, 255, 100), () -> this.page.is(Page.Render)));
    private final ColorSetting endColor = this.add(new ColorSetting("EndFill", new Color(255, 255, 255, 100), () -> this.page.is(Page.Render)));
    private final ColorSetting endOutlineColor = this.add(new ColorSetting("EndOutline", new Color(255, 255, 255, 100), () -> this.page.is(Page.Render)));
    private final ColorSetting doubleColor = this.add(new ColorSetting("DoubleFill", new Color(88, 94, 255, 100), () -> this.doubleBreak.getValue() && this.page.is(Page.Render)));
    private final ColorSetting doubleOutlineColor = this.add(new ColorSetting("DoubleOutline", new Color(88, 94, 255, 100), () -> this.doubleBreak.getValue() && this.page.is(Page.Render)));
    private final BooleanSetting text = this.add(new BooleanSetting("Text", true, () -> this.page.is(Page.Render)));
    private final BooleanSetting box = this.add(new BooleanSetting("Box", true, () -> this.page.is(Page.Render)));
    private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true, () -> this.page.is(Page.Render)));
    private final Timer mineTimer = new Timer();
    private final Timer sync = new Timer();
    private final Timer secondTimer = new Timer();
    private final Timer delayTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer startTime = new Timer();
    public static boolean ghost;
    public static boolean complete;
    int lastSlot = -1;
    Vec3 directionVec = null;
    Runnable switchBack;
    BlockPos breakPos;
    boolean startPacket = false;
    int breakNumber = 0;
    double breakFinalTime;
    double secondFinalTime;
    boolean sendGroundPacket = false;
    boolean swapped = false;
    int mainSlot = 0;

    public PacketMine() {
        super("PacketMine", Module.Category.Player);
        this.setChinese("\u53d1\u5305\u6316\u6398");
        INSTANCE = this;
    }

    public static BlockPos getBreakPos() {
        if (INSTANCE.isOn()) {
            return PacketMine.INSTANCE.breakPos;
        }
        return null;
    }

    @Override
    public String getInfo() {
        if (progress >= 1.0) {
            return "Done";
        }
        return this.df.format(progress * 100.0) + "%";
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.rotate.getValue() && this.shouldYawStep() && this.directionVec != null && !this.sync.passedMs(this.syncTime.getValue())) {
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @Override
    public void onLogin() {
        this.startPacket = false;
        ghost = false;
        complete = false;
        this.breakPos = null;
        secondPos = null;
    }

    @Override
    public void onDisable() {
        this.startPacket = false;
        ghost = false;
        complete = false;
        this.breakPos = null;
    }

    private void autoSwitch() {
        if (this.autoSwitch.getValue() && this.doubleBreak.getValue()) {
            int index = -1;
            if (secondPos != null) {
                float CurrentFastest = 1.0f;
                for (int i = 0; i < 9; ++i) {
                    float destroySpeed;
                    float digSpeed;
                    ItemStack stack = PacketMine.mc.player.getInventory().method_5438(i);
                    if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.method_8225((Holder)((Holder)PacketMine.mc.level.method_30349().method_30530(Enchantments.EFFICIENCY.method_58273()).method_40264(Enchantments.EFFICIENCY).get()), (ItemStack)stack)) + (destroySpeed = stack.method_7924(PacketMine.mc.level.method_8320(secondPos))) > CurrentFastest)) continue;
                    CurrentFastest = digSpeed + destroySpeed;
                    index = i;
                }
            }
            if (index == -1 || PacketMine.mc.options.keyUse.isDown() || PacketMine.mc.options.keyAttack.isDown() || PacketMine.mc.player.isUsingItem() || !this.secondTimer.passedMs(this.getBreakTime(secondPos, index, this.start.getValue()))) {
                if (this.swapped) {
                    InventoryUtil.switchToSlot(this.mainSlot);
                    this.swapped = false;
                }
            } else if (index != PacketMine.mc.player.getInventory().selected) {
                this.mainSlot = PacketMine.mc.player.getInventory().selected;
                InventoryUtil.switchToSlot(index);
                this.swapped = true;
            }
        }
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        int oldSlot;
        if (PacketMine.nullCheck()) {
            return;
        }
        if (this.breakPos != null && PacketMine.mc.level.isEmptyBlock(this.breakPos)) {
            complete = true;
        }
        if (secondPos != null) {
            int secondSlot = this.getTool(secondPos);
            if (secondSlot == -1) {
                secondSlot = PacketMine.mc.player.getInventory().selected;
            }
            this.secondFinalTime = this.getBreakTime(secondPos, secondSlot, 1.0);
            if (this.isAir(secondPos) || PacketMine.unbreakable(secondPos)) {
                secondPos = null;
            } else {
                double time = this.getBreakTime(secondPos, PacketMine.mc.player.getInventory().selected, 1.0);
                if (this.secondTimer.passedMs(time * this.timeOut.getValue())) {
                    secondPos = null;
                }
            }
        }
        if (this.switchBack != null && event.isPre()) {
            this.switchBack.run();
            this.switchBack = null;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (PacketMine.mc.player.isDeadOrDying()) {
            secondPos = null;
        }
        this.autoSwitch();
        if (PacketMine.mc.player.method_7337()) {
            this.startPacket = false;
            ghost = false;
            complete = false;
            this.breakNumber = 0;
            this.breakPos = null;
            progress = 0.0;
            return;
        }
        if (this.breakPos == null) {
            this.breakNumber = 0;
            this.startPacket = false;
            ghost = false;
            complete = false;
            progress = 0.0;
            return;
        }
        int slot = this.getTool(this.breakPos);
        if (slot == -1) {
            slot = PacketMine.mc.player.getInventory().selected;
        }
        this.breakFinalTime = this.getBreakTime(this.breakPos, slot);
        progress = (double)this.mineTimer.getMs() / this.breakFinalTime;
        if (this.isAir(this.breakPos)) {
            this.breakNumber = 0;
        }
        if ((double)this.breakNumber > this.maxBreak.getValue() - 1.0 && this.maxBreak.getValue() > 0.0 && !complete || !this.wait.getValue() && this.isAir(this.breakPos) && !this.instant.getValue()) {
            if (this.breakPos.equals((Object)secondPos)) {
                secondPos = null;
            }
            this.startPacket = false;
            ghost = false;
            complete = false;
            this.breakNumber = 0;
            this.breakPos = null;
            return;
        }
        if (PacketMine.unbreakable(this.breakPos)) {
            if (this.unbreakableCancel.getValue()) {
                this.breakPos = null;
                this.startPacket = false;
                ghost = false;
                complete = false;
            }
            this.breakNumber = 0;
            return;
        }
        if ((double)Mth.method_15355((float)((float)PacketMine.mc.player.getEyePosition().method_1025(this.breakPos.method_46558()))) > this.range.getValue()) {
            if (this.farCancel.getValue()) {
                this.startPacket = false;
                ghost = false;
                complete = false;
                this.breakNumber = 0;
                this.breakPos = null;
            }
            return;
        }
        if (this.usingPause.getValue() && PacketMine.mc.player.isUsingItem() && (!this.allowOffhand.getValue() || PacketMine.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND)) {
            return;
        }
        if (this.pause.isPressed()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.breakPos.equals((Object)AutoAnchor.INSTANCE.currentPos) && BlockUtil.getBlock(PacketMine.getBreakPos()) instanceof RespawnAnchorBlock) {
            return;
        }
        if (!this.hotBar.getValue() && !EntityUtil.inInventory()) {
            return;
        }
        if (this.isAir(this.breakPos)) {
            if (this.shouldCrystal()) {
                for (Direction facing : Direction.values()) {
                    CombatUtil.attackCrystal(this.breakPos.method_10093(facing), this.placeRotate.getValue(), true);
                }
            }
            if (this.placeTimer.passedMs(this.placeDelay.getValue()) && BlockUtil.canPlace(this.breakPos) && PacketMine.mc.screen == null) {
                int obsidian;
                if (this.enderChest.isPressed()) {
                    int eChest = this.findBlock(Blocks.ENDER_CHEST);
                    if (eChest != -1) {
                        oldSlot = PacketMine.mc.player.getInventory().selected;
                        this.doSwap(eChest, eChest);
                        BlockUtil.placeBlock(this.breakPos, this.placeRotate.getValue(), true);
                        this.doSwap(oldSlot, eChest);
                        this.placeTimer.reset();
                    }
                } else if (this.obsidian.isPressed() && (obsidian = this.findBlock(Blocks.OBSIDIAN)) != -1) {
                    int hasCrystal = 0;
                    if (this.shouldCrystal()) {
                        for (Entity entity : BlockUtil.getEntities(new AABB(this.breakPos.method_10084()))) {
                            if (!(entity instanceof EndCrystal)) continue;
                            hasCrystal = 1;
                            break;
                        }
                    }
                    if (!hasCrystal || this.spamPlace.getValue()) {
                        oldSlot = PacketMine.mc.player.getInventory().selected;
                        this.doSwap(obsidian, obsidian);
                        BlockUtil.placeBlock(this.breakPos, this.placeRotate.getValue(), true);
                        this.doSwap(oldSlot, obsidian);
                        this.placeTimer.reset();
                    }
                }
            }
            this.breakNumber = 0;
        } else if (this.canPlaceCrystal(this.breakPos.method_10084()) && this.shouldCrystal() && (this.placeTimer.passedMs(this.placeDelay.getValue()) ? (this.checkDamage.getValue() ? (double)this.mineTimer.getMs() / this.breakFinalTime >= this.crystalDamage.getValue() && !this.placeCrystal() : !this.placeCrystal()) : this.startPacket)) {
            return;
        }
        if (this.waitPlace.getValue()) {
            for (Direction i : Direction.values()) {
                if (!this.breakPos.method_10093(i).equals((Object)AutoCrystal.INSTANCE.crystalPos)) continue;
                if (!AutoCrystal.INSTANCE.canPlaceCrystal(this.breakPos, false, false)) break;
                return;
            }
        }
        if (!this.delayTimer.passed((long)this.stopDelay.getValue())) {
            return;
        }
        if (this.startPacket) {
            if (this.isAir(this.breakPos)) {
                return;
            }
            if (this.onlyGround.getValue() && !PacketMine.mc.player.onGround()) {
                return;
            }
            if (this.mineTimer.passed((long)this.breakFinalTime)) {
                int shouldSwitch;
                if (this.endRotate.getValue() && this.shouldYawStep() && !this.faceVector(this.breakPos.method_46558().method_43206(BlockUtil.getClickSide(this.breakPos), 0.5))) {
                    return;
                }
                int old = PacketMine.mc.player.getInventory().selected;
                if (this.hotBar.getValue()) {
                    shouldSwitch = slot != old ? 1 : 0;
                } else {
                    if (slot < 9) {
                        slot += 36;
                    }
                    shouldSwitch = old + 36 != slot ? 1 : 0;
                    oldSlot = shouldSwitch;
                }
                if (shouldSwitch) {
                    if (this.hotBar.getValue()) {
                        InventoryUtil.switchToSlot(slot);
                    } else {
                        PacketMine.mc.gameMode.method_2906(PacketMine.mc.player.containerMenu.containerId, slot, old, ClickType.SWAP, (Player)PacketMine.mc.player);
                    }
                }
                int finalSlot = slot;
                this.switchBack = () -> this.lambda$onTick$71(shouldSwitch != 0, old, finalSlot);
                if (!this.noGhostHand.getValue()) {
                    this.switchBack.run();
                    this.switchBack = null;
                }
            }
        } else {
            if (!this.startTime.passed(this.startDelay.getValueInt())) {
                return;
            }
            if (!this.mineAir.getValue() && this.isAir(this.breakPos)) {
                return;
            }
            Direction side = BlockUtil.getClickSide(this.breakPos);
            if (this.rotate.getValue()) {
                Vec3i vec3i = side.getNormal();
                if (!this.faceVector(this.breakPos.method_46558().method_1019(new Vec3((double)vec3i.method_10263() * 0.5, (double)vec3i.method_10264() * 0.5, (double)vec3i.getZ() * 0.5)))) {
                    return;
                }
            }
            this.mineTimer.reset();
            this.animationTime.reset();
            if (this.swing.getValue()) {
                EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.swingMode.getValue());
            }
            if (this.doubleBreak.getValue()) {
                if (secondPos == null || this.isAir(secondPos)) {
                    double breakTime = this.getBreakTime(this.breakPos, slot, 1.0);
                    this.secondAnim.reset();
                    this.secondAnim.setLength((long)breakTime);
                    this.secondTimer.reset();
                    secondPos = this.breakPos;
                }
                this.doDoubleBreak(side);
            }
            PacketMine.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.START_DESTROY_BLOCK, this.breakPos, side, id));
            if (this.rotate.getValue() && !this.shouldYawStep()) {
                Aevum.ROTATION.snapBack();
            }
            this.startTime.reset();
        }
    }

    private void breakBlock(BlockPos breakPos) {
        PacketMine.mc.level.method_8320(breakPos).method_26204().method_9576((Level)PacketMine.mc.level, breakPos, PacketMine.mc.level.method_8320(breakPos), (Player)PacketMine.mc.player);
    }

    void doDoubleBreak(Direction side) {
        PacketMine.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.START_DESTROY_BLOCK, this.breakPos, side, id));
        PacketMine.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, this.breakPos, side, id));
    }

    boolean placeCrystal() {
        int crystal = this.findCrystal();
        if (crystal != -1) {
            int oldSlot = PacketMine.mc.player.getInventory().selected;
            this.doSwap(crystal, crystal);
            BlockUtil.placeCrystal(this.breakPos.method_10084(), this.placeRotate.getValue());
            this.doSwap(oldSlot, crystal);
            this.placeTimer.reset();
            return !this.waitPlace.getValue();
        }
        return true;
    }

    @EventListener
    public void onAttackBlock(ClickBlockEvent event) {
        int slot;
        if (PacketMine.nullCheck()) {
            return;
        }
        if (PacketMine.mc.player.method_7337()) {
            return;
        }
        event.cancel();
        BlockPos pos = event.getPos();
        if (pos.equals((Object)this.breakPos)) {
            return;
        }
        if (PacketMine.unbreakable(pos)) {
            return;
        }
        if (this.breakPos != null && this.preferWeb.getValue() && BlockUtil.getBlock(this.breakPos) == Blocks.COBWEB) {
            return;
        }
        if (this.breakPos != null && this.preferHead.getValue() && PacketMine.mc.player.isVisuallyCrawling() && EntityUtil.getPlayerPos(true).method_10084().equals((Object)this.breakPos)) {
            return;
        }
        if (BlockUtil.getClickSideStrict(pos) == null) {
            return;
        }
        if ((double)Mth.method_15355((float)((float)PacketMine.mc.player.getEyePosition().method_1025(pos.method_46558()))) > this.range.getValue()) {
            return;
        }
        this.breakPos = pos;
        this.breakNumber = 0;
        this.startPacket = false;
        ghost = false;
        complete = false;
        this.mineTimer.reset();
        this.animationTime.reset();
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        Direction side = BlockUtil.getClickSide(this.breakPos);
        if (this.rotate.getValue()) {
            Vec3i vec3i = side.getNormal();
            if (!this.faceVector(this.breakPos.method_46558().method_1019(new Vec3((double)vec3i.method_10263() * 0.5, (double)vec3i.method_10264() * 0.5, (double)vec3i.getZ() * 0.5)))) {
                return;
            }
        }
        if (!this.startTime.passed(this.startDelay.getValueInt())) {
            return;
        }
        if (this.swing.getValue()) {
            EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.swingMode.getValue());
        }
        if (this.doubleBreak.getValue()) {
            if (secondPos == null || this.isAir(secondPos)) {
                int slot2 = this.getTool(this.breakPos);
                if (slot2 == -1) {
                    slot2 = PacketMine.mc.player.getInventory().selected;
                }
                this.secondFinalTime = this.getBreakTime(this.breakPos, slot2, 1.0);
                this.secondAnim.reset();
                this.secondAnim.setLength((long)this.secondFinalTime);
                this.secondTimer.reset();
                secondPos = this.breakPos;
            }
            this.doDoubleBreak(side);
        }
        if ((slot = this.getTool(this.breakPos)) == -1) {
            slot = PacketMine.mc.player.getInventory().selected;
        }
        this.breakFinalTime = this.getBreakTime(this.breakPos, slot);
        PacketMine.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.START_DESTROY_BLOCK, this.breakPos, side, id));
        if (this.rotate.getValue() && !this.shouldYawStep()) {
            Aevum.ROTATION.snapBack();
        }
        this.startTime.reset();
    }

    public void mine(BlockPos pos) {
        if (PacketMine.nullCheck()) {
            return;
        }
        if (PacketMine.mc.player.method_7337()) {
            PacketMine.mc.gameMode.method_2910(pos, BlockUtil.getClickSide(pos));
            return;
        }
        if (this.isOff()) {
            PacketMine.mc.gameMode.method_2910(pos, BlockUtil.getClickSide(pos));
            return;
        }
        if (pos.equals((Object)this.breakPos)) {
            return;
        }
        if (PacketMine.unbreakable(pos)) {
            return;
        }
        if (this.breakPos != null && this.preferWeb.getValue() && BlockUtil.getBlock(this.breakPos) == Blocks.COBWEB) {
            return;
        }
        if (this.breakPos != null && this.preferHead.getValue() && PacketMine.mc.player.isVisuallyCrawling() && EntityUtil.getPlayerPos(true).method_10084().equals((Object)this.breakPos)) {
            return;
        }
        if (BlockUtil.getClickSideStrict(pos) == null) {
            return;
        }
        if ((double)Mth.method_15355((float)((float)PacketMine.mc.player.getEyePosition().method_1025(pos.method_46558()))) > this.range.getValue()) {
            return;
        }
        this.breakPos = pos;
        this.breakNumber = 0;
        this.startPacket = false;
        ghost = false;
        complete = false;
        this.mineTimer.reset();
        this.animationTime.reset();
    }

    boolean faceVector(Vec3 directionVec) {
        if (!this.shouldYawStep()) {
            Aevum.ROTATION.lookAt(directionVec);
            return true;
        }
        this.sync.reset();
        this.directionVec = directionVec;
        if (Aevum.ROTATION.inFov(directionVec, this.fov.getValueFloat())) {
            return true;
        }
        return !this.checkFov.getValue();
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (this.breakPos != null && PacketMine.mc.level.isEmptyBlock(this.breakPos)) {
            complete = true;
        }
        if (!PacketMine.mc.player.method_7337()) {
            double ease;
            if (secondPos != null) {
                if (this.isAir(secondPos)) {
                    secondPos = null;
                    return;
                }
                if (!this.checkDouble.getValue() || !secondPos.equals((Object)this.breakPos)) {
                    this.secondAnim.setLength((long)this.secondFinalTime);
                    ease = this.secondAnim.ease(this.ease.getValue());
                    if (this.box.getValue()) {
                        Render3DUtil.drawFill(matrixStack, this.getFillBox(secondPos, ease), this.doubleColor.getValue());
                    }
                    if (this.outline.getValue()) {
                        Render3DUtil.drawBox(matrixStack, this.getOutlineBox(secondPos, ease), this.doubleOutlineColor.getValue());
                    }
                }
            }
            if (this.breakPos != null) {
                progress = (double)this.mineTimer.getMs() / this.breakFinalTime;
                this.animationTime.setLength((long)this.breakFinalTime);
                ease = this.animationTime.ease(this.ease.getValue());
                if (PacketMine.unbreakable(this.breakPos)) {
                    if (this.box.getValue()) {
                        Render3DUtil.drawFill(matrixStack, new AABB(this.breakPos), this.startColor.getValue());
                    }
                    if (this.outline.getValue()) {
                        Render3DUtil.drawBox(matrixStack, new AABB(this.breakPos), this.startOutlineColor.getValue());
                    }
                    return;
                }
                if (this.box.getValue()) {
                    Render3DUtil.drawFill(matrixStack, this.getFillBox(this.breakPos, ease), this.getColor(this.animationTime.ease(this.fadeEase.getValue())));
                }
                if (this.outline.getValue()) {
                    Render3DUtil.drawBox(matrixStack, this.getOutlineBox(this.breakPos, ease), this.getOutlineColor(this.animationTime.ease(this.fadeEase.getValue())));
                }
                if (this.text.getValue()) {
                    if (this.isAir(this.breakPos)) {
                        Render3DUtil.drawText3D("Waiting", this.breakPos.method_46558(), -1);
                    } else if ((double)((int)this.mineTimer.getMs()) < this.breakFinalTime) {
                        Render3DUtil.drawText3D(this.df.format(progress * 100.0) + "%", this.breakPos.method_46558(), -1);
                    } else {
                        Render3DUtil.drawText3D("100.0%", this.breakPos.method_46558(), -1);
                    }
                }
            } else {
                progress = 0.0;
            }
        } else {
            progress = 0.0;
        }
    }

    private AABB getFillBox(BlockPos pos, double ease) {
        switch (this.animation.getValue().ordinal()) {
            case 1: {
                ease = (1.0 - ease) / 2.0;
                return new AABB(pos).method_1002(ease, 0.0, ease).method_1002(-ease, 0.0, -ease);
            }
            case 3: {
                return new AABB((double)pos.method_10263(), (double)(pos.method_10264() + 1) - ease, (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.getZ() + 1));
            }
            case 2: {
                return new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)pos.method_10264() + ease, (double)(pos.getZ() + 1));
            }
            case 0: {
                ease = (1.0 - ease) / 2.0;
                return new AABB(pos).method_1002(ease, ease, ease).method_1002(-ease, -ease, -ease);
            }
            case 4: {
                return new AABB(pos).method_1002(ease, ease, ease).method_1002(-ease, -ease, -ease);
            }
        }
        return new AABB(pos);
    }

    private AABB getOutlineBox(BlockPos pos, double ease) {
        ease = Math.min(ease + this.expandLine.getValue(), 1.0);
        switch (this.animation.getValue().ordinal()) {
            case 1: {
                ease = (1.0 - ease) / 2.0;
                return new AABB(pos).method_1002(ease, 0.0, ease).method_1002(-ease, 0.0, -ease);
            }
            case 3: {
                return new AABB((double)pos.method_10263(), (double)(pos.method_10264() + 1) - ease, (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.getZ() + 1));
            }
            case 2: {
                return new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)pos.method_10264() + ease, (double)(pos.getZ() + 1));
            }
            case 0: {
                ease = (1.0 - ease) / 2.0;
                return new AABB(pos).method_1002(ease, ease, ease).method_1002(-ease, -ease, -ease);
            }
            case 4: {
                return new AABB(pos).method_1002(ease, ease, ease).method_1002(-ease, -ease, -ease);
            }
        }
        return new AABB(pos);
    }

    @EventListener(priority=-200)
    public void onPacketSend(PacketEvent.Send event) {
        if (PacketMine.nullCheck() || PacketMine.mc.player.method_7337()) {
            return;
        }
        if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
            if (this.bypassGround.getValue() && !PacketMine.mc.player.isFallFlying() && this.breakPos != null && !this.isAir(this.breakPos) && this.bypassTime.getValue() > 0.0 && Mth.method_15355((float)((float)this.breakPos.method_46558().method_1025(PacketMine.mc.player.getEyePosition()))) <= this.range.getValueFloat() + 2.0f) {
                double breakTime = this.breakFinalTime - this.bypassTime.getValue();
                if (breakTime <= 0.0 || this.mineTimer.passed((long)breakTime)) {
                    this.sendGroundPacket = true;
                    ((IPlayerMoveC2SPacket)event.getPacket()).setOnGround(true);
                }
            } else {
                this.sendGroundPacket = false;
            }
        } else {
            Packet<?> packet = event.getPacket();
            if (packet instanceof ServerboundSetCarriedItemPacket) {
                ServerboundSetCarriedItemPacket packet2 = (ServerboundSetCarriedItemPacket)packet;
                if (packet2.getSlot() != this.lastSlot) {
                    this.lastSlot = packet2.getSlot();
                    if (this.switchReset.getValue()) {
                        this.startPacket = false;
                        ghost = false;
                        complete = false;
                        this.mineTimer.reset();
                        this.animationTime.reset();
                    }
                }
            } else {
                packet = event.getPacket();
                if (packet instanceof ServerboundPlayerActionPacket) {
                    ServerboundPlayerActionPacket packet3 = (ServerboundPlayerActionPacket)packet;
                    if (packet3.getAction() == ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.START_DESTROY_BLOCK) {
                        if (this.breakPos == null || !packet3.getPos().equals((Object)this.breakPos)) {
                            return;
                        }
                        if (this.grimDisabler.getValue()) {
                            mc.getConnection().method_52787((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, packet3.getPos(), packet3.getDirection()));
                        }
                        this.startPacket = true;
                    } else if (packet3.getAction() == ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK) {
                        if (this.breakPos == null || !packet3.getPos().equals((Object)this.breakPos)) {
                            return;
                        }
                        if (!this.instant.getValue()) {
                            this.startPacket = false;
                            ghost = false;
                            complete = false;
                        }
                    }
                }
            }
        }
    }

    boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.method_10074();
        BlockPos boost = obsPos.method_10084();
        return !(BlockUtil.getBlock(obsPos) != Blocks.BEDROCK && BlockUtil.getBlock(obsPos) != Blocks.OBSIDIAN || BlockUtil.getClickSideStrict(obsPos) == null || !this.noEntity(boost) || !this.noEntity(boost.method_10084()) || ClientSetting.INSTANCE.lowVersion.getValue() && !PacketMine.mc.level.isEmptyBlock(boost.method_10084()));
    }

    boolean noEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new AABB(pos))) {
            if (entity instanceof ItemEntity || entity instanceof ArmorStand && AntiCheat.INSTANCE.ignoreArmorStand.getValue()) continue;
            return false;
        }
        return true;
    }

    void doSwap(int slot, int inv) {
        if (!this.inventory.getValue()) {
            InventoryUtil.switchToSlot(slot);
        } else {
            InventoryUtil.inventorySwap(inv, PacketMine.mc.player.getInventory().selected);
        }
    }

    int findCrystal() {
        if (this.inventory.getValue()) {
            return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
        }
        return InventoryUtil.findItem(Items.END_CRYSTAL);
    }

    int findBlock(Block block) {
        if (this.inventory.getValue()) {
            return InventoryUtil.findBlockInventorySlot(block);
        }
        return InventoryUtil.findBlock(block);
    }

    boolean shouldCrystal() {
        return this.crystal.getValue() && (!this.onlyHeadBomber.getValue() || this.obsidian.isPressed()) || CevBreaker.INSTANCE.isOn();
    }

    public static double getBreakTime(BlockPos pos) {
        int slot = INSTANCE.getTool(pos);
        if (slot == -1) {
            slot = PacketMine.mc.player.getInventory().selected;
        }
        return INSTANCE.getBreakTime(pos, slot);
    }

    double getBreakTime(BlockPos pos, int slot) {
        return this.getBreakTime(pos, slot, this.damage.getValue());
    }

    double getBreakTime(BlockPos pos, int slot, double damage) {
        return (double)(1.0f / this.getBlockStrength(pos, PacketMine.mc.player.getInventory().method_5438(slot)) / 20.0f * 1000.0f) * damage;
    }

    float getBlockStrength(BlockPos position, ItemStack itemStack) {
        BlockState state = PacketMine.mc.level.method_8320(position);
        float hardness = state.method_26214((BlockGetter)PacketMine.mc.level, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        float i = !state.requiresCorrectToolForDrops() || itemStack.method_7951(state) ? 30.0f : 100.0f;
        return this.getDigSpeed(state, itemStack) / hardness / i;
    }

    float getDigSpeed(BlockState state, ItemStack itemStack) {
        int efficiencyModifier;
        float digSpeed = this.getDestroySpeed(state, itemStack);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.method_8225((Holder)((Holder)PacketMine.mc.level.method_30349().method_30530(Enchantments.EFFICIENCY.method_58273()).method_40264(Enchantments.EFFICIENCY).get()), (ItemStack)itemStack)) > 0 && !itemStack.method_7960()) {
            digSpeed += (float)(StrictMath.pow(efficiencyModifier, 2.0) + 1.0);
        }
        if (PacketMine.mc.player.method_6059(MobEffects.DIG_SPEED)) {
            digSpeed *= 1.0f + (float)(PacketMine.mc.player.method_6112(MobEffects.DIG_SPEED).method_5578() + 1) * 0.2f;
        }
        if (PacketMine.mc.player.method_6059(MobEffects.DIG_SLOWDOWN)) {
            digSpeed *= (switch (PacketMine.mc.player.method_6112(MobEffects.DIG_SLOWDOWN).method_5578()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            });
        }
        if (PacketMine.mc.player.isUnderWater()) {
            digSpeed *= (float)PacketMine.mc.player.method_5996(Attributes.SUBMERGED_MINING_SPEED).method_6194();
        }
        boolean inWeb = this.checkWeb.getValue() && Aevum.PLAYER.isInWeb((Player)PacketMine.mc.player) && PacketMine.mc.level.method_8320(this.breakPos).method_26204() == Blocks.COBWEB;
        boolean bl = inWeb;
        if ((!PacketMine.mc.player.onGround() || inWeb) && PacketMine.INSTANCE.checkGround.getValue() && (!this.smart.getValue() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground) && Criticals.INSTANCE.isOn() || PacketMine.mc.player.isFallFlying() || inWeb)) {
            digSpeed /= 5.0f;
        }
        return digSpeed < 0.0f ? 0.0f : digSpeed;
    }

    float getDestroySpeed(BlockState state, ItemStack itemStack) {
        float destroySpeed = 1.0f;
        if (itemStack != null && !itemStack.method_7960()) {
            destroySpeed *= itemStack.method_7924(state);
        }
        return destroySpeed;
    }

    Color getColor(double quad) {
        int sR = this.startColor.getValue().getRed();
        int sG = this.startColor.getValue().getGreen();
        int sB = this.startColor.getValue().getBlue();
        int sA = this.startColor.getValue().getAlpha();
        int eR = this.endColor.getValue().getRed();
        int eG = this.endColor.getValue().getGreen();
        int eB = this.endColor.getValue().getBlue();
        int eA = this.endColor.getValue().getAlpha();
        return new Color((int)((double)sR + (double)(eR - sR) * quad), (int)((double)sG + (double)(eG - sG) * quad), (int)((double)sB + (double)(eB - sB) * quad), (int)((double)sA + (double)(eA - sA) * quad));
    }

    Color getOutlineColor(double quad) {
        int sR = this.startOutlineColor.getValue().getRed();
        int sG = this.startOutlineColor.getValue().getGreen();
        int sB = this.startOutlineColor.getValue().getBlue();
        int sA = this.startOutlineColor.getValue().getAlpha();
        int eR = this.endOutlineColor.getValue().getRed();
        int eG = this.endOutlineColor.getValue().getGreen();
        int eB = this.endOutlineColor.getValue().getBlue();
        int eA = this.endOutlineColor.getValue().getAlpha();
        return new Color((int)((double)sR + (double)(eR - sR) * quad), (int)((double)sG + (double)(eG - sG) * quad), (int)((double)sB + (double)(eB - sB) * quad), (int)((double)sA + (double)(eA - sA) * quad));
    }

    int getTool(BlockPos pos) {
        if (this.hotBar.getValue()) {
            int index = -1;
            float CurrentFastest = 1.0f;
            for (int i = 0; i < 9; ++i) {
                float destroySpeed;
                float digSpeed;
                ItemStack stack = PacketMine.mc.player.getInventory().method_5438(i);
                if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.method_8225((Holder)((Holder)PacketMine.mc.level.method_30349().method_30530(Enchantments.EFFICIENCY.method_58273()).method_40264(Enchantments.EFFICIENCY).get()), (ItemStack)stack)) + (destroySpeed = stack.method_7924(PacketMine.mc.level.method_8320(pos))) > CurrentFastest)) continue;
                CurrentFastest = digSpeed + destroySpeed;
                index = i;
            }
            return index;
        }
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        float CurrentFastest = 1.0f;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            float destroySpeed;
            float digSpeed;
            if (entry.getValue().method_7909() instanceof AirItem || !((digSpeed = (float)EnchantmentHelper.method_8225((Holder)((Holder)PacketMine.mc.level.method_30349().method_30530(Enchantments.EFFICIENCY.method_58273()).method_40264(Enchantments.EFFICIENCY).get()), (ItemStack)entry.getValue())) + (destroySpeed = entry.getValue().method_7924(PacketMine.mc.level.method_8320(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            slot.set(entry.getKey());
        }
        return slot.get();
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (PacketMine.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    boolean isAir(BlockPos breakPos) {
        return PacketMine.mc.level.isEmptyBlock(breakPos) || BlockUtil.getBlock(breakPos) == Blocks.FIRE && BlockUtil.hasCrystal(breakPos);
    }

    public static boolean unbreakable(BlockPos blockPos) {
        Block block = PacketMine.mc.level.method_8320(blockPos).method_26204();
        return !(block instanceof AirBlock) && (block.defaultDestroyTime() == -1.0f || block.defaultDestroyTime() == 100.0f);
    }

    private /* synthetic */ void lambda$onTick$71(boolean shouldSwitch, int old, int finalSlot) {
        if (this.endRotate.getValue() && !this.faceVector(this.breakPos.method_46558().method_43206(BlockUtil.getClickSide(this.breakPos), 0.5))) {
            if (shouldSwitch) {
                if (this.hotBar.getValue()) {
                    InventoryUtil.switchToSlot(old);
                } else {
                    PacketMine.mc.gameMode.method_2906(PacketMine.mc.player.containerMenu.containerId, finalSlot, old, ClickType.SWAP, (Player)PacketMine.mc.player);
                    EntityUtil.syncInventory();
                }
            }
            return;
        }
        PacketMine.sendSequencedPacket(id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.ServerboundPlayerActionPacket$Action.STOP_DESTROY_BLOCK, this.breakPos, BlockUtil.getClickSide(this.breakPos), id));
        if (this.endSwing.getValue()) {
            EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.swingMode.getValue());
        }
        if (shouldSwitch) {
            if (this.hotBar.getValue()) {
                InventoryUtil.switchToSlot(old);
            } else {
                PacketMine.mc.gameMode.method_2906(PacketMine.mc.player.containerMenu.containerId, finalSlot, old, ClickType.SWAP, (Player)PacketMine.mc.player);
                EntityUtil.syncInventory();
            }
        }
        ++this.breakNumber;
        this.delayTimer.reset();
        this.startTime.reset();
        if (this.afterBreak.getValue() && this.shouldCrystal()) {
            for (Direction facing : Direction.values()) {
                CombatUtil.attackCrystal(this.breakPos.method_10093(facing), this.placeRotate.getValue(), true);
            }
        }
        if (this.setAir.getValue()) {
            PacketMine.mc.level.method_8501(this.breakPos, Blocks.AIR.defaultBlockState());
        }
        if (this.endRotate.getValue() && !this.shouldYawStep()) {
            Aevum.ROTATION.snapBack();
        }
        ghost = true;
    }

    static {
        progress = 0.0;
        ghost = false;
        complete = false;
    }

    public static enum Page {
        General,
        Check,
        Place,
        Rotation,
        Render;

    }

    private static enum Animation {
        Center,
        Grow,
        Up,
        Down,
        Oscillation,
        None;

    }
}

