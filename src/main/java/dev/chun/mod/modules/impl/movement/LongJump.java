package dev.chun.mod.modules.impl.movement;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.MoveEvent;
import dev.chun.api.events.impl.MovedEvent;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.MovementUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class LongJump extends Module {
   public static LongJump INSTANCE;
   private final EnumSetting<LongJump.Mode> mode = this.add(new EnumSetting<>("Mode", LongJump.Mode.Strafe));
   private final SliderSetting strafeSpeed = this.add(new SliderSetting("Speed", 0.2873, 0.0, 1.0, 1.0E-4, () -> this.mode.is(LongJump.Mode.Strafe)));
   private final BooleanSetting jump = this.add(new BooleanSetting("Jump", true, () -> this.mode.is(LongJump.Mode.Strafe)));
   private final SliderSetting lagTime = this.add(new SliderSetting("LagTime", 500.0, 0.0, 1000.0, 1.0, () -> this.mode.is(LongJump.Mode.Strafe)));
   private final BooleanSetting airStop = this.add(new BooleanSetting("AirStop", false));
   private final Timer lagTimer = new Timer();
   private boolean stop;
   private double speed;
   private double distance;
   private int stage;
   private boolean boost;

   public LongJump() {
      super("LongJump", Module.Category.Movement);
      this.setChinese("\u957f\u8df3");
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.speed = MovementUtil.getSpeed(false);
         this.distance = MovementUtil.getDistance2D();
      }

      this.stage = 4;
   }

   @EventListener(
      priority = 100
   )
   public void invoke(PacketEvent.Receive event) {
      if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.lagTimer.reset();
         this.resetStrafe();
      }
   }

   @EventListener
   public void onMove(MovedEvent event) {
      if (!Module.nullCheck()) {
         double dx = mc.player.getX() - mc.player.prevX;
         double dz = mc.player.getZ() - mc.player.prevZ;
         this.distance = Math.sqrt(dx * dx + dz * dz);
      }
   }

   @EventListener
   public void invoke(MoveEvent event) {
      if (!MovementUtil.isMoving() && this.airStop.getValue()) {
         MovementUtil.setMotionX(0.0);
         MovementUtil.setMotionZ(0.0);
      }

      if (mc.player.isRiding() || mc.player.isHoldingOntoLadder() || mc.player.getAbilities().flying || mc.player.isFallFlying() || !MovementUtil.isMoving()) {
         this.resetStrafe();
         this.stop = true;
      } else if (this.mode.is(LongJump.Mode.Strafe)) {
         if (this.stop) {
            this.stop = false;
         } else if (this.lagTimer.passed((long)this.lagTime.getValueInt())) {
            if (this.stage == 1) {
               this.speed = 1.35 * MovementUtil.getSpeed(false, this.strafeSpeed.getValue()) - 0.01;
            } else if (this.stage != 2 || !mc.player.isOnGround() || !mc.options.jumpKey.isPressed() && !this.jump.getValue()) {
               if (this.stage == 3) {
                  this.speed = this.distance - 0.66 * (this.distance - MovementUtil.getSpeed(false, this.strafeSpeed.getValue()));
                  this.boost = !this.boost;
               } else {
                  if ((BlockUtil.canCollide(null, mc.player.getBoundingBox().offset(0.0, MovementUtil.getMotionY(), 0.0)) || mc.player.collidedSoftly)
                     && this.stage > 0) {
                     this.stage = 1;
                  }

                  this.speed = this.distance - this.distance / 159.0;
               }
            } else {
               double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
               MovementUtil.setMotionY(yMotion);
               event.setY(yMotion);
               this.speed = this.speed * (this.boost ? 1.6835 : 1.395);
            }

            this.speed = Math.min(this.speed, 10.0);
            this.speed = Math.max(this.speed, MovementUtil.getSpeed(false, this.strafeSpeed.getValue()));
            double n = (double)mc.player.input.movementForward;
            double n2 = (double)mc.player.input.movementSideways;
            double n3 = (double)mc.player.getYaw();
            if (n == 0.0 && n2 == 0.0) {
               event.setX(0.0);
               event.setZ(0.0);
            } else if (n != 0.0 && n2 != 0.0) {
               n *= Math.sin(Math.PI / 4);
               n2 *= Math.cos(Math.PI / 4);
            }

            event.setX((n * this.speed * -Math.sin(Math.toRadians(n3)) + n2 * this.speed * Math.cos(Math.toRadians(n3))) * 0.99);
            event.setZ((n * this.speed * Math.cos(Math.toRadians(n3)) - n2 * this.speed * -Math.sin(Math.toRadians(n3))) * 0.99);
            this.stage++;
         }
      }
   }

   public void resetStrafe() {
      this.stage = 4;
      this.speed = 0.0;
      this.distance = 0.0;
   }

   public static enum Mode {
      Strafe;

      private Mode() {
      }
   }
}
