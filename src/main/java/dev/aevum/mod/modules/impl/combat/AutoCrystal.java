/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.EntityType
 *  net.minecraft.LivingEntity
 *  net.minecraft.EndCrystal
 *  net.minecraft.ItemEntity
 *  net.minecraft.Player
 *  net.minecraft.ClickType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Direction
 *  net.minecraft.Position
 *  net.minecraft.AABB
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundAddEntityPacket
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundPickItemPacket
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  net.minecraft.AbstractContainerScreen
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.EntitySpawnedEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.AnimateUtil;
import dev.aevum.api.utils.math.DamageUtils;
import dev.aevum.api.utils.math.ExplosionUtil;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.AntiCheat;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoPush;
import dev.aevum.mod.modules.impl.combat.AutoRegear;
import dev.aevum.mod.modules.impl.exploit.Phase;
import dev.aevum.mod.modules.impl.player.PacketMine;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.EntityType;
import net.minecraft.LivingEntity;
import net.minecraft.EndCrystal;
import net.minecraft.ItemEntity;
import net.minecraft.Player;
import net.minecraft.ClickType;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Direction;
import net.minecraft.Position;
import net.minecraft.AABB;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ClientboundAddEntityPacket;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundPickItemPacket;
import net.minecraft.Mth;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import net.minecraft.AbstractContainerScreen;

public class AutoCrystal
extends Module {
    public static AutoCrystal INSTANCE;
    private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.Place));
    private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 300, 0, 1000, () -> this.page.is(Page.Place)).setSuffix("ms"));
    private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.Place)).setSuffix("m"));
    private final SliderSetting minPDamage = this.add(new SliderSetting("MinPDamage", 5.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Place)).setSuffix("dmg"));
    private final SliderSetting maxSelfDamage = this.add(new SliderSetting("MaxSelfDmg", 12.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Place)).setSuffix("dmg"));
    private final SliderSetting reserve = this.add(new SliderSetting("Reserve", 2.0, 0.0, 10.0, 0.1, () -> this.page.is(Page.Place)).setSuffix("hp"));
    private final BooleanSetting facePlace = this.add(new BooleanSetting("FacePlace", false, () -> this.page.is(Page.Place))).setParent();
    private final SliderSetting facePlaceHealth = this.add(new SliderSetting("FacePlaceHealth", 4.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Place) && this.facePlace.isOpen()).setSuffix("hp"));
    private final SliderSetting facePlaceArmor = this.add(new SliderSetting("FacePlaceArmor", 10.0, 0.0, 100.0, 1.0, () -> this.page.is(Page.Place) && this.facePlace.isOpen()));
    private final SliderSetting breakDelay = this.add(new SliderSetting("BreakDelay", 300, 0, 1000, () -> this.page.is(Page.Break)).setSuffix("ms"));
    private final SliderSetting breakRange = this.add(new SliderSetting("BreakRange", 4.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.Break)).setSuffix("m"));
    private final BooleanSetting breakRemove = this.add(new BooleanSetting("Remove", false, () -> this.page.is(Page.Break)));
    private final SliderSetting breakFactor = this.add(new SliderSetting("BreakFactor", 1.0, 1.0, 10.0, 1.0, () -> this.page.is(Page.Break)));
    private final BooleanSetting instantBreak = this.add(new BooleanSetting("InstantBreak", true, () -> this.page.is(Page.Break)));
    private final BooleanSetting basePlace = this.add(new BooleanSetting("BasePlace", true, () -> this.page.is(Page.Base)).setParent());
    private final SliderSetting basePlaceRange = this.add(new SliderSetting("BasePlaceRange", 5.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.Base) && this.basePlace.isOpen()).setSuffix("m"));
    private final SliderSetting baseDelay = this.add(new SliderSetting("BaseDelay", 3000, 0, 10000, () -> this.page.is(Page.Base) && this.basePlace.isOpen()).setSuffix("ms"));
    private final SliderSetting baseMin = this.add(new SliderSetting("BaseMin", 6.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Base) && this.basePlace.isOpen()).setSuffix("dmg"));
    private final SliderSetting baseMax = this.add(new SliderSetting("BaseMax", 12.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Base) && this.basePlace.isOpen()).setSuffix("dmg"));
    private final SliderSetting overrideMax = this.add(new SliderSetting("OverrideMax", 8.0, 0.0, 36.0, 0.1, () -> this.page.is(Page.Base) && this.basePlace.isOpen()).setSuffix("dmg"));
    private final BooleanSetting baseBalance = this.add(new BooleanSetting("BaseBalance", true, () -> this.page.is(Page.Base) && this.basePlace.isOpen()));
    private final BooleanSetting onlyBelow = this.add(new BooleanSetting("OnlyBelow", true, () -> this.page.is(Page.Base) && this.basePlace.isOpen()));
    private final BooleanSetting baseInventorySwap = this.add(new BooleanSetting("InventorySwap", true, () -> this.page.is(Page.Base) && this.basePlace.isOpen()));
    private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true, () -> this.page.is(Page.Base) && this.basePlace.isOpen()));
    private final BooleanSetting rotateOnPlace = this.add(new BooleanSetting("OnPlace", true, () -> this.page.is(Page.Rotation)));
    private final BooleanSetting rotateOnBreak = this.add(new BooleanSetting("OnBreak", true, () -> this.page.is(Page.Rotation)));
    private final SliderSetting extraHitboxExpand = this.add(new SliderSetting("HitboxExpand", 0.5, 0.1, 2.0, 0.1, () -> this.page.is(Page.Rotation) && (this.rotateOnPlace.getValue() || this.rotateOnBreak.getValue())));
    private final BooleanSetting placeTWall = this.add(new BooleanSetting("PlaceTWall", false, () -> this.page.is(Page.RayTrace)).setParent());
    private final SliderSetting pWallRange = this.add(new SliderSetting("PWallRange", 3.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.RayTrace) && this.placeTWall.isOpen()).setSuffix("m"));
    private final BooleanSetting breakTWall = this.add(new BooleanSetting("BreakTWall", false, () -> this.page.is(Page.RayTrace)).setParent());
    private final SliderSetting bWallRange = this.add(new SliderSetting("BWallRange", 3.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.RayTrace) && this.breakTWall.isOpen()).setSuffix("m"));
    private final BooleanSetting predict = this.add(new BooleanSetting("Predict", true, () -> this.page.is(Page.Prediction)).setParent());
    private final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 4, 0, 20, () -> this.page.is(Page.Prediction) && this.predict.isOpen()).setSuffix("ticks"));
    private final SliderSetting maxMotionXZ = this.add(new SliderSetting("MaxMotionXZ", 0.6, 0.0, 2.0, 0.01, () -> this.page.is(Page.Prediction) && this.predict.isOpen()).setSuffix("b/t"));
    private final SliderSetting maxMotionY = this.add(new SliderSetting("MaxMotionY", 0.34, 0.0, 2.0, 0.01, () -> this.page.is(Page.Prediction) && this.predict.isOpen()).setSuffix("b/t"));
    private final BooleanSetting selfPredict = this.add(new BooleanSetting("SelfPredict", true, () -> this.page.is(Page.Prediction)).setParent());
    private final SliderSetting selfPredictTicks = this.add(new SliderSetting("SelfPredictTicks", 0, 0, 10, () -> this.page.is(Page.Prediction) && this.selfPredict.isOpen()).setSuffix("ticks"));
    private final BooleanSetting autoPredict = this.add(new BooleanSetting("AutoPredict", false, () -> this.page.is(Page.Prediction)));
    private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting guiPause = this.add(new BooleanSetting("GuiPause", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting regearPause = this.add(new BooleanSetting("RegearPause", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting phasePause = this.add(new BooleanSetting("PhasePause", true, () -> this.page.is(Page.Misc)));
    private final BooleanSetting pushPause = this.add(new BooleanSetting("PushPause", true, () -> this.page.is(Page.Misc)));
    private final SliderSetting posUpdateDelay = this.add(new SliderSetting("PosUpdateDelay", 50, 0, 5000, () -> this.page.is(Page.Misc)).setSuffix("ms"));
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, 0.1, () -> this.page.is(Page.Misc)).setSuffix("m"));
    private final SliderSetting wallRange = this.add(new SliderSetting("WallRange", 6.0, 0.0, 6.0, 0.01, () -> this.page.is(Page.Misc)).setSuffix("m"));
    private final BooleanSetting balance = this.add(new BooleanSetting("Balance", true, () -> this.page.is(Page.Misc)).setParent());
    private final SliderSetting balanceOffset = this.add(new SliderSetting("BalanceOffset", 0.0, -20.0, 20.0, 0.1, () -> this.page.is(Page.Misc) && this.balance.isOpen()).setSuffix("hp"));
    public final BooleanSetting airPlace = this.add(new BooleanSetting("AirPlace", false, () -> this.page.is(Page.Misc)));
    public final BooleanSetting replace = this.add(new BooleanSetting("Replace", false, () -> this.page.is(Page.Misc)));
    private final BooleanSetting render = this.add(new BooleanSetting("Render", true, () -> this.page.is(Page.Render)));
    private final BooleanSetting shrink = this.add(new BooleanSetting("Shrink", true, () -> this.page.is(Page.Render) && this.render.getValue()));
    private final ColorSetting fillColor = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100), () -> this.page.is(Page.Render) && this.render.getValue()).injectBoolean(true));
    private final ColorSetting boxColor = this.add(new ColorSetting("Box", new Color(255, 255, 255, 255), () -> this.page.is(Page.Render) && this.render.getValue()).injectBoolean(true));
    private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 1.5, 0.01, 3.0, 0.01, () -> this.page.is(Page.Render) && this.render.getValue()));
    private final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01, () -> this.page.is(Page.Render) && this.render.getValue()));
    private final SliderSetting startFadeTime = this.add(new SliderSetting("StartFade", 0.3, 0.0, 2.0, 0.01, () -> this.page.is(Page.Render) && this.render.getValue()).setSuffix("s"));
    private final SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2, 0.01, 1.0, 0.01, () -> this.page.is(Page.Render) && this.render.getValue()));
    private final BooleanSetting renderText = this.add(new BooleanSetting("DamageText", true, () -> this.page.is(Page.Render) && this.render.getValue()));
    private final ColorSetting textColor = this.add(new ColorSetting("TextColor", new Color(255, 255, 255, 255), () -> this.page.is(Page.Render) && this.render.getValue() && this.renderText.getValue()));
    private final BooleanSetting forcePlace = this.add(new BooleanSetting("ForcePlace", false, () -> this.page.is(Page.Place)));
    private boolean firstBase = true;
    public final Timer lastBreakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer baseTimer = new Timer();
    private final Timer posLockTimer = new Timer();
    private final Timer noPosTimer = new Timer();
    private final Timer rotationResetTimer = new Timer();
    private final DecimalFormat df = new DecimalFormat("0.0");
    private Vec3 rotationTarget = null;
    private Vec3 placeRotationPoint = null;
    private Vec3 unifiedRotationPoint = null;
    private BlockPos tempPos = null;
    private BlockPos tempBasePos = null;
    public BlockPos crystalPos = null;
    private BlockPos instantPendingPos = null;
    private int mioPendingBreakId = -1;
    private BlockPos basePos = null;
    private EndCrystal tempBreakCrystal = null;
    private EndCrystal breakCrystal = null;
    private Player currentTarget = null;
    private float tempDamage = 0.0f;
    public float lastDamage = 0.0f;
    private float breakDamage = 0.0f;
    private PosCandidate lockedCandidate1 = null;
    private PosCandidate lockedCandidate2 = null;
    private boolean useSecondPos = false;
    private Vec3 placeVec3d = null;
    private Vec3 curVec3d = null;
    private double currentFade = 0.0;
    private ExecutorService executor;
    private CompletableFuture<CalcResult> calcFuture;
    private final AtomicReference<CalcResult> bestResult = new AtomicReference<Object>(null);

    public AutoCrystal() {
        super("AutoCrystal", Module.Category.Combat);
        this.setChinese("\u81ea\u52a8\u6c34\u6676");
        INSTANCE = this;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void onThread() {
    }

    @Override
    public String getInfo() {
        if (this.currentTarget != null && this.lastDamage > 0.0f) {
            return this.df.format(this.lastDamage);
        }
        return null;
    }

    @Override
    public void onEnable() {
        this.firstBase = true;
        this.tempPos = null;
        this.tempBasePos = null;
        this.crystalPos = null;
        this.basePos = null;
        this.tempBreakCrystal = null;
        this.breakCrystal = null;
        this.currentTarget = null;
        this.tempDamage = 0.0f;
        this.lastDamage = 0.0f;
        this.breakDamage = 0.0f;
        this.lockedCandidate1 = null;
        this.lockedCandidate2 = null;
        this.useSecondPos = false;
        this.placeVec3d = null;
        this.curVec3d = null;
        this.instantPendingPos = null;
        this.mioPendingBreakId = -1;
        this.currentFade = 0.0;
        this.rotationTarget = null;
        this.placeRotationPoint = null;
        this.unifiedRotationPoint = null;
        this.noPosTimer.reset();
        this.posLockTimer.reset();
        this.placeTimer.reset();
        this.breakTimer.reset();
        this.baseTimer.reset();
        this.rotationResetTimer.reset();
        this.calcFuture = null;
        this.bestResult.set(null);
    }

    @Override
    public void onDisable() {
        this.tempPos = null;
        this.tempBasePos = null;
        this.crystalPos = null;
        this.basePos = null;
        this.tempBreakCrystal = null;
        this.breakCrystal = null;
        this.currentTarget = null;
        this.lockedCandidate1 = null;
        this.lockedCandidate2 = null;
        this.instantPendingPos = null;
        this.mioPendingBreakId = -1;
        this.rotationTarget = null;
        this.placeRotationPoint = null;
        this.unifiedRotationPoint = null;
        CombatUtil.modifyPos = null;
        CombatUtil.modifyBlockState = null;
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (this.rotationResetTimer.passedMs(300.0)) {
            this.rotationTarget = null;
        }
        if (this.rotationTarget != null) {
            event.setTarget(this.rotationTarget, 1.0f, 100.0f);
        }
    }

    private void setRotationTarget(Vec3 pos) {
        if (pos == null) {
            return;
        }
        this.rotationTarget = pos;
        this.rotationResetTimer.reset();
    }

    private void setPlaceRotation(Vec3 placeVec) {
        if (placeVec == null) {
            return;
        }
        this.setRotationTarget(placeVec);
        this.placeRotationPoint = placeVec;
    }

    private void setBreakRotation(Vec3 crystalVec, EndCrystal crystal) {
        if (this.placeRotationPoint != null) {
            this.setRotationTarget(this.placeRotationPoint);
        } else {
            this.setRotationTarget(new Vec3(crystal.getX(), crystal.getY() - this.extraHitboxExpand.getValue(), crystal.getZ()));
        }
    }

    private boolean canHitWithExpandedHitbox(EndCrystal crystal, Vec3 lookTarget) {
        AABB expandedBox = crystal.method_5829().method_1009(0.0, this.extraHitboxExpand.getValue(), 0.0).method_989(0.0, -this.extraHitboxExpand.getValue(), 0.0);
        Vec3 eyePos = AutoCrystal.mc.player.getEyePosition();
        float[] angles = this.calculateAngles(lookTarget);
        Vec3 lookVec = this.getVectorForRotation(angles[1], angles[0]);
        Vec3 reachVec = eyePos.method_1019(lookVec.method_1021(this.breakRange.getValue()));
        return expandedBox.method_992(eyePos, reachVec).isPresent();
    }

    private float[] calculateAngles(Vec3 target) {
        Vec3 eyePos = AutoCrystal.mc.player.getEyePosition();
        double dx = target.x - eyePos.x;
        double dy = target.y - eyePos.y;
        double dz = target.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new float[]{Mth.method_15393((float)yaw), Mth.method_15363((float)pitch, (float)-90.0f, (float)90.0f)};
    }

    private Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = Mth.method_15362((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f1 = Mth.method_15374((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f2 = -Mth.method_15362((float)(-pitch * ((float)Math.PI / 180)));
        float f3 = Mth.method_15374((float)(-pitch * ((float)Math.PI / 180)));
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (AutoCrystal.nullCheck()) {
            return;
        }
        if (event.isPost()) {
            return;
        }
        if (this.shouldPause()) {
            this.crystalPos = null;
            this.basePos = null;
            this.breakCrystal = null;
            this.currentTarget = null;
            this.rotationTarget = null;
            this.placeRotationPoint = null;
            this.unifiedRotationPoint = null;
            this.lockedCandidate1 = null;
            this.lockedCandidate2 = null;
            CombatUtil.modifyPos = null;
            CombatUtil.modifyBlockState = null;
            return;
        }
        this.consumeCalc();
        if (this.posLockTimer.passedMs(this.posUpdateDelay.getValue())) {
            this.posLockTimer.reset();
            this.calcCrystalPos();
            CombatUtil.modifyPos = null;
            CombatUtil.modifyBlockState = null;
        }
        this.placeRotationPoint = null;
        this.unifiedRotationPoint = null;
        this.doInteract();
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        boolean hasPos;
        if (AutoCrystal.nullCheck()) {
            return;
        }
        BlockPos cpos = this.crystalPos;
        if (cpos != null) {
            this.placeVec3d = cpos.method_10074().method_46558();
            this.noPosTimer.reset();
        }
        if (this.placeVec3d == null) {
            return;
        }
        boolean bl = hasPos = !this.noPosTimer.passed((long)(this.startFadeTime.getValue() * 1000.0));
        this.currentFade = this.fadeSpeed.getValue() >= 1.0 ? (hasPos ? 0.5 : 0.0) : AnimateUtil.animate(this.currentFade, hasPos ? 0.5 : 0.0, this.fadeSpeed.getValue() / 10.0);
        if (this.currentFade <= 0.001) {
            this.curVec3d = null;
            return;
        }
        if (this.curVec3d != null && this.sliderSpeed.getValue() < 1.0) {
            double sp = this.sliderSpeed.getValue() / 10.0;
            this.curVec3d = new Vec3(AnimateUtil.animate(this.curVec3d.x, this.placeVec3d.x, sp), AnimateUtil.animate(this.curVec3d.y, this.placeVec3d.y, sp), AnimateUtil.animate(this.curVec3d.z, this.placeVec3d.z, sp));
        } else {
            this.curVec3d = this.placeVec3d;
        }
        if (!this.render.getValue()) {
            return;
        }
        AABB box = new AABB(this.curVec3d, this.curVec3d);
        box = this.shrink.getValue() ? box.method_1014(this.currentFade) : box.method_1014(0.5);
        double alphaMultiplier = this.currentFade * 2.0;
        if (this.fillColor.booleanValue) {
            Render3DUtil.drawFill(matrixStack, box, ColorUtil.injectAlpha(this.fillColor.getValue(), (int)((double)this.fillColor.getValue().getAlpha() * alphaMultiplier)));
        }
        if (this.boxColor.booleanValue) {
            Render3DUtil.drawBox(matrixStack, box, ColorUtil.injectAlpha(this.boxColor.getValue(), (int)((double)this.boxColor.getValue().getAlpha() * alphaMultiplier)), this.lineWidth.getValueFloat());
        }
        if (this.renderText.getValue() && this.lastDamage > 0.0f && hasPos) {
            Render3DUtil.drawText3D(this.df.format(this.lastDamage), this.curVec3d, this.textColor.getValue());
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        ClientboundAddEntityPacket spawn;
        if (AutoCrystal.nullCheck()) {
            return;
        }
        if (this.shouldPause()) {
            return;
        }
        Packet<?> class_25962 = event.getPacket();
        if (class_25962 instanceof ClientboundAddEntityPacket && (spawn = (ClientboundAddEntityPacket)class_25962).getType() == EntityType.END_CRYSTAL) {
            this.handleMioSpawnBreak(spawn);
        }
    }

    private void handleMioSpawnBreak(ClientboundAddEntityPacket spawn) {
        boolean nearTarget;
        if (!this.instantBreak.getValue()) {
            return;
        }
        Player tgt = this.currentTarget;
        if (tgt == null || !tgt.isAlive()) {
            return;
        }
        Vec3 pos = new Vec3(spawn.getX(), spawn.getY(), spawn.getZ());
        if (AutoCrystal.mc.player.getEyePosition().method_1022(pos) > this.breakRange.getValue()) {
            return;
        }
        BlockPos placed = this.crystalPos;
        boolean nearPlaced = placed != null && pos.method_1025(placed.method_46558()) <= 4.0;
        boolean bl = nearTarget = pos.method_1025(tgt.position()) <= 2.25;
        if (!nearPlaced && !nearTarget) {
            return;
        }
        this.mioPendingBreakId = spawn.getId();
    }

    @EventListener
    private void onEntitySpawn(EntitySpawnedEvent event) {
        if (AutoCrystal.nullCheck()) {
            return;
        }
        if (this.shouldPause()) {
            return;
        }
        if (this.basePos != null) {
            return;
        }
        Entity class_12972 = event.getEntity();
        if (!(class_12972 instanceof EndCrystal)) {
            return;
        }
        EndCrystal crystal = (EndCrystal)class_12972;
        if (this.mioPendingBreakId != -1 && crystal.method_5628() == this.mioPendingBreakId) {
            this.mioPendingBreakId = -1;
            this.executeBreak(crystal);
            return;
        }
        BlockPos pos = this.crystalPos;
        if (pos == null) {
            return;
        }
        if (this.isInPlaceBox(pos, crystal.position())) {
            this.executeBreak(crystal);
        }
    }

    private void instantBreakNow(EndCrystal crystal) {
        if (crystal == null || !crystal.isAlive()) {
            return;
        }
        for (int i = 0; i < this.breakFactor.getValueInt(); ++i) {
            if (i == 0 && this.rotateOnBreak.getValue()) {
                Vec3 attackVec = this.getAttackVec(crystal.position());
                Vec3 breakVec = attackVec != null ? attackVec : crystal.position();
                this.setBreakRotation(breakVec, crystal);
            }
            mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)crystal, (boolean)AutoCrystal.mc.player.isShiftKeyDown()));
        }
        this.swingForCrystal();
        AutoCrystal.mc.player.resetAttackStrengthTicker();
        if (this.breakRemove.getValue()) {
            AutoCrystal.mc.level.method_2945(crystal.method_5628(), Entity.Entity$RemovalReason.KILLED);
        }
        this.breakTimer.reset();
    }

    private boolean shouldPause() {
        if (this.phasePause.getValue() && Phase.INSTANCE != null && Phase.INSTANCE.isOn()) {
            return true;
        }
        if (this.pushPause.getValue() && AutoPush.INSTANCE != null && AutoPush.INSTANCE.isOn()) {
            return true;
        }
        if (this.usingPause.getValue() && AutoCrystal.mc.player.isUsingItem()) {
            return true;
        }
        if (this.usingPause.getValue() && AutoCrystal.mc.options.keyUse.isDown()) {
            Item mainItem = AutoCrystal.mc.player.getMainHandItem().method_7909();
            Item offItem = AutoCrystal.mc.player.getOffhandItem().method_7909();
            if (this.isUseableItem(mainItem) || this.isUseableItem(offItem)) {
                return true;
            }
        }
        if (this.guiPause.getValue() && AutoCrystal.mc.screen instanceof AbstractContainerScreen) {
            return true;
        }
        return this.regearPause.getValue() && AutoRegear.INSTANCE != null && AutoRegear.INSTANCE.isOn();
    }

    private boolean isUseableItem(Item item) {
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.CHORUS_FRUIT || item == Items.POTION || item == Items.BOW || item == Items.CROSSBOW || item == Items.TRIDENT || item == Items.EXPERIENCE_BOTTLE;
    }

    public float calculateDamage(BlockPos pos, Player player, Player predict) {
        return ExplosionUtil.calculateDamage(new Vec3((double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.getZ() + 0.5), (LivingEntity)player, (LivingEntity)predict, 6.0f);
    }

    public float calculateDamage(Vec3 pos, Player player, Player predict) {
        return ExplosionUtil.calculateDamage(pos, (LivingEntity)player, (LivingEntity)predict, 6.0f);
    }

    private float calculateBaseDamage(BlockPos pos, Player player, Player predict) {
        return DamageUtils.overridingExplosionDamage((LivingEntity)player, (LivingEntity)predict, new Vec3((double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.getZ() + 0.5), 12.0f, pos.method_10074(), Blocks.OBSIDIAN.defaultBlockState());
    }

    private void calcCrystalPos() {
        if (this.calcFuture != null && !this.calcFuture.isDone()) {
            return;
        }
        this.calcFuture = CompletableFuture.supplyAsync(this::calcAndReturn, this.executor);
    }

    private void consumeCalc() {
        CalcResult result;
        if (this.calcFuture == null || !this.calcFuture.isDone()) {
            return;
        }
        try {
            result = this.calcFuture.get();
        }
        catch (Throwable ignored) {
            this.calcFuture = null;
            return;
        }
        this.calcFuture = null;
        this.bestResult.set(null);
        if (result != null && (result.tempPos != null || result.tempBasePos != null)) {
            this.tempDamage = result.tempDamage;
            this.tempBreakCrystal = result.tempBreakCrystal;
            this.tempPos = result.tempPos;
            this.tempBasePos = result.tempBasePos;
            this.currentTarget = result.currentTarget;
            this.lockedCandidate2 = result.lockedCandidate2;
            this.lastDamage = this.tempDamage;
            this.breakCrystal = this.tempBreakCrystal;
            this.crystalPos = this.tempPos;
            this.basePos = this.tempBasePos;
        }
    }

    private CalcResult calcAndReturn() {
        Vec3 attackVec;
        int enemyTicks;
        if (mc == null || AutoCrystal.mc.level == null || AutoCrystal.mc.player == null) {
            return new CalcResult(null, null, null, 0.0f, 0.0f, null, null);
        }
        EndCrystal bestBreakCrystal = null;
        float bestBreakDamage = 0.0f;
        BlockPos bestPos = null;
        float bestDamage = 0.0f;
        BlockPos bestBasePos = null;
        float bestBaseDamage = 0.0f;
        Player bestTarget = null;
        List<Player> enemies = CombatUtil.getEnemies(this.targetRange.getValueFloat());
        if (enemies.isEmpty()) {
            return new CalcResult(null, null, null, 0.0f, 0.0f, null, null);
        }
        boolean needBasePlace = this.basePlace.getValue() && this.getObsidianSlot() != -1;
        int n = enemyTicks = this.predict.getValue() ? this.predictTicks.getValueInt() : 0;
        if (this.autoPredict.getValue() && this.predict.getValue()) {
            for (Player enemy : enemies) {
                double hSpeed = Math.sqrt((enemy.getX() - enemy.xo) * (enemy.getX() - enemy.xo) + (enemy.getZ() - enemy.zo) * (enemy.getZ() - enemy.zo));
                int auto = (int)Math.round(hSpeed * 20.0 * 0.5);
                if (auto <= enemyTicks) continue;
                enemyTicks = auto;
            }
            enemyTicks = Math.min(enemyTicks, this.predictTicks.getValueInt() * 2);
        }
        int selfTicks = this.selfPredict.getValue() ? this.selfPredictTicks.getValueInt() : 0;
        ArrayList<PlayerEntityPredict> list = new ArrayList<PlayerEntityPredict>();
        for (Player enemy : enemies) {
            list.add(new PlayerEntityPredict(enemy, this.maxMotionXZ.getValue(), this.maxMotionY.getValue(), enemyTicks));
        }
        PlayerEntityPredict self = new PlayerEntityPredict((Player)AutoCrystal.mc.player, this.maxMotionXZ.getValue(), this.maxMotionY.getValue(), selfTicks);
        for (Entity entity : Aevum.THREAD.getEntities()) {
            float selfDamage;
            Vec3 attackVec2;
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive() || (attackVec2 = this.getAttackVec(crystal.position())) == null || !AutoCrystal.mc.player.method_6057((Entity)crystal) && AutoCrystal.mc.player.getEyePosition().method_1022(attackVec2) > this.wallRange.getValue() || (double)(selfDamage = this.calculateDamage(new BlockPosX(crystal.position().x - 0.5, crystal.position().y - 1.0, crystal.position().z - 0.5), self.player, self.predict)) > this.maxSelfDamage.getValue() || this.reserve.getValue() > 0.0 && selfDamage > AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() - this.reserve.getValueFloat()) continue;
            for (PlayerEntityPredict pap : list) {
                float damage = this.calculateDamage(new BlockPosX(crystal.position().x - 0.5, crystal.position().y - 1.0, crystal.position().z - 0.5), pap.player, pap.predict);
                if (!(damage > bestBreakDamage) || !((double)damage >= this.getMinDamage(pap.player)) || this.balance.getValue() && !((double)damage >= (double)selfDamage + this.balanceOffset.getValue())) continue;
                bestBreakDamage = damage;
                bestBreakCrystal = crystal;
                bestTarget = pap.player;
            }
        }
        for (BlockPos pos : BlockUtil.getSphere((float)this.placeRange.getValue() + 1.5f)) {
            if (!this.canPlaceCrystal(pos) || (attackVec = this.getAttackVec(pos.method_61082())) == null || !this.isMultipointVisible(pos) && AutoCrystal.mc.player.getEyePosition().method_1022(pos.method_61082()) > this.wallRange.getValue() || !this.canTouch(pos.method_10074(), this.placeRange.getValue())) continue;
            float selfDamage = this.calculateDamage(pos, self.player, self.predict);
            for (PlayerEntityPredict pap : list) {
                float damage = this.calculateDamage(pos, pap.player, pap.predict);
                if (!(damage > bestDamage) || !((double)selfDamage <= this.maxSelfDamage.getValue()) || !(this.reserve.getValue() <= 0.0) && !(selfDamage <= AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() - this.reserve.getValueFloat()) || !((double)damage >= this.getMinDamage(pap.player)) || this.balance.getValue() && !((double)damage >= (double)selfDamage + this.balanceOffset.getValue())) continue;
                bestTarget = pap.player;
                bestPos = pos;
                bestDamage = damage;
            }
        }
        if (needBasePlace && (double)bestDamage <= this.overrideMax.getValue()) {
            for (BlockPos pos : BlockUtil.getSphere((float)this.basePlaceRange.getValue() + 1.5f)) {
                if (!BlockUtil.canPlace(pos.method_10074()) || !this.canPlaceCrystalWithBase(pos) || (attackVec = this.getAttackVec(pos.method_61082())) == null || !this.isMultipointVisible(pos) && AutoCrystal.mc.player.getEyePosition().method_1022(pos.method_61082()) > this.wallRange.getValue() || !this.canTouch(pos.method_10074(), this.basePlaceRange.getValue())) continue;
                CombatUtil.modifyPos = pos.method_10074();
                CombatUtil.modifyBlockState = Blocks.OBSIDIAN.defaultBlockState();
                float selfDamage = this.calculateBaseDamage(pos, self.player, self.predict);
                for (PlayerEntityPredict pap : list) {
                    float damage;
                    if (this.onlyBelow.getValue() && (double)pos.method_10264() - 0.5 > pap.player.getY() || !((damage = this.calculateBaseDamage(pos, pap.player, pap.predict)) > bestDamage) || !(damage > bestBaseDamage) || !((double)selfDamage <= this.baseMax.getValue()) || !(this.reserve.getValue() <= 0.0) && !(selfDamage <= AutoCrystal.mc.player.getHealth() + AutoCrystal.mc.player.getAbsorptionAmount() - this.reserve.getValueFloat()) || !(damage >= EntityUtil.getHealth((Entity)pap.player)) && (!((double)damage >= this.baseMin.getValue()) || this.baseBalance.getValue() && !(damage >= selfDamage))) continue;
                    bestTarget = pap.player;
                    bestBaseDamage = damage;
                    bestBasePos = pos.method_10074();
                    bestPos = null;
                }
                CombatUtil.modifyPos = null;
                CombatUtil.modifyBlockState = null;
            }
        }
        return new CalcResult(bestPos, bestBasePos, bestBreakCrystal, bestDamage, bestBreakDamage, bestTarget, null);
    }

    private double getMinDamage(Player target) {
        if (this.facePlace.getValue() && target != null && (EntityUtil.getHealth((Entity)target) <= this.facePlaceHealth.getValueFloat() || this.getArmorDurability(target) <= this.facePlaceArmor.getValueFloat())) {
            return 0.6;
        }
        return this.minPDamage.getValue();
    }

    private double getMinDamage() {
        return this.getMinDamage(this.currentTarget);
    }

    private float getArmorDurability(Player p) {
        float min = 100.0f;
        for (ItemStack stack : p.getInventory().armor) {
            if (stack.method_7960()) continue;
            float dur = 100.0f - (float)((int)((1.0f - (float)(stack.getMaxDamage() - stack.getDamageValue()) / (float)stack.getMaxDamage()) * 100.0f));
            min = Math.min(min, dur);
        }
        return min;
    }

    private boolean breakBlockingCrystals(BlockPos placePos) {
        if (placePos == null) {
            return false;
        }
        ArrayList<BlockPos> checkPositions = new ArrayList<BlockPos>();
        checkPositions.add(placePos);
        checkPositions.add(placePos.method_10084());
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                if (dx == 0 && dz == 0) continue;
                BlockPos offset = placePos.method_10069(dx, 0, dz);
                checkPositions.add(offset);
                checkPositions.add(offset.method_10084());
            }
        }
        ArrayList<EndCrystal> blockingCrystals = new ArrayList<EndCrystal>();
        block2: for (Entity entity : Aevum.THREAD.getEntities()) {
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive()) continue;
            BlockPos crystalBlockPos = crystal.method_24515();
            for (BlockPos checkPos : checkPositions) {
                AABB checkBox = new AABB(checkPos);
                if (!crystal.method_5829().method_994(checkBox)) continue;
                if (blockingCrystals.contains(crystal)) continue block2;
                blockingCrystals.add(crystal);
                continue block2;
            }
        }
        if (blockingCrystals.isEmpty()) {
            return false;
        }
        EndCrystal targetCrystal = null;
        double minDist = Double.MAX_VALUE;
        for (EndCrystal crystal : blockingCrystals) {
            double dist;
            Vec3 attackVec = this.getAttackVec(crystal.position());
            if (attackVec == null || (dist = AutoCrystal.mc.player.getEyePosition().method_1022(attackVec)) > this.breakRange.getValue() || this.placeRotationPoint != null && !this.canHitWithExpandedHitbox(crystal, this.placeRotationPoint) || !AutoCrystal.mc.player.method_6057((Entity)crystal) && (!this.breakTWall.getValue() || dist > this.bWallRange.getValue()) || !(dist < minDist)) continue;
            minDist = dist;
            targetCrystal = crystal;
        }
        if (targetCrystal != null) {
            if (this.rotateOnBreak.getValue()) {
                Vec3 attackVec = this.getAttackVec(targetCrystal.position());
                Vec3 breakVec = attackVec != null ? attackVec : targetCrystal.position();
                this.setBreakRotation(breakVec, targetCrystal);
            }
            for (int i = 0; i < this.breakFactor.getValueInt(); ++i) {
                mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206(targetCrystal, (boolean)AutoCrystal.mc.player.isShiftKeyDown()));
            }
            this.swingForCrystal();
            AutoCrystal.mc.player.resetAttackStrengthTicker();
            if (this.breakRemove.getValue()) {
                AutoCrystal.mc.level.method_2945(targetCrystal.method_5628(), Entity.Entity$RemovalReason.KILLED);
            }
            this.breakTimer.reset();
            return true;
        }
        return false;
    }

    private void doInteract() {
        BlockPos activePlacePos;
        BlockPos cp = this.crystalPos;
        BlockPos bp = this.basePos;
        if (cp == null && this.breakCrystal == null && bp == null) {
            return;
        }
        if (bp != null) {
            Vec3 baseRotateVec = this.getBaseRotationVec(bp);
            if (baseRotateVec != null) {
                this.setRotationTarget(baseRotateVec);
            }
            this.doPlaceBase(bp);
            return;
        }
        if (this.breakCrystal != null) {
            if (this.rotateOnBreak.getValue()) {
                Vec3 attackVec = this.getAttackVec(this.breakCrystal.position());
                Vec3 breakVec = attackVec != null ? attackVec : this.breakCrystal.position();
                this.setBreakRotation(breakVec, this.breakCrystal);
            }
            this.doBreakCrystal(this.breakCrystal);
            this.breakCrystal = null;
        }
        if ((activePlacePos = cp) == null || !this.canTouch(activePlacePos.method_10074(), this.placeRange.getValue())) {
            this.rotationTarget = null;
            return;
        }
        if (this.breakBlockingCrystals(activePlacePos)) {
            return;
        }
        if (this.rotateOnPlace.getValue()) {
            this.setPlaceRotation(this.getPlaceRotationVec(activePlacePos));
        }
        this.doPlaceCrystal(activePlacePos);
    }

    private Vec3 getBaseRotationVec(BlockPos pos) {
        Direction side = BlockUtil.getClickSide(pos);
        if (side == null) {
            return pos.method_46558();
        }
        return pos.method_46558().method_1031((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5);
    }

    private void doPlaceBase(BlockPos obsPos) {
        BlockPos breakPos;
        if (!this.firstBase && !this.baseTimer.passedMs(this.baseDelay.getValue())) {
            return;
        }
        if (BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN || BlockUtil.getBlock(obsPos) == Blocks.BEDROCK) {
            return;
        }
        if (!BlockUtil.canPlace(obsPos)) {
            return;
        }
        if (this.detectMining.getValue() && (breakPos = PacketMine.getBreakPos()) != null && breakPos.equals((Object)obsPos)) {
            return;
        }
        int obbySlot = this.getObsidianSlot();
        if (obbySlot == -1) {
            return;
        }
        int oldSlot = AutoCrystal.mc.player.getInventory().selected;
        if (!this.baseInventorySwap.getValue()) {
            InventoryUtil.switchToSlot(obbySlot < 36 ? obbySlot : obbySlot - 36);
            BlockUtil.placeBlock(obsPos, false);
            InventoryUtil.switchToSlot(oldSlot);
        } else {
            boolean bypass;
            boolean bl = bypass = AntiCheat.INSTANCE != null && AntiCheat.INSTANCE.invSwapBypass.getValue();
            if (obbySlot >= 36) {
                InventoryUtil.inventorySwap(obbySlot, oldSlot);
                BlockUtil.placeBlock(obsPos, false);
                InventoryUtil.inventorySwap(obbySlot, oldSlot);
            } else if (bypass) {
                mc.getConnection().method_52787((Packet)new ServerboundPickItemPacket(obbySlot));
                BlockUtil.placeBlock(obsPos, false);
                InventoryUtil.switchToSlot(oldSlot);
            } else {
                AutoCrystal.mc.gameMode.method_2906(AutoCrystal.mc.player.containerMenu.containerId, obbySlot, oldSlot, ClickType.SWAP, (Player)AutoCrystal.mc.player);
                AutoCrystal.mc.player.getInventory().method_7381();
                BlockUtil.placeBlock(obsPos, false);
                AutoCrystal.mc.gameMode.method_2906(AutoCrystal.mc.player.containerMenu.containerId, obbySlot, oldSlot, ClickType.SWAP, (Player)AutoCrystal.mc.player);
                AutoCrystal.mc.player.getInventory().method_7381();
            }
            EntityUtil.syncInventory();
        }
        this.firstBase = false;
        this.baseTimer.reset();
    }

    private void doPlaceCrystal(BlockPos pos) {
        InteractionHand hand;
        BlockPos obsPos;
        Direction facing;
        if (!this.placeTimer.passedMs(this.placeDelay.getValue())) {
            return;
        }
        if (this.hasCrystalAt(pos)) {
            return;
        }
        if (!this.canPlaceCrystal(pos)) {
            return;
        }
        if (!this.canTouch(pos.method_10074(), this.placeRange.getValue())) {
            return;
        }
        boolean mainHand = AutoCrystal.mc.player.getMainHandItem().method_7909() == Items.END_CRYSTAL;
        boolean offHand = AutoCrystal.mc.player.getOffhandItem().method_7909() == Items.END_CRYSTAL;
        int crystalSlot = -1;
        if (!mainHand && !offHand && (crystalSlot = this.getCrystalSlot()) == -1) {
            return;
        }
        int oldSlot = AutoCrystal.mc.player.getInventory().selected;
        if (crystalSlot != -1) {
            InventoryUtil.inventorySwap(crystalSlot, oldSlot);
        }
        if ((facing = BlockUtil.getClickSide(obsPos = pos.method_10074())) == null) {
            facing = Direction.UP;
        }
        InteractionHand class_12682 = hand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (crystalSlot != -1) {
            hand = InteractionHand.MAIN_HAND;
        }
        BlockUtil.clickBlock(obsPos, facing, false, hand, SwingSide.All);
        if (crystalSlot != -1) {
            InventoryUtil.inventorySwap(crystalSlot, oldSlot);
        }
        this.placeTimer.reset();
    }

    private void removeLocalCrystalAt(BlockPos pos) {
        AABB box = new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 2), (double)(pos.getZ() + 1));
        for (Entity entity : Aevum.THREAD.getEntities()) {
            if (!(entity instanceof EndCrystal) || !entity.method_5829().method_994(box)) continue;
            AutoCrystal.mc.level.method_2945(entity.method_5628(), Entity.Entity$RemovalReason.KILLED);
        }
    }

    private void doBreakCrystal(EndCrystal crystal) {
        if (crystal == null || !crystal.isAlive()) {
            return;
        }
        if (!this.breakTimer.passedMs(this.breakDelay.getValue())) {
            return;
        }
        if (!this.canReachForBreak(crystal)) {
            return;
        }
        this.executeBreak(crystal);
    }

    private void swingForCrystal() {
        if (AutoCrystal.mc.player == null) {
            return;
        }
        InteractionHand swingHand = AutoCrystal.mc.player.getOffhandItem().method_7909() == Items.END_CRYSTAL ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        EntityUtil.swingHand(swingHand, SwingSide.All);
    }

    private void executeBreak(EndCrystal crystal) {
        if (crystal == null || !crystal.isAlive()) {
            return;
        }
        for (int i = 0; i < this.breakFactor.getValueInt(); ++i) {
            mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)crystal, (boolean)AutoCrystal.mc.player.isShiftKeyDown()));
        }
        this.swingForCrystal();
        AutoCrystal.mc.player.resetAttackStrengthTicker();
        if (this.breakRemove.getValue()) {
            AutoCrystal.mc.level.method_2945(crystal.method_5628(), Entity.Entity$RemovalReason.KILLED);
        }
        this.breakTimer.reset();
        BlockPos cp = this.crystalPos;
        if (cp != null) {
            this.doPlaceCrystal(cp);
        }
    }

    private Vec3 getAttackVec(Vec3 feetPos) {
        return MathUtil.getPointToBoxFromBottom(AutoCrystal.mc.player.getEyePosition(), feetPos, this.breakRange.getValue(), 2.0, 0.1);
    }

    public boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.method_10074();
        if (!this.forcePlace.getValue() && BlockUtil.getBlock(obsPos) != Blocks.BEDROCK && BlockUtil.getBlock(obsPos) != Blocks.OBSIDIAN) {
            return false;
        }
        if (BlockUtil.getClickSideStrict(obsPos) == null) {
            return false;
        }
        if (!AutoCrystal.mc.level.isEmptyBlock(pos) && BlockUtil.getBlock(pos) != Blocks.FIRE) {
            return false;
        }
        if (ClientSetting.INSTANCE.lowVersion.getValue() && !AutoCrystal.mc.level.isEmptyBlock(pos.method_10084())) {
            return false;
        }
        if (!this.noEntityBlockCrystal(pos)) {
            return false;
        }
        return this.noEntityBlockCrystal(pos.method_10084());
    }

    public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
        if (pos == null) {
            return false;
        }
        if (AutoCrystal.mc.level == null) {
            return false;
        }
        BlockPos obs = pos.method_10074();
        if (!this.forcePlace.getValue() && BlockUtil.getBlock(obs) != Blocks.BEDROCK && BlockUtil.getBlock(obs) != Blocks.OBSIDIAN) {
            return false;
        }
        if (!AutoCrystal.mc.level.isEmptyBlock(pos) && BlockUtil.getBlock(pos) != Blocks.FIRE) {
            return false;
        }
        return this.noEntityBlockCrystal(pos);
    }

    private boolean canPlaceCrystalWithBase(BlockPos pos) {
        if (!AutoCrystal.mc.level.isEmptyBlock(pos) && BlockUtil.getBlock(pos) != Blocks.FIRE) {
            return false;
        }
        if (ClientSetting.INSTANCE.lowVersion.getValue() && !AutoCrystal.mc.level.isEmptyBlock(pos.method_10084())) {
            return false;
        }
        if (!this.noEntityBlockCrystal(pos)) {
            return false;
        }
        return this.noEntityBlockCrystal(pos.method_10084());
    }

    private boolean noEntityBlockCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.getEntities(new AABB(pos))) {
            if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof EndCrystal) continue;
            return false;
        }
        return true;
    }

    private boolean behindWall(BlockPos pos, Vec3 attackVec) {
        Vec3 crystalEyePos = new Vec3((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 1.7, (double)pos.getZ() + 0.5);
        BlockHitResult result = AutoCrystal.mc.level.method_17742(new ClipContext(AutoCrystal.mc.player.getEyePosition(), crystalEyePos, ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.NONE, (Entity)AutoCrystal.mc.player));
        if (result == null || result.method_17783() == HitResult.HitResult$Type.MISS) {
            return false;
        }
        return AutoCrystal.mc.player.getEyePosition().method_1022(attackVec) > this.wallRange.getValue();
    }

    private boolean isMultipointVisible(BlockPos pos) {
        for (Vec3 v : this.getMultiPointList(pos)) {
            if (!this.isTraceVisible(AutoCrystal.mc.player.getEyePosition(), v)) continue;
            return true;
        }
        return false;
    }

    private boolean isTraceVisible(Vec3 start, Vec3 end) {
        if (end.method_1022(start) > 256.0) {
            return false;
        }
        BlockHitResult hit = AutoCrystal.mc.level.method_17742(new ClipContext(start, end, ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.NONE, (Entity)AutoCrystal.mc.player));
        return hit.method_17783() == HitResult.HitResult$Type.MISS || hit.getLocation().equals((Object)end) || hit.method_17777().equals((Object)BlockPos.method_49638((Position)end));
    }

    private List<Vec3> getMultiPointList(BlockPos pos) {
        List<Vec3> base = List.of(new Vec3(0.05, 0.05, 0.05), new Vec3(0.05, 0.05, 0.95), new Vec3(0.05, 0.95, 0.05), new Vec3(0.05, 0.95, 0.95), new Vec3(0.95, 0.05, 0.05), new Vec3(0.95, 0.05, 0.95), new Vec3(0.95, 0.95, 0.05), new Vec3(0.95, 0.95, 0.95));
        ArrayList<Vec3> list = new ArrayList<Vec3>();
        for (Vec3 v : base) {
            list.add(v.method_1031((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ()));
        }
        return list;
    }

    private boolean canTouch(BlockPos pos, double range) {
        Direction side = BlockUtil.getClickSideStrict(pos);
        if (side == null) {
            return false;
        }
        return pos.method_46558().method_1031((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5).method_1022(AutoCrystal.mc.player.getEyePosition()) <= range;
    }

    private boolean canReachForBreak(EndCrystal crystal) {
        Vec3 attackVec = this.getAttackVec(crystal.position());
        if (attackVec == null) {
            return false;
        }
        double dist = AutoCrystal.mc.player.getEyePosition().method_1022(attackVec);
        if (dist > this.breakRange.getValue()) {
            return false;
        }
        if (this.placeRotationPoint != null && !this.canHitWithExpandedHitbox(crystal, this.placeRotationPoint)) {
            return false;
        }
        if (!AutoCrystal.mc.player.method_6057((Entity)crystal)) {
            if (this.breakTWall.getValue()) {
                return dist <= this.bWallRange.getValue();
            }
            return false;
        }
        return true;
    }

    private boolean hasCrystalAt(BlockPos pos) {
        AABB box = new AABB((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.getZ(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 2), (double)(pos.getZ() + 1));
        for (Entity entity : Aevum.THREAD.getEntities()) {
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive() || !crystal.method_5829().method_994(box)) continue;
            return true;
        }
        return false;
    }

    private boolean isInPlaceBox(BlockPos placePos, Vec3 crystalPos) {
        AABB box = new AABB((double)placePos.method_10263(), (double)placePos.method_10264(), (double)placePos.getZ(), (double)(placePos.method_10263() + 1), (double)(placePos.method_10264() + 2), (double)(placePos.getZ() + 1));
        return box.method_1006(crystalPos);
    }

    private Vec3 getPlaceRotationVec(BlockPos pos) {
        BlockPos obsPos = pos.method_10074();
        Direction side = BlockUtil.getClickSide(obsPos);
        if (side == null) {
            return obsPos.method_46558().method_1031(0.0, 0.5, 0.0);
        }
        return obsPos.method_46558().method_1031((double)side.getNormal().method_10263() * 0.5, (double)side.getNormal().method_10264() * 0.5, (double)side.getNormal().getZ() * 0.5);
    }

    private int getCrystalSlot() {
        return InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
    }

    private int rawInventorySlot(int slot) {
        return slot >= 36 ? slot - 36 : slot;
    }

    private int getObsidianSlot() {
        return this.baseInventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
    }

    public BlockPos getPlacePos() {
        return this.crystalPos;
    }

    public float getCurrentDamage() {
        return this.lastDamage;
    }

    public void resetBreakTimer() {
        this.breakTimer.reset();
    }

    private static enum Page {
        Place,
        Break,
        Base,
        Rotation,
        RayTrace,
        Prediction,
        Misc,
        Render;

    }

    private static class PosCandidate {
        final BlockPos pos;
        final Player enemy;
        final float enemyDamage;
        final float selfDamage;
        final boolean isBase;

        PosCandidate(BlockPos pos, Player enemy, float enemyDamage, float selfDamage, boolean isBase) {
            this.pos = pos;
            this.enemy = enemy;
            this.enemyDamage = enemyDamage;
            this.selfDamage = selfDamage;
            this.isBase = isBase;
        }
    }

    private static class CalcResult {
        final BlockPos tempPos;
        final BlockPos tempBasePos;
        final EndCrystal tempBreakCrystal;
        final float tempDamage;
        final float breakDamage;
        final Player currentTarget;
        final PosCandidate lockedCandidate2;

        CalcResult(BlockPos tempPos, BlockPos tempBasePos, EndCrystal tempBreakCrystal, float tempDamage, float breakDamage, Player currentTarget, PosCandidate lockedCandidate2) {
            this.tempPos = tempPos;
            this.tempBasePos = tempBasePos;
            this.tempBreakCrystal = tempBreakCrystal;
            this.tempDamage = tempDamage;
            this.breakDamage = breakDamage;
            this.currentTarget = currentTarget;
            this.lockedCandidate2 = lockedCandidate2;
        }
    }

    private static class PlayerEntityPredict {
        final Player player;
        final Player predict;
        final Vec3 predictedPos;
        final AABB predictedBox;

        PlayerEntityPredict(Player player, double maxMotionXZ, double maxMotionY, int ticks) {
            this.player = player;
            double mxz = maxMotionXZ;
            double motionX = Mth.method_15350((double)(player.getX() - player.xo), (double)(-mxz), (double)mxz);
            double motionY = Mth.method_15350((double)(player.getY() - player.yo), (double)(-maxMotionY), (double)maxMotionY);
            double motionZ = Mth.method_15350((double)(player.getZ() - player.zo), (double)(-mxz), (double)mxz);
            double px = player.getX() + motionX * (double)ticks;
            double py = player.getY() + motionY * (double)ticks;
            double pz = player.getZ() + motionZ * (double)ticks;
            this.predictedPos = new Vec3(px, py, pz);
            double w = (double)player.getBbWidth() / 2.0;
            double h = player.getBbHeight();
            this.predictedBox = new AABB(px - w, py, pz - w, px + w, py + h, pz + w);
            this.predict = player;
        }
    }
}

