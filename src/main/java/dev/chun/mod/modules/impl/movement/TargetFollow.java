package dev.chun.mod.modules.impl.movement;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateRotateEvent;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.core.impl.RotationManager;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TargetFollow extends Module {
   private final SliderSetting range = this.add(new SliderSetting("Range", 50.0, 1.0, 100.0, 1.0));

   public TargetFollow() {
      super("TargetFollow", Module.Category.Movement);
      this.setChinese("\u8ddf\u968f\u76ee\u6807");
   }

   @Override
   public void onDisable() {
      if (!nullCheck()) {
         mc.options.forwardKey.setPressed(false);
      }
   }

   @EventListener(
      priority = -9999
   )
   public void onRotation(UpdateRotateEvent event) {
      if (!nullCheck()) {
         PlayerEntity target = this.getClosestPlayer(this.range.getValue());
         if (target != null && ElytraFly.INSTANCE.isFallFlying()) {
            Vec3d pos = MathUtil.getClosestPointToBox(mc.player.getEyePos(), target.getBoundingBox());
            float[] rotations = RotationManager.getRotation(pos);
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);
            mc.options.forwardKey.setPressed(true);
         }
      }
   }

   private PlayerEntity getClosestPlayer(double range) {
      PlayerEntity target = null;
      double distance = range;

      for (Entity entity : Aevum.THREAD.threadSafeEntityList) {
         if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (entity != mc.player && !Aevum.FRIEND.isFriend(player.getName().getString()) && !((double)mc.player.distanceTo(entity) > range)) {
               if (target == null) {
                  target = player;
                  distance = (double)mc.player.distanceTo(entity);
               } else if (!((double)mc.player.distanceTo(entity) >= distance)) {
                  target = player;
                  distance = (double)mc.player.distanceTo(entity);
               }
            }
         }
      }

      return target;
   }
}
