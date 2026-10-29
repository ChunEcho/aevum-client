package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.misc.ClientUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.player.InteractTweaks;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class MaceGodPlus extends Module {
   public static MaceGodPlus INSTANCE;
   private final SliderSetting attackRange = this.add(new SliderSetting("AttackRange", 6.0, 1.0, 20.0, 0.1));
   private final SliderSetting voidY = this.add(new SliderSetting("VoidY", -70.0, -500.0, 320.0, 1.0));
   private final SliderSetting aPT = this.add(new SliderSetting("APT", 1.0, 1.0, 20.0, 1.0));
   private final BooleanSetting autoMace = this.add(new BooleanSetting("AutoMace", true));
   private final BooleanSetting swap = this.add(new BooleanSetting("Swap", false).setParent());
   private final BooleanSetting breachSwap;
   private final BooleanSetting noCrystal;
   private final BooleanSetting inventorySwap;
   private final BooleanSetting customDelay;
   private final SliderSetting attackDelay;
   private final BooleanSetting mobs;
   private final BooleanSetting disableInteract;
   private final BooleanSetting autoCrystalSync;
   private final BooleanSetting ghostHand;
   private int count1;
   private boolean flag1;
   private boolean flag2;
   private boolean flag3;
   private final Timer timer1;
   private boolean flag4;
   private int count2;

   public MaceGodPlus() {
      super("MaceGodPlus", "1-tick void teleport, attack, restore for massive Mace fall damage.", Module.Category.Mace);
      String var1 = "BreachSwap";
      boolean var2 = false;
      BooleanSetting var3 = this.getSwap();
      this.breachSwap = this.add(new BooleanSetting("BreachSwap", false, var3::isOpen));
      String var4 = "NoCrystal";
      boolean var5 = true;
      BooleanSetting var6 = this.getSwap();
      this.noCrystal = this.add(new BooleanSetting("NoCrystal", true, var6::isOpen));
      String var7 = "InventorySwap";
      boolean var8 = false;
      BooleanSetting var9 = this.getSwap();
      this.inventorySwap = this.add(new BooleanSetting("InventorySwap", false, var9::isOpen));
      this.customDelay = this.add(new BooleanSetting("CustomDelay", false).setParent());
      String var10 = "AttackDelay";
      double var11 = 500.0;
      double var13 = 0.0;
      double var15 = 2000.0;
      double var17 = 10.0;
      BooleanSetting var19 = this.wy88();
      this.attackDelay = this.add(new SliderSetting("AttackDelay", 500.0, 0.0, 2000.0, 10.0, var19::isOpen)).setSuffix("ms");
      this.mobs = this.add(new BooleanSetting("Mobs", false));
      this.disableInteract = this.add(new BooleanSetting("DisableInteract", true));
      this.autoCrystalSync = this.add(new BooleanSetting("AutoCrystalSync", false));
      this.ghostHand = this.add(new BooleanSetting("GhostHand", false));
      this.setCount1(-1);
      this.setFlag12(false);
      this.m_5D9l(false);
      this.MAPHRl(false);
      this.timer1 = new Timer();
      this.m_9j00365(false);
      this.m_2bpJ1j(-1);
      this.setChinese("\u865a\u7a7a\u9524plus");
      INSTANCE = this;
   }

   public static boolean m_85jd9B() {
      boolean var0 = false;
      return INSTANCE != null && INSTANCE.isOn();
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.setFlag12(false);
      this.setCount1(-1);
      this.m_9j00365(false);
      this.m_2bpJ1j(-1);
      if (this.getDisableInteract().getValue() && InteractTweaks.INSTANCE != null && InteractTweaks.INSTANCE.isOn()) {
         InteractTweaks.INSTANCE.disable();
         this.m_5D9l(true);
      }

      if (this.getAutoMace().getValue() && !this.getGhostHand().getValue()) {
         this.resetState();
      }

      if (this.getAutoCrystalSync().getValue()) {
         AutoCrystal var1 = AutoCrystal.INSTANCE;
         if (AutoCrystal.INSTANCE != null && !var1.isOn()) {
            var1.enable();
            this.MAPHRl(true);
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.isFlag42()) {
         this.P3HA();
         this.m_9j00365(false);
      }

      this.m_2bpJ1j(-1);
      if (this.isFlag22() && InteractTweaks.INSTANCE != null && !InteractTweaks.INSTANCE.isOn()) {
         InteractTweaks.INSTANCE.enable();
      }

      this.m_5D9l(false);
      this.m_01WgnBj();
      if (this.getAutoCrystalSync().getValue() && this.isFlag32()) {
         AutoCrystal var1 = AutoCrystal.INSTANCE;
         if (var1 != null && var1.isOn()) {
            var1.disable();
         }

         this.MAPHRl(false);
      }
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!var1.isPost()) {
         if (!nullCheck() && mc.getNetworkHandler() != null) {
            Entity var2 = this.NsxQb3();
            if (var2 == null) {
               if (this.getAutoMace().getValue() && !this.getGhostHand().getValue()) {
                  this.resetState();
               }
            } else if (!this.wy88().getValue() || this.getTimer12().passedMs((double)this.getAttackDelay().getValueInt())) {
               this.m_9j00365(false);
               boolean var3 = this.getGhostHand().getValue();
               if (!var3 && this.getAutoMace().getValue()) {
                  this.resetState();
               }

               if (var3 && mc.player.getMainHandStack().getItem() != Items.MACE) {
                  this.m_9j00365(this.m_2dwm4G());
                  if (!this.isFlag42()) {
                     return;
                  }
               }

               try {
                  double var4 = mc.player.getX();
                  double var6 = mc.player.getY();
                  double var8 = mc.player.getZ();
                  double var10 = this.m_86k44().getValue();
                  int var12 = (int)this.getAPT().getValue();

                  for (int var13 = 0; var13 < var12; var13++) {
                     this.m_2Z0E9(var4, var10, var8);
                     this.QVDe9A6(var2);
                  }

                  this.m_2Z0E9(var4, var6, var8);
               } finally {
                  if (this.isFlag42()) {
                     this.P3HA();
                     this.m_9j00365(false);
                  }
               }

               if (this.wy88().getValue()) {
                  this.getTimer12().reset();
               }
            }
         }
      }
   }

   private boolean m_2dwm4G() {
      int var1 = InventoryUtil.findItemInventorySlot(Items.MACE);
      if (var1 == -1) {
         return false;
      } else {
         this.m_2bpJ1j(var1);
         ClientUtil.FgmR(var1, mc.player.getInventory().selectedSlot);
         return mc.player.getMainHandStack().getItem() == Items.MACE;
      }
   }

   private void P3HA() {
      if (this.getCount22()
         != (
            (
                     "\ue442\u7f1a\ufd82\u0257\u682d\u3f70\u8ce4\u444c\uebd0\ua331\u2f11\ua849\u2a50\ue82e\u968f\uba8b\u8fe1\u2218\uaa43\ubef0\ud57a\u4219\u14fc\u21e7\u17aa\u304d\u8407\ubcc6\u82a6\ueb8e\u5f47\u3d79\u07a3\u9a86\u0ae0\u1f2c\uff81\ua073\u9430\udcb0\u26f6\u8ed5\uef60\u71f8\ub04e\u8a59\u2d9c\u448f\u0ab3\u3223\ucd7b\u83bd\uae6e\u2001\uf82f\ub68d\u3e1d\ub0b1\u85e4\u382a\u2cea\u7bb5\u7a0b\u96b1\u560d\u0d3b\u314e\ua1a4\u18a5\u31ed\ud59d\u0567\ubecd\u030f\uf985\u64b5\u2c34\u5bef\u5d38\u43aa\u4de8\u07fe\ua34e\ub593\u1729\u238f\u9460\uf70f\uf996\uf225\u050f\uc366\ub6b1\u48b0\u7511\u0b41\u96e7\ude85\u99a4\uc0e2\ub85d\ub32f\u2b4a\u4664\u3c91\uffc8\ufc28\u8662\u3c91\u8cb9\u8f70\ubee6\u255e\ub2d9\u0f6e\u5842\ueecd\u5a86\ucdf3\u2a28\u5088\u2a0f\ufafd\ud939\u4574\udc61\ua5af\u0ec0\u9b40\u9ad0\ucb9a\u2ca1\ub72f\u747c\u370d\u63f9\u5672\u27a0\u67df\uc66e\u3b2f\uf608\u1369\u9e49\uf889\ucbca\uf644\ud429\u7e9b\uce2f\ua553\u8715\uac01\u53a4\u6fcc\u9f9f\uad82\u527e\ue3a7\ue9f3\u2f12\ucda2\u814d\ud632\u5fe3\uf8c1\u6527\udcbc\u2c13\u0c1d\u5fb6\u9bec\u56f4\u94b8\u2eaa\u3efb\u9c95\ua38b\u212c\u3645\u5952\u0b7a\ucb04\ua2d9\ub1e2\u68d8\uc65d\u7d4e\udba1\u4e2d\ud474\ud554\u175d\u0957\u3a1b\u4ae0\ue3b8\u956a\u9729\uc0db\u2db9\ubfcd\u5da4\u2d57\u8720\ud757\u556e\uba0c\ud0d4\ufeed\u8262\u0eef\u21a9\u47ab\u82d8\ud678\ub396\u9a69\u1478\u5770\u3dbb\u7e20\u9d51\u4bc9\u3711\u5e45\uc803\u1108\u05a2\u1963\uf56f\u5a2f\ufced\u87e7\u4ec2\u9ee7\u529d\u8e80\u4937\u1f63\u9820\uafa1\ua4b4\ud80a\ud51a\ud846\uc3d3\ue78a\u118a\u7d25\ue585\u8121\u3b8d\u48db\uc5ce\u6e0c\u21d1\u1def\u0717\ua864\udbe6\u9b85\ua84c\u3ce7\u86cf\u7222\u6e24\u9a60\ue378\u8be8\u11fd\u556d\u0728\u7b62\u266c\u54c8\u9444\u6967\u5055\ua531\uc628\uf3ae\ue8e3\uae16\u32f4\u436b\ub494\u36e8\u22d4\u7ece\u3be3\u06a2\ud6a6\u051d\u9c35\ub398\u940a\u53ad\u654b\ubad7\u677d\ubf8a\u05af\u7bf2\u918e\u5d62\ue235?\u4730\u7de1\udfb1\ucb9c\ua65a\uf5d6\u595b\u847b\uf61c\u762c\ud6d2\uf020\u2997\ufd33\ucdd3\uc890\ua194\u05f0\u943e\u1a47\u25d1\u67e0\uaa64\ue2bd\u849c\uda66\uf08e\u19fb\u73a0\u85d6\u17f6\u4786\u07fc\ud433\ub3cf\u5df3\ubdb9\u85af\u138b\u4f7f\u63bf\ueaaa\ue3fa\uae43\uc1c6\u7e7e\u3b0f\u1fcf\u6a9a\u2323\uffc3\u4b4b\u0583\u6ea9\uf569\u3ddd\u8e12\u28bc\u88cb\u5ad5\u6b55\ueec7\u9fbe\uc680\u7882\u58a6\uce2e\uc568\u9a38\u44b6\u7853\u9256\ub509\ue7f1\u36e1\u682b\u719c\udbbf\u7de1\u3246\u400b\u2aa9\ufbde\u00bb\ue8e3\uf6c8\ub74c\ufd2a\u7611\u7522\u7272\ud777\ub101\ua2dc\uefae\u85e1\u1844\u8738\u7cb1\u8d74\ube63\u6139\uca74\u362c\u4129\uec69\u491f\uba77\uc15f\ud8e5\u99bb\ub1c9\uee10\u271f\u169d\u675c\ud36e\ue596\u9e8f\u5029\u1d84\u7e63\uaf9d\udf83\uf96e\u6756\ua8be\u7fc1\u64a0\u5cc3\uecc0\ub7c8\u6b19\u8a40\uc7c1\u82b1\u52f5\u4ace\u8a26\u5905\u4b6f\u0ab5\u93f6\udc37\udd77\ud8db\ud163\uf99c\u52ca\u6561\ub901\uafef\u7afb\u5d10\uf2f1\u2da9\ud58b\uf62c\uea61\uf56b\u7547\udab4\u5257\ub02e\uca08\ua2ee\u39af\ue1bc\ucc38\u2c09\u1b42\u7d75\u347f\u6656\ua66d\uc891\u874b\u39dd\ue852\ub96a\u9e1b\u19a3\u45d3\u3448\ua951\u98c4\u31a1\ua76e\u64c1\uc58c\udbdf\ue651\u9d16\ue027\u1346\uc145\ud3a8\u54f2\u3a35\u25da\uf26a\u2b99\ud159\ud12d\uae13\u9b5f\ufe30\u10b4\uaeea\u9a9f\u6bd0\u4f84\uf9d8\uf670\ud5ab\ubd24\ueb35\u351d\u373f\u7a3f\u193a\ueeeb\ude39\uc603\ufa66\u6c82\u0f7c\uf592\u8cf3\uece6\ua44c\u9ddc\u9d34\u02cf\u832d\u55dd\uc821\u1349\ua634\u0b26\u174c\u0974\uf94d\u6d05\u580f\u3a2a\uf814\ua3c9\u1763\ue39f\u0bc7\u677b\u6e41\u8abc\u433e\u36e8\u1ceb\u6602u\ud431\u6543\ucdb6\u1c7c\ucd9c\u2e93\uea0f\u4df9\u9a91\u2065\u72e3\u82dd\u9c7a\ud931\ue83d\u6543\u0ee1\u5de4\u779f\ub01f\uaa37\u2aae\ud32c\ud02e\uea5e"
                              .charAt(562)
                           << 16
                        | "\ue442\u7f1a\ufd82\u0257\u682d\u3f70\u8ce4\u444c\uebd0\ua331\u2f11\ua849\u2a50\ue82e\u968f\uba8b\u8fe1\u2218\uaa43\ubef0\ud57a\u4219\u14fc\u21e7\u17aa\u304d\u8407\ubcc6\u82a6\ueb8e\u5f47\u3d79\u07a3\u9a86\u0ae0\u1f2c\uff81\ua073\u9430\udcb0\u26f6\u8ed5\uef60\u71f8\ub04e\u8a59\u2d9c\u448f\u0ab3\u3223\ucd7b\u83bd\uae6e\u2001\uf82f\ub68d\u3e1d\ub0b1\u85e4\u382a\u2cea\u7bb5\u7a0b\u96b1\u560d\u0d3b\u314e\ua1a4\u18a5\u31ed\ud59d\u0567\ubecd\u030f\uf985\u64b5\u2c34\u5bef\u5d38\u43aa\u4de8\u07fe\ua34e\ub593\u1729\u238f\u9460\uf70f\uf996\uf225\u050f\uc366\ub6b1\u48b0\u7511\u0b41\u96e7\ude85\u99a4\uc0e2\ub85d\ub32f\u2b4a\u4664\u3c91\uffc8\ufc28\u8662\u3c91\u8cb9\u8f70\ubee6\u255e\ub2d9\u0f6e\u5842\ueecd\u5a86\ucdf3\u2a28\u5088\u2a0f\ufafd\ud939\u4574\udc61\ua5af\u0ec0\u9b40\u9ad0\ucb9a\u2ca1\ub72f\u747c\u370d\u63f9\u5672\u27a0\u67df\uc66e\u3b2f\uf608\u1369\u9e49\uf889\ucbca\uf644\ud429\u7e9b\uce2f\ua553\u8715\uac01\u53a4\u6fcc\u9f9f\uad82\u527e\ue3a7\ue9f3\u2f12\ucda2\u814d\ud632\u5fe3\uf8c1\u6527\udcbc\u2c13\u0c1d\u5fb6\u9bec\u56f4\u94b8\u2eaa\u3efb\u9c95\ua38b\u212c\u3645\u5952\u0b7a\ucb04\ua2d9\ub1e2\u68d8\uc65d\u7d4e\udba1\u4e2d\ud474\ud554\u175d\u0957\u3a1b\u4ae0\ue3b8\u956a\u9729\uc0db\u2db9\ubfcd\u5da4\u2d57\u8720\ud757\u556e\uba0c\ud0d4\ufeed\u8262\u0eef\u21a9\u47ab\u82d8\ud678\ub396\u9a69\u1478\u5770\u3dbb\u7e20\u9d51\u4bc9\u3711\u5e45\uc803\u1108\u05a2\u1963\uf56f\u5a2f\ufced\u87e7\u4ec2\u9ee7\u529d\u8e80\u4937\u1f63\u9820\uafa1\ua4b4\ud80a\ud51a\ud846\uc3d3\ue78a\u118a\u7d25\ue585\u8121\u3b8d\u48db\uc5ce\u6e0c\u21d1\u1def\u0717\ua864\udbe6\u9b85\ua84c\u3ce7\u86cf\u7222\u6e24\u9a60\ue378\u8be8\u11fd\u556d\u0728\u7b62\u266c\u54c8\u9444\u6967\u5055\ua531\uc628\uf3ae\ue8e3\uae16\u32f4\u436b\ub494\u36e8\u22d4\u7ece\u3be3\u06a2\ud6a6\u051d\u9c35\ub398\u940a\u53ad\u654b\ubad7\u677d\ubf8a\u05af\u7bf2\u918e\u5d62\ue235?\u4730\u7de1\udfb1\ucb9c\ua65a\uf5d6\u595b\u847b\uf61c\u762c\ud6d2\uf020\u2997\ufd33\ucdd3\uc890\ua194\u05f0\u943e\u1a47\u25d1\u67e0\uaa64\ue2bd\u849c\uda66\uf08e\u19fb\u73a0\u85d6\u17f6\u4786\u07fc\ud433\ub3cf\u5df3\ubdb9\u85af\u138b\u4f7f\u63bf\ueaaa\ue3fa\uae43\uc1c6\u7e7e\u3b0f\u1fcf\u6a9a\u2323\uffc3\u4b4b\u0583\u6ea9\uf569\u3ddd\u8e12\u28bc\u88cb\u5ad5\u6b55\ueec7\u9fbe\uc680\u7882\u58a6\uce2e\uc568\u9a38\u44b6\u7853\u9256\ub509\ue7f1\u36e1\u682b\u719c\udbbf\u7de1\u3246\u400b\u2aa9\ufbde\u00bb\ue8e3\uf6c8\ub74c\ufd2a\u7611\u7522\u7272\ud777\ub101\ua2dc\uefae\u85e1\u1844\u8738\u7cb1\u8d74\ube63\u6139\uca74\u362c\u4129\uec69\u491f\uba77\uc15f\ud8e5\u99bb\ub1c9\uee10\u271f\u169d\u675c\ud36e\ue596\u9e8f\u5029\u1d84\u7e63\uaf9d\udf83\uf96e\u6756\ua8be\u7fc1\u64a0\u5cc3\uecc0\ub7c8\u6b19\u8a40\uc7c1\u82b1\u52f5\u4ace\u8a26\u5905\u4b6f\u0ab5\u93f6\udc37\udd77\ud8db\ud163\uf99c\u52ca\u6561\ub901\uafef\u7afb\u5d10\uf2f1\u2da9\ud58b\uf62c\uea61\uf56b\u7547\udab4\u5257\ub02e\uca08\ua2ee\u39af\ue1bc\ucc38\u2c09\u1b42\u7d75\u347f\u6656\ua66d\uc891\u874b\u39dd\ue852\ub96a\u9e1b\u19a3\u45d3\u3448\ua951\u98c4\u31a1\ua76e\u64c1\uc58c\udbdf\ue651\u9d16\ue027\u1346\uc145\ud3a8\u54f2\u3a35\u25da\uf26a\u2b99\ud159\ud12d\uae13\u9b5f\ufe30\u10b4\uaeea\u9a9f\u6bd0\u4f84\uf9d8\uf670\ud5ab\ubd24\ueb35\u351d\u373f\u7a3f\u193a\ueeeb\ude39\uc603\ufa66\u6c82\u0f7c\uf592\u8cf3\uece6\ua44c\u9ddc\u9d34\u02cf\u832d\u55dd\uc821\u1349\ua634\u0b26\u174c\u0974\uf94d\u6d05\u580f\u3a2a\uf814\ua3c9\u1763\ue39f\u0bc7\u677b\u6e41\u8abc\u433e\u36e8\u1ceb\u6602u\ud431\u6543\ucdb6\u1c7c\ucd9c\u2e93\uea0f\u4df9\u9a91\u2065\u72e3\u82dd\u9c7a\ud931\ue83d\u6543\u0ee1\u5de4\u779f\ub01f\uaa37\u2aae\ud32c\ud02e\uea5e"
                           .charAt(563)
                  )
                  + 750915397
               ^ -1879066158
         )) {
         if (ClientUtil.inInventory()) {
            mc.interactionManager
               .clickSlot(mc.player.currentScreenHandler.syncId, this.getCount22(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.player.getInventory().updateItems();
         }

         this.m_2bpJ1j(-1);
      }
   }

   private void QVDe9A6(Entity var1) {
      if (this.getSwap().getValue()) {
         if (this.getNoCrystal().getValue() && var1 instanceof EndCrystalEntity) {
            this.m_2Z0E9(var1);
            return;
         }

         int var2 = this.m_9F1wHjo();
         if (var2 != -1) {
            int var3 = mc.player.getInventory().selectedSlot;
            this.hjz5DL9KO(var2);
            this.m_2Z0E9(var1);
            if (this.getInventorySwap().getValue()) {
               this.hjz5DL9KO(var2);
               ClientUtil.Njn83();
            } else {
               this.hjz5DL9KO(var3);
            }

            return;
         }
      }

      this.m_2Z0E9(var1);
   }

   private void m_2Z0E9(Entity var1) {
      mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(var1, mc.player.isSneaking()));
      mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
   }

   private void hjz5DL9KO(int var1) {
      if (this.getInventorySwap().getValue()) {
         ClientUtil.FgmR(var1, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(var1);
      }
   }

   private int m_9F1wHjo() {
      boolean var1 = false;
      boolean var2 = this.V6s4().getValue();
      if (!((var2 | true) & true)) {
      }

      if (this.getInventorySwap().getValue()) {
         for (int var5 = 0; var5 < 36; var5++) {
            if (this.QVDe9A6(mc.player.getInventory().getStack(var5), var2)) {
               return var5 < 9 ? var5 + 36 : var5;
            }
         }

         return -1;
      } else {
         for (int var3 = 0; var3 < 9; var3++) {
            if (this.QVDe9A6(mc.player.getInventory().getStack(var3), var2)) {
               return var3;
            }
         }

         return -1;
      }
   }

   private boolean QVDe9A6(ItemStack var1, boolean var2) {
      return var1.getItem() == Items.MACE
         && (
            !var2
               || EnchantmentHelper.getEnchantments(var1)
                  .getEnchantments()
                  .contains(mc.world.getRegistryManager().get(Enchantments.BREACH.getRegistryRef()).getEntry(Enchantments.BREACH).get())
         );
   }

   private void m_2Z0E9(double var1, double var3, double var5) {
      mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var1, var3, var5, false));
   }

   private void resetState() {
      int var1 = this.wL0p();
      if (var1 != -1) {
         if (!this.isFlag12()) {
            this.setCount1(mc.player.getInventory().selectedSlot);
            this.setFlag12(true);
         }

         if (mc.player.getInventory().selectedSlot != var1) {
            mc.player.getInventory().selectedSlot = var1;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(var1));
         }
      }
   }

   private void m_01WgnBj() {
      if (this.isFlag12()
         && this.getCount12() != -1
         && mc.player != null
         && mc.getNetworkHandler() != null
         && mc.player.getInventory().selectedSlot != this.getCount12()) {
         mc.player.getInventory().selectedSlot = this.getCount12();
         mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.getCount12()));
      }

      this.setFlag12(false);
      this.setCount1(-1);
   }

   private int wL0p() {
      for (int var1 = 0; var1 < 9; var1++) {
         if (mc.player.getInventory().getStack(var1).getItem() == Items.MACE) {
            return var1;
         }
      }

      return -1;
   }

   private Entity NsxQb3() {
      double var1 = this.rz9dq().getValue();
      double var3 = var1 * var1;
      PlayerEntity var5 = null;
      double var6 = Double.MAX_VALUE;

      for (PlayerEntity var9 : mc.world.getPlayers()) {
         double var10;
         if (var9 != mc.player
            && var9.isAlive()
            && !var9.isDead()
            && !Aevum.FRIEND.isFriend(var9)
            && !((var10 = mc.player.squaredDistanceTo(var9)) > var3)
            && !(var10 >= var6)) {
            var6 = var10;
            var5 = var9;
         }
      }

      if (var5 != null) {
         return var5;
      } else if (!this.getMobs().getValue()) {
         return null;
      } else {
         Entity var16 = null;
         double var17 = Double.MAX_VALUE;

         for (Entity var12 : mc.world.getEntities()) {
            if (var12 instanceof MobEntity) {
               MobEntity var15 = (MobEntity)var12;
               double var13;
               if (!(var12 instanceof AnimalEntity)
                  && var15.isAlive()
                  && !var15.isDead()
                  && !((var13 = mc.player.squaredDistanceTo(var12)) > var3)
                  && !(var13 >= var17)) {
                  var17 = var13;
                  var16 = var12;
               }
            }
         }

         return var16;
      }
   }

   private SliderSetting rz9dq() {
      return this.attackRange;
   }

   private SliderSetting m_86k44() {
      return this.voidY;
   }

   private SliderSetting getAPT() {
      return this.aPT;
   }

   private BooleanSetting getAutoMace() {
      return this.autoMace;
   }

   private BooleanSetting getSwap() {
      return this.swap;
   }

   private BooleanSetting V6s4() {
      return this.breachSwap;
   }

   private BooleanSetting getNoCrystal() {
      return this.noCrystal;
   }

   private BooleanSetting getInventorySwap() {
      return this.inventorySwap;
   }

   private BooleanSetting wy88() {
      return this.customDelay;
   }

   private SliderSetting getAttackDelay() {
      return this.attackDelay;
   }

   private BooleanSetting getMobs() {
      return this.mobs;
   }

   private BooleanSetting getDisableInteract() {
      return this.disableInteract;
   }

   private BooleanSetting getAutoCrystalSync() {
      return this.autoCrystalSync;
   }

   private BooleanSetting getGhostHand() {
      return this.ghostHand;
   }

   private int getCount12() {
      return this.count1;
   }

   private void setCount1(int var1) {
      this.count1 = var1;
   }

   private boolean isFlag12() {
      return this.flag1;
   }

   private void setFlag12(boolean var1) {
      this.flag1 = var1;
   }

   private boolean isFlag22() {
      return this.flag2;
   }

   private void m_5D9l(boolean var1) {
      if (!((var1 | true) & true)) {
      }

      this.flag2 = var1;
   }

   private boolean isFlag32() {
      return this.flag3;
   }

   private void MAPHRl(boolean var1) {
      this.flag3 = var1;
   }

   private Timer getTimer12() {
      return this.timer1;
   }

   private boolean isFlag42() {
      return this.flag4;
   }

   private void m_9j00365(boolean var1) {
      if (!((var1 | true) & true)) {
      }

      this.flag4 = var1;
   }

   private int getCount22() {
      return this.count2;
   }

   private void m_2bpJ1j(int var1) {
      if (((var1 | 1) & 1) == 0) {
      }

      this.count2 = var1;
   }
}
