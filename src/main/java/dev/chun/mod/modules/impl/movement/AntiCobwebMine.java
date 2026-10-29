package dev.chun.mod.modules.impl.movement;

import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AntiCobwebMine extends Module {
   public static AntiCobwebMine INSTANCE;
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 500));
   private final BooleanSetting mineFeet = this.add(new BooleanSetting("MineFeet", true));
   private final BooleanSetting mineHead = this.add(new BooleanSetting("MineHead", false));
   private final Timer mineTimer = new Timer();

   public AntiCobwebMine() {
      super("AntiCobwebMine", "Auto-mine cobwebs at your feet/head", Module.Category.Movement);
      this.setChinese("\u9632\u86db\u7f51");
      INSTANCE = this;
   }

   @EventListener
   public void onTick(ClientTickEvent clientTickEvent) {
      if (!Module.nullCheck() && !clientTickEvent.isPost()) {
         if (this.mineTimer.passedMs(this.delay.getValue())) {
            BlockPos playerPos = EntityUtil.getPlayerPos(true);
            BlockPos up = playerPos.up();
            if (this.mineFeet.getValue() && this.isCobweb(playerPos)) {
               mc.interactionManager.attackBlock(playerPos, Direction.UP);
               this.mineTimer.reset();
            } else if (this.mineHead.getValue() && this.isCobweb(up)) {
               mc.interactionManager.attackBlock(up, Direction.UP);
               this.mineTimer.reset();
            }
         }
      }
   }

   private boolean isCobweb(BlockPos BlockPos) {
      return mc.world.getBlockState(BlockPos).getBlock() == Blocks.COBWEB;
   }
}
