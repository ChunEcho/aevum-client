package dev.chun.mod.modules.impl.combat;

import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.LookDirectionEvent;
import dev.chun.api.events.impl.PacketEvent;
import dev.chun.api.events.impl.TimerEvent;
import dev.chun.api.utils.combat.CombatUtil;
import dev.chun.api.utils.math.MathUtil;
import dev.chun.api.utils.math.Timer;
import dev.chun.api.utils.misc.ClientUtil;
import dev.chun.api.utils.player.EntityUtil;
import dev.chun.api.utils.player.InventoryUtil;
import dev.chun.api.utils.render.Render3DUtil;
import dev.chun.asm.accessors.IPlayerMoveC2SPacket;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.movement.ElytraFly;
import dev.chun.mod.modules.settings.enums.SwingSide;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.Iterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class MaceAura extends Module {
   private static final double[] ref1 = new double[]{0.4199999868869781, 0.7531999805212017, 0.9999957640154541, 1.1661092609382138};
   private static final double value1 = 0.25;
   public static MaceAura INSTANCE;
   private final SliderSetting targetRange;
   private final SliderSetting attackRange;
   private final SliderSetting triggerDistance;
   private final SliderSetting lagbackDistance;
   private final SliderSetting spoofHeight;
   private final SliderSetting cooldownTicks;
   private final BooleanSetting timer;
   private final SliderSetting timerSpeed;
   private final BooleanSetting maceOnly;
   private final BooleanSetting silent;
   private final EnumSetting<MaceAura.IRC> renderMode;
   private final ColorSetting silentPlayerBox;
   private final BooleanSetting inventorySwap;
   private final BooleanSetting onlyFalling;
   private final BooleanSetting onlyAir;
   private final BooleanSetting autoAttack;
   private final BooleanSetting set;
   private final BooleanSetting groundSpoof;
   private final BooleanSetting attackPull;
   private final EnumSetting<MaceAura.StackDir> qYhGJI;
   private final SliderSetting delay;
   private final SliderSetting delayUntilSwitch;
   private final SliderSetting lagDelay;
   private final SliderSetting delayUntilAttack;
   private final SliderSetting HvJV;
   private final SliderSetting lagFly;
   private final SliderSetting smartXZ;
   private final SliderSetting smartUp;
   private final SliderSetting smartDown;
   private final SliderSetting smartDist;
   private final BooleanSetting noWait;
   private final EnumSetting<SwingSide> swing;
   private PlayerEntity ref2;
   private Vec3d ref3;
   private Vec3d P7hd;
   private int count1;
   private boolean flag1;
   private final Timer timer1 = new Timer();
   private final Timer timer2 = new Timer();
   private int count2;
   private boolean flag2;
   private boolean flag3;
   private boolean flag4;
   private boolean flag5;
   private PlayerEntity ref4;
   private Vec3d ref5;
   private boolean flag6;
   private boolean flag7;
   private float amount1;
   private float amount2;
   private float amount3;
   private float amount4;

   public MaceAura() {
      super("MaceAura", Module.Category.Mace);
      this.targetRange = this.add(new SliderSetting("TargetRange", 5.0, 1.0, 8.0, 0.1));
      this.attackRange = this.add(new SliderSetting("AttackRange", 3.0, 1.0, 8.0, 0.1));
      this.triggerDistance = this.add(new SliderSetting("TriggerDistance", 2.0, 0.1, 20.0, 0.1));
      this.lagbackDistance = this.add(new SliderSetting("LagbackDistance", 5.0, 0.0, 10.0, 0.1));
      this.spoofHeight = this.add(new SliderSetting("SpoofHeight", 25.0, 0.0, 2000.0, 0.1));
      this.cooldownTicks = this.add(new SliderSetting("CooldownTicks", 3, 0, 20));
      this.timer = this.add(new BooleanSetting("Timer", false).setParent());
      this.timerSpeed = this.add(new SliderSetting("TimerSpeed", 2.0, 1.0, 100.0, 0.1, () -> this.getTimer().isOpen()).setSuffix("x"));
      this.maceOnly = this.add(new BooleanSetting("MaceOnly", true));
      this.silent = this.add(new BooleanSetting("Silent", false));
      this.renderMode = this.add(new EnumSetting<>("RenderMode", MaceAura.IRC.Normal));
      this.silentPlayerBox = this.add(new ColorSetting("SilentPlayerBox", new Color(255, 80, 80, 180), () -> this.getRenderMode().is(MaceAura.IRC.Silent)));
      this.inventorySwap = this.add(new BooleanSetting("InventorySwap", false, () -> this.getSilent().getValue()));
      this.onlyFalling = this.add(new BooleanSetting("OnlyFalling", true));
      this.onlyAir = this.add(new BooleanSetting("OnlyAir", true));
      this.autoAttack = this.add(new BooleanSetting("AutoAttack", true));
      this.set = this.add(new BooleanSetting("Set", false).setParent());
      this.groundSpoof = this.add(new BooleanSetting("GroundSpoof", true, () -> this.getSet().isOpen()));
      this.attackPull = this.add(new BooleanSetting("AttackPull", false, () -> this.getSet().isOpen()));
      this.qYhGJI = this.add(new EnumSetting<>("LagMode", MaceAura.StackDir.Fly, () -> this.getSet().isOpen()));
      this.delay = this.add(new SliderSetting("Delay", 400.0, 100.0, 3000.0, 10.0, () -> this.getSet().isOpen()).setSuffix("ms"));
      this.delayUntilSwitch = this.add(new SliderSetting("DelayUntilSwitch", 50.0, 0.0, 1000.0, 10.0, () -> this.getSet().isOpen()).setSuffix("ms"));
      this.lagDelay = this.add(new SliderSetting("LagDelay", 80.0, 0.0, 1000.0, 10.0, () -> this.getSet().isOpen()).setSuffix("ms"));
      this.delayUntilAttack = this.add(new SliderSetting("DelayUntilAttack", 80.0, 0.0, 1000.0, 10.0, () -> this.getSet().isOpen()).setSuffix("ms"));
      this.HvJV = this.add(new SliderSetting("SetRange", 5.0, 1.0, 10.0, 0.1, () -> this.getSet().isOpen()));
      this.lagFly = this.add(new SliderSetting("LagFly", 4.0, 0.1, 10.0, 0.1, () -> true));
      this.smartXZ = this.add(new SliderSetting("SmartXZ", 3.0, 0.0, 10.0, 0.1, () -> true));
      this.smartUp = this.add(new SliderSetting("SmartUp", 3.0, 0.0, 10.0, 0.1, () -> true));
      this.smartDown = this.add(new SliderSetting("SmartDown", 3.0, 0.0, 10.0, 0.1, () -> true));
      this.smartDist = this.add(new SliderSetting("SmartDist", 2.0, 0.0, 10.0, 0.1, () -> true));
      this.noWait = this.add(new BooleanSetting("NoWait", false));
      this.swing = this.add(new EnumSetting<>("Swing", SwingSide.All));
      this.setChinese("\u98ce\u9524\u5149\u73af");
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.gGN22JJ0();
      this.Bu81F(false);
      this.setRef5(null);
      this.setFlag62(false);
      this.setFlag72(false);
      this.z239();
   }

   @Override
   public String getInfo() {
      return this.getRef22() == null ? null : this.getRef22().getName().getString();
   }

   @Override
   public void onDisable() {
      this.m_5ka21(null);
      this.setRef32(null);
      this.uIyzYN(null);
      this.setCount1(0);
      this.setFlag1(false);
      this.gGN22JJ0();
      this.Bu81F(false);
      this.setRef5(null);
      this.setFlag62(false);
      this.setFlag72(false);
   }

   @EventListener
   public void onTimer(TimerEvent var1) {
      if (this.getTimer().getValue() && this.isFlag12()) {
         var1.set(this.getTimerSpeed().getValueFloat());
      }
   }

   @EventListener
   public void onTick(ClientTickEvent var1) {
      if (!var1.isPost() && !nullCheck()) {
         if (this.getCount12() > 0) {
            this.setCount1(this.getCount12() - 1);
         }

         this.m_9cD4WRA();
         this.X1xex5Dvp();
         if (this.getSet().getValue()) {
            this.checkTimers();
         } else if (this.getCount22() != 0 || this.getRef42() != null || this.isFlag42()) {
            this.gGN22JJ0();
         }

         this.m_5ka21(CombatUtil.getClosestEnemy(this.getTargetRange().getValue()));
         if (this.getRef22() == null) {
            return;
         }

         if (this.YKNUVo().getValue() && this.isFlag12() && this.QVDe9A6(this.getRef22()) && this.attack()) {
            if (this.getSet().getValue() && this.uz25().getValue()) {
               this.m_1k7zt5();
            }

            if (this.PSFwGV() != null) {
               this.lK2mnDC(this.PSFwGV());
               return;
            }
         }

         if (this.yb38().getValue() && !this.v95m()) {
            return;
         }

         if (this.getOnlyAir().getValue() && mc.player.isOnGround()) {
            this.at7Am();
            return;
         }

         if (!mc.player.isSwimming() && !mc.player.isClimbing() && !mc.player.isRiding()) {
            if (this.getOnlyFalling().getValue() && mc.player.getVelocity().y >= 0.0) {
               return;
            }

            if (this.PSFwGV() != null && this.PSFwGV().y - mc.player.getY() >= this.O3ZZ8762().getValue() && this.getCount12() <= 0) {
               if (this.getLagbackDistance().getValue() > 0.0 && (double)mc.player.distanceTo(this.getRef22()) > this.getLagbackDistance().getValue()) {
                  return;
               }

               if (this.sr6ablJ() && this.m_3n36d()) {
                  this.setCount1(this.iuEV().getValueInt());
               }
            }

            return;
         }

         this.at7Am();
      }
   }

   private boolean m_3n36d() {
      Vec3d var1 = this.PSFwGV();
      if (var1 == null) {
         return false;
      } else if (!this.getNoWait().getValue() && mc.player.getAttackCooldownProgress(0.5F) < 1.0F) {
         return false;
      } else {
         this.lK2mnDC(var1);
         return true;
      }
   }

   private void lK2mnDC(Vec3d var1) {
      this.Bu81F(true);

      try {
         this.g5hXzPW(var1);
         mc.player.setPosition(var1.x, var1.y, var1.z);
         mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
         mc.player.fallDistance = 0.0F;
         this.at7Am();
         this.setCount1(this.iuEV().getValueInt());
         if (this.getSet().getValue()) {
            this.gGN22JJ0();
         }

         this.Bu81F(false);
      } catch (Throwable var3) {
         this.Bu81F(false);
         throw var3;
      }
   }

   @EventListener
   public void onPacketSend(PacketEvent.Send var1) {
      PlayerMoveC2SPacket var2;
      Packet var3;
      if (!nullCheck()
         && this.getSet().getValue()
         && this.getGroundSpoof().getValue()
         && (var3 = var1.getPacket()) instanceof PlayerMoveC2SPacket
         && (var2 = (PlayerMoveC2SPacket)var3).isOnGround()) {
         ((IPlayerMoveC2SPacket)var2).setOnGround(false);
      }
   }

   private void checkTimers() {
      if (this.isFlag42() && mc.player.isOnGround() && this.getCount22() == 0) {
         this.setFlag42(false);
         this.getTimer12().reset();
      }

      PlayerEntity var1;
      if ((var1 = this.m_2Z0E9(this.HvJVAccess().getValue())) == null) {
         this.sVkz5();
      } else if (this.getCount22() == 0) {
         if (!this.isFlag42()
            && !this.isFlag12()
            && this.getCount12() <= 0
            && this.getTimer12().passedMs((double)this.getDelay().getValueInt())
            && this.m_1k7zt5()) {
            this.wpts2(var1);
            this.setFlag42(true);
            this.getTimer22().reset();
            this.fwAS0(false);
            this.zEi0(false);
            this.setCount2(1);
         }
      } else {
         if (!this.isFlag22() && this.getTimer22().passedMs((double)this.getDelayUntilSwitch().getValueInt())) {
            int var2 = InventoryUtil.findItem(Items.MACE);
            if (var2 != -1) {
               if (((var2 | 1) & 1) == 0) {
               }

               if (mc.player.getInventory().selectedSlot != var2) {
                  mc.player.getInventory().selectedSlot = var2;
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(var2));
               }
            }

            this.fwAS0(true);
         }

         if (!this.isFlag32() && this.getTimer22().passedMs((double)this.getLagDelay().getValueInt())) {
            this.m_0F8p3();
            this.zEi0(true);
         }

         if (this.getTimer22().passedMs((double)this.getDelayUntilAttack().getValueInt())) {
            if (this.isFlag12()
               && this.getRef42() != null
               && this.getRef42().isAlive()
               && (double)mc.player.distanceTo(this.getRef42()) <= this.HvJVAccess().getValue()) {
               mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(this.getRef42(), mc.player.isSneaking()));
               mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
               Vec3d var3 = this.PSFwGV();
               if (var3 != null) {
                  this.lK2mnDC(var3);
                  return;
               }
            }

            this.sVkz5();
         }
      }
   }

   private void m_0F8p3() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "classStruct" because "classNode" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifyNewEnumSwitch(SwitchHelper.java:319)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:41)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:376)
      //
      // Bytecode:
      // 000: aload 0
      // 001: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.qYhGJIAccess ()Ldev/chun/mod/modules/settings/impl/EnumSetting;
      // 004: invokevirtual dev/chun/mod/modules/settings/impl/EnumSetting.getValue ()Ljava/lang/Enum;
      // 007: invokevirtual java/lang/Enum.ordinal ()I
      // 00a: tableswitch 820 0 8 50 379 442 481 510 539 578 633 676
      // 03c: new java/util/ArrayList
      // 03f: dup
      // 040: invokespecial java/util/ArrayList.<init> ()V
      // 043: astore 1
      // 044: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 047: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 04a: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 04d: aload 0
      // 04e: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartXZ ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 051: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 054: dsub
      // 055: dstore 2
      // 056: dload 2
      // 057: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 05a: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 05d: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 060: aload 0
      // 061: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartXZ ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 064: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 067: dadd
      // 068: dcmpg
      // 069: ifge 0e6
      // 06c: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 06f: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 072: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 075: aload 0
      // 076: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartXZ ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 079: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 07c: dsub
      // 07d: dstore 4
      // 07f: dload 4
      // 081: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 084: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 087: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 08a: aload 0
      // 08b: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartXZ ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 08e: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 091: dadd
      // 092: dcmpg
      // 093: ifge 0df
      // 096: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 099: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 09c: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 09f: aload 0
      // 0a0: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartDown ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 0a3: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 0a6: dsub
      // 0a7: dstore 6
      // 0a9: dload 6
      // 0ab: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 0ae: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 0b1: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 0b4: aload 0
      // 0b5: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.getSmartUp ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 0b8: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 0bb: dadd
      // 0bc: dcmpg
      // 0bd: ifge 0d6
      // 0c0: aload 1
      // 0c1: dload 2
      // 0c2: dload 6
      // 0c4: dload 4
      // 0c6: invokestatic net/minecraft/util/math/BlockPos.ofFloored (DDD)Lnet/minecraft/util/math/BlockPos;
      // 0c9: invokevirtual java/util/ArrayList.add (Ljava/lang/Object;)Z
      // 0cc: pop
      // 0cd: dload 6
      // 0cf: dconst_1
      // 0d0: dadd
      // 0d1: dstore 6
      // 0d3: goto 0a9
      // 0d6: dload 4
      // 0d8: dconst_1
      // 0d9: dadd
      // 0da: dstore 4
      // 0dc: goto 07f
      // 0df: dload 2
      // 0e0: dconst_1
      // 0e1: dadd
      // 0e2: dstore 2
      // 0e3: goto 056
      // 0e6: ldc2_w 1.7976931348623157E308
      // 0e9: dstore 2
      // 0ea: aconst_null
      // 0eb: astore 4
      // 0ed: aload 1
      // 0ee: invokevirtual java/util/ArrayList.iterator ()Ljava/util/Iterator;
      // 0f1: astore 5
      // 0f3: aload 5
      // 0f5: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 0fa: ifeq 15b
      // 0fd: aload 5
      // 0ff: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 104: checkcast net/minecraft/util/math/BlockPos
      // 107: astore 6
      // 109: aload 0
      // 10a: aload 6
      // 10c: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.Yod6t (Lnet/minecraft/util/math/BlockPos;)Z
      // 10f: ifeq 0f3
      // 112: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 115: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 118: aload 6
      // 11a: invokevirtual net/minecraft/util/math/BlockPos.toCenterPos ()Lnet/minecraft/util/math/Vec3d;
      // 11d: dconst_0
      // 11e: ldc2_w -0.5
      // 121: dconst_0
      // 122: invokevirtual net/minecraft/util/math/Vec3d.add (DDD)Lnet/minecraft/util/math/Vec3d;
      // 125: invokevirtual net/minecraft/client/network/ClientPlayerEntity.squaredDistanceTo (Lnet/minecraft/util/math/Vec3d;)D
      // 128: d2f
      // 129: invokestatic net/minecraft/util/math/MathHelper.sqrt (F)F
      // 12c: f2d
      // 12d: aload 0
      // 12e: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.E6Xe ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 131: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 134: dcmpl
      // 135: iflt 0f3
      // 138: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 13b: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 13e: aload 6
      // 140: invokevirtual net/minecraft/util/math/BlockPos.toCenterPos ()Lnet/minecraft/util/math/Vec3d;
      // 143: invokevirtual net/minecraft/client/network/ClientPlayerEntity.squaredDistanceTo (Lnet/minecraft/util/math/Vec3d;)D
      // 146: dup2
      // 147: dstore 7
      // 149: dload 2
      // 14a: dcmpl
      // 14b: iflt 151
      // 14e: goto 0f3
      // 151: dload 7
      // 153: dstore 2
      // 154: aload 6
      // 156: astore 4
      // 158: goto 0f3
      // 15b: aload 4
      // 15d: ifnonnull 163
      // 160: goto 33e
      // 163: aload 0
      // 164: aload 4
      // 166: invokevirtual net/minecraft/util/math/BlockPos.getX ()I
      // 169: i2d
      // 16a: ldc2_w 0.5
      // 16d: dadd
      // 16e: aload 4
      // 170: invokevirtual net/minecraft/util/math/BlockPos.getY ()I
      // 173: i2d
      // 174: aload 4
      // 176: invokevirtual net/minecraft/util/math/BlockPos.getZ ()I
      // 179: i2d
      // 17a: ldc2_w 0.5
      // 17d: dadd
      // 17e: bipush 0
      // 17f: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 182: goto 33e
      // 185: bipush 0
      // 186: istore 1
      // 187: iload 1
      // 188: bipush 20
      // 18a: if_icmpge 1c1
      // 18d: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 190: astore 2
      // 191: iload 1
      // 192: bipush 1
      // 193: ior
      // 194: bipush 1
      // 195: iand
      // 196: ifne 199
      // 199: aload 0
      // 19a: aload 2
      // 19b: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 19e: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 1a1: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1a4: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1a7: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 1aa: ldc2_w 1337.0
      // 1ad: dadd
      // 1ae: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1b1: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1b4: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 1b7: bipush 0
      // 1b8: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 1bb: iinc 1 1
      // 1be: goto 187
      // 1c1: goto 33e
      // 1c4: aload 0
      // 1c5: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1c8: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1cb: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 1ce: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1d1: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1d4: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 1d7: ldc2_w 2.3400880035762786
      // 1da: dadd
      // 1db: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1de: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1e1: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 1e4: bipush 0
      // 1e5: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 1e8: goto 33e
      // 1eb: aload 0
      // 1ec: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1ef: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1f2: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 1f5: ldc2_w -70.0
      // 1f8: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 1fb: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 1fe: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 201: bipush 0
      // 202: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 205: goto 33e
      // 208: aload 0
      // 209: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 20c: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 20f: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 212: ldc2_w -7.0
      // 215: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 218: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 21b: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 21e: bipush 0
      // 21f: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 222: goto 33e
      // 225: aload 0
      // 226: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 229: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 22c: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 22f: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 232: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 235: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 238: ldc2_w 1.9
      // 23b: dadd
      // 23c: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 23f: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 242: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 245: bipush 0
      // 246: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 249: goto 33e
      // 24c: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 24f: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 252: getfield net/minecraft/client/network/ClientPlayerEntity.networkHandler Lnet/minecraft/client/network/ClientPlayNetworkHandler;
      // 255: new net/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround
      // 258: dup
      // 259: ldc_w -180.0
      // 25c: ldc_w -90.0
      // 25f: bipush 0
      // 260: invokespecial net/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround.<init> (FFZ)V
      // 263: invokevirtual net/minecraft/client/network/ClientPlayNetworkHandler.sendPacket (Lnet/minecraft/network/packet/Packet;)V
      // 266: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 269: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 26c: getfield net/minecraft/client/network/ClientPlayerEntity.networkHandler Lnet/minecraft/client/network/ClientPlayNetworkHandler;
      // 26f: new net/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround
      // 272: dup
      // 273: ldc_w 180.0
      // 276: ldc_w 90.0
      // 279: bipush 0
      // 27a: invokespecial net/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround.<init> (FFZ)V
      // 27d: invokevirtual net/minecraft/client/network/ClientPlayNetworkHandler.sendPacket (Lnet/minecraft/network/packet/Packet;)V
      // 280: goto 33e
      // 283: aload 0
      // 284: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 287: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 28a: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 28d: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 290: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 293: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 296: aload 0
      // 297: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.W06e ()Ldev/chun/mod/modules/settings/impl/SliderSetting;
      // 29a: invokevirtual dev/chun/mod/modules/settings/impl/SliderSetting.getValue ()D
      // 29d: dadd
      // 29e: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2a1: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2a4: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 2a7: bipush 0
      // 2a8: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 2ab: goto 33e
      // 2ae: aload 0
      // 2af: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2b2: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2b5: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 2b8: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2bb: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2be: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 2c1: ldc2_w 1.1001
      // 2c4: dadd
      // 2c5: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2c8: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2cb: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 2ce: bipush 0
      // 2cf: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 2d2: aload 0
      // 2d3: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2d6: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2d9: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 2dc: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2df: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2e2: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 2e5: ldc2_w 1.0605
      // 2e8: dadd
      // 2e9: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2ec: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2ef: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 2f2: bipush 0
      // 2f3: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 2f6: aload 0
      // 2f7: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 2fa: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 2fd: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 300: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 303: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 306: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 309: ldc2_w 1.0802
      // 30c: dadd
      // 30d: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 310: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 313: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 316: bipush 0
      // 317: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 31a: aload 0
      // 31b: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 31e: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 321: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getX ()D
      // 324: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 327: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 32a: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getY ()D
      // 32d: ldc2_w 1.1127
      // 330: dadd
      // 331: getstatic dev/chun/mod/modules/impl/combat/MaceAura.mc Lnet/minecraft/client/MinecraftClient;
      // 334: getfield net/minecraft/client/MinecraftClient.player Lnet/minecraft/client/network/ClientPlayerEntity;
      // 337: invokevirtual net/minecraft/client/network/ClientPlayerEntity.getZ ()D
      // 33a: bipush 0
      // 33b: invokevirtual dev/chun/mod/modules/impl/combat/MaceAura.m_2Z0E9 (DDDZ)V
      // 33e: return
   }

   private boolean m_1k7zt5() {
      boolean var2 = false;
      int var1;
      if (mc.player.getMainHandStack().getItem() == Items.WIND_CHARGE) {
         var1 = mc.player.getInventory().selectedSlot;
      } else {
         int var3 = InventoryUtil.findItem(Items.WIND_CHARGE);
         if (var3 != -1) {
            var1 = var3;
         } else {
            int var4 = InventoryUtil.findItemInventorySlotFromZero(Items.WIND_CHARGE);
            if (var4 == -1) {
               return false;
            }

            var1 = var4;
            var2 = true;
         }
      }

      float var5 = mc.player.getYaw();
      Aevum.ROTATION.snapAt(var5, 90.0F);
      if (mc.player.getMainHandStack().getItem() == Items.WIND_CHARGE) {
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, var5, 90.0F));
         mc.player.swingHand(Hand.MAIN_HAND);
      } else if (var2) {
         ClientUtil.FgmR(var1, mc.player.getInventory().selectedSlot);
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, var5, 90.0F));
         mc.player.swingHand(Hand.MAIN_HAND);
         ClientUtil.FgmR(var1, mc.player.getInventory().selectedSlot);
         ClientUtil.Njn83();
      } else {
         int var6 = mc.player.getInventory().selectedSlot;
         InventoryUtil.switchToSlot(var1);
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, var5, 90.0F));
         mc.player.swingHand(Hand.MAIN_HAND);
         InventoryUtil.switchToSlot(var6);
      }

      Aevum.ROTATION.snapBack();
      return true;
   }

   private boolean Yod6t(BlockPos var1) {
      return mc.world.getBlockState(var1).isAir() && mc.world.getBlockState(var1.up()).isAir();
   }

   private void m_2Z0E9(double var1, double var3, double var5, boolean var7) {
      ClientPlayNetworkHandler var8 = mc.player.networkHandler;
      if (!((var7 | true) & true)) {
      }

      var8.sendPacket(new PositionAndOnGround(var1, var3, var5, var7));
   }

   private void sVkz5() {
      this.setCount2(0);
      this.fwAS0(false);
      this.zEi0(false);
      this.wpts2(null);
      this.getTimer12().reset();
   }

   private void gGN22JJ0() {
      this.sVkz5();
      this.setFlag42(false);
   }

   private PlayerEntity m_2Z0E9(double var1) {
      PlayerEntity var3 = null;
      double var4 = Double.MAX_VALUE;

      for (PlayerEntity var7 : mc.world.getPlayers()) {
         double var8;
         if (var7 != mc.player && var7.isAlive() && !Aevum.FRIEND.isFriend(var7) && !((var8 = (double)mc.player.distanceTo(var7)) > var1) && !(var8 >= var4)) {
            var4 = var8;
            var3 = var7;
         }
      }

      return var3;
   }

   private void g5hXzPW(Vec3d var1) {
      boolean var2 = false;
      double var3 = mc.player.getX();
      double var5 = mc.player.getY();
      MinecraftClient var7 = mc;
      double var8 = var7.player.getZ();
      double[] var10 = getRef1();
      int var11 = var10.length;

      for (int var12 = 0; var12 < var11; var12++) {
         this.QVDe9A6(var3, var5 + var10[var12], var8);
      }

      Vec3d var14 = new Vec3d(var3, var5 + getRef1()[getRef1().length - 1], var8);
      this.QVDe9A6(var14, var1);
      Vec3d var13 = new Vec3d(var1.x, var1.y + this.getSpoofHeight().getValue(), var1.z);
      this.m_2Z0E9(var14, var13);
      this.QVDe9A6(var13.x, var13.y, var13.z, false);
      this.m_2Z0E9(var13, var1);
      this.QVDe9A6(var1.x, var1.y, var1.z, true);
   }

   private void QVDe9A6(Vec3d var1, Vec3d var2) {
      double var3 = var1.x;
      double var5 = var1.z;
      double var7 = var1.y;

      while (Math.abs(var3 - var2.x) > 0.25) {
         double var9;
         double var11;
         if (var3 > var2.x) {
            var11 = -0.25;
            var9 = var3;
         } else {
            var11 = 0.25;
            var9 = var3;
         }

         var3 = var9 + var11;
         this.QVDe9A6(var3, var7, var5);
      }

      while (Math.abs(var5 - var2.z) > 0.25) {
         double var17;
         double var18;
         if (var5 > var2.z) {
            var18 = -0.25;
            var17 = var5;
         } else {
            var18 = 0.25;
            var17 = var5;
         }

         var5 = var17 + var18;
         this.QVDe9A6(var3, var7, var5);
      }

      if (var3 != var2.x || var5 != var2.z) {
         this.QVDe9A6(var2.x, var7, var2.z);
      }
   }

   private void m_2Z0E9(Vec3d var1, Vec3d var2) {
      int var3 = (int)Math.ceil(var1.distanceTo(var2) / 10.0)
         - (
            (
                     "\u9090\uf103\u0bc7\ucf77\u4f23\ue57c\ubf11\uf7dc\u5c4b\u02dd\uff98\ufeb8\udb23\u7cde\u2377\ua2df\ua91f\u60b3\u11fe\u0485\u6828\u1aaa\u894f\u6016\u8243\udac9\uc945\u6c37\u28e7\u9387\u0991\ud23e\u3496\uc127\u181d\u52f3\u1971\u1ee7\u4fdf\ue22c\udb3d\u3111\u4c6f\u76f1\u0d96\u1e4c\u6431\u3520\u2cde\u58b5\u4137\u775b\ub299\uc18b\ua218\ufece\ua177\u97e9\ua3ea\u7c28\u4780\udc05\u268a\u6fed\uf452\u6587\u7ba8\ub079\u8ab0\u3391\ue482\uad4d\u89f2\u6b7b\ub863\ue1dd\u3ed5\uc6a2\u0390\ubb64\u566d\u668c\u60a3\uf16e\u8641\u5586\ufee9\u5372\ubb3f\u61a1\u3836\u269f\u5824\u0d88\ucbd1\u11af\u586a\u3aff\uf942\ub976\u3f7b\u1769\ufb49\u5a22\u27d6\u4a3e\u5cdb\uc5f6\u9f51\ub8cb\u19c7\ue411\u9afe\u7fb9\ubf82\uaa7d\u9529\u0bcf\u9fb7\u69ff\u34a3\uecc0\u3035\f\ud276\ucd5b\u2211\uba91\u5241\u8a5c\ue345\u34d6\u6290\ud7ed\ubb18\u2500\u81fa\ucb49\u0a3b\uf5ab\u8693\ud085\u8af2\u79c2\u7893\u83ad\u3d86\ue108\u4671\u587e\u2f6b\ucdef\u2ac6\u039f\ub4b0\u29a8\u6b04\uebfc\ue1f6\u77f6\u3af9\u59a2\u8dcc\u4995\u07e5\u3ae3\ua16c\u0387\udd98\ubd83\u7fc4\ub781\u5257\uf69c\u64b2\u4805\u41cb\u41e5\u161e\u0d70\uad5f\u0505\u39bf\u3fd7\ue68e\u4f13\u73b2\u62c3\u87e4\u872c\ub495\u0be5\u2601\u1cbd\u7e5b\u7dc3\ue4c6\ufccc\u775e\uae45\u8961\u5895\uec82\u02cc\u5794\u37a4\ua3b8\uef02\u0470\uee8d\u75c6\ub070\u1dad\u120e\u6e10\uc814\u8166\ue8db\ufabe\u9eb5\ue07a\ued10\u56b1\u121f\ube15\u36d3\u2b74\ud700\uc007\ud767\ub06d\u4866\u27c0\uec06\u014e\u0615\u8a14\u50fc\u62a8\u3012\u55f4\u197b\u0c41\u8539\u1d0a\u4023\u7437\u0497\u8da9\u6f0e\udd6a\u83b8\u53fb\u2bdd\u85cf\u3ffb\uabe3\udbed\u2b55\ua007\ueef4\ud325\ue62d\ucba0\u7281\ud8b4\u6273\u38e6\u872d\uc1e6\uf3ab\ufd7c\ue02a\u7335\udd23\u10d7\u8d92\u74be\u1c2f\ua31f\ubfd7\u772a\u2a4a\u6e4e\u6e1b\u1ba8\ue911\u475d\u9b29\ua03f\ub23c\u9fa6\u1732\u6de3\u9de9\u8304\ub502\ue58a\u3d99\ud36c\udde8\u408f\uff13\u7548\ubd54\ua609\udaa4\u2ae0\u0f87\u14b7\u9e07\u00bc\uf5e3\u50ed\ud7f1\u8176\ua9c7\u8ea9\u555e\u05be\u4af9\u4efd\u8d7e\u00d7\u0ad5\u52a6\ud4e9\ua8f8\uce0d\u1651\uf236\ud251\udedf\u3235\u15db\u60d0\u92c4\u39c6\u7bb3\ueeb4\uff55\u40aa\u7f27\u84d7\uc173\ue748\u4f6a\u561d\ud463\ud5d8\uea58\uac9e\u8e88\u477c\ufc88\u1e4f\ue2b2\u2039\uc81b\uf772\u1b51\uf2e7\ua20e\ub99a\u3fb5\u60d9\ubf39\u4bce\u1ac6\u42a2\ub847\u8975\u9775\u345d\uea8a\u59b6\u498d\u818f\u70ca\uc629\u747c\ud187\ubad9\u4f32\u4788\ud223\u35bb\ud11d\ubcd1\u53ad\ub116\ud2f2\uda84\u45b2\u3756\ub068\u1720\u6512\u2b1c\u566b\u6d17\u9df6\uc587\uaa20\uc428\u9467\u5444\u0932\uf1cb\ubcab\ub82d\ufab7\u5883\u271c\u5119\u9f87\udce5\u9958\u1eaa\uae1b\u902c\u6268\u72fa\u6df1\u37af\u5c72\u9f84\ud0d4\u83d4\ua14b\ud8cd\u4096\ud8ab\u7337\u054d\uea7b\u0d8c\u3aab\u184b\u10a1\uf289\u07ef\ubc5f\ub364\uc460\ue2ab\u41e0\u8dab\u6cfa\uc556\u9109\u677f\u57bc\u9c72\u1eac\ubf7a\u1b71\u94df\u985f\u26ab\ub157\u6c72\ua6db\u4f7c\uced9\uc03f\u461d\ud69c\u22a0\u1032\u83eb\u7ed2\u3bb9\u54a4\u7908\u159a\u02a9\u2f19\u3817\u9784\u89e1\u7b14\u6bf2\ud7e0\ubd55\ud15e\u2f4a\ue694\u4afd\ub545\u87d1\u857a\u5605\u6780\u1284\u38e4\u6ec8\u58bd\u5729\u59ef\ue483\u2c11\u6dab\u983d\ubb40\uf9e8\ufb26\u62f6\u9ad7\u9346\u2f48\ua18d\uaacd\u198a\uce5f\ub430\u16d9\u4051\u2966\u56c2\u90ba\u175e\u65b3\ua6a7\u37f5\u65a6\udfbc\u6fc5\u28d3\u58a4\u86e9\ud67d\u6c16\u7cbb\u21ee\u03c9\u9183\uf0c8\u2768\uf42f\u4680\ud539\u9940\ufc7b\u8b5d\uc6d7\u05ab\u0315\u3253\u8aef\u766c\u7856\u4c3f\u66a3\u2b14\u9ae1\ue837\ue4ba\u97fb\u03fe8\u5fcd\u5bc5\ud713\u466b\ua5ad\ue37e\u46fa\uc7c7\u5c08Y\u02d9\u5b4a\u75b5\uaba3\uef9c\ud023\uac57\u580f\ubbc0\ue054\u8de6\u569b\u77b9\ub261\u29c7\uc309\u891e\uf701\ue37b\u481c\u96fd\u47e2\ue487\u8fab\u8d14\u94aa\u1fe8\u5e65\u82e1\ub883\ua23f\uebec\u8d2a\ue095\u7e88\ua39a\ud475\ua0fd\u0771\u2a96\u6306\u1437\u7c79\uc0df\ua26b\u06f4\ubc62\uc61d\ua261\ue9cc\ue288\u0f4c\u9999\ub0f2\ud7ce\u17ad\ud134\ud568\u3f39\u29ba\u90c5\u2352\ue5bd\u5153\u49f9\u30e7\u7a6a\u805b\uaffa\ua233\u04bc\u7336\u2947\ue098\u16f6\u2e67\ubb34\u9da7\u8b15\u1bc1\u5c96\u90ea\u1a93\ubb13\uf9a6\u5d89\u3c80\ub694\u2e00\u5d85\ubbe5\u66db\ua3d1\u8b9b\ud342\u255a\u34b6\u8540\uedcf\ucb56\ubef6\u2cca\u0093\ue5f8\u6c5b\u4f2e\uadbd\u3b5a\uc255\u4e14\uee19\u4854\u3cfd\u1f04\u0dab\u7154\u1fd0\uc001\u0f53\u61bf\u3ebd\u576c\u3fba\u300a\u83a8\u29ec\u8ed2\u6895\u31b2\ua8db\u1ff5\ua068\u4e32\u1822\u55b9\u8f52\ue2c3\u6013\u356d\ue453\u5885\ud7f9\udb74\ue566\ub389\ucbad\ufdaf\u07e3\u2861\udc82\u4004\ua7e1\uacc5\u3082\u01ae\u5d03\u28a7\ub884\u6ff3\u717a\ube9d\ufe3d\u7323\ufb0f\u9bed\u0836\ub4c5\u9d46\ub8f7\u43b0\uaf06\u9d03\ue8a7\u2cc8\uf2ca\u37b0\ucef1\u4b39\u05af\ub9c7\u94d0\uf07f\u9eca\u7c23\u9e5b\uc90b\ue519\ub81b\u921e\uc211\ua791\ub596\u92f9\u609b\u5737\u7743\ue966\ucde7\u39a3\u6db0\u8713\u8e70\u8fc0\uea2e\u0502\uea67\ua67a\u8d02\u517b\u3dda\u8089\u9875\u2a9a\u9d51\ua68a\u745b\u2b68\ucfaf\u7803\u0cc7\u53c9\u984f\u5f23\u58f7\u18f7\uea12\uc169\ude2c\ufc41\u7e17\uf79f\ub004\uc00a\uae04\ud229\u7139\u4b3d\u23d2\u0fcf\ub008\ud4c4\u3156\u34a9\ucafd\uaf3e\u7f38\ua983\u4eb4\u4ec7\uc6c1\u9f6f\u5899\ue81d\u702a\uce6f\u8613\u43c5\u71a5\uaaba\ub8a3\u9310\ue22c\uf174\ufa4b\ua9bf\u7ba7\udaed\u62d8\ua4be\u0e43\u08be\u036a\uf4c1\u4bb5\uac98\uf43b\uee19\ue7db\uad49\u0a2e\u5e43\ucb33\u5867\uefb8\u46e8\uf257\uca58\u10eb\u5894\ue4ae\u6353\u4fec\ueb37\u21aa\u5ce1\u8770\u78e6\ue065\ua75d\ue502\u783e\ubc33\u19a8\ua537\ue311\u1f5f\uec41\uea18\uc933\ub2f5\uddb4\u05c3\uc49e\ub4d4\u0632\u21a5\u61c1\u31bc\u0580\ue599\udaa2\ufb01\u5725\u7b6f\u9193\u4138\ucd3a\uce64\u01c6\ud786\u9aae\ud541\u40dd\ud93b\u0ad2\u35b7\u7bb6\uf6eb\u0349\u23fa\u60dc\u7cee\uaf10\u1239\u7a31\uee30\u893f\u1f1e\u708a\u9539\uae7f\uf3cc\ue30c\ubb29\u79de\u5149\u863c\ud700\u8b9c\uf6f5\u0dda\u3efd\uf1e8\u29cf\u8d8f\u1657\u927f\u21b5\udac1\u2a4e\u9282\ub6b1\u00a7\u838b\u82e9\u2548\u2385\u02ed\ucf41\u9b08\u8e5d\u12c8\u8288\u68a3\u4383\u90f2\u3782\ud71b\ucfb6\u5c86\ua3fc\u081f\u24c5\u0a9b\uf3ad\ub96f\u1c17\u9b52\ub1cb\ua91d\u08bd\u1ac3\uc090\ueb56\u5593\ufd71\u2501\uf8c3\ua0c7\u4e3c\u8eeb\u5a75\u5c8b\u3a4e\u7701\u37c6\u550b\u25b5\u737e\u9a0b\ue6f5\u6af4\u1c33\u4b76\u6bd8\u2d7f\u7b0a\u7523\u661e\ue509\u69b5\u9c99\uf5a0\uaca0\u04b7\u4fe3\u70f0\u76f6\ube1e\uee10\ufd63\ubbe8\u4b7a\u73b4\u5fb7\u45ad\uf450\ua3c5\ua433\udf01\ucef7\u3bd0\u8663\uf46a\ue104\u253d\u8782\u0ff4\ud618\u691a\u46db\u88c9\u9e40\u9454\ua36e\ufcb9\u3a55\ucd48\u2705\u6c03\ua1f5\u6946\u97fa\uc83f\ud0d0\u9aea\u70dd\u3a56\u1e43\uf9b9\u2479\ud795\uc9ad\ud069\u5e8b\uc4a8\u91bc\u908d\u34d2\u17ad\u48cb\ude66\ua1c3\u2501\u420d\u2226\ua44e\u4a0f\ubc02\u8a4d\u5f1d\u3df6\u0ed2\u41e6\u085f\u54ec\u8f69\u78d8\u2790\udc31\ub7fc\u2fd7\ucc43\u47b2\u0d63\u6933\u127e\u9814\u1252\u4a40\u3986\uaca9\u434d\u49c7\u9e8c\u5880\uab1c\uae69\u7c4a\u810e\u60ad\u0f52\ue435\u0c98\u5d6a\uf48e\ucfad\u9ee0\uf7aa"
                              .charAt(1036)
                           << 16
                        | "\u9090\uf103\u0bc7\ucf77\u4f23\ue57c\ubf11\uf7dc\u5c4b\u02dd\uff98\ufeb8\udb23\u7cde\u2377\ua2df\ua91f\u60b3\u11fe\u0485\u6828\u1aaa\u894f\u6016\u8243\udac9\uc945\u6c37\u28e7\u9387\u0991\ud23e\u3496\uc127\u181d\u52f3\u1971\u1ee7\u4fdf\ue22c\udb3d\u3111\u4c6f\u76f1\u0d96\u1e4c\u6431\u3520\u2cde\u58b5\u4137\u775b\ub299\uc18b\ua218\ufece\ua177\u97e9\ua3ea\u7c28\u4780\udc05\u268a\u6fed\uf452\u6587\u7ba8\ub079\u8ab0\u3391\ue482\uad4d\u89f2\u6b7b\ub863\ue1dd\u3ed5\uc6a2\u0390\ubb64\u566d\u668c\u60a3\uf16e\u8641\u5586\ufee9\u5372\ubb3f\u61a1\u3836\u269f\u5824\u0d88\ucbd1\u11af\u586a\u3aff\uf942\ub976\u3f7b\u1769\ufb49\u5a22\u27d6\u4a3e\u5cdb\uc5f6\u9f51\ub8cb\u19c7\ue411\u9afe\u7fb9\ubf82\uaa7d\u9529\u0bcf\u9fb7\u69ff\u34a3\uecc0\u3035\f\ud276\ucd5b\u2211\uba91\u5241\u8a5c\ue345\u34d6\u6290\ud7ed\ubb18\u2500\u81fa\ucb49\u0a3b\uf5ab\u8693\ud085\u8af2\u79c2\u7893\u83ad\u3d86\ue108\u4671\u587e\u2f6b\ucdef\u2ac6\u039f\ub4b0\u29a8\u6b04\uebfc\ue1f6\u77f6\u3af9\u59a2\u8dcc\u4995\u07e5\u3ae3\ua16c\u0387\udd98\ubd83\u7fc4\ub781\u5257\uf69c\u64b2\u4805\u41cb\u41e5\u161e\u0d70\uad5f\u0505\u39bf\u3fd7\ue68e\u4f13\u73b2\u62c3\u87e4\u872c\ub495\u0be5\u2601\u1cbd\u7e5b\u7dc3\ue4c6\ufccc\u775e\uae45\u8961\u5895\uec82\u02cc\u5794\u37a4\ua3b8\uef02\u0470\uee8d\u75c6\ub070\u1dad\u120e\u6e10\uc814\u8166\ue8db\ufabe\u9eb5\ue07a\ued10\u56b1\u121f\ube15\u36d3\u2b74\ud700\uc007\ud767\ub06d\u4866\u27c0\uec06\u014e\u0615\u8a14\u50fc\u62a8\u3012\u55f4\u197b\u0c41\u8539\u1d0a\u4023\u7437\u0497\u8da9\u6f0e\udd6a\u83b8\u53fb\u2bdd\u85cf\u3ffb\uabe3\udbed\u2b55\ua007\ueef4\ud325\ue62d\ucba0\u7281\ud8b4\u6273\u38e6\u872d\uc1e6\uf3ab\ufd7c\ue02a\u7335\udd23\u10d7\u8d92\u74be\u1c2f\ua31f\ubfd7\u772a\u2a4a\u6e4e\u6e1b\u1ba8\ue911\u475d\u9b29\ua03f\ub23c\u9fa6\u1732\u6de3\u9de9\u8304\ub502\ue58a\u3d99\ud36c\udde8\u408f\uff13\u7548\ubd54\ua609\udaa4\u2ae0\u0f87\u14b7\u9e07\u00bc\uf5e3\u50ed\ud7f1\u8176\ua9c7\u8ea9\u555e\u05be\u4af9\u4efd\u8d7e\u00d7\u0ad5\u52a6\ud4e9\ua8f8\uce0d\u1651\uf236\ud251\udedf\u3235\u15db\u60d0\u92c4\u39c6\u7bb3\ueeb4\uff55\u40aa\u7f27\u84d7\uc173\ue748\u4f6a\u561d\ud463\ud5d8\uea58\uac9e\u8e88\u477c\ufc88\u1e4f\ue2b2\u2039\uc81b\uf772\u1b51\uf2e7\ua20e\ub99a\u3fb5\u60d9\ubf39\u4bce\u1ac6\u42a2\ub847\u8975\u9775\u345d\uea8a\u59b6\u498d\u818f\u70ca\uc629\u747c\ud187\ubad9\u4f32\u4788\ud223\u35bb\ud11d\ubcd1\u53ad\ub116\ud2f2\uda84\u45b2\u3756\ub068\u1720\u6512\u2b1c\u566b\u6d17\u9df6\uc587\uaa20\uc428\u9467\u5444\u0932\uf1cb\ubcab\ub82d\ufab7\u5883\u271c\u5119\u9f87\udce5\u9958\u1eaa\uae1b\u902c\u6268\u72fa\u6df1\u37af\u5c72\u9f84\ud0d4\u83d4\ua14b\ud8cd\u4096\ud8ab\u7337\u054d\uea7b\u0d8c\u3aab\u184b\u10a1\uf289\u07ef\ubc5f\ub364\uc460\ue2ab\u41e0\u8dab\u6cfa\uc556\u9109\u677f\u57bc\u9c72\u1eac\ubf7a\u1b71\u94df\u985f\u26ab\ub157\u6c72\ua6db\u4f7c\uced9\uc03f\u461d\ud69c\u22a0\u1032\u83eb\u7ed2\u3bb9\u54a4\u7908\u159a\u02a9\u2f19\u3817\u9784\u89e1\u7b14\u6bf2\ud7e0\ubd55\ud15e\u2f4a\ue694\u4afd\ub545\u87d1\u857a\u5605\u6780\u1284\u38e4\u6ec8\u58bd\u5729\u59ef\ue483\u2c11\u6dab\u983d\ubb40\uf9e8\ufb26\u62f6\u9ad7\u9346\u2f48\ua18d\uaacd\u198a\uce5f\ub430\u16d9\u4051\u2966\u56c2\u90ba\u175e\u65b3\ua6a7\u37f5\u65a6\udfbc\u6fc5\u28d3\u58a4\u86e9\ud67d\u6c16\u7cbb\u21ee\u03c9\u9183\uf0c8\u2768\uf42f\u4680\ud539\u9940\ufc7b\u8b5d\uc6d7\u05ab\u0315\u3253\u8aef\u766c\u7856\u4c3f\u66a3\u2b14\u9ae1\ue837\ue4ba\u97fb\u03fe8\u5fcd\u5bc5\ud713\u466b\ua5ad\ue37e\u46fa\uc7c7\u5c08Y\u02d9\u5b4a\u75b5\uaba3\uef9c\ud023\uac57\u580f\ubbc0\ue054\u8de6\u569b\u77b9\ub261\u29c7\uc309\u891e\uf701\ue37b\u481c\u96fd\u47e2\ue487\u8fab\u8d14\u94aa\u1fe8\u5e65\u82e1\ub883\ua23f\uebec\u8d2a\ue095\u7e88\ua39a\ud475\ua0fd\u0771\u2a96\u6306\u1437\u7c79\uc0df\ua26b\u06f4\ubc62\uc61d\ua261\ue9cc\ue288\u0f4c\u9999\ub0f2\ud7ce\u17ad\ud134\ud568\u3f39\u29ba\u90c5\u2352\ue5bd\u5153\u49f9\u30e7\u7a6a\u805b\uaffa\ua233\u04bc\u7336\u2947\ue098\u16f6\u2e67\ubb34\u9da7\u8b15\u1bc1\u5c96\u90ea\u1a93\ubb13\uf9a6\u5d89\u3c80\ub694\u2e00\u5d85\ubbe5\u66db\ua3d1\u8b9b\ud342\u255a\u34b6\u8540\uedcf\ucb56\ubef6\u2cca\u0093\ue5f8\u6c5b\u4f2e\uadbd\u3b5a\uc255\u4e14\uee19\u4854\u3cfd\u1f04\u0dab\u7154\u1fd0\uc001\u0f53\u61bf\u3ebd\u576c\u3fba\u300a\u83a8\u29ec\u8ed2\u6895\u31b2\ua8db\u1ff5\ua068\u4e32\u1822\u55b9\u8f52\ue2c3\u6013\u356d\ue453\u5885\ud7f9\udb74\ue566\ub389\ucbad\ufdaf\u07e3\u2861\udc82\u4004\ua7e1\uacc5\u3082\u01ae\u5d03\u28a7\ub884\u6ff3\u717a\ube9d\ufe3d\u7323\ufb0f\u9bed\u0836\ub4c5\u9d46\ub8f7\u43b0\uaf06\u9d03\ue8a7\u2cc8\uf2ca\u37b0\ucef1\u4b39\u05af\ub9c7\u94d0\uf07f\u9eca\u7c23\u9e5b\uc90b\ue519\ub81b\u921e\uc211\ua791\ub596\u92f9\u609b\u5737\u7743\ue966\ucde7\u39a3\u6db0\u8713\u8e70\u8fc0\uea2e\u0502\uea67\ua67a\u8d02\u517b\u3dda\u8089\u9875\u2a9a\u9d51\ua68a\u745b\u2b68\ucfaf\u7803\u0cc7\u53c9\u984f\u5f23\u58f7\u18f7\uea12\uc169\ude2c\ufc41\u7e17\uf79f\ub004\uc00a\uae04\ud229\u7139\u4b3d\u23d2\u0fcf\ub008\ud4c4\u3156\u34a9\ucafd\uaf3e\u7f38\ua983\u4eb4\u4ec7\uc6c1\u9f6f\u5899\ue81d\u702a\uce6f\u8613\u43c5\u71a5\uaaba\ub8a3\u9310\ue22c\uf174\ufa4b\ua9bf\u7ba7\udaed\u62d8\ua4be\u0e43\u08be\u036a\uf4c1\u4bb5\uac98\uf43b\uee19\ue7db\uad49\u0a2e\u5e43\ucb33\u5867\uefb8\u46e8\uf257\uca58\u10eb\u5894\ue4ae\u6353\u4fec\ueb37\u21aa\u5ce1\u8770\u78e6\ue065\ua75d\ue502\u783e\ubc33\u19a8\ua537\ue311\u1f5f\uec41\uea18\uc933\ub2f5\uddb4\u05c3\uc49e\ub4d4\u0632\u21a5\u61c1\u31bc\u0580\ue599\udaa2\ufb01\u5725\u7b6f\u9193\u4138\ucd3a\uce64\u01c6\ud786\u9aae\ud541\u40dd\ud93b\u0ad2\u35b7\u7bb6\uf6eb\u0349\u23fa\u60dc\u7cee\uaf10\u1239\u7a31\uee30\u893f\u1f1e\u708a\u9539\uae7f\uf3cc\ue30c\ubb29\u79de\u5149\u863c\ud700\u8b9c\uf6f5\u0dda\u3efd\uf1e8\u29cf\u8d8f\u1657\u927f\u21b5\udac1\u2a4e\u9282\ub6b1\u00a7\u838b\u82e9\u2548\u2385\u02ed\ucf41\u9b08\u8e5d\u12c8\u8288\u68a3\u4383\u90f2\u3782\ud71b\ucfb6\u5c86\ua3fc\u081f\u24c5\u0a9b\uf3ad\ub96f\u1c17\u9b52\ub1cb\ua91d\u08bd\u1ac3\uc090\ueb56\u5593\ufd71\u2501\uf8c3\ua0c7\u4e3c\u8eeb\u5a75\u5c8b\u3a4e\u7701\u37c6\u550b\u25b5\u737e\u9a0b\ue6f5\u6af4\u1c33\u4b76\u6bd8\u2d7f\u7b0a\u7523\u661e\ue509\u69b5\u9c99\uf5a0\uaca0\u04b7\u4fe3\u70f0\u76f6\ube1e\uee10\ufd63\ubbe8\u4b7a\u73b4\u5fb7\u45ad\uf450\ua3c5\ua433\udf01\ucef7\u3bd0\u8663\uf46a\ue104\u253d\u8782\u0ff4\ud618\u691a\u46db\u88c9\u9e40\u9454\ua36e\ufcb9\u3a55\ucd48\u2705\u6c03\ua1f5\u6946\u97fa\uc83f\ud0d0\u9aea\u70dd\u3a56\u1e43\uf9b9\u2479\ud795\uc9ad\ud069\u5e8b\uc4a8\u91bc\u908d\u34d2\u17ad\u48cb\ude66\ua1c3\u2501\u420d\u2226\ua44e\u4a0f\ubc02\u8a4d\u5f1d\u3df6\u0ed2\u41e6\u085f\u54ec\u8f69\u78d8\u2790\udc31\ub7fc\u2fd7\ucc43\u47b2\u0d63\u6933\u127e\u9814\u1252\u4a40\u3986\uaca9\u434d\u49c7\u9e8c\u5880\uab1c\uae69\u7c4a\u810e\u60ad\u0f52\ue435\u0c98\u5d6a\uf48e\ucfad\u9ee0\uf7aa"
                           .charAt(1037)
                  )
                  - 838548951
               ^ -893518721
         );

      for (int var4 = 0; var4 < var3; var4++) {
         mc.player.networkHandler.sendPacket(new OnGroundOnly(true));
      }
   }

   private void QVDe9A6(double var1, double var3, double var5) {
      mc.player.networkHandler.sendPacket(new PositionAndOnGround(var1, var3, var5, false));
   }

   private void QVDe9A6(double var1, double var3, double var5, boolean var7) {
      ClientPlayNetworkHandler var8 = mc.player.networkHandler;
      if (!((var7 | true) & true)) {
      }

      var8.sendPacket(new Full(var1, var3, var5, mc.player.getYaw(), mc.player.getPitch(), var7));
   }

   private void X1xex5Dvp() {
      if (mc.player.isSwimming() || mc.player.isClimbing() || mc.player.isRiding()) {
         this.uIyzYN(mc.player.getPos());
         this.at7Am();
      } else if (mc.player.isOnGround() || !(mc.player.getVelocity().y < 0.0)) {
         this.uIyzYN(mc.player.getPos());
         this.setFlag1(false);
      } else if (!this.isFlag12()) {
         this.setFlag1(true);
         MaceAura var1;
         Vec3d var2;
         if (this.getP7hd() != null) {
            var2 = this.getP7hd();
            var1 = this;
         } else {
            var2 = mc.player.getPos();
            var1 = this;
         }

         var1.setRef32(var2);
         if (this.getRenderMode().is(MaceAura.IRC.Silent)) {
            this.setRef5(this.PSFwGV().add(0.0, (double)mc.player.getEyeHeight(mc.player.getPose()), 0.0));
            this.z239();
         }
      }
   }

   private void m_9cD4WRA() {
      boolean var1 = false;
      boolean var2 = this.getRenderMode().is(MaceAura.IRC.Silent);
      if (!var2) {
         this.setFlag62(false);
         this.setFlag72(false);
      } else {
         if (ClientUtil.isMoving()) {
            if (!this.isFlag62()) {
               this.setFlag62(true);
               this.TAV5eDbaq();
            }
         } else {
            this.setFlag62(false);
         }

         boolean var3 = false;
         var3 = ElytraFly.INSTANCE != null && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying();
         if (var3) {
            if (!this.isFlag72()) {
               this.setFlag72(true);
               this.TAV5eDbaq();
            }
         } else {
            this.setFlag72(false);
         }
      }
   }

   private void TAV5eDbaq() {
      if (this.isFlag12() && this.PSFwGV() != null) {
         this.lK2mnDC(this.PSFwGV());
      }
   }

   public void render(MatrixStack var1) {
      if (this.L82Oqt5LX()) {
         Render3DUtil.drawBox(var1, mc.player.getBoundingBox(), this.getSilentPlayerBox().getValue());
      }

      this.m_04giit0(this.U3EM3X());
      this.I80816IY0(this.dWfE());
   }

   @EventListener
   public void onLookDirection(LookDirectionEvent var1) {
      if (this.L82Oqt5LX()) {
         this.setAmount1((float)((double)this.U3EM3X() + var1.getCursorDeltaX() * 0.15F));
         this.BYnFXard(MathHelper.clamp((float)((double)this.dWfE() + var1.getCursorDeltaY() * 0.15F), -90.0F, 90.0F));
         var1.cancel();
      }
   }

   public boolean L82Oqt5LX() {
      return this.isOn() && this.getRenderMode().is(MaceAura.IRC.Silent) && this.isFlag12() && this.m_2o6y() != null && this.W7PV06N5();
   }

   public boolean z3Io() {
      return this.isOn() && this.isFlag12() && this.getRef22() != null && CombatUtil.isValid(this.getRef22());
   }

   private boolean m_2Z0E9(Entity var1) {
      if (!CombatUtil.isValid(var1)) {
         return false;
      } else {
         Vec3d var4 = this.NsxQb3(var1);
         double var5 = mc.player.getX() - var4.x;
         double var2;
         return var5 * var5 + (var2 = mc.player.getZ() - var4.z) * var2 <= this.getAttackRange().getValue() * this.getAttackRange().getValue();
      }
   }

   private boolean W7PV06N5() {
      Iterator var1 = mc.world.getPlayers().iterator();

      while (var1.hasNext()) {
         if (this.m_2Z0E9((Entity)var1.next())) {
            return true;
         }
      }

      return false;
   }

   public Vec3d m_2o6y2() {
      return this.m_2o6y();
   }

   public float m_2hUAl6v() {
      return MathHelper.lerpAngleDegrees(mc.getRenderTickCounter().getTickDelta(true), this.guJ1(), this.U3EM3X());
   }

   public float wL0p() {
      return MathHelper.lerpAngleDegrees(mc.getRenderTickCounter().getTickDelta(true), this.getAmount42(), this.dWfE());
   }

   private void z239() {
      if (mc.player != null) {
         this.setAmount1(mc.player.getYaw());
         this.BYnFXard(mc.player.getPitch());
         this.m_04giit0(this.U3EM3X());
         this.I80816IY0(this.dWfE());
      }
   }

   private boolean v95m() {
      return mc.player.getMainHandStack().getItem() == Items.MACE ? true : this.getSilent().getValue() && this.m_9F1wHjo() != -1;
   }

   private boolean QVDe9A6(Entity var1) {
      return CombatUtil.isValid(var1) && mc.player.getEyePos().distanceTo(this.NsxQb3(var1)) <= this.getAttackRange().getValue();
   }

   private Vec3d NsxQb3(Entity var1) {
      return MathUtil.getClosestPointToBox(mc.player.getEyePos(), var1.getBoundingBox());
   }

   private boolean attack() {
      int var1 = mc.player.getInventory().selectedSlot;
      int var2 = this.m_9F1wHjo();
      boolean var3 = false;
      Vec3d var4 = this.NsxQb3(this.getRef22());
      if (mc.player.getMainHandStack().getItem() != Items.MACE) {
         if (!this.getSilent().getValue() || var2 == -1) {
            return false;
         }

         this.hjz5DL9KO(var2);
         var3 = true;
      }

      Aevum.ROTATION.lookAt(var4);
      mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(this.getRef22(), mc.player.isSneaking()));
      mc.player.swingHand(Hand.MAIN_HAND);
      EntityUtil.swingHand(Hand.MAIN_HAND, (SwingSide)this.getSwing().getValue());
      Aevum.ROTATION.snapBack();
      if (var3) {
         if (this.getInventorySwap().getValue()) {
            this.hjz5DL9KO(var2);
            ClientUtil.Njn83();
         } else {
            InventoryUtil.switchToSlot(var1);
         }
      }

      return true;
   }

   private boolean sr6ablJ() {
      long var1 = this.g5hXzPW();
      return var1 != Long.MAX_VALUE && var1 <= (long)(42 + this.aaO455a0());
   }

   private long g5hXzPW() {
      if (mc.player.getVelocity().y >= 0.0) {
         return Long.MAX_VALUE;
      } else {
         Vec3d var1 = mc.player.getPos().add(0.0, 0.01, 0.0);
         BlockHitResult var2 = mc.world.raycast(new RaycastContext(var1, var1.add(0.0, -256.0, 0.0), ShapeType.COLLIDER, FluidHandling.WATER, mc.player));
         if (var2.getType() == Type.MISS) {
            return Long.MAX_VALUE;
         } else {
            double var3 = mc.player.getY() - var2.getPos().y;
            if (var3 <= 0.0) {
               return 0L;
            } else {
               double var5 = mc.player.getVelocity().y;
               double var7 = 0.0;
               double var9 = this.getTimer().getValue() ? 50.0 / this.getTimerSpeed().getValue() : 50.0;

               for (int var13 = 1; var13 <= 200; var13++) {
                  if ((var7 -= var5 = (var5 - 0.08) * 0.98) >= var3) {
                     return (long)Math.ceil((double)var13 * var9);
                  }
               }

               return Long.MAX_VALUE;
            }
         }
      }
   }

   private int aaO455a0() {
      PlayerListEntry var1 = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
      return var1 == null ? 0 : var1.getLatency();
   }

   private void hjz5DL9KO(int var1) {
      if (this.getInventorySwap().getValue()) {
         ClientUtil.FgmR(var1, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(var1);
      }
   }

   private int m_9F1wHjo() {
      return this.getInventorySwap().getValue() ? InventoryUtil.findItemInventorySlot(Items.MACE) : InventoryUtil.findItem(Items.MACE);
   }

   private void at7Am() {
      this.setRef32(null);
      this.setFlag1(false);
      this.setRef5(null);
   }

   private static double[] getRef1() {
      return ref1;
   }

   private SliderSetting getTargetRange() {
      return this.targetRange;
   }

   private SliderSetting getAttackRange() {
      return this.attackRange;
   }

   private SliderSetting O3ZZ8762() {
      return this.triggerDistance;
   }

   private SliderSetting getLagbackDistance() {
      return this.lagbackDistance;
   }

   private SliderSetting getSpoofHeight() {
      return this.spoofHeight;
   }

   private SliderSetting iuEV() {
      return this.cooldownTicks;
   }

   private BooleanSetting getTimer() {
      return this.timer;
   }

   private SliderSetting getTimerSpeed() {
      return this.timerSpeed;
   }

   private BooleanSetting yb38() {
      return this.maceOnly;
   }

   private BooleanSetting getSilent() {
      return this.silent;
   }

   private EnumSetting getRenderMode() {
      return this.renderMode;
   }

   private ColorSetting getSilentPlayerBox() {
      return this.silentPlayerBox;
   }

   private BooleanSetting getInventorySwap() {
      return this.inventorySwap;
   }

   private BooleanSetting getOnlyFalling() {
      return this.onlyFalling;
   }

   private BooleanSetting getOnlyAir() {
      return this.onlyAir;
   }

   private BooleanSetting YKNUVo() {
      return this.autoAttack;
   }

   private BooleanSetting getSet() {
      return this.set;
   }

   private BooleanSetting getGroundSpoof() {
      return this.groundSpoof;
   }

   private BooleanSetting uz25() {
      return this.attackPull;
   }

   private EnumSetting qYhGJIAccess() {
      return this.qYhGJI;
   }

   private SliderSetting getDelay() {
      return this.delay;
   }

   private SliderSetting getDelayUntilSwitch() {
      return this.delayUntilSwitch;
   }

   private SliderSetting getLagDelay() {
      return this.lagDelay;
   }

   private SliderSetting getDelayUntilAttack() {
      return this.delayUntilAttack;
   }

   private SliderSetting HvJVAccess() {
      return this.HvJV;
   }

   private SliderSetting W06e() {
      return this.lagFly;
   }

   private SliderSetting getSmartXZ() {
      return this.smartXZ;
   }

   private SliderSetting getSmartUp() {
      return this.smartUp;
   }

   private SliderSetting getSmartDown() {
      return this.smartDown;
   }

   private SliderSetting E6Xe() {
      return this.smartDist;
   }

   private BooleanSetting getNoWait() {
      return this.noWait;
   }

   private EnumSetting getSwing() {
      return this.swing;
   }

   private PlayerEntity getRef22() {
      return this.ref2;
   }

   private void m_5ka21(PlayerEntity var1) {
      this.ref2 = var1;
   }

   private Vec3d PSFwGV() {
      return this.ref3;
   }

   private void setRef32(Vec3d var1) {
      this.ref3 = var1;
   }

   private Vec3d getP7hd() {
      return this.P7hd;
   }

   private void uIyzYN(Vec3d var1) {
      this.P7hd = var1;
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

   private void setFlag1(boolean var1) {
      this.flag1 = var1;
   }

   private Timer getTimer12() {
      return this.timer1;
   }

   private Timer getTimer22() {
      return this.timer2;
   }

   private int getCount22() {
      return this.count2;
   }

   private void setCount2(int var1) {
      this.count2 = var1;
   }

   private boolean isFlag22() {
      return this.flag2;
   }

   private void fwAS0(boolean var1) {
      this.flag2 = var1;
   }

   private boolean isFlag32() {
      return this.flag3;
   }

   private void zEi0(boolean var1) {
      this.flag3 = var1;
   }

   private boolean isFlag42() {
      return this.flag4;
   }

   private void setFlag42(boolean var1) {
      this.flag4 = var1;
   }

   private void Bu81F(boolean var1) {
      this.flag5 = var1;
   }

   private PlayerEntity getRef42() {
      return this.ref4;
   }

   private void wpts2(PlayerEntity var1) {
      this.ref4 = var1;
   }

   private Vec3d m_2o6y() {
      return this.ref5;
   }

   private void setRef5(Vec3d var1) {
      this.ref5 = var1;
   }

   private boolean isFlag62() {
      return this.flag6;
   }

   private void setFlag62(boolean var1) {
      this.flag6 = var1;
   }

   private boolean isFlag72() {
      return this.flag7;
   }

   private void setFlag72(boolean var1) {
      this.flag7 = var1;
   }

   private float U3EM3X() {
      return this.amount1;
   }

   private void setAmount1(float var1) {
      this.amount1 = var1;
   }

   private float dWfE() {
      return this.amount2;
   }

   private void BYnFXard(float var1) {
      this.amount2 = var1;
   }

   private float guJ1() {
      return this.amount3;
   }

   private void m_04giit0(float var1) {
      this.amount3 = var1;
   }

   private float getAmount42() {
      return this.amount4;
   }

   private void I80816IY0(float var1) {
      this.amount4 = var1;
   }

   private static Throwable xb(Throwable var0) {
      return var0;
   }

   public static enum IRC {
      Normal,
      Silent;

      private IRC() {
      }
   }

   public static enum StackDir {
      Smart,
      Invalid,
      TrollHack,
      ToVoid,
      ToVoid2,
      Normal,
      Rotation,
      Fly,
      Glide;

      private StackDir() {
      }
   }
}
