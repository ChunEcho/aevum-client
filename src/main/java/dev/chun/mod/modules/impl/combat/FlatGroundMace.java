package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.utils.combat.CombatUtil;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.enums.SwingSide;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class FlatGroundMace extends Module {
   public static FlatGroundMace INSTANCE;
   private final EnumSetting<Aura.Cooldown> mode = this.add(new EnumSetting<>("Mode", Aura.Cooldown.Vanilla));
   private final SliderSetting enFUJwkCj = this.add(new SliderSetting("FallHeight", 4.0, 0.5, 15.0, 0.1));
   private final SliderSetting resetDistance = this.add(new SliderSetting("ResetDistance", 0.1, 0.05, 0.5, 0.05));
   private final BooleanSetting requireMace = this.add(new BooleanSetting("RequireMace", false));
   private final BooleanSetting lockView = this.add(new BooleanSetting("LockView", true));
   private final SliderSetting splitStep = this.add(new SliderSetting("SplitStep", 0.42, 0.1, 1.0, 0.01, () -> true));
   private final SliderSetting blinkDelay = this.add(new SliderSetting("BlinkDelay", 3.0, 1.0, 20.0, 1.0, () -> this.getMode().is(Aura.Cooldown.BlinkBuffer)));
   private final SliderSetting spoofHeight = this.add(
      new SliderSetting("SpoofHeight", 12.0, 2.0, 70.0, 1.0, () -> this.getMode().is(Aura.Cooldown.MotionSpoof))
   );
   private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 1.0, 8.0, 0.5, () -> true));
   private final SliderSetting wallRange = this.add(new SliderSetting("WallRange", 4.0, 0.5, 8.0, 0.5, () -> true));
   private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> true));
   private final BooleanSetting mobs = this.add(new BooleanSetting("Mobs", false, () -> true));
   private final BooleanSetting animals = this.add(new BooleanSetting("Animals", false, () -> true));
   private final BooleanSetting villagers = this.add(new BooleanSetting("Villagers", false, () -> true));
   private final SliderSetting voidDepth = this.add(new SliderSetting("VoidDepth", 8.0, 3.0, 20.0, 1.0, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
   private final SliderSetting attackDelay = this.add(new SliderSetting("AttackDelay", 1.0, 0.0, 10.0, 1.0, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
   private final BooleanSetting spoof = this.add(new BooleanSetting("Spoof", true, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
   private final EnumSetting<FlatGroundMace.Synthetic3> vMMode = this.add(
      new EnumSetting<>("VMMode", FlatGroundMace.Synthetic3.DoublePacket, () -> this.getMode().is(Aura.Cooldown.VoidMace))
   );
   private final SliderSetting voidY = this.add(new SliderSetting("VoidY", -60.0, -100.0, -50.0, 1.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final SliderSetting attackPackets = this.add(
      new SliderSetting("AttackPackets", 3.0, 1.0, 10.0, 1.0, () -> this.getMode().is(Aura.Cooldown.VoidMace))
   );
   private final SliderSetting cycleDelay = this.add(new SliderSetting("CycleDelay", 0.0, 0.0, 1000.0, 50.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final SliderSetting vMFallHeight = this.add(
      new SliderSetting("VMFallHeight", 100.0, 20.0, 500.0, 10.0, () -> this.getMode().is(Aura.Cooldown.VoidMace))
   );
   private final BooleanSetting swing = this.add(new BooleanSetting("Swing", true, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final BooleanSetting antiWeb = this.add(new BooleanSetting("AntiWeb", true, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final BooleanSetting viewLock = this.add(new BooleanSetting("ViewLock", false, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final BooleanSetting burrowBypass = this.add(new BooleanSetting("BurrowBypass", false, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
   private final SliderSetting burrowLift = this.add(new SliderSetting("BurrowLift", 1.5, 1.0, 3.0, 0.1, () -> true));
   private final BooleanSetting autoReburrow = this.add(new BooleanSetting("AutoReburrow", false, () -> true));
   private final SliderSetting timerSpeed = this.add(new SliderSetting("TimerSpeed", 5.2, 1.0, 10.0, 0.1, () -> true));
   private final SliderSetting fallTime = this.add(new SliderSetting("FallTime", 300.0, 50.0, 1000.0, 50.0, () -> true));
   private final SliderSetting tPCooldown = this.add(new SliderSetting("TPCooldown", 5.0, 1.0, 30.0, 1.0, () -> this.getMode().is(Aura.Cooldown.Delay)));
   private double w0Nz;
   private double value1;
   private double value2;
   private double value3;
   private double value4;
   private boolean RDY2;
   private double value5;
   private final CopyOnWriteArrayList<Packet<?>> packetBuffer = new CopyOnWriteArrayList<>();
   private boolean c33y;
   private int count1;
   private boolean flag1;
   private int count2;
   private Entity target1;
   private double value6;
   private int count3;
   private int count4;
   private boolean NC9y;
   private int count5;
   private Entity target2;
   private double value7;
   private double value8;
   private double yn8T;
   private double value9;
   private int count6;
   private int count7;
   private double KfSK;
   private double value10;
   private double value11;
   private boolean flag2;
   private FlatGroundMace.Synthetic4 ref1;
   private final Timer timer1;
   private final Timer timer2;

   public FlatGroundMace() {
      super(
         "FlatGroundMace",
         "\u6a21\u62df\u9ad8\u7a7a\u4e0b\u5760\u5faa\u73af\uff0c\u89e6\u53d1\u91cd\u9524\u66b4\u51fb\u30029\u79cd\u6a21\u5f0f\u9002\u914d\u4e0d\u540c\u53cd\u4f5c\u5f0a\u73af\u5883",
         Module.Category.Mace
      );
      this.setRef12(FlatGroundMace.Synthetic4.IDLE);
      this.timer1 = new Timer();
      this.timer2 = new Timer();
      this.setChinese("\u5e73\u5730\u91cd\u9524");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         this.PRfLKxXf(this.m_2hUAl6v());
         this.BJJN(mc.player.getX());
         this.PX4C(mc.player.getY());
         this.I4l2(mc.player.getZ());
         this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
         this.O744Cf05(false);
         this.setValue52(mc.player.getY());
         this.getPacketBuffer().clear();
         this.setC33y(false);
         this.vhuem9c0(0);
         this.m_2e3B7v61(false);
         this.setCount22(0);
         this.setTarget12(null);
         this.setValue6(mc.player.getY());
         this.setCount32(0);
         this.lw43w903(0);
         this.M8nC(false);
         this.setCount5(0);
         this.setEntity(null);
         this.HORmHtB(0);
         this.eR4J4(0);
         this.setKfSK(0.0);
         this.setValue10(0.0);
         this.n49691(0.0);
         this.setFlag22(false);
         this.setRef12(FlatGroundMace.Synthetic4.IDLE);
         this.getTimer12().reset();
         this.getTimer22().reset();
      }
   }

   @Override
   public void onDisable() {
      if (this.isC33y() && !this.getPacketBuffer().isEmpty()) {
         Iterator var1 = this.getPacketBuffer().iterator();

         while (var1.hasNext()) {
            mc.getNetworkHandler().sendPacket((Packet)var1.next());
         }
      }

      this.getPacketBuffer().clear();
      this.setC33y(false);
      this.O744Cf05(false);
      if (this.Vve9() && this.Wdpl() != null) {
         mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(this.Wdpl(), mc.player.isSneaking()));
      }

      this.m_2e3B7v61(false);
      this.setTarget12(null);
      if (this.isNC9y() && this.QcCZCW() != null && this.QcCZCW().isAlive()) {
         mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(this.QcCZCW(), mc.player.isSneaking()));
      }

      this.M8nC(false);
      this.setEntity(null);
      if (this.LOUBbq()) {
         Aevum.TIMER.reset();
         this.setFlag22(false);
      }

      this.X2YPT();
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!nullCheck()) {
         if (var1.isPre()) {
            if (this.getRequireMace().getValue() && mc.player.getMainHandStack().getItem() != Items.MACE) {
               return;
            }

            double var2 = mc.player.getY();
            double var4 = this.getW0Nz() + this.m_548i3().getValue();
            this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
            if (this.getCount42() > 0) {
               this.lw43w903(this.getCount42() - 1);
            }

            if (this.getCount32() > 0) {
               this.setCount32(this.getCount32() - 1);
            }

            switch ((Aura.Cooldown)this.getMode().getValue()) {
               case Vanilla:
                  this.lK2mnDC(var2, var4);
                  break;
               case Delay:
                  this.NsxQb3(var2, var4);
                  break;
               case VerticalSplit:
                  this.g5hXzPW(var2, var4);
                  break;
               case BlinkBuffer:
                  this.QVDe9A6(var2, var4);
                  break;
               case MotionSpoof:
                  this.attack();
                  break;
               case WindCharge:
                  this.sendPacket(var2, var4);
                  break;
               case Hybrid:
                  this.m_2Z0E9(var2, var4);
                  break;
               case TheVoid:
                  this.attack2();
                  break;
               case VoidMace:
                  this.yU1d08();
            }
         } else if (this.getLockView().getValue()) {
            mc.player.prevX = this.getValue12();
            mc.player.prevY = this.getValue2();
            mc.player.prevZ = this.getValue32();
         }
      }
   }

   @EventListener
   public void onPacketSend(PacketEvent.Send var1) {
      if (!nullCheck()) {
         Packet var2 = var1.getPacket();
         if (this.getMode().is(Aura.Cooldown.BlinkBuffer) && this.isC33y() && var2 instanceof PlayerMoveC2SPacket) {
            var1.cancel();
            this.getPacketBuffer().add(var2);
         }
      }
   }

   @EventListener
   public void onPacketReceive(PacketEvent.Receive var1) {
      if (!nullCheck()) {
         if (this.getMode().is(Aura.Cooldown.VoidMace)) {
            if (var1.getPacket() instanceof PlayerPositionLookS2CPacket var3) {
               mc.player.setPosition(var3.getX(), var3.getY(), var3.getZ());
               mc.player.setYaw(var3.getYaw());
               mc.player.setPitch(var3.getPitch());
               mc.player.fallDistance = 0.0F;
               this.X2YPT();
               if (this.LOUBbq()) {
                  Aevum.TIMER.reset();
                  this.setFlag22(false);
               }
            }
         }
      }
   }

   private void lK2mnDC(double var1, double var3) {
      if (var1 <= var3) {
         mc.player.setPosition(mc.player.getX(), this.getValue42(), mc.player.getZ());
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
      }
   }

   private void NsxQb3(double var1, double var3) {
      if (var1 <= var3 && this.getCount32() <= 0) {
         mc.getNetworkHandler().sendPacket(new PositionAndOnGround(mc.player.getX(), this.getValue42(), mc.player.getZ(), false));
         this.setCount32((int)this.m_80j5().getValue());
         mc.player.setPosition(mc.player.getX(), this.getValue42(), mc.player.getZ());
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
      }
   }

   private void g5hXzPW(double var1, double var3) {
      if (!this.isRDY2() && var1 <= var3) {
         this.O744Cf05(true);
         this.setValue52(var1);
      }

      if (this.isRDY2()) {
         if (this.XeyVw() < this.getValue42()) {
            this.setValue52(this.XeyVw() + this.rFpDuRQ().getValue());
            if (this.XeyVw() > this.getValue42()) {
               this.setValue52(this.getValue42());
            }

            mc.player.setPosition(mc.player.getX(), this.XeyVw(), mc.player.getZ());
            mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
         } else {
            this.O744Cf05(false);
         }
      }
   }

   private void QVDe9A6(double var1, double var3) {
      if (!this.isC33y() && var1 <= var3) {
         this.setC33y(true);
         this.vhuem9c0(0);
         mc.player.setPosition(mc.player.getX(), this.getValue42(), mc.player.getZ());
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
      } else {
         if (this.isC33y()) {
            this.vhuem9c0(this.getCount12() + 1);
            if (this.getCount12() >= (int)this.getBlinkDelay().getValue()) {
               Iterator var5 = this.getPacketBuffer().iterator();

               while (var5.hasNext()) {
                  mc.getNetworkHandler().sendPacket((Packet)var5.next());
               }

               this.getPacketBuffer().clear();
               this.setC33y(false);
            }
         }
      }
   }

   private void attack() {
      if (this.Vve9()) {
         if (this.getCount22() < 6) {
            mc.getNetworkHandler()
               .sendPacket(
                  new Full(
                     mc.player.getX(),
                     this.getValue62() + (double)(this.getCount22() + 1) * (this.getSpoofHeight().getValue() / 6.0),
                     mc.player.getZ(),
                     mc.player.getYaw(),
                     mc.player.getPitch(),
                     false
                  )
               );
            this.setCount22(this.getCount22() + 1);
         } else if (this.getCount22() == 6) {
            mc.getNetworkHandler().sendPacket(new Full(mc.player.getX(), this.getValue62(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false));
            this.setCount22(this.getCount22() + 1);
         } else if (this.getCount22() == 7) {
            this.setCount22(this.getCount22() + 1);
         } else if (this.getCount22() == 8) {
            if (this.Wdpl() != null && this.Wdpl().isAlive() && CombatUtil.isValid(this.Wdpl())) {
               mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(this.Wdpl(), mc.player.isSneaking()));
               mc.player.resetLastAttackedTicks();
               EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
            }

            this.setTarget12(null);
            this.m_2e3B7v61(false);
         }
      } else {
         Entity var1 = this.findTarget();
         if (var1 != null) {
            this.setTarget12(var1);
            this.m_2e3B7v61(true);
            this.setCount22(0);
            this.setValue6(mc.player.getY());
         }
      }
   }

   private Entity findTarget() {
      Entity var1 = null;
      double var2 = this.getRange().getValue();

      for (Entity var5 : Aevum.THREAD.getEntities()) {
         double var6;
         if (var5 != null
            && var5 != mc.player
            && var5.isAlive()
            && this.g5hXzPW(var5)
            && CombatUtil.isValid(var5)
            && !((var6 = mc.player.getEyePos().distanceTo(var5.getPos())) > this.getRange().getValue())
            && (mc.player.canSee(var5) || !(var6 > this.getWallRange().getValue()))
            && (var1 == null || !(var6 >= var2))) {
            var1 = var5;
            var2 = var6;
         }
      }

      return var1;
   }

   private boolean g5hXzPW(Entity var1) {
      if (var1 instanceof PlayerEntity) {
         return this.de5C().getValue();
      } else if (var1 instanceof VillagerEntity || var1 instanceof WanderingTraderEntity) {
         return this.getVillagers().getValue();
      } else {
         return var1 instanceof AnimalEntity ? this.getAnimals().getValue() : var1 instanceof MobEntity && this.getMobs().getValue();
      }
   }

   private void sendPacket(double var1, double var3) {
      if (var1 <= var3 && this.getCount42() <= 0) {
         sendSequencedPacket(var0 -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, var0, mc.player.getYaw(), mc.player.getPitch()));
         this.lw43w903(10);
         mc.player.setPosition(mc.player.getX(), this.getValue42(), mc.player.getZ());
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
      }
   }

   private void m_2Z0E9(double var1, double var3) {
      if (!this.isRDY2() && var1 <= var3) {
         this.O744Cf05(true);
         this.setValue52(var1);
      }

      if (this.isRDY2()) {
         if (this.XeyVw() < this.getValue42()) {
            this.setValue52(this.XeyVw() + (this.m_11R08A8() ? this.rFpDuRQ().getValue() * 2.0 : this.rFpDuRQ().getValue()));
            if (this.XeyVw() > this.getValue42()) {
               this.setValue52(this.getValue42());
            }

            mc.player.setPosition(mc.player.getX(), this.XeyVw(), mc.player.getZ());
            mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
         } else {
            this.O744Cf05(false);
         }
      }
   }

   private void attack2() {
      if (!this.isNC9y()) {
         Entity var1 = this.findTarget();
         if (var1 != null) {
            this.setEntity(var1);
            this.setValue7(mc.player.getX());
            this.setValue8(mc.player.getY());
            this.setYn8T(mc.player.getZ());
            this.Tv7S2((double)mc.world.getBottomY() - this.getVoidDepth().getValue());
            this.HORmHtB(0);
            this.eR4J4(0);
            this.M8nC(true);
            this.setCount5(0);
         }
      } else {
         switch (this.s83b7()) {
            case 0:
               this.NsxQb3(this.m_1ovb(), this.getValue9(), this.getYn8T());
               this.NsxQb3(this.m_1ovb(), this.getValue9() - this.getVoidDepth().getValue() / 2.0, this.getYn8T());
               this.NsxQb3(this.m_1ovb(), this.getValue9() - this.getVoidDepth().getValue(), this.getYn8T());
               this.NsxQb3(this.m_1ovb(), this.getValue82() + 1.0, this.getYn8T());
               this.setCount5(1);
               this.eR4J4((int)this.getAttackDelay().getValue());
               break;
            case 1:
               this.eR4J4(this.getCount72() + 1);
               if (this.getCount72() > (int)this.getAttackDelay().getValue()) {
                  if (this.QcCZCW() == null || !this.QcCZCW().isAlive() || !CombatUtil.isValid(this.QcCZCW())) {
                     this.M8nC(false);
                     this.setEntity(null);
                     return;
                  }

                  this.eR4J4(0);
                  mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(this.QcCZCW(), mc.player.isSneaking()));
                  mc.player.resetLastAttackedTicks();
                  if (this.getCount62() == 0) {
                     EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
                  }

                  this.HORmHtB(this.getCount62() + 1);
                  if (this.getCount62() >= 3) {
                     this.M8nC(false);
                     this.setEntity(null);
                  }
               }
         }
      }
   }

   private void NsxQb3(double var1, double var3, double var5) {
      if (this.getSpoof().getValue()) {
         mc.getNetworkHandler().sendPacket(new Full(var1, var3, var5, mc.player.getYaw(), mc.player.getPitch(), false));
      } else {
         mc.player.setPosition(var1, var3, var5);
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
      }
   }

   private void yU1d08() {
      if (this.getVMMode().is(FlatGroundMace.Synthetic3.DoublePacket)) {
         this.T066P();
      } else {
         this.Yod6t();
      }
   }

   private void T066P() {
      if (this.getTimer22().passedMs((double)((long)this.V92OL().getValue()))) {
         this.getTimer22().reset();
         Entity var1 = this.findTarget();
         if (var1 != null) {
            double var2 = mc.player.getX();
            double var4 = mc.player.getY();
            double var6 = mc.player.getZ();
            double var8 = var4;
            boolean var10 = false;
            var10 = this.m_1h01().getValue() && this.YVa9u();
            if (var10) {
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var2, var4 + this.getBurrowLift().getValue(), var6, false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(var2, var4 + this.getBurrowLift().getValue(), var6);
               }

               var4 += this.getBurrowLift().getValue();
            }

            if (this.getAntiWeb().getValue() && this.m_9Vi8()) {
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var2, var4 + 1.1, var6, false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(var2, var4 + 1.1, var6);
               }

               var4++;
            }

            mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var2, this.getVoidY().getValue(), var6, false));
            mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var2, var4 + 1.0, var6, false));
            if (!this.getViewLock().getValue()) {
               mc.player.setPosition(var2, var4 + 1.0, var6);
               mc.player.fallDistance = (float)this.gksn3().getValue();
            } else {
               mc.player.fallDistance = 0.0F;
            }

            this.attack(var1);
            if (this.jWxr().getValue() && var10) {
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(var2, var8, var6, false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(var2, var8, var6);
               }
            }
         }
      }
   }

   private void Yod6t() {
      Entity var1 = this.findTarget();
      if (var1 == null) {
         if (this.LOUBbq()) {
            Aevum.TIMER.reset();
            this.setFlag22(false);
         }

         this.X2YPT();
      } else {
         switch (this.FaTboL()) {
            case IDLE:
               if (!this.getTimer22().passedMs((double)((long)this.V92OL().getValue()))) {
                  return;
               }

               this.setKfSK(mc.player.getX());
               this.setValue10(mc.player.getY());
               this.n49691(mc.player.getZ());
               double var6 = this.getVoidY().getValue();
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(this.KfSKAccess(), var6, this.uF33(), false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(this.KfSKAccess(), var6, this.uF33());
                  mc.player.fallDistance = (float)this.gksn3().getValue();
               } else {
                  mc.player.fallDistance = 0.0F;
               }

               Aevum.TIMER.set(this.q4K6().getValueFloat());
               this.setFlag22(true);
               this.setRef12(FlatGroundMace.Synthetic4.FALLING);
               this.getTimer12().reset();
               break;
            case FALLING:
               if (!this.getTimer12().passedMs((double)((long)this.getFallTime().getValue()))) {
                  return;
               }

               double var2 = this.getValue10() + 1.0;
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(this.KfSKAccess(), var2, this.uF33(), false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(this.KfSKAccess(), var2, this.uF33());
               }

               if (this.LOUBbq()) {
                  Aevum.TIMER.reset();
                  this.setFlag22(false);
               }

               if (this.getAttackPackets().getValueInt() > 0) {
                  this.attack(var1);
               }

               this.setKfSK(mc.player.getX());
               this.setValue10(mc.player.getY());
               this.n49691(mc.player.getZ());
               double var4 = this.getVoidY().getValue();
               mc.getNetworkHandler().sendPacket(new PositionAndOnGround(this.KfSKAccess(), var4, this.uF33(), false));
               if (!this.getViewLock().getValue()) {
                  mc.player.setPosition(this.KfSKAccess(), var4, this.uF33());
                  mc.player.fallDistance = (float)this.gksn3().getValue();
               } else {
                  mc.player.fallDistance = 0.0F;
               }

               Aevum.TIMER.set(this.q4K6().getValueFloat());
               this.setFlag22(true);
               this.getTimer12().reset();
         }
      }
   }

   private void attack(Entity var1) {
      if (var1 != null && var1.isAlive()) {
         int var2 = this.getAttackPackets().getValueInt();

         for (int var3 = 0; var3 < var2; var3++) {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(var1, mc.player.isSneaking()));
         }

         mc.player.resetLastAttackedTicks();
         if (this.getSwing().getValue()) {
            EntityUtil.swingHand(Hand.MAIN_HAND, SwingSide.All);
         }
      }
   }

   private boolean m_9Vi8() {
      BlockPos var1 = mc.player.getBlockPos();
      return mc.world.getBlockState(var1).isOf(Blocks.COBWEB) || mc.world.getBlockState(var1.up()).isOf(Blocks.COBWEB);
   }

   private boolean YVa9u() {
      BlockPos var1 = mc.player.getBlockPos();
      return mc.world.getBlockState(var1).isFullCube(mc.world, var1);
   }

   private void X2YPT() {
      this.setRef12(FlatGroundMace.Synthetic4.IDLE);
      this.getTimer12().reset();
      this.getTimer22().reset();
      this.setKfSK(0.0);
      this.setValue10(0.0);
      this.n49691(0.0);
   }

   private boolean m_11R08A8() {
      boolean var1 = false;
      BlockPos var2 = mc.player.getBlockPos();
      BlockState var3 = mc.world.getBlockState(var2.north());
      return var3.blocksMovement()
         || mc.world.getBlockState(var2.south()).blocksMovement()
         || mc.world.getBlockState(var2.east()).blocksMovement()
         || mc.world.getBlockState(var2.west()).blocksMovement();
   }

   private double m_2hUAl6v() {
      BlockPos var1 = mc.player.getBlockPos();

      while (var1.getY() > mc.world.getBottomY()) {
         BlockPos var2 = var1.down();
         if (mc.world.getBlockState(var2).blocksMovement()) {
            return (double)var2.getY() + 1.0;
         }

         var1 = var2;
      }

      return mc.player.getY();
   }

   public void m_0Q8veEz() {
      this.PRfLKxXf(this.m_2hUAl6v());
      this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
   }

   @Override
   public String getInfo() {
      Object var1 = null;

      return switch ((Aura.Cooldown)this.getMode().getValue()) {
         case Vanilla -> "\u539f\u7248";
         case Delay -> "TP";
         case VerticalSplit -> "\u62c6\u5206";
         case BlinkBuffer -> "\u5ef6\u8fdf";
         case MotionSpoof -> "\u6b3a\u9a97" + this.getSpoofHeight().getValueInt() + "\u683c";
         case WindCharge -> "\u98ce\u5f39";
         case Hybrid -> "\u6df7\u5408";
         case TheVoid -> "\u865a\u7a7a" + this.getVoidDepth().getValueInt() + "\u683c";
         case VoidMace -> "VM-" + this.getVMMode().getValue().name();
      } + " " + this.enFUJwkCjAccess().getValueInt() + "\u683c";
   }

   private EnumSetting getMode() {
      return this.mode;
   }

   private SliderSetting enFUJwkCjAccess() {
      return this.enFUJwkCj;
   }

   private SliderSetting m_548i3() {
      return this.resetDistance;
   }

   private BooleanSetting getRequireMace() {
      return this.requireMace;
   }

   private BooleanSetting getLockView() {
      return this.lockView;
   }

   private SliderSetting rFpDuRQ() {
      return this.splitStep;
   }

   private SliderSetting getBlinkDelay() {
      return this.blinkDelay;
   }

   private SliderSetting getSpoofHeight() {
      return this.spoofHeight;
   }

   private SliderSetting getRange() {
      return this.range;
   }

   private SliderSetting getWallRange() {
      return this.wallRange;
   }

   private BooleanSetting de5C() {
      return this.players;
   }

   private BooleanSetting getMobs() {
      return this.mobs;
   }

   private BooleanSetting getAnimals() {
      return this.animals;
   }

   private BooleanSetting getVillagers() {
      return this.villagers;
   }

   private SliderSetting getVoidDepth() {
      return this.voidDepth;
   }

   private SliderSetting getAttackDelay() {
      return this.attackDelay;
   }

   private BooleanSetting getSpoof() {
      return this.spoof;
   }

   private EnumSetting getVMMode() {
      return this.vMMode;
   }

   private SliderSetting getVoidY() {
      return this.voidY;
   }

   private SliderSetting getAttackPackets() {
      return this.attackPackets;
   }

   private SliderSetting V92OL() {
      return this.cycleDelay;
   }

   private SliderSetting gksn3() {
      return this.vMFallHeight;
   }

   private BooleanSetting getSwing() {
      return this.swing;
   }

   private BooleanSetting getAntiWeb() {
      return this.antiWeb;
   }

   private BooleanSetting getViewLock() {
      return this.viewLock;
   }

   private BooleanSetting m_1h01() {
      return this.burrowBypass;
   }

   private SliderSetting getBurrowLift() {
      return this.burrowLift;
   }

   private BooleanSetting jWxr() {
      return this.autoReburrow;
   }

   private SliderSetting q4K6() {
      return this.timerSpeed;
   }

   private SliderSetting getFallTime() {
      return this.fallTime;
   }

   private SliderSetting m_80j5() {
      return this.tPCooldown;
   }

   private double getW0Nz() {
      return this.w0Nz;
   }

   private void PRfLKxXf(double var1) {
      this.w0Nz = var1;
   }

   private double getValue12() {
      return this.value1;
   }

   private void BJJN(double var1) {
      this.value1 = var1;
   }

   private double getValue2() {
      return this.value2;
   }

   private void PX4C(double var1) {
      this.value2 = var1;
   }

   private double getValue32() {
      return this.value3;
   }

   private void I4l2(double var1) {
      this.value3 = var1;
   }

   private double getValue42() {
      return this.value4;
   }

   private void setValue42(double var1) {
      this.value4 = var1;
   }

   private boolean isRDY2() {
      return this.RDY2;
   }

   private void O744Cf05(boolean var1) {
      if (!((var1 | true) & true)) {
      }

      this.RDY2 = var1;
   }

   private double XeyVw() {
      return this.value5;
   }

   private void setValue52(double var1) {
      this.value5 = var1;
   }

   private CopyOnWriteArrayList getPacketBuffer() {
      return this.packetBuffer;
   }

   private boolean isC33y() {
      return this.c33y;
   }

   private void setC33y(boolean var1) {
      this.c33y = var1;
   }

   private int getCount12() {
      return this.count1;
   }

   private void vhuem9c0(int var1) {
      this.count1 = var1;
   }

   private boolean Vve9() {
      return this.flag1;
   }

   private void m_2e3B7v61(boolean var1) {
      if (!((var1 | true) & true)) {
      }

      this.flag1 = var1;
   }

   private int getCount22() {
      return this.count2;
   }

   private void setCount22(int var1) {
      this.count2 = var1;
   }

   private Entity Wdpl() {
      return this.target1;
   }

   private void setTarget12(Entity var1) {
      this.target1 = var1;
   }

   private double getValue62() {
      return this.value6;
   }

   private void setValue6(double var1) {
      this.value6 = var1;
   }

   private int getCount32() {
      return this.count3;
   }

   private void setCount32(int var1) {
      this.count3 = var1;
   }

   private int getCount42() {
      return this.count4;
   }

   private void lw43w903(int var1) {
      if (((var1 | 1) & 1) == 0) {
      }

      this.count4 = var1;
   }

   private boolean isNC9y() {
      return this.NC9y;
   }

   private void M8nC(boolean var1) {
      this.NC9y = var1;
   }

   private int s83b7() {
      return this.count5;
   }

   private void setCount5(int var1) {
      this.count5 = var1;
   }

   private Entity QcCZCW() {
      return this.target2;
   }

   private void setEntity(Entity var1) {
      this.target2 = var1;
   }

   private double m_1ovb() {
      return this.value7;
   }

   private void setValue7(double var1) {
      this.value7 = var1;
   }

   private double getValue82() {
      return this.value8;
   }

   private void setValue8(double var1) {
      this.value8 = var1;
   }

   private double getYn8T() {
      return this.yn8T;
   }

   private void setYn8T(double var1) {
      this.yn8T = var1;
   }

   private double getValue9() {
      return this.value9;
   }

   private void Tv7S2(double var1) {
      this.value9 = var1;
   }

   private int getCount62() {
      return this.count6;
   }

   private void HORmHtB(int var1) {
      this.count6 = var1;
   }

   private int getCount72() {
      return this.count7;
   }

   private void eR4J4(int var1) {
      this.count7 = var1;
   }

   private double KfSKAccess() {
      return this.KfSK;
   }

   private void setKfSK(double var1) {
      this.KfSK = var1;
   }

   private double getValue10() {
      return this.value10;
   }

   private void setValue10(double var1) {
      this.value10 = var1;
   }

   private double uF33() {
      return this.value11;
   }

   private void n49691(double var1) {
      this.value11 = var1;
   }

   private boolean LOUBbq() {
      return this.flag2;
   }

   private void setFlag22(boolean var1) {
      this.flag2 = var1;
   }

   private FlatGroundMace.Synthetic4 FaTboL() {
      return this.ref1;
   }

   private void setRef12(FlatGroundMace.Synthetic4 var1) {
      this.ref1 = var1;
   }

   private Timer getTimer12() {
      return this.timer1;
   }

   private Timer getTimer22() {
      return this.timer2;
   }

   public static enum Synthetic3 {
      DoublePacket,
      Legacy;

      private Synthetic3() {
      }
   }

   private static enum Synthetic4 {
      IDLE,
      FALLING;

      private Synthetic4() {
      }
   }
}
