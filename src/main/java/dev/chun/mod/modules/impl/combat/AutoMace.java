package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.MoveEvent;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoMace extends Module {
   public static AutoMace INSTANCE;
   public final SliderSetting targetRange = this.add(new SliderSetting("Target Range", 10.0, 10.0, 30.0, 1.0));
   public final SliderSetting attackRange = this.add(new SliderSetting("Attack Range", 6.0, 3.0, 6.0, 0.1));
   public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
   public final BooleanSetting track = this.add(new BooleanSetting("Track", true));
   public final BooleanSetting lagback = this.add(new BooleanSetting("Lagback", true));
   private PlayerEntity ref1;
   private long time1;
   private boolean flag1;

   public AutoMace() {
      super("AutoMace", "Automatically attacks players with mace after falling", Module.Category.Mace);
      this.EljF(null);
      this.setLength(0L);
      this.setFlag12(false);
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (!nullCheck()) {
         if (this.hLIV8S() && mc.player.isFallFlying()) {
            mc.player.stopFallFlying();
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.EljF(null);
      this.setLength(0L);
      this.setFlag12(false);
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!nullCheck()) {
         this.EljF(this.NsxQb3());
         if (this.getRef1() != null && (double)mc.player.distanceTo(this.getRef1()) <= this.attackRange.getValue()) {
            int var2 = this.g5hXzPW(Items.MACE);
            if (var2 == -1) {
               return;
            }

            if (this.rotate.getValue()) {
               Aevum.ROTATION.lookAt(MathUtil.getClosestPointToBox(mc.player.getEyePos(), this.getRef1().getBoundingBox()));
            }

            if (System.currentTimeMillis() - this.getTime12() > 1000L) {
               this.N75Vj(var2);
               this.setLength(System.currentTimeMillis());
            }
         }
      }
   }

   @EventListener
   public void onPacketReceive(PacketEvent.Receive var1) {
      if (var1.getPacket() instanceof PlayerPositionLookS2CPacket && this.isFlag12()) {
         mc.player.networkHandler.sendPacket(new OnGroundOnly(mc.player.isOnGround()));
         this.setFlag12(false);
      }
   }

   @EventListener
   public void onMove(MoveEvent var1) {
      if (this.track.getValue() && !nullCheck() && !mc.player.isOnGround() && this.getRef1() != null) {
         if ((double)mc.player.distanceTo(this.getRef1()) <= this.attackRange.getValue()) {
            return;
         }

         if (System.currentTimeMillis() - this.getTime12() < 200L) {
            return;
         }

         if (mc.player.squaredDistanceTo(this.getRef1().getX(), mc.player.getY(), this.getRef1().getZ()) > 36.0) {
            if (!mc.player.isFallFlying()) {
               this.Mpg1COX();
            }
         } else if (mc.player.isFallFlying()) {
            this.m_6oeO0Kq();
         }

         Vec3d var2 = mc.player.getPos();
         Vec3d var3 = this.getRef1().getPos();
         double var4 = var3.x - var2.x;
         double var6 = var3.z - var2.z;
         double var8 = Math.sqrt(var4 * var4 + var6 * var6);
         if (var8 > 0.0) {
            double var10 = var4 / var8;
            double var12 = var6 / var8;
            double var14 = mc.player.isFallFlying() ? 0.15 : 0.2873;
            var1.setX(var10 * var14);
            var1.setZ(var12 * var14);
         }
      }
   }

   private void N75Vj(int var1) {
      int var2 = mc.player.getInventory().selectedSlot;
      Vec3d var3 = mc.player.getPos();
      if (var1 != var2) {
         mc.player.getInventory().selectedSlot = var1;
      }

      mc.interactionManager.attackEntity(mc.player, this.getRef1());
      mc.player.swingHand(Hand.MAIN_HAND);
      if (var1 != var2) {
         mc.player.getInventory().selectedSlot = var2;
      }

      if (this.lagback.getValue()) {
         mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
         mc.player.networkHandler.sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(), false));
         mc.player.networkHandler.sendPacket(new PositionAndOnGround(var3.x, var3.y, var3.z, false));
         this.setFlag12(true);
      }

      if (this.rotate.getValue()) {
         Aevum.ROTATION.snapBack();
      }
   }

   private PlayerEntity NsxQb3() {
      PlayerEntity var1 = null;
      double var2 = Double.MAX_VALUE;

      for (PlayerEntity var5 : mc.world.getPlayers()) {
         double var6;
         if (var5 != mc.player
            && var5.isAlive()
            && !(var5.getHealth() <= 0.0F)
            && !(mc.player.squaredDistanceTo(var5) > MathHelper.square(this.targetRange.getValue()))
            && !((var6 = (double)mc.player.distanceTo(var5)) >= var2)) {
            var2 = var6;
            var1 = var5;
         }
      }

      return var1;
   }

   private int g5hXzPW(Item var1) {
      for (int var2 = 0; var2 < 9; var2++) {
         if (mc.player.getInventory().getStack(var2).getItem() == var1) {
            return var2;
         }
      }

      return -1;
   }

   private boolean hLIV8S() {
      return mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
   }

   private void Mpg1COX() {
      for (int var1 = (
                  "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99"
                           .charAt(168)
                        << 16
                     | "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99"
                        .charAt(169)
               )
               - 1449807115
            ^ 1550697449;
         var1 < 4;
         var1++
      ) {
         if (mc.player.getInventory().getArmorStack(var1).getItem() == Items.ELYTRA) {
            mc.player.getInventory().armor.set(2, mc.player.getInventory().getArmorStack(var1));
            mc.player.getInventory().armor.set(var1, ItemStack.EMPTY);
            return;
         }
      }
   }

   private void m_6oeO0Kq() {
      int var1 = 0;

      while (true) {
         var1++;
         String var3 = "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99";
         if (var1
            >= (
               (
                        "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99"
                                 .charAt(170)
                              << 16
                           | 45007
                     )
                     - 1488784709
                  ^ 166634126
            )) {
            break;
         }

         if (var1 != 2 && mc.player.getInventory().getArmorStack(var1).isEmpty()) {
            mc.player.getInventory().armor.set(var1, mc.player.getInventory().getArmorStack(2));
            mc.player.getInventory().armor.set(2, ItemStack.EMPTY);
            break;
         }
      }
   }

   private PlayerEntity getRef1() {
      return this.ref1;
   }

   private void EljF(PlayerEntity var1) {
      this.ref1 = var1;
   }

   private long getTime12() {
      return this.time1;
   }

   private void setLength(long var1) {
      this.time1 = var1;
   }

   private boolean isFlag12() {
      return this.flag1;
   }

   private void setFlag12(boolean var1) {
      this.flag1 = var1;
   }
}
