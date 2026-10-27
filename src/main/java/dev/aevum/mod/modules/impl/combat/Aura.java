/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Mob
 *  net.minecraft.LivingEntity
 *  net.minecraft.Animal
 *  net.minecraft.Slime
 *  net.minecraft.Villager
 *  net.minecraft.Player
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.WanderingTrader
 *  net.minecraft.PoseStack
 *  net.minecraft.Attributes
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.RotationEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Animation;
import dev.aevum.api.utils.math.Easing;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.JelloUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.asm.accessors.IEntity;
import dev.aevum.asm.accessors.ILivingEntity;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.impl.movement.Velocity;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.enums.Timing;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Mob;
import net.minecraft.LivingEntity;
import net.minecraft.Animal;
import net.minecraft.Slime;
import net.minecraft.Villager;
import net.minecraft.Player;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.WanderingTrader;
import net.minecraft.PoseStack;
import net.minecraft.Attributes;

public class Aura
extends Module {
    public static Aura INSTANCE;
    public static Entity target;
    public final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
    public final SliderSetting range = this.add(new SliderSetting("Range", 6.0, (double)0.1f, 7.0, () -> this.page.getValue() == Page.General));
    private final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0, (double)0.1f, 14.0, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Cooldown> cooldownMode = this.add(new EnumSetting<Cooldown>("CooldownMode", Cooldown.Delay, () -> this.page.getValue() == Page.General));
    private final BooleanSetting reset = this.add(new BooleanSetting("Reset", true, () -> this.page.getValue() == Page.General && this.cooldownMode.is(Cooldown.Delay)));
    private final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.All, () -> this.page.getValue() == Page.General));
    private final SliderSetting hurtTime = this.add(new SliderSetting("HurtTime", 10.0, 0.0, 10.0, 1.0, () -> this.page.getValue() == Page.General));
    private final SliderSetting cooldown = this.add(new SliderSetting("Cooldown", 1.1f, 0.0, 1.2f, 0.01, () -> this.page.getValue() == Page.General));
    private final SliderSetting wallRange = this.add(new SliderSetting("WallRange", 6.0, (double)0.1f, 7.0, () -> this.page.getValue() == Page.General));
    private final BooleanSetting whileEating = this.add(new BooleanSetting("WhileUsing", true, () -> this.page.getValue() == Page.General));
    private final BooleanSetting weaponOnly = this.add(new BooleanSetting("WeaponOnly", true, () -> this.page.getValue() == Page.General));
    private final EnumSetting<Timing> timing = this.add(new EnumSetting<Timing>("Timing", Timing.All, () -> this.page.getValue() == Page.General));
    private final BooleanSetting Players = this.add(new BooleanSetting("Players", true, () -> this.page.getValue() == Page.Target).setParent());
    private final BooleanSetting armorLow = this.add(new BooleanSetting("ArmorLow", true, () -> this.page.getValue() == Page.Target && this.Players.isOpen()));
    private final BooleanSetting Mobs = this.add(new BooleanSetting("Mobs", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Animals = this.add(new BooleanSetting("Animals", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Villagers = this.add(new BooleanSetting("Villagers", true, () -> this.page.getValue() == Page.Target));
    private final BooleanSetting Slimes = this.add(new BooleanSetting("Slimes", true, () -> this.page.getValue() == Page.Target));
    private final EnumSetting<TargetMode> targetMode = this.add(new EnumSetting<TargetMode>("Filter", TargetMode.DISTANCE, () -> this.page.getValue() == Page.Target));
    private final EnumSetting<TargetESP> mode = this.add(new EnumSetting<TargetESP>("TargetESP", TargetESP.Fill, () -> this.page.getValue() == Page.Render));
    private final SliderSetting animationTime = this.add(new SliderSetting("AnimationTime", 200.0, 0.0, 2000.0, 1.0, () -> this.page.getValue() == Page.Render));
    private final EnumSetting<Easing> ease = this.add(new EnumSetting<Easing>("Ease", Easing.CubicInOut, () -> this.page.getValue() == Page.Render));
    private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 50), () -> this.page.getValue() == Page.Render));
    private final ColorSetting outlineColor = this.add(new ColorSetting("OutlineColor", new Color(255, 255, 255, 50), () -> this.page.getValue() == Page.Render));
    private final ColorSetting hitColor = this.add(new ColorSetting("HitColor", new Color(255, 255, 255, 150), () -> this.page.getValue() == Page.Render));
    private final ColorSetting hitOutlineColor = this.add(new ColorSetting("HitOutlineColor", new Color(255, 255, 255, 150), () -> this.page.getValue() == Page.Render));
    private final Animation animation = new Animation();
    private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, () -> this.page.getValue() == Page.Rotate));
    private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", false, () -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate).setParent());
    private final BooleanSetting whenElytra = this.add(new BooleanSetting("FallFlying", true, () -> this.rotate.isOpen() && this.yawStep.isOpen() && this.page.getValue() == Page.Rotate));
    private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.05, 0.0, 1.0, 0.01, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final BooleanSetting checkFov = this.add(new BooleanSetting("OnlyLooking", true, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting fov = this.add(new SliderSetting("Fov", 20.0, 0.0, 360.0, 0.1, () -> this.checkFov.getValue() && this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final SliderSetting priority = this.add(new SliderSetting("Priority", 10, 0, 100, () -> this.page.getValue() == Page.Rotate && this.yawStep.isOpen()));
    private final Timer tick = new Timer();
    public Vec3 directionVec = null;

    public Aura() {
        super("Aura", Module.Category.Combat);
        this.setChinese("\u6740\u622e\u5149\u73af");
        INSTANCE = this;
    }

    public static void doRender(PoseStack stack, float partialTicks, Entity entity, Color color, Color outlineColor, TargetESP mode) {
        switch (mode.ordinal()) {
            case 1: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entity.xOld, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.yOld, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.zOld, entity.getZ(), (double)partialTicks))).method_1009(0.0, 0.1, 0.0), color, outlineColor, true, true);
                break;
            }
            case 0: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entity.xOld, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.yOld, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.zOld, entity.getZ(), (double)partialTicks))).method_1009(0.0, 0.1, 0.0), color, outlineColor, false, true);
                break;
            }
            case 2: {
                JelloUtil.drawJello(stack, entity, color);
                break;
            }
            case 3: {
                Render3DUtil.drawTargetEsp(stack, entity, color);
            }
        }
    }

    public static float getAttackCooldownProgressPerTick() {
        return (float)(1.0 / Aura.mc.player.method_45325(Attributes.ATTACK_SPEED) * 20.0);
    }

    @Override
    public void onRender3D(PoseStack matrixStack) {
        if (target != null) {
            this.doRender(matrixStack, mc.getTimer().method_60637(true), target, this.mode.getValue());
        }
    }

    public void doRender(PoseStack stack, float partialTicks, Entity entity, TargetESP mode) {
        switch (mode.ordinal()) {
            case 1: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entity.xOld, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.yOld, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.zOld, entity.getZ(), (double)partialTicks))).method_1009(0.0, 0.1, 0.0), ColorUtil.fadeColor(this.color.getValue(), this.hitColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), ColorUtil.fadeColor(this.outlineColor.getValue(), this.hitOutlineColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), true, true);
                break;
            }
            case 0: {
                Render3DUtil.draw3DBox(stack, ((IEntity)entity).getDimensions().method_30757(new Vec3(MathUtil.interpolate(entity.xOld, entity.getX(), (double)partialTicks), MathUtil.interpolate(entity.yOld, entity.getY(), (double)partialTicks), MathUtil.interpolate(entity.zOld, entity.getZ(), (double)partialTicks))).method_1009(0.0, 0.1, 0.0), ColorUtil.fadeColor(this.color.getValue(), this.hitColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), ColorUtil.fadeColor(this.outlineColor.getValue(), this.hitOutlineColor.getValue(), this.animation.get(0.0, this.animationTime.getValueInt(), this.ease.getValue())), false, true);
                break;
            }
            case 2: {
                JelloUtil.drawJello(stack, entity, this.color.getValue());
                break;
            }
            case 3: {
                Render3DUtil.drawTargetEsp(stack, target, this.color.getValue());
            }
        }
    }

    @Override
    public String getInfo() {
        return target == null ? null : target.method_5477().getString();
    }

    @EventListener
    public void onTick(ClientTickEvent event) {
        if (Aura.nullCheck()) {
            return;
        }
        if (this.timing.is(Timing.Pre) && event.isPost() || this.timing.is(Timing.Post) && event.isPre()) {
            return;
        }
        if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon((Player)Aura.mc.player)) {
            target = null;
            return;
        }
        target = this.getTarget(this.range.getValueFloat());
        if (target == null) {
            target = this.getTarget(this.targetRange.getValueFloat());
            return;
        }
        this.doAura();
    }

    @EventListener
    public void onRotate(RotationEvent event) {
        if (target != null && this.rotate.getValue() && this.shouldYawStep()) {
            this.directionVec = this.getAttackVec(target);
            event.setTarget(this.directionVec, this.steps.getValueFloat(), this.priority.getValueFloat());
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        Packet<?> packet;
        if (this.reset.getValue() && ((packet = event.getPacket()) instanceof ServerboundSwingPacket || packet instanceof ServerboundInteractPacket && Criticals.getInteractType((ServerboundInteractPacket)packet) == ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK)) {
            this.tick.reset();
        }
    }

    private boolean check() {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return false;
        }
        int at = (int)(this.tick.getMs() / 50L);
        if (this.cooldownMode.getValue() == Cooldown.Vanilla) {
            at = ((ILivingEntity)Aura.mc.player).getLastAttackedTicks();
        }
        if (!((double)Math.max((float)(at = (int)((float)at * Aevum.SERVER.getTPSFactor())) / Aura.getAttackCooldownProgressPerTick(), 0.0f) >= this.cooldown.getValue())) {
            return false;
        }
        Entity entity = target;
        if (entity instanceof LivingEntity) {
            LivingEntity entity2 = (LivingEntity)entity;
            if ((double)entity2.hurtTime > this.hurtTime.getValue()) {
                return false;
            }
        }
        return this.whileEating.getValue() || !Aura.mc.player.isUsingItem();
    }

    private void doAura() {
        Vec3 hitVec;
        if (!this.check()) {
            return;
        }
        if (this.rotate.getValue() && !this.faceVector(hitVec = this.getAttackVec(target))) {
            return;
        }
        this.animation.to = 1.0;
        this.animation.from = 1.0;
        mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)target, (boolean)Aura.mc.player.isShiftKeyDown()));
        Aura.mc.player.resetAttackStrengthTicker();
        EntityUtil.swingHand(InteractionHand.MAIN_HAND, this.swingMode.getValue());
        this.tick.reset();
        if (this.rotate.getValue() && !this.shouldYawStep()) {
            Aevum.ROTATION.snapBack();
        }
    }

    private Vec3 getAttackVec(Entity entity) {
        return MathUtil.getClosestPointToBox(Aura.mc.player.getEyePosition(), entity.method_5829());
    }

    private boolean shouldYawStep() {
        if (!this.whenElytra.getValue() && (Aura.mc.player.isFallFlying() || ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying())) {
            return false;
        }
        return this.yawStep.getValue() && !Velocity.INSTANCE.noRotation();
    }

    public boolean faceVector(Vec3 directionVec) {
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

    public Entity getTarget(double range) {
        Entity target = null;
        double getDistance = range;
        double maxHealth = 36.0;
        for (Entity entity : Aevum.THREAD.getEntities()) {
            if (!this.isEnemy(entity)) continue;
            Vec3 hitVec = this.getAttackVec(entity);
            if (Aura.mc.player.getEyePosition().method_1022(hitVec) > range || !Aura.mc.player.method_6057(entity) && Aura.mc.player.getEyePosition().method_1022(hitVec) > this.wallRange.getValue() || !CombatUtil.isValid(entity)) continue;
            if (target == null) {
                target = entity;
                getDistance = Aura.mc.player.getEyePosition().method_1022(hitVec);
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (this.armorLow.getValue() && entity instanceof Player && EntityUtil.isArmorLow((Player)entity, 10)) {
                target = entity;
                break;
            }
            if (this.targetMode.getValue() == TargetMode.HEALTH && (double)EntityUtil.getHealth(entity) < maxHealth) {
                target = entity;
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (this.targetMode.getValue() != TargetMode.DISTANCE || !(Aura.mc.player.getEyePosition().method_1022(hitVec) < getDistance)) continue;
            target = entity;
            getDistance = Aura.mc.player.getEyePosition().method_1022(hitVec);
        }
        return target;
    }

    private boolean isEnemy(Entity entity) {
        if (entity instanceof Slime) {
            return this.Slimes.getValue();
        }
        if (entity instanceof Player) {
            return this.Players.getValue();
        }
        if (entity instanceof Villager || entity instanceof WanderingTrader) {
            return this.Villagers.getValue();
        }
        if (entity instanceof Animal) {
            return this.Animals.getValue();
        }
        if (entity instanceof Mob) {
            return this.Mobs.getValue();
        }
        return false;
    }

    public static enum Page {
        General,
        Rotate,
        Target,
        Render;

    }

    public static enum Cooldown {
        Vanilla,
        Delay,
        VerticalSplit,
        BlinkBuffer,
        MotionSpoof,
        WindCharge,
        Hybrid,
        TheVoid,
        VoidMace;

    }

    private static enum TargetMode {
        DISTANCE,
        HEALTH;

    }

    public static enum TargetESP {
        Fill,
        Box,
        Jello,
        ThunderHack,
        None;

    }

    public static enum Mode {
        Mace,
        Axe,
        Sword;

    }
}

