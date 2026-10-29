package dev.chun.mod.modules.impl.combat;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.EntitySpawnedEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.enums.SwingSide;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class AntiCobweb extends Module {
   public static AntiCobweb INSTANCE;
   private BlockPos targetCrystalPos;
   private final Timer breakTimer = new Timer();
   private final Timer placeTimer = new Timer();
   private final SliderSetting safeHealth = this.add(new SliderSetting("SafeHealth", 5.0, 0.0, 20.0, 0.5).setSuffix("hp"));

   public AntiCobweb() {
      super("AntiCobweb", "Uses crystal to break cobwebs around you", Module.Category.Combat);
      this.setChinese("\u81ea\u52a8\u7834\u7f51");
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.targetCrystalPos = null;
   }

   @EventListener
   public void onTick(ClientTickEvent event) {
      if (!nullCheck()) {
         if (!event.isPost()) {
            BlockPos playerPos = mc.player.getBlockPos();
            BlockPos cobwebPos = null;
            if (mc.world.getBlockState(playerPos).getBlock() == Blocks.COBWEB) {
               cobwebPos = playerPos;
            } else if (mc.world.getBlockState(playerPos.up()).getBlock() == Blocks.COBWEB) {
               cobwebPos = playerPos.up();
            }

            if (cobwebPos == null) {
               this.targetCrystalPos = null;
            } else if (!((double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= this.safeHealth.getValue())) {
               if (this.targetCrystalPos != null && this.breakTimer.passedMs(100.0)) {
                  this.breakCrystalAt(this.targetCrystalPos);
                  this.targetCrystalPos = null;
               } else {
                  if (this.targetCrystalPos == null && this.placeTimer.passedMs(300.0)) {
                     BlockPos crystalPos = this.findBestCrystalPos(cobwebPos);
                     if (crystalPos != null) {
                        this.placeCrystalAt(crystalPos);
                     }
                  }
               }
            }
         }
      }
   }

   @EventListener
   public void onEntitySpawn(EntitySpawnedEvent event) {
      if (this.targetCrystalPos != null && event.getEntity() instanceof EndCrystalEntity crystal && crystal.getBlockPos().equals(this.targetCrystalPos)) {
         this.doBreak(crystal);
         this.targetCrystalPos = null;
      }
   }

   private BlockPos findBestCrystalPos(BlockPos cobwebPos) {
      BlockPos bestPos = null;
      double bestDist = Double.MAX_VALUE;

      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            if (x != 0 || z != 0) {
               BlockPos crystalPos = new BlockPos(cobwebPos.getX() + x, cobwebPos.getY(), cobwebPos.getZ() + z);
               if (this.isValidCrystalPos(crystalPos)) {
                  double dist = crystalPos.getSquaredDistance(cobwebPos);
                  if (dist < bestDist) {
                     bestDist = dist;
                     bestPos = crystalPos;
                  }
               }
            }
         }
      }

      return bestPos;
   }

   private boolean isValidCrystalPos(BlockPos pos) {
      BlockPos below = pos.down();
      return (BlockUtil.getBlock(below) == Blocks.BEDROCK || BlockUtil.getBlock(below) == Blocks.OBSIDIAN)
         && BlockUtil.getClickSideStrict(below) != null
         && mc.world.isAir(pos)
         && mc.world.isAir(pos.up());
   }

   private void placeCrystalAt(BlockPos pos) {
      int crystalSlot = InventoryUtil.findItem(Items.END_CRYSTAL);
      if (crystalSlot != -1) {
         boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
         int oldSlot = mc.player.getInventory().selectedSlot;
         if (!offhand) {
            InventoryUtil.switchToSlot(crystalSlot);
         }

         BlockPos obsPos = pos.down();
         Direction facing = BlockUtil.getClickSide(obsPos);
         BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, SwingSide.All);
         if (!offhand) {
            InventoryUtil.switchToSlot(oldSlot);
         }

         this.targetCrystalPos = pos;
         this.placeTimer.reset();
         this.breakTimer.reset();
      }
   }

   private void breakCrystalAt(BlockPos pos) {
      for (EndCrystalEntity crystal : BlockUtil.getEndCrystals(
         new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1))
      )) {
         if (crystal.isAlive()) {
            this.doBreak(crystal);
         }
      }
   }

   private void doBreak(EndCrystalEntity crystal) {
      mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
      mc.player.resetLastAttackedTicks();
      EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
   }
}
