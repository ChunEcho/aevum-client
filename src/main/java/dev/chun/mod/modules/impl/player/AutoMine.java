package dev.chun.mod.modules.impl.player;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoMine extends Module {
   private final SliderSetting minY = this.add(new SliderSetting("MinY", -64.0, -64.0, 40.0, 1.0));
   private final SliderSetting reach = this.add(new SliderSetting("Reach", 4.5, 2.0, 6.0, 0.1));
   private final BooleanSetting avoidContainers = this.add(new BooleanSetting("AvoidContainers", true));
   private final BooleanSetting skipExposedOnly = this.add(new BooleanSetting("SkipBuried", true));
   private int retargetCooldown;

   public AutoMine() {
      super("AutoMine", Module.Category.Player);
      this.setChinese("\u533a\u5757\u6316\u6398");
   }

   @Override
   public void onEnable() {
      this.retargetCooldown = 0;
      if (PacketMine.INSTANCE != null && PacketMine.INSTANCE.isOff()) {
         PacketMine.INSTANCE.enable();
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (!nullCheck()) {
         if (PacketMine.getBreakPos() == null || mc.world.isAir(PacketMine.getBreakPos())) {
            if (this.retargetCooldown > 0) {
               this.retargetCooldown--;
            } else {
               BlockPos scanTarget = this.scanTarget();
               if (scanTarget == null) {
                  this.retargetCooldown = 10;
               } else {
                  PacketMine.INSTANCE.mine(scanTarget);
               }
            }
         }
      }
   }

   private BlockPos scanTarget() {
      ClientPlayerEntity player = mc.player;
      BlockPos getVelocityAffectingPos = player.getVelocityAffectingPos();
      int getDefaultName = getVelocityAffectingPos.getY();
      int valueInt = this.minY.getValueInt();
      int n = (int)Math.ceil((double)this.reach.getValueFloat());

      for (int i = getDefaultName; i >= valueInt; i--) {
         BlockPos BlockPos = null;
         double n2 = Double.MAX_VALUE;

         for (int j = -n; j <= n; j++) {
            for (int k = -n; k <= n; k++) {
               BlockPos class_2339 = new BlockPos(getVelocityAffectingPos.getX() + j, i, getVelocityAffectingPos.getZ() + k);
               BlockState offsetX = mc.world.getBlockState(class_2339);
               if (!offsetX.isAir()
                  && offsetX.getAmbientOcclusionLightLevel(mc.world, class_2339) >= 0.0F
                  && offsetX.getFluidState().isStill()
                  && !PacketMine.unbreakable(class_2339)
                  && (!this.avoidContainers.getValue() || !(offsetX.getBlock() instanceof AirBlock))
                  && (!this.skipExposedOnly.getValue() || BlockUtil.getClickSideStrict(class_2339) != null)) {
                  double getX = player.getEyePos().distanceTo(Vec3d.ofCenter(class_2339));
                  if (getX <= (double)this.reach.getValueFloat() && getX < n2) {
                     n2 = getX;
                     BlockPos = class_2339;
                  }
               }
            }
         }

         if (BlockPos != null) {
            return BlockPos;
         }
      }

      return null;
   }
}
