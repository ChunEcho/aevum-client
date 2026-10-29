package dev.chun.mod.modules.impl.player;

import net.minecraft.util.math.BlockPos;

public final class PacketMineFix {
   private static final BlockPos FALLBACK = new BlockPos(0, 0, 0);

   private PacketMineFix() {
   }

   public static BlockPos safePos(BlockPos BlockPos) {
      return BlockPos == null ? FALLBACK : BlockPos;
   }
}
