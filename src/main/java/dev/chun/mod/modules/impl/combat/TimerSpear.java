package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TimerSpear extends Module {
   public static TimerSpear INSTANCE;
   public final SliderSetting delay = this.add(new SliderSetting("Delay", 500.0, 0.0, 5000.0, 10.0));
   public final SliderSetting lagBackRange = this.add(new SliderSetting("LagBackRange", 3.5, 0.0, 10.0, 0.1));
   public final BooleanSetting doubleAttack = this.add(new BooleanSetting("DoubleAttack", false));
   public final SliderSetting doubleTime = this.add(new SliderSetting("DoubleTime", 500.0, 0.0, 99999.0, 50.0));
   public final BooleanSetting noHitLagBack = this.add(new BooleanSetting("NoHitLagBack", false));
   public final BooleanSetting setYMotion0 = this.add(new BooleanSetting("SetYMotion0", false));
   public final BooleanSetting slowFall = this.add(new BooleanSetting("SlowFall", false));
   public final SliderSetting slowFallSpeed = this.add(new SliderSetting("SlowFallSpeed", 0.03, 0.01, 0.9, 0.01));
   public final BooleanSetting debug = this.add(new BooleanSetting("Debug", false));
   private final Timer delayTimer = new Timer();
   private final Timer doubleTimer = new Timer();
   private Vec3d lagBackPos = null;
   private double fallDistance = 0.0;
   private boolean lagBackYFrozen = false;
   private boolean isDoubleAttack = false;
   private boolean pendingRearm = false;

   public TimerSpear() {
      super("TimerSpear", Module.Category.Spear);
      this.setChinese("\u62c9\u56de\u77db");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.reset();
      this.delayTimer.reset();
      this.doubleTimer.reset();
   }

   @Override
   public void onDisable() {
      this.reset();
   }

   private void reset() {
      this.lagBackPos = null;
      this.fallDistance = 0.0;
      this.lagBackYFrozen = false;
      this.isDoubleAttack = false;
      this.pendingRearm = false;
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!var1.isPost()) {
         if (nullCheck()) {
            this.reset();
         } else if (mc.player != null && mc.world != null) {
            this.doLagBackMode();
         }
      }
   }

   private void doLagBackMode() {
      double var1 = this.findLandingY();
      Vec3d var3 = mc.player.getPos();
      if (this.lagBackPos == null) {
         this.lagBackPos = var3;
      } else {
         this.fallDistance = this.lagBackPos.y - var1 + 0.5;
         if (this.fallDistance < this.lagBackRange.getValue()) {
            this.isDoubleAttack = false;
            this.lagBackYFrozen = false;
            this.lagBackPos = var3;
         } else if (mc.player.getY() - var1 > this.lagBackRange.getValue()) {
            this.isDoubleAttack = false;
            this.lagBackYFrozen = false;
            this.doubleTimer.reset();
         } else {
            this.lagBackYFrozen = this.setYMotion0.getValue();
            if (this.doubleAttack.getValue() && !this.doubleTimer.passed((long)this.doubleTime.getValue())) {
               this.isDoubleAttack = true;
            } else {
               if (this.delayTimer.passed((long)this.delay.getValue())) {
                  this.doLagBack();
                  this.delayTimer.reset();
                  this.doubleTimer.reset();
               }
            }
         }
      }
   }

   private void doLagBack() {
      try {
         if (mc.getNetworkHandler() == null) {
            return;
         }

         double var1 = mc.player.getX();
         double var3 = mc.player.getZ();
         boolean var5 = mc.player.isOnGround();
         PositionAndOnGround var6 = new PositionAndOnGround(var1, 10000.0, var3, var5);
         mc.getNetworkHandler().sendPacket(var6);
         if (this.debug.getValue()) {
            this.sendMessage("\u00a7b[TimerSpear] \u62c9\u56de @ " + String.format("%.1f", mc.player.getY()));
         }
      } catch (Throwable var7) {
         if (this.debug.getValue()) {
            this.sendMessage("\u00a7c[TimerSpear] \u5f02\u5e38: " + var7);
         }
      }
   }

   private double findLandingY() {
      try {
         double var1 = mc.player.getY();
         BlockPos var3 = mc.player.getBlockPos();

         for (int var4 = 0; var4 < 64; var4++) {
            BlockPos var5 = var3.add(new Vec3i(0, -var4, 0));
            BlockState var6 = mc.world.getBlockState(var5);
            if (!var6.isAir()) {
               return (double)var5.getY() + 1.0;
            }
         }

         return var1 - 1.0;
      } catch (Throwable var7) {
         return mc.player.getY() - 1.0;
      }
   }

   @Override
   public String getInfo() {
      return String.format("%.0fms", this.delay.getValue());
   }
}
