package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.render.SpearFix;
import dev.chun.mod.modules.settings.enums.SwingSide;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class SpearAura extends Module {
   public static SpearAura INSTANCE;
   public static Entity target;
   public final SliderSetting range = this.add(new SliderSetting("Range", 4.5, 1.0, 8.0, 0.1));
   public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0, 1.0, 20.0, 0.5));
   public final SliderSetting attackDelay = this.add(new SliderSetting("AttackDelay", 250.0, 0.0, 1000.0, 10.0));
   public final SliderSetting hurtTime = this.add(new SliderSetting("HurtTime", 10.0, 0.0, 10.0, 1.0));
   public final BooleanSetting playersOnly = this.add(new BooleanSetting("PlayersOnly", true));
   public final BooleanSetting autoMove = this.add(new BooleanSetting("AutoMove", true));
   public final SliderSetting moveSpeed = this.add(new SliderSetting("MoveSpeed", 0.28, 0.05, 1.5, 0.01));
   public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   public final BooleanSetting swing = this.add(new BooleanSetting("Swing", true));
   public final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting<>("SwingMode", SwingSide.All));
   public final BooleanSetting requireSpear = this.add(new BooleanSetting("RequireSpear", true));
   public final BooleanSetting debug = this.add(new BooleanSetting("Debug", false));
   private final Timer attackTimer = new Timer();
   private SpearAura.State state = SpearAura.State.IDLE;
   private Vec3d origin = null;
   private int lastTargetId = -1;

   public SpearAura() {
      super("SpearAura", Module.Category.Spear);
      this.setChinese("\u957f\u77db\u5149\u73af");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.state = SpearAura.State.IDLE;
      this.origin = null;
      this.lastTargetId = -1;
      this.attackTimer.reset();
   }

   @Override
   public void onDisable() {
      this.state = SpearAura.State.IDLE;
      this.origin = null;
      target = null;
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (var1.isPost()) {
         if (nullCheck()) {
            this.reset();
         } else if (mc.player == null || mc.world == null) {
            this.reset();
         } else if (this.requireSpear.getValue() && !this.isHoldingSpear()) {
            this.reset();
         } else {
            switch (this.state) {
               case IDLE:
                  this.tickIdle();
                  break;
               case APPROACH:
                  this.tickApproach();
                  break;
               case ATTACK:
                  this.tickAttack();
                  break;
               case RETREAT:
                  this.tickRetreat();
            }
         }
      }
   }

   private void tickIdle() {
      target = this.findTarget();
      if (target != null) {
         this.origin = mc.player.getPos();
         this.lastTargetId = target.getId();
         this.state = SpearAura.State.APPROACH;
         if (this.debug.getValue()) {
            this.sendMessage("\u00a7b[SpearAura] \u76ee\u6807\u9501\u5b9a: " + target.getName());
         }
      }
   }

   private void tickApproach() {
      if (!this.isValid(target)) {
         this.state = SpearAura.State.RETREAT;
      } else {
         double var1 = (double)mc.player.distanceTo(target);
         if (var1 <= this.range.getValue()) {
            this.state = SpearAura.State.ATTACK;
         } else {
            if (this.autoMove.getValue()) {
               this.moveToward(target.getPos(), this.moveSpeed.getValue());
            }

            if (this.rotate.getValue()) {
               this.faceEntity(target);
            }
         }
      }
   }

   private void tickAttack() {
      if (!this.isValid(target)) {
         this.state = SpearAura.State.RETREAT;
      } else {
         double var1 = (double)mc.player.distanceTo(target);
         if (var1 > this.range.getValue() + 1.0) {
            this.state = SpearAura.State.APPROACH;
         } else {
            if (this.rotate.getValue()) {
               this.faceEntity(target);
            }

            if (this.attackTimer.passed((long)this.attackDelay.getValue())) {
               this.doAttack(target);
               this.attackTimer.reset();
               this.state = SpearAura.State.RETREAT;
            }
         }
      }
   }

   private void tickRetreat() {
      if (this.origin == null) {
         this.state = SpearAura.State.IDLE;
      } else {
         double var1 = mc.player.getPos().distanceTo(this.origin);
         if (!(var1 < 0.6) && this.autoMove.getValue()) {
            this.moveToward(this.origin, this.moveSpeed.getValue());
         } else {
            this.origin = null;
            this.state = SpearAura.State.IDLE;
         }
      }
   }

   private void doAttack(Entity var1) {
      try {
         this.faceEntity(var1);
         mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(var1, mc.player.isSneaking()));
         if (this.swing.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
         }

         if (SpearFix.INSTANCE != null) {
            SpearFix.INSTANCE.playAttackSound(mc.player.getMainHandStack(), true);
         }

         if (this.debug.getValue()) {
            this.sendMessage("\u00a7a[SpearAura] \u7a81\u523a! dist=" + String.format("%.2f", mc.player.distanceTo(var1)));
         }
      } catch (Throwable var3) {
         if (this.debug.getValue()) {
            this.sendMessage("\u00a7c[SpearAura] \u653b\u51fb\u5f02\u5e38: " + var3);
         }
      }
   }

   private Entity findTarget() {
      ArrayList var1 = new ArrayList();

      for (Entity var3 : mc.world.getEntities()) {
         if (var3 != mc.player && var3 instanceof LivingEntity) {
            LivingEntity var4 = (LivingEntity)var3;
            if (var4.isAlive() && !var4.isSpectator() && (!this.playersOnly.getValue() || var3 instanceof PlayerEntity)) {
               double var5 = (double)mc.player.distanceTo(var3);
               if (!(var5 > this.targetRange.getValue()) && !(var4.getAbsorptionAmount() > (float)((int)this.hurtTime.getValue()))) {
                  var1.add(var3);
               }
            }
         }
      }

      if (var1.isEmpty()) {
         return null;
      } else {
         var1.sort(Comparator.comparingDouble(var0 -> (double)mc.player.distanceTo((net.minecraft.entity.Entity)var0)));
         return (Entity)var1.get(0);
      }
   }

   private boolean isValid(Entity var1) {
      if (var1 == null) {
         return false;
      } else if (!(var1 instanceof LivingEntity var2)) {
         return false;
      } else {
         return var2.isAlive() && !var2.isSpectator() ? !((double)mc.player.distanceTo(var1) > this.targetRange.getValue() + 4.0) : false;
      }
   }

   private boolean isHoldingSpear() {
      ItemStack var1 = mc.player.getMainHandStack();
      ItemStack var2 = mc.player.getActiveItem();
      return this.isSpearItem(var1) || this.isSpearItem(var2);
   }

   private boolean isSpearItem(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         String var2 = var1.getName().getString().toLowerCase();
         return var2.contains("spear") || var2.contains("\u957f\u77db") || var2.contains("\u77db");
      } else {
         return false;
      }
   }

   private void faceEntity(Entity var1) {
      try {
         Vec3d var2 = mc.player.getEyePos();
         Vec3d var3 = var1.getPos().add(0.0, (double)var1.getHeight() * 0.8, 0.0);
         double var4 = var3.x - var2.x;
         double var6 = var3.y - var2.y;
         double var8 = var3.z - var2.z;
         double var10 = Math.sqrt(var4 * var4 + var8 * var8);
         float var12 = (float)(Math.toDegrees(Math.atan2(var8, var4)) - 90.0);
         float var13 = (float)(-Math.toDegrees(Math.atan2(var6, var10)));
         mc.player.setYaw(var12);
         mc.player.setPitch(var13);
      } catch (Throwable var14) {
      }
   }

   private void moveToward(Vec3d var1, double var2) {
      try {
         Vec3d var4 = mc.player.getPos();
         double var5 = var1.x - var4.x;
         double var7 = var1.z - var4.z;
         double var9 = Math.sqrt(var5 * var5 + var7 * var7);
         if (var9 < 0.01) {
            return;
         }

         double var11 = var5 / var9 * var2;
         double var13 = var7 / var9 * var2;
         mc.player.setVelocity(new Vec3d(var11, mc.player.getVelocity().y, var13));
      } catch (Throwable var15) {
      }
   }

   private void reset() {
      target = null;
      this.state = SpearAura.State.IDLE;
      this.origin = null;
   }

   public static enum State {
      IDLE,
      APPROACH,
      ATTACK,
      RETREAT;

      private State() {
      }
   }
}
