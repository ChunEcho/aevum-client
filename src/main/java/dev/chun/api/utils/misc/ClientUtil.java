package dev.chun.api.utils.misc;

import dev.chun.api.utils.combat.CombatUtil;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.api.utils.player.MovementUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.impl.combat.PistonCrystal;
import net.minecraft.util.math.BlockPos;

public final class ClientUtil {
   private ClientUtil() {
   }

   public static void Njn83() {
      EntityUtil.syncInventory();
   }

   public static void FgmR(int slot, int selectedSlot) {
      InventoryUtil.inventorySwap(slot, selectedSlot);
   }

   public static boolean isMoving() {
      return MovementUtil.isMoving();
   }

   public static boolean inInventory() {
      return EntityUtil.inInventory();
   }

   public static void m_1z8dKi(BlockPos pos, boolean rotate, boolean packet) {
      BlockUtil.placeBlock(pos, rotate, packet);
   }

   public static boolean m_7lzNX(PistonCrystal module, boolean b1, boolean b2, boolean b3) {
      return module.check(b1, b2, b3);
   }

   public static void attack(BlockPos pos, boolean rotate, boolean eatingPause) {
      CombatUtil.attackCrystal(pos, rotate, eatingPause);
   }
}
