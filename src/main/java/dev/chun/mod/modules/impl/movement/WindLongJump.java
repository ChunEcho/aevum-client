package dev.chun.mod.modules.impl.movement;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.api.utils.player.MovementUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class WindLongJump extends Module {
   public static WindLongJump INSTANCE;
   private final SliderSetting throwDelay = this.add(new SliderSetting("ThrowDelay", 70.0, 0.0, 1000.0, 10.0).setSuffix("ms"));
   private final SliderSetting upSpeed = this.add(new SliderSetting("UpSpeed", 5.0, 0.1, 10.0, 0.1));
   private final SliderSetting forwardSpeed = this.add(new SliderSetting("ForwardSpeed", 3.0, 0.5, 20.0, 0.1));
   private final SliderSetting maintainTime = this.add(new SliderSetting("MaintainTime", 50.0, 10.0, 500.0, 10.0).setSuffix("ms"));
   private final BooleanSetting antiKick = this.add(new BooleanSetting("AntiKick", true));
   private final Timer stopTimer = new Timer();
   private long boostStartTime;
   private boolean thrown;
   private boolean jumped;
   private boolean stopping;

   public WindLongJump() {
      super("HighJump", "Wind charge boost", Module.Category.Movement);
      this.setChinese("\u98ce\u5f39\u9ad8\u8df3");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      if (!nullCheck()) {
         this.thrown = false;
         this.jumped = false;
         this.boostStartTime = 0L;
         this.stopping = false;
         if (!this.throwCharge()) {
            this.disable();
         } else {
            this.thrown = true;
            this.boostStartTime = System.currentTimeMillis();
         }
      }
   }

   @Override
   public void onDisable() {
      this.thrown = false;
      this.boostStartTime = 0L;
      this.stopping = false;
      if (mc.player != null) {
         MovementUtil.setMotionX(0.0);
         MovementUtil.setMotionZ(0.0);
      }
   }

   @EventListener
   public void onUpdate(UpdateEvent updateEvent) {
      if (!nullCheck()) {
         if (this.stopping) {
            if (this.antiKick.getValue() && this.stopTimer.passed(500L)) {
               BlockUtil.clickBlock(mc.player.getBlockPos().down(), Direction.DOWN, false, Hand.MAIN_HAND);
               this.disable();
            } else if (!this.antiKick.getValue()) {
               this.disable();
            }
         } else if (this.thrown && this.boostStartTime != 0L) {
            long n = System.currentTimeMillis() - this.boostStartTime;
            long n2 = (long)this.throwDelay.getValue();
            long n3 = n2 + (long)this.maintainTime.getValue();
            if (n >= n2 && n < n3) {
               if (!this.jumped) {
                  if (mc.player.isOnGround()) {
                     mc.player.jump();
                  }

                  MovementUtil.setMotionY(this.upSpeed.getValue());
                  this.jumped = true;
               }

               double radians = Math.toRadians((double)Aevum.ROTATION.rotationYaw);
               double value = this.forwardSpeed.getValue();
               MovementUtil.setMotionX(-Math.sin(radians) * value);
               MovementUtil.setMotionZ(Math.cos(radians) * value);
            } else if (n >= n3) {
               MovementUtil.setMotionX(0.0);
               MovementUtil.setMotionZ(0.0);
               this.stopping = true;
               this.stopTimer.reset();
            }
         }
      }
   }

   private boolean throwCharge() {
      boolean b = false;
      ClientPlayerEntity player = mc.player;
      int n;
      if (player.getMainHandStack().getItem() == Items.WIND_CHARGE) {
         n = player.getInventory().selectedSlot;
      } else {
         n = InventoryUtil.findItem(Items.WIND_CHARGE);
         if (n == -1 && (n = InventoryUtil.findItemInventorySlotFromZero(Items.WIND_CHARGE)) != -1) {
            b = true;
         }
      }

      if (n == -1) {
         return false;
      } else {
         float rotationYaw = Aevum.ROTATION.rotationYaw;
         Aevum.ROTATION.snapBack();
         Aevum.ROTATION.snapAt(rotationYaw, 90.0F);
         if (player.getMainHandStack().getItem() == Items.WIND_CHARGE) {
            Module.sendSequencedPacket(n2 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n2, rotationYaw, 90.0F));
            player.swingHand(Hand.MAIN_HAND);
         } else if (b) {
            InventoryUtil.inventorySwap(n, player.getInventory().selectedSlot);
            Module.sendSequencedPacket(n2 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n2, rotationYaw, 90.0F));
            player.swingHand(Hand.MAIN_HAND);
            InventoryUtil.inventorySwap(n, player.getInventory().selectedSlot);
            EntityUtil.syncInventory();
         } else {
            int field_1725 = player.getInventory().selectedSlot;
            InventoryUtil.switchToSlot(n);
            Module.sendSequencedPacket(n2 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n2, rotationYaw, 90.0F));
            player.swingHand(Hand.MAIN_HAND);
            InventoryUtil.switchToSlot(field_1725);
         }

         Aevum.ROTATION.snapBack();
         return true;
      }
   }
}
