package dev.chun.mod.modules.impl.render;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SpearFix extends Module {
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
   private static final Identifier SPEAR_ATTACK_ID = Identifier.of("minecraft", "item.spear.attack");
   private static final Identifier SPEAR_HIT_ID = Identifier.of("minecraft", "item.spear.hit");
   private static final Identifier SPEAR_USE_ID = Identifier.of("minecraft", "item.spear.use");
   private static final Identifier SPEAR_LUNGE_ID = Identifier.of("minecraft", "item.spear.lunge");
   private static final Identifier SPEAR_WOOD_ATTACK_ID = Identifier.of("minecraft", "item.spear_wood.attack");
   private static final Identifier SPEAR_WOOD_HIT_ID = Identifier.of("minecraft", "item.spear_wood.hit");
   private static final Identifier SPEAR_WOOD_USE_ID = Identifier.of("minecraft", "item.spear_wood.use");
   private static final Identifier[] SPEAR_MODELS = new Identifier[]{
      Identifier.of("minecraft", "item/wooden_spear"),
      Identifier.of("minecraft", "item/wooden_spear_in_hand"),
      Identifier.of("minecraft", "item/stone_spear"),
      Identifier.of("minecraft", "item/stone_spear_in_hand"),
      Identifier.of("minecraft", "item/copper_spear"),
      Identifier.of("minecraft", "item/copper_spear_in_hand"),
      Identifier.of("minecraft", "item/golden_spear"),
      Identifier.of("minecraft", "item/golden_spear_in_hand"),
      Identifier.of("minecraft", "item/iron_spear"),
      Identifier.of("minecraft", "item/iron_spear_in_hand"),
      Identifier.of("minecraft", "item/diamond_spear"),
      Identifier.of("minecraft", "item/diamond_spear_in_hand"),
      Identifier.of("minecraft", "item/netherite_spear"),
      Identifier.of("minecraft", "item/netherite_spear_in_hand"),
      Identifier.of("minecraft", "item/spear_in_hand")
   };
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
      registerSounds();
      registerModels();
   }

   public static void registerModels() {
      try {
         ModelLoadingPlugin.register(context -> {
            List<Identifier> existing = new ArrayList<>();
            ClassLoader cl = SpearFix.class.getClassLoader();

            for (Identifier id : SPEAR_MODELS) {
               if (cl.getResource("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json") != null) {
                  existing.add(id);
               }
            }

            if (!existing.isEmpty()) {
               context.addModels(existing);
            }
         });
      } catch (Throwable var1) {
      }
   }

   public static void registerSounds() {
      SPEAR_ATTACK = registerSound(SPEAR_ATTACK_ID);
      SPEAR_HIT = registerSound(SPEAR_HIT_ID);
      SPEAR_USE = registerSound(SPEAR_USE_ID);
      SPEAR_LUNGE = registerSound(SPEAR_LUNGE_ID);
      SPEAR_WOOD_ATTACK = registerSound(SPEAR_WOOD_ATTACK_ID);
      SPEAR_WOOD_HIT = registerSound(SPEAR_WOOD_HIT_ID);
      SPEAR_WOOD_USE = registerSound(SPEAR_WOOD_USE_ID);
   }

   private static SoundEvent registerSound(Identifier Identifier) {
      try {
         return (SoundEvent)Registry.register(Registries.SOUND_EVENT, Identifier.toString(), SoundEvent.of(Identifier));
      } catch (RuntimeException var3) {
         SoundEvent class_2961 = (SoundEvent)Registries.SOUND_EVENT.get(Identifier);
         return class_2961 == null ? SoundEvent.of(Identifier) : class_2961;
      }
   }

   public boolean shouldAnimate(ItemStack ItemStack) {
      return ItemStack != null
         && !ItemStack.isEmpty()
         && !this.isOff()
         && (
            this.itemId.getValue() && Registries.ITEM.getId(ItemStack.getItem()).getPath().toLowerCase(Locale.ROOT).contains("spear")
               || this.itemName.getValue() && ItemStack.getName().getString().toLowerCase(Locale.ROOT).contains("spear")
         );
   }

   public boolean shouldAnimateFirstPerson(ItemStack ItemStack) {
      return this.firstPerson.getValue() && this.shouldAnimate(ItemStack);
   }

   public boolean shouldAnimateThirdPerson(ItemStack ItemStack) {
      return this.thirdPerson.getValue() && this.shouldAnimate(ItemStack);
   }

   public boolean shouldSuppressEquip(ItemStack ItemStack, float n) {
      return this.suppressEquip.getValue() && this.shouldAnimateFirstPerson(ItemStack) && n > 0.0F;
   }

   public boolean shouldUseAnimate(ItemStack ItemStack) {
      return this.useAnimation.getValue() && this.shouldAnimateFirstPerson(ItemStack);
   }

   public boolean isFallbackUseActive() {
      return this.viaUseFallback.getValue() && this.fallbackUseTicks > 0;
   }

   public float getFallbackUseTicks(float n) {
      return Math.max(0.0F, (float)this.fallbackUseTicks + n);
   }

   public boolean shouldTransformHeldItem(ItemStack ItemStack) {
      return this.itemTransform.getValue() && this.shouldAnimateThirdPerson(ItemStack);
   }

   public int getSwingTicks(LivingEntity LivingEntity) {
      return this.swingDuration.getValue() && LivingEntity != null && this.shouldAnimate(LivingEntity.getMainHandStack())
         ? Math.max(1, this.getSpearProfile(LivingEntity.getMainHandStack()).swingTicks())
         : -1;
   }

   public boolean shouldAllowSprint(LivingEntity LivingEntity) {
      return !this.grimSafe.getValue()
         && this.allowSprint.getValue()
         && LivingEntity != null
         && LivingEntity.isUsingItem()
         && this.shouldAnimate(LivingEntity.getActiveItem());
   }

   public boolean shouldReplaceModel(ItemStack ItemStack) {
      return this.model.getValue() && this.shouldAnimate(ItemStack);
   }

   public Identifier getModelId(ItemStack ItemStack, ModelTransformationMode ModelPredicateProvider) {
      return Identifier.of(
         "minecraft",
         "item/"
            + this.getSpearMaterial(ItemStack)
            + "_spear"
            + (
               ModelPredicateProvider != ModelTransformationMode.FIRST_PERSON_LEFT_HAND
                     && ModelPredicateProvider != ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
                     && ModelPredicateProvider != ModelTransformationMode.THIRD_PERSON_LEFT_HAND
                     && ModelPredicateProvider != ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                  ? ""
                  : "_in_hand"
            )
      );
   }

   public String getSpearMaterial(ItemStack ItemStack) {
      String s = "";
      if (ItemStack != null && !ItemStack.isEmpty()) {
         s = Registries.ITEM.getId(ItemStack.getItem()).getPath() + " " + ItemStack.getName().getString();
      }

      String lowerCase = s.toLowerCase(Locale.ROOT);
      if (lowerCase.contains("wood")) {
         return "wooden";
      } else if (lowerCase.contains("stone")) {
         return "stone";
      } else if (lowerCase.contains("copper")) {
         return "copper";
      } else if (lowerCase.contains("gold")) {
         return "golden";
      } else if (lowerCase.contains("diamond")) {
         return "diamond";
      } else {
         return lowerCase.contains("netherite") ? "netherite" : "iron";
      }
   }

   public void playAttackSound(ItemStack ItemStack, boolean b) {
      if (this.sounds.getValue() && this.shouldAnimate(ItemStack)) {
         long currentTimeMillis = System.currentTimeMillis();
         if (currentTimeMillis - this.lastAttackSound >= 120L) {
            this.lastAttackSound = currentTimeMillis;
            boolean equals = this.getSpearMaterial(ItemStack).equals("wooden");
            this.playSound(equals ? SPEAR_WOOD_ATTACK : SPEAR_ATTACK, 1.0F);
            if (b && this.hitSounds.getValue()) {
               this.playSound(equals ? SPEAR_WOOD_HIT : SPEAR_HIT, 1.0F);
            }
         }
      }
   }

   public void playUseSound(ItemStack ItemStack) {
      if (this.sounds.getValue() && this.useSounds.getValue() && this.shouldAnimate(ItemStack)) {
         long currentTimeMillis = System.currentTimeMillis();
         if (currentTimeMillis - this.lastUseSound >= 250L) {
            this.lastUseSound = currentTimeMillis;
            this.playSound(this.getSpearMaterial(ItemStack).equals("wooden") ? SPEAR_WOOD_USE : SPEAR_USE, 1.0F);
         }
      }
   }

   public void tickUseSound(LivingEntity LivingEntity) {
      if (this.sounds.getValue() && LivingEntity != null) {
         ItemStack class_1310 = LivingEntity.isUsingItem() ? LivingEntity.getActiveItem() : LivingEntity.getMainHandStack();
         int n = LivingEntity.isUsingItem() ? LivingEntity.getItemUseTime() : this.fallbackUseTicks;
         if (this.shouldAnimate(class_1310) && n >= Math.max(1, (int)this.getSpearProfile(class_1310).delayTicks())) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - this.lastLungeSound >= 900L) {
               this.lastLungeSound = currentTimeMillis;
               this.playSound(SPEAR_LUNGE, 1.0F);
            }
         }
      }
   }

   private void playSound(SoundEvent SoundEvent, float n) {
      MinecraftClient getInstance = MinecraftClient.getInstance();
      if (SoundEvent != null && getInstance.getSoundManager() != null) {
         getInstance.getSoundManager().play(PositionedSoundInstance.master(SoundEvent, n));
      }
   }

   public void applyStabAnimation(MatrixStack MatrixStack, float n, ItemStack VertexConsumer) {
      float valueFloat = this.strength.getValueFloat();
      if (!(valueFloat <= 0.0F)) {
         float n2 = -(MathHelper.cos((float) Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(n, 0.0F, 0.05F), 0.0F, 1.0F)) - 1.0F) / 2.0F;
         float clamp = MathHelper.clamp(MathHelper.getLerpProgress(n, 0.05F, 0.2F), 0.0F, 1.0F);
         float n3 = clamp * clamp;
         float method_15364 = MathHelper.clamp(MathHelper.getLerpProgress(n, 0.4F, 1.0F), 0.0F, 1.0F);
         float n4;
         if (method_15364 < 0.5F) {
            n4 = method_15364 == 0.0F ? 0.0F : (float)(Math.pow(2.0, (double)(20.0F * method_15364 - 10.0F)) / 2.0);
         } else {
            n4 = method_15364 == 1.0F ? 1.0F : (float)((2.0 - Math.pow(2.0, (double)(-20.0F * method_15364 + 10.0F))) / 2.0);
         }

         boolean b = VertexConsumer != null && VertexConsumer.isOf(Items.TRIDENT);
         MatrixStack.translate(
            valueFloat * n4 * 0.1F * (n2 - n3), valueFloat * -0.075F * (n2 - n4), valueFloat * ((b ? -0.4F * (n2 - n4) : 0.0F) + 0.65F * (n2 - n3))
         );
         MatrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(valueFloat * -(b ? 50.0F : 70.0F) * (n2 - n4)));
         MatrixStack.translate(0.0F, 0.0F, valueFloat * -0.25F * (n4 - n3));
      }
   }

   public float getThirdPersonSwing(float n) {
      float n2 = -(MathHelper.cos((float) Math.PI * MathHelper.clamp(MathHelper.getLerpProgress(n, 0.0F, 0.05F), 0.0F, 1.0F)) - 1.0F) / 2.0F;
      float clamp = MathHelper.clamp(MathHelper.getLerpProgress(n, 0.05F, 0.2F), 0.0F, 1.0F);
      float n3 = clamp * clamp;
      float method_15364 = MathHelper.clamp(MathHelper.getLerpProgress(n, 0.4F, 1.0F), 0.0F, 1.0F);
      float n4;
      if (method_15364 < 0.5F) {
         n4 = method_15364 == 0.0F ? 0.0F : (float)(Math.pow(2.0, (double)(20.0F * method_15364 - 10.0F)) / 2.0);
      } else {
         n4 = method_15364 == 1.0F ? 1.0F : (float)((2.0 - Math.pow(2.0, (double)(-20.0F * method_15364 + 10.0F))) / 2.0);
      }

      return this.strength.getValueFloat() * (90.0F * n2 - 120.0F * n3 + 30.0F * n4) * (float) (Math.PI / 180.0);
   }

   public void applyFirstPersonUseAnimation(MatrixStack MatrixStack, LivingEntity VertexConsumer, Arm class_4589, ItemStack AffineTransformation, float n) {
      float valueFloat = this.strength.getValueFloat();
      if (!(valueFloat <= 0.0F) && VertexConsumer != null) {
         int n2 = class_4589 == Arm.RIGHT ? 1 : -1;
         SpearFix.HoldUpAnimation holdUpAnimation = this.getHoldUpAnimation(n, AffineTransformation);
         MatrixStack.translate((float)n2 * 0.56F, -0.52F, -0.72F);
         MatrixStack.translate(
            valueFloat
               * (float)n2
               * (
                  holdUpAnimation.raiseProgress() * 0.15F
                     + holdUpAnimation.raiseProgressEnd() * -0.05F
                     + holdUpAnimation.swayProgress() * -0.1F
                     + holdUpAnimation.swayScaleSlow() * 0.005F
               ),
            valueFloat * (holdUpAnimation.raiseProgress() * -0.075F + holdUpAnimation.raiseProgressMiddle() * 0.075F + holdUpAnimation.swayScaleFast() * 0.01F),
            valueFloat
               * (holdUpAnimation.raiseProgressStart() * 0.05F + holdUpAnimation.raiseProgressEnd() * -0.05F + holdUpAnimation.swayScaleSlow() * 0.005F)
         );
         float raiseProgress = holdUpAnimation.raiseProgress();
         float n3;
         if (raiseProgress < 0.5F) {
            n3 = 4.0F * raiseProgress * raiseProgress * (7.189819F * raiseProgress - 2.5949094F) / 2.0F;
         } else {
            float n4 = 2.0F * raiseProgress - 2.0F;
            n3 = (n4 * n4 * (3.5949094F * n4 + 2.5949094F) + 2.0F) / 2.0F;
         }

         MatrixStack.multiply(
            RotationAxis.POSITIVE_X
               .rotationDegrees(
                  valueFloat
                     * (
                        -65.0F * n3
                           - 35.0F * (1.0F - holdUpAnimation.lowerProgress())
                           + 100.0F * holdUpAnimation.raiseBackProgress()
                           + -0.5F * holdUpAnimation.swayScaleFast()
                     )
               ),
            0.0F,
            0.1F,
            0.0F
         );
         MatrixStack.multiply(
            RotationAxis.NEGATIVE_Y
               .rotationDegrees(
                  valueFloat
                     * (float)n2
                     * (
                        -90.0F * MathHelper.clamp(MathHelper.getLerpProgress(holdUpAnimation.raiseProgress(), 0.5F, 0.55F), 0.0F, 1.0F)
                           + 90.0F * holdUpAnimation.swayProgress()
                           + 2.0F * holdUpAnimation.swayScaleSlow()
                     )
               ),
            (float)n2 * 0.15F,
            0.0F,
            0.0F
         );
      }
   }

   @EventListener
   public void onTick(ClientTickEvent clientTickEvent) {
      if (clientTickEvent.isPost()) {
         MinecraftClient getInstance = MinecraftClient.getInstance();
         if (getInstance.player != null && getInstance.options != null && this.viaUseFallback.getValue()) {
            boolean b = getInstance.options.useKey.isPressed() && this.shouldUseAnimate(getInstance.player.getMainHandStack());
            boolean b2 = getInstance.player.isUsingItem()
               && (this.shouldUseAnimate(getInstance.player.getActiveItem()) || this.shouldUseAnimate(getInstance.player.getMainHandStack()));
            if (!b && !b2 && this.fallbackKeepAliveTicks <= 0) {
               this.fallbackUseTicks = 0;
               this.fallbackKeepAliveTicks = 0;
            } else {
               this.fallbackUseTicks++;
               if (!b && !b2) {
                  this.fallbackKeepAliveTicks--;
               } else {
                  this.fallbackKeepAliveTicks = 6;
               }

               if (this.fallbackUseTicks == 1) {
                  this.playUseSound(getInstance.player.getMainHandStack());
               }
            }
         }
      }
   }

   public void applyThirdPersonHeldItemAnimation(MatrixStack MatrixStack, LivingEntity VertexConsumer, ItemStack class_4589, Arm AffineTransformation) {
      if (this.shouldTransformHeldItem(class_4589) && VertexConsumer != null) {
         float valueFloat = this.strength.getValueFloat();
         float tickDelta = this.getTickDelta();
         if (VertexConsumer.handSwingProgress > 0.0F && VertexConsumer.getMainArm() == AffineTransformation) {
            float n = 0.125F;
            float n2 = VertexConsumer.isUsingItem() ? 1.0F : VertexConsumer.getHandSwingProgress(tickDelta);
            float clamp = MathHelper.clamp(MathHelper.getLerpProgress(n2, 0.05F, 0.2F), 0.0F, 1.0F);
            float n3 = clamp * clamp;
            float easeOutExpo = easeOutExpo(MathHelper.clamp(MathHelper.getLerpProgress(n2, 0.4F, 1.0F), 0.0F, 1.0F));
            MatrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(valueFloat * 70.0F * (n3 - easeOutExpo)), 0.0F, -0.125F, 0.125F);
            MatrixStack.translate(0.0F, valueFloat * this.getSpearProfile(class_4589).forwardMovement() * (n3 - easeOutExpo), 0.0F);
         }

         if (VertexConsumer.isUsingItem() && ItemStack.areEqual(VertexConsumer.getActiveItem(), class_4589)) {
            float n4 = (float)class_4589.getMaxUseTime(VertexConsumer) - ((float)VertexConsumer.getItemUseTimeLeft() - tickDelta + 1.0F);
            if (n4 <= 0.0F) {
               return;
            }

            SpearFix.HoldUpAnimation holdUpAnimation = this.getHoldUpAnimation(n4, class_4589);
            int n5 = AffineTransformation == Arm.RIGHT ? 1 : -1;
            float n6 = 1.0F - holdUpAnimation.raiseProgress() - 1.0F;
            MatrixStack.translate(
               0.0F,
               0.0F,
               valueFloat
                  * -this.getSpearProfile(class_4589).forwardMovement()
                  * (1.0F - (1.0F + 2.70158F * n6 * n6 * n6 + 1.70158F * MathHelper.square(n6)) - holdUpAnimation.raiseBackProgress())
            );
            MatrixStack.multiply(
               RotationAxis.NEGATIVE_X.rotationDegrees(valueFloat * (holdUpAnimation.raiseProgress() * 70.0F - holdUpAnimation.raiseBackProgress() * 70.0F)),
               0.0F,
               -0.03125F,
               0.125F
            );
            MatrixStack.multiply(
               RotationAxis.POSITIVE_Y
                  .rotationDegrees(valueFloat * (holdUpAnimation.raiseProgress() * (float)n5 * 90.0F - holdUpAnimation.swayProgress() * (float)n5 * 90.0F)),
               0.0F,
               0.0F,
               0.125F
            );
         }
      }
   }

   public SpearFix.HoldUpAnimation getHoldUpAnimation(float n) {
      return this.getHoldUpAnimation(n, SpearFix.SpearProfile.IRON);
   }

   public SpearFix.HoldUpAnimation getHoldUpAnimation(float n, ItemStack ItemStack) {
      return this.getHoldUpAnimation(n, this.getSpearProfile(ItemStack));
   }

   private SpearFix.HoldUpAnimation getHoldUpAnimation(float n, SpearFix.SpearProfile spearProfile) {
      float delayTicks = spearProfile.delayTicks();
      float a = delayTicks + spearProfile.dismountTicks();
      float b = delayTicks + spearProfile.knockbackTicks();
      float b2 = delayTicks + spearProfile.damageTicks();
      float raiseProgress = delayTicks <= 0.0F ? 1.0F : MathHelper.clamp(MathHelper.getLerpProgress(n, 0.0F, delayTicks), 0.0F, 1.0F);
      float clamp = MathHelper.clamp(MathHelper.getLerpProgress(raiseProgress, 0.0F, 0.5F), 0.0F, 1.0F);
      float method_15364 = MathHelper.clamp(MathHelper.getLerpProgress(raiseProgress, 0.5F, 0.8F), 0.0F, 1.0F);
      float method_15365 = MathHelper.clamp(MathHelper.getLerpProgress(raiseProgress, 0.8F, 1.0F), 0.0F, 1.0F);
      float nextDouble = MathHelper.clamp(MathHelper.getLerpProgress(n, a, b), 0.0F, 1.0F);
      float floorMod = MathHelper.clamp(MathHelper.getLerpProgress(n, b, b2 - 5.0F), 0.0F, 1.0F);
      if (floorMod > 0.0F && floorMod < 1.0F) {
         double sin = Math.sin((20.0 * (double)floorMod - 11.125) * Math.PI * 4.0 / 9.0);
         floorMod = floorMod < 0.5F
            ? (float)(-(Math.pow(2.0, 20.0 * (double)floorMod - 10.0) * sin) / 2.0)
            : (float)(Math.pow(2.0, -20.0 * (double)floorMod + 10.0) * sin / 2.0 + 1.0);
      }

      float lowerProgress = 1.0F - floorMod * floorMod * floorMod;
      float method_15368 = MathHelper.clamp(MathHelper.getLerpProgress(n, b2 - 5.0F, b2), 0.0F, 1.0F);
      float swayIntensity = 2.0F * (float)Math.sqrt(1.0 + (double)MathHelper.square(1.0F - nextDouble))
         - 2.0F * (-((float)Math.sqrt(1.0 - (double)(nextDouble * nextDouble))) + 1.0F);
      float hsvToRgb = MathHelper.sin(n * 19.0F * (float) (Math.PI / 180.0));
      float getLerpProgress = MathHelper.sin(n * 30.0F * (float) (Math.PI / 180.0));
      float n2 = Math.clamp((1.0F - n / Math.max(Math.max(a, b), b2)) * 20.0F, 0.0F, 1.0F) * (2.9F - swayIntensity);
      return new SpearFix.HoldUpAnimation(
         raiseProgress, clamp, method_15364, method_15365, nextDouble, lowerProgress, method_15368, swayIntensity, hsvToRgb * n2, getLerpProgress * n2
      );
   }

   private SpearFix.SpearProfile getSpearProfile(ItemStack ItemStack) {
      String spearMaterial = this.getSpearMaterial(ItemStack);

      return switch (spearMaterial) {
         case "wooden" -> SpearFix.SpearProfile.WOODEN;
         case "stone" -> SpearFix.SpearProfile.STONE;
         case "copper" -> SpearFix.SpearProfile.COPPER;
         case "golden" -> SpearFix.SpearProfile.GOLDEN;
         case "diamond" -> SpearFix.SpearProfile.DIAMOND;
         case "netherite" -> SpearFix.SpearProfile.NETHERITE;
         default -> SpearFix.SpearProfile.IRON;
      };
   }

   private static float easeOutExpo(float n) {
      if (n < 0.5F) {
         return n == 0.0F ? 0.0F : (float)(Math.pow(2.0, (double)(20.0F * n - 10.0F)) / 2.0);
      } else {
         return n == 1.0F ? 1.0F : (float)((2.0 - Math.pow(2.0, (double)(-20.0F * n + 10.0F))) / 2.0);
      }
   }

   public float getTickDelta() {
      return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
   }

   static record HoldUpAnimation(
      float raiseProgress,
      float raiseProgressStart,
      float raiseProgressMiddle,
      float raiseProgressEnd,
      float swayProgress,
      float lowerProgress,
      float raiseBackProgress,
      float swayIntensity,
      float swayScaleSlow,
      float swayScaleFast
   ) {
   }

   static record SpearProfile(int swingTicks, float delayTicks, float dismountTicks, float knockbackTicks, float damageTicks, float forwardMovement) {
      private static final SpearFix.SpearProfile WOODEN = new SpearFix.SpearProfile(13, 15.0F, 100.0F, 120.0F, 300.0F, 0.38F);
      private static final SpearFix.SpearProfile STONE = new SpearFix.SpearProfile(15, 14.0F, 90.0F, 110.0F, 275.0F, 0.38F);
      private static final SpearFix.SpearProfile COPPER = new SpearFix.SpearProfile(17, 13.0F, 80.0F, 100.0F, 250.0F, 0.38F);
      private static final SpearFix.SpearProfile GOLDEN = new SpearFix.SpearProfile(19, 14.0F, 70.0F, 110.0F, 275.0F, 0.38F);
      private static final SpearFix.SpearProfile IRON = new SpearFix.SpearProfile(19, 12.0F, 50.0F, 90.0F, 225.0F, 0.38F);
      private static final SpearFix.SpearProfile DIAMOND = new SpearFix.SpearProfile(21, 10.0F, 60.0F, 80.0F, 200.0F, 0.38F);
      private static final SpearFix.SpearProfile NETHERITE = new SpearFix.SpearProfile(23, 8.0F, 50.0F, 70.0F, 175.0F, 0.38F);
   }
}
