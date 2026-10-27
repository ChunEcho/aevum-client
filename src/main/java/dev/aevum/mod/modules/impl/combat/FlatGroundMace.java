/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Mob
 *  net.minecraft.Animal
 *  net.minecraft.Villager
 *  net.minecraft.Player
 *  net.minecraft.Items
 *  net.minecraft.BlockGetter
 *  net.minecraft.Blocks
 *  net.minecraft.BlockPos
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.WanderingTrader
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.Aura;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Mob;
import net.minecraft.Animal;
import net.minecraft.Villager;
import net.minecraft.Player;
import net.minecraft.Items;
import net.minecraft.BlockGetter;
import net.minecraft.Blocks;
import net.minecraft.BlockPos;
import net.minecraft.Packet;
import net.minecraft.BlockState;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.WanderingTrader;

public class FlatGroundMace
extends Module {
    public static FlatGroundMace INSTANCE;
    private final EnumSetting<Aura.Cooldown> mode = this.add(new EnumSetting<Aura.Cooldown>("Mode", Aura.Cooldown.Vanilla));
    private final SliderSetting enFUJwkCj = this.add(new SliderSetting("FallHeight", 4.0, 0.5, 15.0, 0.1));
    private final SliderSetting resetDistance = this.add(new SliderSetting("ResetDistance", 0.1, 0.05, 0.5, 0.05));
    private final BooleanSetting requireMace = this.add(new BooleanSetting("RequireMace", false));
    private final BooleanSetting lockView = this.add(new BooleanSetting("LockView", true));
    private final SliderSetting splitStep = this.add(new SliderSetting("SplitStep", 0.42, 0.1, 1.0, 0.01, () -> true));
    private final SliderSetting blinkDelay = this.add(new SliderSetting("BlinkDelay", 3.0, 1.0, 20.0, 1.0, () -> this.getMode().is(Aura.Cooldown.BlinkBuffer)));
    private final SliderSetting spoofHeight = this.add(new SliderSetting("SpoofHeight", 12.0, 2.0, 70.0, 1.0, () -> this.getMode().is(Aura.Cooldown.MotionSpoof)));
    private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 1.0, 8.0, 0.5, () -> true));
    private final SliderSetting wallRange = this.add(new SliderSetting("WallRange", 4.0, 0.5, 8.0, 0.5, () -> true));
    private final BooleanSetting players = this.add(new BooleanSetting("Players", true, () -> true));
    private final BooleanSetting mobs = this.add(new BooleanSetting("Mobs", false, () -> true));
    private final BooleanSetting animals = this.add(new BooleanSetting("Animals", false, () -> true));
    private final BooleanSetting villagers = this.add(new BooleanSetting("Villagers", false, () -> true));
    private final SliderSetting voidDepth = this.add(new SliderSetting("VoidDepth", 8.0, 3.0, 20.0, 1.0, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
    private final SliderSetting attackDelay = this.add(new SliderSetting("AttackDelay", 1.0, 0.0, 10.0, 1.0, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
    private final BooleanSetting spoof = this.add(new BooleanSetting("Spoof", true, () -> this.getMode().is(Aura.Cooldown.TheVoid)));
    private final EnumSetting<Synthetic3> vMMode = this.add(new EnumSetting<Synthetic3>("VMMode", Synthetic3.DoublePacket, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
    private final SliderSetting voidY = this.add(new SliderSetting("VoidY", -60.0, -100.0, -50.0, 1.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
    private final SliderSetting attackPackets = this.add(new SliderSetting("AttackPackets", 3.0, 1.0, 10.0, 1.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
    private final SliderSetting cycleDelay = this.add(new SliderSetting("CycleDelay", 0.0, 0.0, 1000.0, 50.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
    private final SliderSetting vMFallHeight = this.add(new SliderSetting("VMFallHeight", 100.0, 20.0, 500.0, 10.0, () -> this.getMode().is(Aura.Cooldown.VoidMace)));
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
    private final CopyOnWriteArrayList<Packet<?>> packetBuffer = new CopyOnWriteArrayList();
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
    private Synthetic4 ref1;
    private final Timer timer1;
    private final Timer timer2;

    public FlatGroundMace() {
        super("FlatGroundMace", "\u6a21\u62df\u9ad8\u7a7a\u4e0b\u5760\u5faa\u73af\uff0c\u89e6\u53d1\u91cd\u9524\u66b4\u51fb\u30029\u79cd\u6a21\u5f0f\u9002\u914d\u4e0d\u540c\u53cd\u4f5c\u5f0a\u73af\u5883", Module.Category.Combat);
        this.setRef12(Synthetic4.IDLE);
        this.timer1 = new Timer();
        this.timer2 = new Timer();
        this.setChinese("\u5e73\u5730\u91cd\u9524");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (FlatGroundMace.nullCheck()) {
            this.disable();
            return;
        }
        this.PRfLKxXf(this.m_2hUAl6v());
        this.BJJN(FlatGroundMace.mc.player.getX());
        this.PX4C(FlatGroundMace.mc.player.getY());
        this.I4l2(FlatGroundMace.mc.player.getZ());
        this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
        this.O744Cf05(false);
        this.setValue52(FlatGroundMace.mc.player.getY());
        this.getPacketBuffer().clear();
        this.setC33y(false);
        this.vhuem9c0(0);
        this.m_2e3B7v61(false);
        this.setCount22(0);
        this.setTarget12(null);
        this.setValue6(FlatGroundMace.mc.player.getY());
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
        this.setRef12(Synthetic4.IDLE);
        this.getTimer12().reset();
        this.getTimer22().reset();
    }

    @Override
    public void onDisable() {
        if (this.isC33y() && !this.getPacketBuffer().isEmpty()) {
            Iterator iterator = this.getPacketBuffer().iterator();
            while (iterator.hasNext()) {
                mc.getConnection().method_52787((Packet)iterator.next());
            }
        }
        this.getPacketBuffer().clear();
        this.setC33y(false);
        this.O744Cf05(false);
        if (this.Vve9() && this.Wdpl() != null) {
            mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.Wdpl(), (boolean)FlatGroundMace.mc.player.isShiftKeyDown()));
        }
        this.m_2e3B7v61(false);
        this.setTarget12(null);
        if (this.isNC9y() && this.QcCZCW() != null && this.QcCZCW().isAlive()) {
            mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.QcCZCW(), (boolean)FlatGroundMace.mc.player.isShiftKeyDown()));
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
    public void onTick(ClientTickEvent clientTickEvent) {
        if (FlatGroundMace.nullCheck()) {
            return;
        }
        if (clientTickEvent.isPre()) {
            if (this.getRequireMace().getValue() && FlatGroundMace.mc.player.getMainHandItem().method_7909() != Items.MACE) {
                return;
            }
            double y = FlatGroundMace.mc.player.getY();
            double n = this.getW0Nz() + this.m_548i3().getValue();
            this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
            if (this.getCount42() > 0) {
                this.lw43w903(this.getCount42() - 1);
            }
            if (this.getCount32() > 0) {
                this.setCount32(this.getCount32() - 1);
            }
            switch (((Aura.Cooldown)((Object)this.getMode().getValue())).ordinal()) {
                case 0: {
                    this.lK2mnDC(y, n);
                    break;
                }
                case 1: {
                    this.NsxQb3(y, n);
                    break;
                }
                case 2: {
                    this.g5hXzPW(y, n);
                    break;
                }
                case 3: {
                    this.QVDe9A6(y, n);
                    break;
                }
                case 4: {
                    this.attack();
                    break;
                }
                case 5: {
                    this.sendPacket(y, n);
                    break;
                }
                case 6: {
                    this.m_2Z0E9(y, n);
                    break;
                }
                case 7: {
                    this.attack2();
                    break;
                }
                case 8: {
                    this.yU1d08();
                }
            }
        } else if (this.getLockView().getValue()) {
            FlatGroundMace.mc.player.xo = this.getValue12();
            FlatGroundMace.mc.player.yo = this.getValue2();
            FlatGroundMace.mc.player.zo = this.getValue32();
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send send) {
        if (FlatGroundMace.nullCheck()) {
            return;
        }
        Packet<?> packet = send.getPacket();
        if (this.getMode().is(Aura.Cooldown.BlinkBuffer) && this.isC33y() && packet instanceof ServerboundMovePlayerPacket) {
            send.cancel();
            this.getPacketBuffer().add(packet);
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive receive) {
        if (FlatGroundMace.nullCheck()) {
            return;
        }
        if (!this.getMode().is(Aura.Cooldown.VoidMace)) {
            return;
        }
        Packet<?> packet = receive.getPacket();
        if (packet instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket playerPositionLookS2CPacket = (ClientboundPlayerPositionPacket)packet;
            FlatGroundMace.mc.player.method_5814(playerPositionLookS2CPacket.getX(), playerPositionLookS2CPacket.getY(), playerPositionLookS2CPacket.getZ());
            FlatGroundMace.mc.player.method_36456(playerPositionLookS2CPacket.getYRot());
            FlatGroundMace.mc.player.method_36457(playerPositionLookS2CPacket.getXRot());
            FlatGroundMace.mc.player.fallDistance = 0.0f;
            this.X2YPT();
            if (this.LOUBbq()) {
                Aevum.TIMER.reset();
                this.setFlag22(false);
            }
        }
    }

    private void lK2mnDC(double n, double n2) {
        if (n <= n2) {
            FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.getValue42(), FlatGroundMace.mc.player.getZ());
            FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
        }
    }

    private void NsxQb3(double n, double n2) {
        if (n <= n2 && this.getCount32() <= 0) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(FlatGroundMace.mc.player.getX(), this.getValue42(), FlatGroundMace.mc.player.getZ(), false));
            this.setCount32((int)this.m_80j5().getValue());
            FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.getValue42(), FlatGroundMace.mc.player.getZ());
            FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
        }
    }

    private void g5hXzPW(double value52, double n) {
        if (!this.isRDY2() && value52 <= n) {
            this.O744Cf05(true);
            this.setValue52(value52);
        }
        if (this.isRDY2()) {
            if (this.XeyVw() < this.getValue42()) {
                this.setValue52(this.XeyVw() + this.rFpDuRQ().getValue());
                if (this.XeyVw() > this.getValue42()) {
                    this.setValue52(this.getValue42());
                }
                FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.XeyVw(), FlatGroundMace.mc.player.getZ());
                FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
            } else {
                this.O744Cf05(false);
            }
        }
    }

    private void QVDe9A6(double n, double n2) {
        if (!this.isC33y() && n <= n2) {
            this.setC33y(true);
            this.vhuem9c0(0);
            FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.getValue42(), FlatGroundMace.mc.player.getZ());
            FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
            return;
        }
        if (this.isC33y()) {
            this.vhuem9c0(this.getCount12() + 1);
            if (this.getCount12() >= (int)this.getBlinkDelay().getValue()) {
                Iterator iterator = this.getPacketBuffer().iterator();
                while (iterator.hasNext()) {
                    mc.getConnection().method_52787((Packet)iterator.next());
                }
                this.getPacketBuffer().clear();
                this.setC33y(false);
            }
        }
    }

    private void attack() {
        if (this.Vve9()) {
            if (this.getCount22() < 6) {
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(FlatGroundMace.mc.player.getX(), this.getValue62() + (double)(this.getCount22() + 1) * (this.getSpoofHeight().getValue() / 6.0), FlatGroundMace.mc.player.getZ(), FlatGroundMace.mc.player.getYRot(), FlatGroundMace.mc.player.getXRot(), false));
                this.setCount22(this.getCount22() + 1);
            } else if (this.getCount22() == 6) {
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(FlatGroundMace.mc.player.getX(), this.getValue62(), FlatGroundMace.mc.player.getZ(), FlatGroundMace.mc.player.getYRot(), FlatGroundMace.mc.player.getXRot(), false));
                this.setCount22(this.getCount22() + 1);
            } else if (this.getCount22() == 7) {
                this.setCount22(this.getCount22() + 1);
            } else if (this.getCount22() == 8) {
                if (this.Wdpl() != null && this.Wdpl().isAlive() && CombatUtil.isValid(this.Wdpl())) {
                    mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.Wdpl(), (boolean)FlatGroundMace.mc.player.isShiftKeyDown()));
                    FlatGroundMace.mc.player.resetAttackStrengthTicker();
                    EntityUtil.swingHand(InteractionHand.MAIN_HAND, SwingSide.All);
                }
                this.setTarget12(null);
                this.m_2e3B7v61(false);
            }
            return;
        }
        Entity target = this.findTarget();
        if (target == null) {
            return;
        }
        this.setTarget12(target);
        this.m_2e3B7v61(true);
        this.setCount22(0);
        this.setValue6(FlatGroundMace.mc.player.getY());
    }

    private Entity findTarget() {
        Entity entity = null;
        double value = this.getRange().getValue();
        for (Entity entity2 : Aevum.THREAD.getEntities()) {
            double distanceTo;
            if (entity2 == null || entity2 == FlatGroundMace.mc.player || !entity2.isAlive() || !this.g5hXzPW(entity2) || !CombatUtil.isValid(entity2) || (distanceTo = FlatGroundMace.mc.player.getEyePosition().method_1022(entity2.position())) > this.getRange().getValue() || !FlatGroundMace.mc.player.method_6057(entity2) && distanceTo > this.getWallRange().getValue() || entity != null && distanceTo >= value) continue;
            entity = entity2;
            value = distanceTo;
        }
        return entity;
    }

    private boolean g5hXzPW(Entity entity) {
        if (entity instanceof Player) {
            return this.de5C().getValue();
        }
        if (!(entity instanceof Villager) && !(entity instanceof WanderingTrader)) {
            if (entity instanceof Animal) {
                return this.getAnimals().getValue();
            }
            return entity instanceof Mob && this.getMobs().getValue();
        }
        return this.getVillagers().getValue();
    }

    private void sendPacket(double n, double n2) {
        if (n <= n2 && this.getCount42() <= 0) {
            FlatGroundMace.sendSequencedPacket(seq -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, seq, FlatGroundMace.mc.player.getYRot(), FlatGroundMace.mc.player.getXRot()));
            this.lw43w903(10);
            FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.getValue42(), FlatGroundMace.mc.player.getZ());
            FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
        }
    }

    private void m_2Z0E9(double value52, double n) {
        if (!this.isRDY2() && value52 <= n) {
            this.O744Cf05(true);
            this.setValue52(value52);
        }
        if (this.isRDY2()) {
            if (this.XeyVw() < this.getValue42()) {
                this.setValue52(this.XeyVw() + (this.m_11R08A8() ? this.rFpDuRQ().getValue() * 2.0 : this.rFpDuRQ().getValue()));
                if (this.XeyVw() > this.getValue42()) {
                    this.setValue52(this.getValue42());
                }
                FlatGroundMace.mc.player.method_5814(FlatGroundMace.mc.player.getX(), this.XeyVw(), FlatGroundMace.mc.player.getZ());
                FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
            } else {
                this.O744Cf05(false);
            }
        }
    }

    private void attack2() {
        if (this.isNC9y()) {
            switch (this.s83b7()) {
                case 0: {
                    this.NsxQb3(this.m_1ovb(), this.getValue9(), this.getYn8T());
                    this.NsxQb3(this.m_1ovb(), this.getValue9() - this.getVoidDepth().getValue() / 2.0, this.getYn8T());
                    this.NsxQb3(this.m_1ovb(), this.getValue9() - this.getVoidDepth().getValue(), this.getYn8T());
                    this.NsxQb3(this.m_1ovb(), this.getValue82() + 1.0, this.getYn8T());
                    this.setCount5(1);
                    this.eR4J4((int)this.getAttackDelay().getValue());
                    break;
                }
                case 1: {
                    this.eR4J4(this.getCount72() + 1);
                    if (this.getCount72() <= (int)this.getAttackDelay().getValue()) break;
                    if (this.QcCZCW() != null && this.QcCZCW().isAlive() && CombatUtil.isValid(this.QcCZCW())) {
                        this.eR4J4(0);
                        mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.QcCZCW(), (boolean)FlatGroundMace.mc.player.isShiftKeyDown()));
                        FlatGroundMace.mc.player.resetAttackStrengthTicker();
                        if (this.getCount62() == 0) {
                            EntityUtil.swingHand(InteractionHand.MAIN_HAND, SwingSide.All);
                        }
                        this.HORmHtB(this.getCount62() + 1);
                        if (this.getCount62() < 3) break;
                        this.M8nC(false);
                        this.setEntity(null);
                        break;
                    }
                    this.M8nC(false);
                    this.setEntity(null);
                    return;
                }
            }
            return;
        }
        Entity target = this.findTarget();
        if (target == null) {
            return;
        }
        this.setEntity(target);
        this.setValue7(FlatGroundMace.mc.player.getX());
        this.setValue8(FlatGroundMace.mc.player.getY());
        this.setYn8T(FlatGroundMace.mc.player.getZ());
        this.Tv7S2((double)FlatGroundMace.mc.level.method_31607() - this.getVoidDepth().getValue());
        this.HORmHtB(0);
        this.eR4J4(0);
        this.M8nC(true);
        this.setCount5(0);
    }

    private void NsxQb3(double n, double n2, double n3) {
        if (this.getSpoof().getValue()) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(n, n2, n3, FlatGroundMace.mc.player.getYRot(), FlatGroundMace.mc.player.getXRot(), false));
        } else {
            FlatGroundMace.mc.player.method_5814(n, n2, n3);
            FlatGroundMace.mc.player.method_18800(FlatGroundMace.mc.player.getDeltaMovement().x, 0.0, FlatGroundMace.mc.player.getDeltaMovement().z);
        }
    }

    private void yU1d08() {
        if (this.getVMMode().is(Synthetic3.DoublePacket)) {
            this.T066P();
        } else {
            this.Yod6t();
        }
    }

    private void T066P() {
        if (!this.getTimer22().passedMs((long)this.V92OL().getValue())) {
            return;
        }
        this.getTimer22().reset();
        Entity target = this.findTarget();
        if (target == null) {
            return;
        }
        double x = FlatGroundMace.mc.player.getX();
        double y = FlatGroundMace.mc.player.getY();
        double z = FlatGroundMace.mc.player.getZ();
        double n = y;
        boolean b = false;
        b = this.m_1h01().getValue() && this.YVa9u();
        boolean b2 = b;
        if (b2) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y + this.getBurrowLift().getValue(), z, false));
            if (!this.getViewLock().getValue()) {
                FlatGroundMace.mc.player.method_5814(x, y + this.getBurrowLift().getValue(), z);
            }
            y += this.getBurrowLift().getValue();
        }
        if (this.getAntiWeb().getValue() && this.m_9Vi8()) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y + 1.1, z, false));
            if (!this.getViewLock().getValue()) {
                FlatGroundMace.mc.player.method_5814(x, y + 1.1, z);
            }
            y += 1.1;
        }
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, this.getVoidY().getValue(), z, false));
        mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, y + 1.0, z, false));
        if (!this.getViewLock().getValue()) {
            FlatGroundMace.mc.player.method_5814(x, y + 1.0, z);
            FlatGroundMace.mc.player.fallDistance = (float)this.gksn3().getValue();
        } else {
            FlatGroundMace.mc.player.fallDistance = 0.0f;
        }
        this.attack(target);
        if (this.jWxr().getValue() && b2) {
            mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(x, n, z, false));
            if (!this.getViewLock().getValue()) {
                FlatGroundMace.mc.player.method_5814(x, n, z);
            }
        }
    }

    private void Yod6t() {
        Entity target = this.findTarget();
        if (target == null) {
            if (this.LOUBbq()) {
                Aevum.TIMER.reset();
                this.setFlag22(false);
            }
            this.X2YPT();
            return;
        }
        switch (this.FaTboL().ordinal()) {
            case 0: {
                if (!this.getTimer22().passedMs((long)this.V92OL().getValue())) {
                    return;
                }
                this.setKfSK(FlatGroundMace.mc.player.getX());
                this.setValue10(FlatGroundMace.mc.player.getY());
                this.n49691(FlatGroundMace.mc.player.getZ());
                double value = this.getVoidY().getValue();
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(this.KfSKAccess(), value, this.uF33(), false));
                if (!this.getViewLock().getValue()) {
                    FlatGroundMace.mc.player.method_5814(this.KfSKAccess(), value, this.uF33());
                    FlatGroundMace.mc.player.fallDistance = (float)this.gksn3().getValue();
                } else {
                    FlatGroundMace.mc.player.fallDistance = 0.0f;
                }
                Aevum.TIMER.set(this.q4K6().getValueFloat());
                this.setFlag22(true);
                this.setRef12(Synthetic4.FALLING);
                this.getTimer12().reset();
                break;
            }
            case 1: {
                if (!this.getTimer12().passedMs((long)this.getFallTime().getValue())) {
                    return;
                }
                double n = this.getValue10() + 1.0;
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(this.KfSKAccess(), n, this.uF33(), false));
                if (!this.getViewLock().getValue()) {
                    FlatGroundMace.mc.player.method_5814(this.KfSKAccess(), n, this.uF33());
                }
                if (this.LOUBbq()) {
                    Aevum.TIMER.reset();
                    this.setFlag22(false);
                }
                if (this.getAttackPackets().getValueInt() > 0) {
                    this.attack(target);
                }
                this.setKfSK(FlatGroundMace.mc.player.getX());
                this.setValue10(FlatGroundMace.mc.player.getY());
                this.n49691(FlatGroundMace.mc.player.getZ());
                double value2 = this.getVoidY().getValue();
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(this.KfSKAccess(), value2, this.uF33(), false));
                if (!this.getViewLock().getValue()) {
                    FlatGroundMace.mc.player.method_5814(this.KfSKAccess(), value2, this.uF33());
                    FlatGroundMace.mc.player.fallDistance = (float)this.gksn3().getValue();
                } else {
                    FlatGroundMace.mc.player.fallDistance = 0.0f;
                }
                Aevum.TIMER.set(this.q4K6().getValueFloat());
                this.setFlag22(true);
                this.getTimer12().reset();
                break;
            }
        }
    }

    private void attack(Entity entity) {
        if (entity != null && entity.isAlive()) {
            int valueInt = this.getAttackPackets().getValueInt();
            for (int i = 0; i < valueInt; ++i) {
                mc.getConnection().method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)entity, (boolean)FlatGroundMace.mc.player.isShiftKeyDown()));
            }
            FlatGroundMace.mc.player.resetAttackStrengthTicker();
            if (this.getSwing().getValue()) {
                EntityUtil.swingHand(InteractionHand.MAIN_HAND, SwingSide.All);
            }
        }
    }

    private boolean m_9Vi8() {
        BlockPos blockPos = FlatGroundMace.mc.player.method_24515();
        return FlatGroundMace.mc.level.method_8320(blockPos).method_27852(Blocks.COBWEB) || FlatGroundMace.mc.level.method_8320(blockPos.method_10084()).method_27852(Blocks.COBWEB);
    }

    private boolean YVa9u() {
        BlockPos blockPos = FlatGroundMace.mc.player.method_24515();
        return FlatGroundMace.mc.level.method_8320(blockPos).method_26234((BlockGetter)FlatGroundMace.mc.level, blockPos);
    }

    private void X2YPT() {
        this.setRef12(Synthetic4.IDLE);
        this.getTimer12().reset();
        this.getTimer22().reset();
        this.setKfSK(0.0);
        this.setValue10(0.0);
        this.n49691(0.0);
    }

    private boolean m_11R08A8() {
        boolean b = false;
        BlockPos blockPos = FlatGroundMace.mc.player.method_24515();
        BlockState blockState = FlatGroundMace.mc.level.method_8320(blockPos.method_10095());
        return blockState.method_51366() || FlatGroundMace.mc.level.method_8320(blockPos.method_10072()).method_51366() || FlatGroundMace.mc.level.method_8320(blockPos.east()).method_51366() || FlatGroundMace.mc.level.method_8320(blockPos.method_10067()).method_51366();
    }

    private double m_2hUAl6v() {
        BlockPos blockPos = FlatGroundMace.mc.player.method_24515();
        while (blockPos.method_10264() > FlatGroundMace.mc.level.method_31607()) {
            BlockPos down = blockPos.method_10074();
            if (FlatGroundMace.mc.level.method_8320(down).method_51366()) {
                return (double)down.method_10264() + 1.0;
            }
            blockPos = down;
        }
        return FlatGroundMace.mc.player.getY();
    }

    public void m_0Q8veEz() {
        this.PRfLKxXf(this.m_2hUAl6v());
        this.setValue42(this.getW0Nz() + this.enFUJwkCjAccess().getValue());
    }

    @Override
    public String getInfo() {
        Object obj = null;
        switch (((Aura.Cooldown)((Object)this.getMode().getValue())).ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                obj = "\u539f\u7248";
                break;
            }
            case 1: {
                obj = "TP";
                break;
            }
            case 2: {
                obj = "\u62c6\u5206";
                break;
            }
            case 3: {
                obj = "\u5ef6\u8fdf";
                break;
            }
            case 4: {
                obj = "\u6b3a\u9a97" + this.getSpoofHeight().getValueInt() + "\u683c";
                break;
            }
            case 5: {
                obj = "\u98ce\u5f39";
                break;
            }
            case 6: {
                obj = "\u6df7\u5408";
                break;
            }
            case 7: {
                obj = "\u865a\u7a7a" + this.getVoidDepth().getValueInt() + "\u683c";
                break;
            }
            case 8: {
                obj = "VM-" + String.valueOf(((Enum)this.getVMMode().getValue()).name());
            }
        }
        return String.valueOf(obj) + " " + this.enFUJwkCjAccess().getValueInt() + "\u683c";
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

    private void PRfLKxXf(double w0Nz) {
        this.w0Nz = w0Nz;
    }

    private double getValue12() {
        return this.value1;
    }

    private void BJJN(double value1) {
        this.value1 = value1;
    }

    private double getValue2() {
        return this.value2;
    }

    private void PX4C(double value2) {
        this.value2 = value2;
    }

    private double getValue32() {
        return this.value3;
    }

    private void I4l2(double value3) {
        this.value3 = value3;
    }

    private double getValue42() {
        return this.value4;
    }

    private void setValue42(double value4) {
        this.value4 = value4;
    }

    private boolean isRDY2() {
        return this.RDY2;
    }

    private void O744Cf05(boolean rdy2) {
        if (!((rdy2 | true) & true)) {
            // empty if block
        }
        this.RDY2 = rdy2;
    }

    private double XeyVw() {
        return this.value5;
    }

    private void setValue52(double value5) {
        this.value5 = value5;
    }

    private CopyOnWriteArrayList getPacketBuffer() {
        return this.packetBuffer;
    }

    private boolean isC33y() {
        return this.c33y;
    }

    private void setC33y(boolean c33y) {
        this.c33y = c33y;
    }

    private int getCount12() {
        return this.count1;
    }

    private void vhuem9c0(int count1) {
        this.count1 = count1;
    }

    private boolean Vve9() {
        return this.flag1;
    }

    private void m_2e3B7v61(boolean flag1) {
        if (!((flag1 | true) & true)) {
            // empty if block
        }
        this.flag1 = flag1;
    }

    private int getCount22() {
        return this.count2;
    }

    private void setCount22(int count2) {
        this.count2 = count2;
    }

    private Entity Wdpl() {
        return this.target1;
    }

    private void setTarget12(Entity target1) {
        this.target1 = target1;
    }

    private double getValue62() {
        return this.value6;
    }

    private void setValue6(double value6) {
        this.value6 = value6;
    }

    private int getCount32() {
        return this.count3;
    }

    private void setCount32(int count3) {
        this.count3 = count3;
    }

    private int getCount42() {
        return this.count4;
    }

    private void lw43w903(int count4) {
        if (((count4 | 1) & 1) == 0) {
            // empty if block
        }
        this.count4 = count4;
    }

    private boolean isNC9y() {
        return this.NC9y;
    }

    private void M8nC(boolean nc9y) {
        this.NC9y = nc9y;
    }

    private int s83b7() {
        return this.count5;
    }

    private void setCount5(int count5) {
        this.count5 = count5;
    }

    private Entity QcCZCW() {
        return this.target2;
    }

    private void setEntity(Entity target2) {
        this.target2 = target2;
    }

    private double m_1ovb() {
        return this.value7;
    }

    private void setValue7(double value7) {
        this.value7 = value7;
    }

    private double getValue82() {
        return this.value8;
    }

    private void setValue8(double value8) {
        this.value8 = value8;
    }

    private double getYn8T() {
        return this.yn8T;
    }

    private void setYn8T(double yn8T) {
        this.yn8T = yn8T;
    }

    private double getValue9() {
        return this.value9;
    }

    private void Tv7S2(double value9) {
        this.value9 = value9;
    }

    private int getCount62() {
        return this.count6;
    }

    private void HORmHtB(int count6) {
        this.count6 = count6;
    }

    private int getCount72() {
        return this.count7;
    }

    private void eR4J4(int count7) {
        this.count7 = count7;
    }

    private double KfSKAccess() {
        return this.KfSK;
    }

    private void setKfSK(double kfSK) {
        this.KfSK = kfSK;
    }

    private double getValue10() {
        return this.value10;
    }

    private void setValue10(double value10) {
        this.value10 = value10;
    }

    private double uF33() {
        return this.value11;
    }

    private void n49691(double value11) {
        this.value11 = value11;
    }

    private boolean LOUBbq() {
        return this.flag2;
    }

    private void setFlag22(boolean flag2) {
        this.flag2 = flag2;
    }

    private Synthetic4 FaTboL() {
        return this.ref1;
    }

    private void setRef12(Synthetic4 ref1) {
        this.ref1 = ref1;
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

    }

    private static enum Synthetic4 {
        IDLE,
        FALLING;

    }
}

