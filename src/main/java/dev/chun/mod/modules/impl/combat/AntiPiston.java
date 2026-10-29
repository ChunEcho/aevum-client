package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.AntiCheat;
import dev.chun.mod.modules.impl.movement.FastWeb;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AntiPiston extends Module {
   public static AntiPiston INSTANCE;
   public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
   public final BooleanSetting swing = this.add(new BooleanSetting("Swing", true));
   public final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true));
   public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50.0, 0.0, 500.0, 1.0));
   private final Timer timer = new Timer();
   private boolean suppressedFastWeb = false;

   public AntiPiston() {
      super("AntiPiston", "Places a cobweb above you when your lower body is stuck in obsidian", Module.Category.Combat);
      this.setChinese("\u9632\u6d3b\u585e");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      if (FastWeb.INSTANCE != null && FastWeb.INSTANCE.isOn()) {
         FastWeb.INSTANCE.pauseForAntiPiston();
         this.suppressedFastWeb = true;
      }
   }

   @Override
   public void onDisable() {
      if (this.suppressedFastWeb && FastWeb.INSTANCE != null) {
         FastWeb.INSTANCE.resumeAfterAntiPiston();
      }

      this.suppressedFastWeb = false;
   }

   @EventListener
   public void onTick(ClientTickEvent event) {
      if (!Module.nullCheck() && !event.isPost()) {
         if (this.timer.passed((long)this.placeDelay.getValueInt())) {
            BlockPos headWebPos = this.getHeadWebPos();
            if (headWebPos != null) {
               if (this.getWebSlot() != -1) {
                  if (this.placeWeb(headWebPos)) {
                     this.timer.reset();
                  }
               }
            }
         }
      }
   }

   private BlockPos getHeadWebPos() {
      if (mc.player != null && mc.world != null) {
         BlockPos getBlockPos = mc.player.getBlockPos();
         BlockPos isInRange = getBlockPos.down();
         BlockPos method_24517 = isInRange.down();
         if (!this.isObsidian(isInRange) && !this.isObsidian(method_24517)) {
            return null;
         } else if (!BlockUtil.canCollide(mc.player, mc.player.getBoundingBox())) {
            return null;
         } else {
            BlockPos isHolding = getBlockPos.up(1);
            if (!BlockUtil.canReplace(isHolding)) {
               return null;
            } else {
               return BlockUtil.getPlaceSide(isHolding, 5.0) == null && !BlockUtil.allowAirPlace() ? null : isHolding;
            }
         }
      } else {
         return null;
      }
   }

   private boolean isObsidian(BlockPos pos) {
      return BlockUtil.getBlock(pos) == Blocks.OBSIDIAN;
   }

   private boolean placeWeb(BlockPos pos) {
      Direction placeSide = BlockUtil.getPlaceSide(pos);
      BlockPos offset = pos;
      Direction side = Direction.DOWN;
      if (placeSide == null) {
         if (!BlockUtil.allowAirPlace()) {
            return false;
         }
      } else {
         offset = pos.offset(placeSide);
         side = placeSide.getOpposite();
      }

      Vec3d iterate = offset.toCenterPos()
         .add((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5);
      if (this.rotate.getValue()) {
         Aevum.ROTATION.lookAt(iterate);
      }

      int webSlot = this.getWebSlot();
      int selectedSlot = mc.player.getInventory().selectedSlot;
      this.doSwap(webSlot);
      EntityUtil.swingHand(Hand.MAIN_HAND, AntiCheat.INSTANCE.interactSwing.getValue());
      BlockHitResult bhr = new BlockHitResult(iterate, side, offset, false);
      Module.sendSequencedPacket(n -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, n));
      if (this.inventorySwap.getValue()) {
         this.doSwap(webSlot);
         EntityUtil.syncInventory();
      } else {
         this.doSwap(selectedSlot);
      }

      BlockUtil.placedPos.add(pos);
      return true;
   }

   private void doSwap(int slot) {
      if (this.inventorySwap.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }
   }

   private int getWebSlot() {
      return this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.COBWEB) : InventoryUtil.findBlock(Blocks.COBWEB);
   }
}
