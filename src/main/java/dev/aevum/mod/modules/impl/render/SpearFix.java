/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
 *  net.minecraft.SimpleSoundInstance
 *  net.minecraft.SoundInstance
 *  net.minecraft.HumanoidArm
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Registry
 *  net.minecraft.ResourceLocation
 *  net.minecraft.Minecraft
 *  net.minecraft.SoundEvent
 *  net.minecraft.Mth
 *  net.minecraft.PoseStack
 *  net.minecraft.Axis
 *  net.minecraft.BuiltInRegistries
 *  net.minecraft.ItemDisplayContext
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.Locale;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.SimpleSoundInstance;
import net.minecraft.SoundInstance;
import net.minecraft.HumanoidArm;
import net.minecraft.LivingEntity;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Registry;
import net.minecraft.ResourceLocation;
import net.minecraft.Minecraft;
import net.minecraft.SoundEvent;
import net.minecraft.Mth;
import net.minecraft.PoseStack;
import net.minecraft.Axis;
import net.minecraft.BuiltInRegistries;
import net.minecraft.ItemDisplayContext;

public class SpearFix
extends Module {
    public static SpearFix INSTANCE;
    public final SliderSetting strength = this.add(new SliderSetting("Strength", 1.0, 0.0, 1.0, 0.01));
    public final BooleanSetting itemId = this.add(new BooleanSetting("ItemId", true));
    public final BooleanSetting itemName = this.add(new BooleanSetting("ItemName", true));
    public final BooleanSetting firstPerson = this.add(new BooleanSetting("FirstPerson", true));
    public final BooleanSetting thirdPerson = this.add(new BooleanSetting("ThirdPerson", true));
    public final BooleanSetting suppressEquip = this.add(new BooleanSetting("SuppressEquip", true));
    public final BooleanSetting useAnimation = this.add(new BooleanSetting("UseAnimation", true));
    public final BooleanSetting itemTransform = this.add(new BooleanSetting("ItemTransform", true));
    public final BooleanSetting swingDuration = this.add(new BooleanSetting("SwingDuration", true));
    public final BooleanSetting grimSafe = this.add(new BooleanSetting("GrimSafe", true));
    public final BooleanSetting allowSprint = this.add(new BooleanSetting("AllowSprint", false, () -> !this.grimSafe.getValue()));
    public final BooleanSetting viaUseFallback = this.add(new BooleanSetting("ViaUseFallback", true));
    public final BooleanSetting model = this.add(new BooleanSetting("Model", true));
    public final BooleanSetting sounds = this.add(new BooleanSetting("Sounds", true));
    public final BooleanSetting hitSounds = this.add(new BooleanSetting("HitSounds", true));
    public final BooleanSetting useSounds = this.add(new BooleanSetting("UseSounds", true));
    public final SliderSetting swingTicks = this.add(new SliderSetting("SwingTicks", 18, 6, 30));
    public final SliderSetting chargeDelay = this.add(new SliderSetting("ChargeDelay", 12, 0, 30));
    public final SliderSetting forwardMovement = this.add(new SliderSetting("ForwardMove", 0.38, 0.0, 1.0, 0.01));
    private static final ResourceLocation SPEAR_ATTACK_ID;
    private static final ResourceLocation SPEAR_HIT_ID;
    private static final ResourceLocation SPEAR_USE_ID;
    private static final ResourceLocation SPEAR_LUNGE_ID;
    private static final ResourceLocation SPEAR_WOOD_ATTACK_ID;
    private static final ResourceLocation SPEAR_WOOD_HIT_ID;
    private static final ResourceLocation SPEAR_WOOD_USE_ID;
    private static final ResourceLocation[] SPEAR_MODELS;
    private static SoundEvent SPEAR_ATTACK;
    private static SoundEvent SPEAR_HIT;
    private static SoundEvent SPEAR_USE;
    private static SoundEvent SPEAR_LUNGE;
    private static SoundEvent SPEAR_WOOD_ATTACK;
    private static SoundEvent SPEAR_WOOD_HIT;
    private static SoundEvent SPEAR_WOOD_USE;
    private long lastAttackSound;
    private long lastUseSound;
    private long lastLungeSound;
    private int fallbackUseTicks;
    private int fallbackKeepAliveTicks;

    public SpearFix() {
        super("SpearFix", Module.Category.Render);
        this.setChinese("\u957f\u77db\u4fee\u8865");
        INSTANCE = this;
        SpearFix.registerSounds();
        SpearFix.registerModels();
    }

    public static void registerModels() {
        try {
            ModelLoadingPlugin.register(context -> {
                ArrayList<ResourceLocation> existing = new ArrayList<ResourceLocation>();
                ClassLoader cl = SpearFix.class.getClassLoader();
                for (ResourceLocation id : SPEAR_MODELS) {
                    if (cl.getResource("assets/" + id.method_12836() + "/models/" + id.method_12832() + ".json") == null) continue;
                    existing.add(id);
                }
                if (!existing.isEmpty()) {
                    context.addModels(existing);
                }
            });
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void registerSounds() {
        SPEAR_ATTACK = SpearFix.registerSound(SPEAR_ATTACK_ID);
        SPEAR_HIT = SpearFix.registerSound(SPEAR_HIT_ID);
        SPEAR_USE = SpearFix.registerSound(SPEAR_USE_ID);
        SPEAR_LUNGE = SpearFix.registerSound(SPEAR_LUNGE_ID);
        SPEAR_WOOD_ATTACK = SpearFix.registerSound(SPEAR_WOOD_ATTACK_ID);
        SPEAR_WOOD_HIT = SpearFix.registerSound(SPEAR_WOOD_HIT_ID);
        SPEAR_WOOD_USE = SpearFix.registerSound(SPEAR_WOOD_USE_ID);
    }

    private static SoundEvent registerSound(ResourceLocation Identifier) {
        try {
            return (SoundEvent)Registry.method_10226((Registry)BuiltInRegistries.SOUND_EVENT, (String)Identifier.toString(), (Object)SoundEvent.method_47908((ResourceLocation)Identifier));
        }
        catch (RuntimeException ex) {
            SoundEvent ResourceLocation$Serializer = (SoundEvent)BuiltInRegistries.SOUND_EVENT.method_10223(Identifier);
            return ResourceLocation$Serializer == null ? SoundEvent.method_47908((ResourceLocation)Identifier) : ResourceLocation$Serializer;
        }
    }

    public boolean shouldAnimate(ItemStack ItemStack) {
        return ItemStack != null && !ItemStack.method_7960() && !this.isOff() && (this.itemId.getValue() && BuiltInRegistries.ITEM.method_10221((Object)ItemStack.method_7909()).method_12832().toLowerCase(Locale.ROOT).contains("spear") || this.itemName.getValue() && ItemStack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("spear"));
    }

    public boolean shouldAnimateFirstPerson(ItemStack ItemStack) {
        return this.firstPerson.getValue() && this.shouldAnimate(ItemStack);
    }

    public boolean shouldAnimateThirdPerson(ItemStack ItemStack) {
        return this.thirdPerson.getValue() && this.shouldAnimate(ItemStack);
    }

    public boolean shouldSuppressEquip(ItemStack ItemStack, float n) {
        return this.suppressEquip.getValue() && this.shouldAnimateFirstPerson(ItemStack) && n > 0.0f;
    }

    public boolean shouldUseAnimate(ItemStack ItemStack) {
        return this.useAnimation.getValue() && this.shouldAnimateFirstPerson(ItemStack);
    }

    public boolean isFallbackUseActive() {
        return this.viaUseFallback.getValue() && this.fallbackUseTicks > 0;
    }

    public float getFallbackUseTicks(float n) {
        return Math.max(0.0f, (float)this.fallbackUseTicks + n);
    }

    public boolean shouldTransformHeldItem(ItemStack ItemStack) {
        return this.itemTransform.getValue() && this.shouldAnimateThirdPerson(ItemStack);
    }

    public int getSwingTicks(LivingEntity LivingEntity) {
        return this.swingDuration.getValue() && LivingEntity != null && this.shouldAnimate(LivingEntity.getMainHandItem()) ? Math.max(1, this.getSpearProfile(LivingEntity.getMainHandItem()).swingTicks()) : -1;
    }

    public boolean shouldAllowSprint(LivingEntity LivingEntity) {
        return !this.grimSafe.getValue() && this.allowSprint.getValue() && LivingEntity != null && LivingEntity.isUsingItem() && this.shouldAnimate(LivingEntity.getUseItem());
    }

    public boolean shouldReplaceModel(ItemStack ItemStack) {
        return this.model.getValue() && this.shouldAnimate(ItemStack);
    }

    public ResourceLocation getModelId(ItemStack ItemStack, ItemDisplayContext ModelPredicateProvider) {
        return ResourceLocation.method_60655((String)"minecraft", (String)("item/" + this.getSpearMaterial(ItemStack) + "_spear" + (ModelPredicateProvider == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || ModelPredicateProvider == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || ModelPredicateProvider == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || ModelPredicateProvider == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? "_in_hand" : "")));
    }

    public String getSpearMaterial(ItemStack ItemStack) {
        String lowerCase;
        Object s = "";
        if (ItemStack != null && !ItemStack.method_7960()) {
            s = BuiltInRegistries.ITEM.method_10221((Object)ItemStack.method_7909()).method_12832() + " " + ItemStack.getHoverName().getString();
        }
        if ((lowerCase = ((String)s).toLowerCase(Locale.ROOT)).contains("wood")) {
            return "wooden";
        }
        if (lowerCase.contains("stone")) {
            return "stone";
        }
        if (lowerCase.contains("copper")) {
            return "copper";
        }
        if (lowerCase.contains("gold")) {
            return "golden";
        }
        if (lowerCase.contains("diamond")) {
            return "diamond";
        }
        return lowerCase.contains("netherite") ? "netherite" : "iron";
    }

    public void playAttackSound(ItemStack ItemStack, boolean b) {
        long currentTimeMillis;
        if (this.sounds.getValue() && this.shouldAnimate(ItemStack) && (currentTimeMillis = System.currentTimeMillis()) - this.lastAttackSound >= 120L) {
            this.lastAttackSound = currentTimeMillis;
            boolean equals = this.getSpearMaterial(ItemStack).equals("wooden");
            this.playSound(equals ? SPEAR_WOOD_ATTACK : SPEAR_ATTACK, 1.0f);
            if (b && this.hitSounds.getValue()) {
                this.playSound(equals ? SPEAR_WOOD_HIT : SPEAR_HIT, 1.0f);
            }
        }
    }

    public void playUseSound(ItemStack ItemStack) {
        long currentTimeMillis;
        if (this.sounds.getValue() && this.useSounds.getValue() && this.shouldAnimate(ItemStack) && (currentTimeMillis = System.currentTimeMillis()) - this.lastUseSound >= 250L) {
            this.lastUseSound = currentTimeMillis;
            this.playSound(this.getSpearMaterial(ItemStack).equals("wooden") ? SPEAR_WOOD_USE : SPEAR_USE, 1.0f);
        }
    }

    public void tickUseSound(LivingEntity LivingEntity) {
        if (this.sounds.getValue() && LivingEntity != null) {
            long currentTimeMillis;
            int n;
            ItemStack class_1310 = LivingEntity.isUsingItem() ? LivingEntity.getUseItem() : LivingEntity.getMainHandItem();
            int n2 = n = LivingEntity.isUsingItem() ? LivingEntity.getTicksUsingItem() : this.fallbackUseTicks;
            if (this.shouldAnimate(class_1310) && n >= Math.max(1, (int)this.getSpearProfile(class_1310).delayTicks()) && (currentTimeMillis = System.currentTimeMillis()) - this.lastLungeSound >= 900L) {
                this.lastLungeSound = currentTimeMillis;
                this.playSound(SPEAR_LUNGE, 1.0f);
            }
        }
    }

    private void playSound(SoundEvent SoundEvent, float n) {
        Minecraft getInstance = Minecraft.getInstance();
        if (SoundEvent != null && getInstance.getSoundManager() != null) {
            getInstance.getSoundManager().method_4873((SoundInstance)SimpleSoundInstance.method_4758((SoundEvent)SoundEvent, (float)n));
        }
    }

    public void applyStabAnimation(PoseStack MatrixStack, float n, ItemStack VertexConsumer) {
        float valueFloat = this.strength.getValueFloat();
        if (valueFloat <= 0.0f) {
            return;
        }
        float n2 = -(Mth.method_15362((float)((float)Math.PI * Mth.method_15363((float)Mth.method_37960((float)n, (float)0.0f, (float)0.05f), (float)0.0f, (float)1.0f))) - 1.0f) / 2.0f;
        float clamp = Mth.method_15363((float)Mth.method_37960((float)n, (float)0.05f, (float)0.2f), (float)0.0f, (float)1.0f);
        float n3 = clamp * clamp;
        float method_15364 = Mth.method_15363((float)Mth.method_37960((float)n, (float)0.4f, (float)1.0f), (float)0.0f, (float)1.0f);
        float n4 = method_15364 < 0.5f ? (method_15364 == 0.0f ? 0.0f : (float)(Math.pow(2.0, 20.0f * method_15364 - 10.0f) / 2.0)) : (method_15364 == 1.0f ? 1.0f : (float)((2.0 - Math.pow(2.0, -20.0f * method_15364 + 10.0f)) / 2.0));
        boolean b = VertexConsumer != null && VertexConsumer.method_31574(Items.TRIDENT);
        MatrixStack.method_46416(valueFloat * n4 * 0.1f * (n2 - n3), valueFloat * -0.075f * (n2 - n4), valueFloat * ((b ? -0.4f * (n2 - n4) : 0.0f) + 0.65f * (n2 - n3)));
        MatrixStack.method_22907(Axis.XP.rotationDegrees(valueFloat * -(b ? 50.0f : 70.0f) * (n2 - n4)));
        MatrixStack.method_46416(0.0f, 0.0f, valueFloat * -0.25f * (n4 - n3));
    }

    public float getThirdPersonSwing(float n) {
        float n2 = -(Mth.method_15362((float)((float)Math.PI * Mth.method_15363((float)Mth.method_37960((float)n, (float)0.0f, (float)0.05f), (float)0.0f, (float)1.0f))) - 1.0f) / 2.0f;
        float clamp = Mth.method_15363((float)Mth.method_37960((float)n, (float)0.05f, (float)0.2f), (float)0.0f, (float)1.0f);
        float n3 = clamp * clamp;
        float method_15364 = Mth.method_15363((float)Mth.method_37960((float)n, (float)0.4f, (float)1.0f), (float)0.0f, (float)1.0f);
        float n4 = method_15364 < 0.5f ? (method_15364 == 0.0f ? 0.0f : (float)(Math.pow(2.0, 20.0f * method_15364 - 10.0f) / 2.0)) : (method_15364 == 1.0f ? 1.0f : (float)((2.0 - Math.pow(2.0, -20.0f * method_15364 + 10.0f)) / 2.0));
        return this.strength.getValueFloat() * (90.0f * n2 - 120.0f * n3 + 30.0f * n4) * ((float)Math.PI / 180);
    }

    public void applyFirstPersonUseAnimation(PoseStack MatrixStack, LivingEntity VertexConsumer, HumanoidArm VertexMultiConsumer$Double, ItemStack AffineTransformation, float n) {
        float n3;
        float valueFloat = this.strength.getValueFloat();
        if (valueFloat <= 0.0f || VertexConsumer == null) {
            return;
        }
        int n2 = VertexMultiConsumer$Double == HumanoidArm.RIGHT ? 1 : -1;
        HoldUpAnimation holdUpAnimation = this.getHoldUpAnimation(n, AffineTransformation);
        MatrixStack.method_46416((float)n2 * 0.56f, -0.52f, -0.72f);
        MatrixStack.method_46416(valueFloat * (float)n2 * (holdUpAnimation.raiseProgress() * 0.15f + holdUpAnimation.raiseProgressEnd() * -0.05f + holdUpAnimation.swayProgress() * -0.1f + holdUpAnimation.swayScaleSlow() * 0.005f), valueFloat * (holdUpAnimation.raiseProgress() * -0.075f + holdUpAnimation.raiseProgressMiddle() * 0.075f + holdUpAnimation.swayScaleFast() * 0.01f), valueFloat * (holdUpAnimation.raiseProgressStart() * 0.05f + holdUpAnimation.raiseProgressEnd() * -0.05f + holdUpAnimation.swayScaleSlow() * 0.005f));
        float raiseProgress = holdUpAnimation.raiseProgress();
        if (raiseProgress < 0.5f) {
            n3 = 4.0f * raiseProgress * raiseProgress * (7.189819f * raiseProgress - 2.5949094f) / 2.0f;
        } else {
            float n4 = 2.0f * raiseProgress - 2.0f;
            n3 = (n4 * n4 * (3.5949094f * n4 + 2.5949094f) + 2.0f) / 2.0f;
        }
        MatrixStack.method_49278(Axis.XP.rotationDegrees(valueFloat * (-65.0f * n3 - 35.0f * (1.0f - holdUpAnimation.lowerProgress()) + 100.0f * holdUpAnimation.raiseBackProgress() + -0.5f * holdUpAnimation.swayScaleFast())), 0.0f, 0.1f, 0.0f);
        MatrixStack.method_49278(Axis.YN.rotationDegrees(valueFloat * (float)n2 * (-90.0f * Mth.method_15363((float)Mth.method_37960((float)holdUpAnimation.raiseProgress(), (float)0.5f, (float)0.55f), (float)0.0f, (float)1.0f) + 90.0f * holdUpAnimation.swayProgress() + 2.0f * holdUpAnimation.swayScaleSlow())), (float)n2 * 0.15f, 0.0f, 0.0f);
    }

    @EventListener
    public void onTick(ClientTickEvent clientTickEvent) {
        if (!clientTickEvent.isPost()) {
            return;
        }
        Minecraft getInstance = Minecraft.getInstance();
        if (getInstance.player != null && getInstance.options != null && this.viaUseFallback.getValue()) {
            boolean b2;
            boolean b = getInstance.options.keyUse.isDown() && this.shouldUseAnimate(getInstance.player.getMainHandItem());
            boolean bl = b2 = getInstance.player.isUsingItem() && (this.shouldUseAnimate(getInstance.player.getUseItem()) || this.shouldUseAnimate(getInstance.player.getMainHandItem()));
            if (!b && !b2 && this.fallbackKeepAliveTicks <= 0) {
                this.fallbackUseTicks = 0;
                this.fallbackKeepAliveTicks = 0;
            } else {
                ++this.fallbackUseTicks;
                this.fallbackKeepAliveTicks = !b && !b2 ? --this.fallbackKeepAliveTicks : 6;
                if (this.fallbackUseTicks == 1) {
                    this.playUseSound(getInstance.player.getMainHandItem());
                }
            }
        }
    }

    public void applyThirdPersonHeldItemAnimation(PoseStack MatrixStack, LivingEntity VertexConsumer, ItemStack VertexMultiConsumer$Double, HumanoidArm AffineTransformation) {
        if (!this.shouldTransformHeldItem(VertexMultiConsumer$Double) || VertexConsumer == null) {
            return;
        }
        float valueFloat = this.strength.getValueFloat();
        float tickDelta = this.getTickDelta();
        if (VertexConsumer.attackAnim > 0.0f && VertexConsumer.method_6068() == AffineTransformation) {
            float n = 0.125f;
            float n2 = VertexConsumer.isUsingItem() ? 1.0f : VertexConsumer.method_6055(tickDelta);
            float clamp = Mth.method_15363((float)Mth.method_37960((float)n2, (float)0.05f, (float)0.2f), (float)0.0f, (float)1.0f);
            float n3 = clamp * clamp;
            float easeOutExpo = SpearFix.easeOutExpo(Mth.method_15363((float)Mth.method_37960((float)n2, (float)0.4f, (float)1.0f), (float)0.0f, (float)1.0f));
            MatrixStack.method_49278(Axis.XN.rotationDegrees(valueFloat * 70.0f * (n3 - easeOutExpo)), 0.0f, -0.125f, 0.125f);
            MatrixStack.method_46416(0.0f, valueFloat * this.getSpearProfile(VertexMultiConsumer$Double).forwardMovement() * (n3 - easeOutExpo), 0.0f);
        }
        if (VertexConsumer.isUsingItem() && ItemStack.method_7973((ItemStack)VertexConsumer.getUseItem(), (ItemStack)VertexMultiConsumer$Double)) {
            float n4 = (float)VertexMultiConsumer$Double.method_7935(VertexConsumer) - ((float)VertexConsumer.getUseItemRemainingTicks() - tickDelta + 1.0f);
            if (n4 <= 0.0f) {
                return;
            }
            HoldUpAnimation holdUpAnimation = this.getHoldUpAnimation(n4, VertexMultiConsumer$Double);
            int n5 = AffineTransformation == HumanoidArm.RIGHT ? 1 : -1;
            float n6 = 1.0f - holdUpAnimation.raiseProgress() - 1.0f;
            MatrixStack.method_46416(0.0f, 0.0f, valueFloat * -this.getSpearProfile(VertexMultiConsumer$Double).forwardMovement() * (1.0f - (1.0f + 2.70158f * n6 * n6 * n6 + 1.70158f * Mth.method_27285((float)n6)) - holdUpAnimation.raiseBackProgress()));
            MatrixStack.method_49278(Axis.XN.rotationDegrees(valueFloat * (holdUpAnimation.raiseProgress() * 70.0f - holdUpAnimation.raiseBackProgress() * 70.0f)), 0.0f, -0.03125f, 0.125f);
            MatrixStack.method_49278(Axis.YP.rotationDegrees(valueFloat * (holdUpAnimation.raiseProgress() * (float)n5 * 90.0f - holdUpAnimation.swayProgress() * (float)n5 * 90.0f)), 0.0f, 0.0f, 0.125f);
        }
    }

    public HoldUpAnimation getHoldUpAnimation(float n) {
        return this.getHoldUpAnimation(n, SpearProfile.IRON);
    }

    public HoldUpAnimation getHoldUpAnimation(float n, ItemStack ItemStack) {
        return this.getHoldUpAnimation(n, this.getSpearProfile(ItemStack));
    }

    private HoldUpAnimation getHoldUpAnimation(float n, SpearProfile spearProfile) {
        float delayTicks = spearProfile.delayTicks();
        float a = delayTicks + spearProfile.dismountTicks();
        float b = delayTicks + spearProfile.knockbackTicks();
        float b2 = delayTicks + spearProfile.damageTicks();
        float raiseProgress = delayTicks <= 0.0f ? 1.0f : Mth.method_15363((float)Mth.method_37960((float)n, (float)0.0f, (float)delayTicks), (float)0.0f, (float)1.0f);
        float clamp = Mth.method_15363((float)Mth.method_37960((float)raiseProgress, (float)0.0f, (float)0.5f), (float)0.0f, (float)1.0f);
        float method_15364 = Mth.method_15363((float)Mth.method_37960((float)raiseProgress, (float)0.5f, (float)0.8f), (float)0.0f, (float)1.0f);
        float method_15365 = Mth.method_15363((float)Mth.method_37960((float)raiseProgress, (float)0.8f, (float)1.0f), (float)0.0f, (float)1.0f);
        float nextDouble = Mth.method_15363((float)Mth.method_37960((float)n, (float)a, (float)b), (float)0.0f, (float)1.0f);
        float floorMod = Mth.method_15363((float)Mth.method_37960((float)n, (float)b, (float)(b2 - 5.0f)), (float)0.0f, (float)1.0f);
        if (floorMod > 0.0f && floorMod < 1.0f) {
            double sin = Math.sin((20.0 * (double)floorMod - 11.125) * Math.PI * 4.0 / 9.0);
            floorMod = floorMod < 0.5f ? (float)(-(Math.pow(2.0, 20.0 * (double)floorMod - 10.0) * sin) / 2.0) : (float)(Math.pow(2.0, -20.0 * (double)floorMod + 10.0) * sin / 2.0 + 1.0);
        }
        float lowerProgress = 1.0f - floorMod * floorMod * floorMod;
        float method_15368 = Mth.method_15363((float)Mth.method_37960((float)n, (float)(b2 - 5.0f), (float)b2), (float)0.0f, (float)1.0f);
        float swayIntensity = 2.0f * (float)Math.sqrt(1.0 + (double)Mth.method_27285((float)(1.0f - nextDouble))) - 2.0f * (-((float)Math.sqrt(1.0 - (double)(nextDouble * nextDouble))) + 1.0f);
        float hsvToRgb = Mth.method_15374((float)(n * 19.0f * ((float)Math.PI / 180)));
        float getLerpProgress = Mth.method_15374((float)(n * 30.0f * ((float)Math.PI / 180)));
        float n2 = Math.clamp((1.0f - n / Math.max(Math.max(a, b), b2)) * 20.0f, 0.0f, 1.0f) * (2.9f - swayIntensity);
        return new HoldUpAnimation(raiseProgress, clamp, method_15364, method_15365, nextDouble, lowerProgress, method_15368, swayIntensity, hsvToRgb * n2, getLerpProgress * n2);
    }

    private SpearProfile getSpearProfile(ItemStack ItemStack) {
        String spearMaterial;
        return switch (spearMaterial = this.getSpearMaterial(ItemStack)) {
            case "wooden" -> SpearProfile.WOODEN;
            case "stone" -> SpearProfile.STONE;
            case "copper" -> SpearProfile.COPPER;
            case "golden" -> SpearProfile.GOLDEN;
            case "diamond" -> SpearProfile.DIAMOND;
            case "netherite" -> SpearProfile.NETHERITE;
            default -> SpearProfile.IRON;
        };
    }

    private static float easeOutExpo(float n) {
        if (n < 0.5f) {
            return n == 0.0f ? 0.0f : (float)(Math.pow(2.0, 20.0f * n - 10.0f) / 2.0);
        }
        return n == 1.0f ? 1.0f : (float)((2.0 - Math.pow(2.0, -20.0f * n + 10.0f)) / 2.0);
    }

    public float getTickDelta() {
        return Minecraft.getInstance().getTimer().method_60637(true);
    }

    static {
        SPEAR_ATTACK_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear.attack");
        SPEAR_HIT_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear.hit");
        SPEAR_USE_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear.use");
        SPEAR_LUNGE_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear.lunge");
        SPEAR_WOOD_ATTACK_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear_wood.attack");
        SPEAR_WOOD_HIT_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear_wood.hit");
        SPEAR_WOOD_USE_ID = ResourceLocation.method_60655((String)"minecraft", (String)"item.spear_wood.use");
        SPEAR_MODELS = new ResourceLocation[]{ResourceLocation.method_60655((String)"minecraft", (String)"item/wooden_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/wooden_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/stone_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/stone_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/copper_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/copper_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/golden_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/golden_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/iron_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/iron_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/diamond_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/diamond_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/netherite_spear"), ResourceLocation.method_60655((String)"minecraft", (String)"item/netherite_spear_in_hand"), ResourceLocation.method_60655((String)"minecraft", (String)"item/spear_in_hand")};
    }

    record SpearProfile(int swingTicks, float delayTicks, float dismountTicks, float knockbackTicks, float damageTicks, float forwardMovement) {
        private static final SpearProfile WOODEN = new SpearProfile(13, 15.0f, 100.0f, 120.0f, 300.0f, 0.38f);
        private static final SpearProfile STONE = new SpearProfile(15, 14.0f, 90.0f, 110.0f, 275.0f, 0.38f);
        private static final SpearProfile COPPER = new SpearProfile(17, 13.0f, 80.0f, 100.0f, 250.0f, 0.38f);
        private static final SpearProfile GOLDEN = new SpearProfile(19, 14.0f, 70.0f, 110.0f, 275.0f, 0.38f);
        private static final SpearProfile IRON = new SpearProfile(19, 12.0f, 50.0f, 90.0f, 225.0f, 0.38f);
        private static final SpearProfile DIAMOND = new SpearProfile(21, 10.0f, 60.0f, 80.0f, 200.0f, 0.38f);
        private static final SpearProfile NETHERITE = new SpearProfile(23, 8.0f, 50.0f, 70.0f, 175.0f, 0.38f);
    }

    record HoldUpAnimation(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle, float raiseProgressEnd, float swayProgress, float lowerProgress, float raiseBackProgress, float swayIntensity, float swayScaleSlow, float swayScaleFast) {
    }
}

