/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Items
 *  net.minecraft.BlockPos
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.ServerboundMovePlayerPacket$StatusOnly
 *  net.minecraft.ServerboundSetCarriedItemPacket
 *  net.minecraft.ServerboundSwingPacket
 *  net.minecraft.ServerboundUseItemPacket
 *  net.minecraft.Minecraft
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$Fluid
 *  net.minecraft.ClipContext$Block
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PoseStack
 *  net.minecraft.ClientPacketListener
 *  net.minecraft.PlayerInfo
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.LookDirectionEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.TimerEvent;
import dev.aevum.api.utils.combat.CombatUtil;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.api.utils.math.Timer;
import dev.aevum.api.utils.misc.ClientUtil;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.api.utils.render.Render3DUtil;
import dev.aevum.asm.accessors.IPlayerMoveC2SPacket;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.movement.ElytraFly;
import dev.aevum.mod.modules.settings.enums.SwingSide;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.HitResult;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.ServerboundSetCarriedItemPacket;
import net.minecraft.ServerboundSwingPacket;
import net.minecraft.ServerboundUseItemPacket;
import net.minecraft.Minecraft;
import net.minecraft.Mth;
import net.minecraft.ClipContext;
import net.minecraft.BlockHitResult;
import net.minecraft.PoseStack;
import net.minecraft.ClientPacketListener;
import net.minecraft.PlayerInfo;

public class MaceAura
extends Module {
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
    private final EnumSetting<IRC> renderMode;
    private final ColorSetting silentPlayerBox;
    private final BooleanSetting inventorySwap;
    private final BooleanSetting onlyFalling;
    private final BooleanSetting onlyAir;
    private final BooleanSetting autoAttack;
    private final BooleanSetting set;
    private final BooleanSetting groundSpoof;
    private final BooleanSetting attackPull;
    private final EnumSetting<StackDir> qYhGJI;
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
    private Player ref2;
    private Vec3 ref3;
    private Vec3 P7hd;
    private int count1;
    private boolean flag1;
    private final Timer timer1 = new Timer();
    private final Timer timer2 = new Timer();
    private int count2;
    private boolean flag2;
    private boolean flag3;
    private boolean flag4;
    private boolean flag5;
    private Player ref4;
    private Vec3 ref5;
    private boolean flag6;
    private boolean flag7;
    private float amount1;
    private float amount2;
    private float amount3;
    private float amount4;

    public MaceAura() {
        super("MaceAura", Module.Category.Combat);
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
        this.renderMode = this.add(new EnumSetting<IRC>("RenderMode", IRC.Normal));
        this.silentPlayerBox = this.add(new ColorSetting("SilentPlayerBox", new Color(255, 80, 80, 180), () -> this.getRenderMode().is(IRC.Silent)));
        this.inventorySwap = this.add(new BooleanSetting("InventorySwap", false, () -> this.getSilent().getValue()));
        this.onlyFalling = this.add(new BooleanSetting("OnlyFalling", true));
        this.onlyAir = this.add(new BooleanSetting("OnlyAir", true));
        this.autoAttack = this.add(new BooleanSetting("AutoAttack", true));
        this.set = this.add(new BooleanSetting("Set", false).setParent());
        this.groundSpoof = this.add(new BooleanSetting("GroundSpoof", true, () -> this.getSet().isOpen()));
        this.attackPull = this.add(new BooleanSetting("AttackPull", false, () -> this.getSet().isOpen()));
        this.qYhGJI = this.add(new EnumSetting<StackDir>("LagMode", StackDir.Fly, () -> this.getSet().isOpen()));
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
        this.swing = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.All));
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
        String string = this.getRef22() == null ? null : this.getRef22().method_5477().getString();
        return string;
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
    public void onTimer(TimerEvent timerEvent) {
        if (this.getTimer().getValue() && this.isFlag12()) {
            timerEvent.set(this.getTimerSpeed().getValueFloat());
        }
    }

    @EventListener
    public void onTick(ClientTickEvent clientTickEvent) {
        if (!clientTickEvent.isPost() && !MaceAura.nullCheck()) {
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
            if (this.YKNUVo().getValue() && this.isFlag12() && this.QVDe9A6((Entity)this.getRef22()) && this.attack()) {
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
            if (this.getOnlyAir().getValue() && MaceAura.mc.player.onGround()) {
                this.at7Am();
                return;
            }
            if (!(MaceAura.mc.player.isSwimming() || MaceAura.mc.player.onClimbable() || MaceAura.mc.player.isHandsBusy())) {
                if (this.getOnlyFalling().getValue() && MaceAura.mc.player.getDeltaMovement().y >= 0.0) {
                    return;
                }
                if (this.PSFwGV() != null && this.PSFwGV().y - MaceAura.mc.player.getY() >= this.O3ZZ8762().getValue() && this.getCount12() <= 0) {
                    if (this.getLagbackDistance().getValue() > 0.0 && (double)MaceAura.mc.player.method_5739((Entity)this.getRef22()) > this.getLagbackDistance().getValue()) {
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
        Vec3 psFwGV = this.PSFwGV();
        if (psFwGV == null) {
            return false;
        }
        if (!this.getNoWait().getValue() && MaceAura.mc.player.getAttackStrengthScale(0.5f) < 1.0f) {
            return false;
        }
        this.lK2mnDC(psFwGV);
        return true;
    }

    private void lK2mnDC(Vec3 vec3d) {
        this.Bu81F(true);
        try {
            this.g5hXzPW(vec3d);
            MaceAura.mc.player.method_5814(vec3d.x, vec3d.y, vec3d.z);
            MaceAura.mc.player.method_18800(MaceAura.mc.player.getDeltaMovement().x, 0.0, MaceAura.mc.player.getDeltaMovement().z);
            MaceAura.mc.player.fallDistance = 0.0f;
            this.at7Am();
            this.setCount1(this.iuEV().getValueInt());
            if (this.getSet().getValue()) {
                this.gGN22JJ0();
            }
            this.Bu81F(false);
        }
        catch (Throwable xb) {
            this.Bu81F(false);
            throw xb;
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send send) {
        ServerboundMovePlayerPacket playerMoveC2SPacket;
        Packet<?> class_25962;
        if (!MaceAura.nullCheck() && this.getSet().getValue() && this.getGroundSpoof().getValue() && (class_25962 = send.getPacket()) instanceof ServerboundMovePlayerPacket && (playerMoveC2SPacket = (ServerboundMovePlayerPacket)class_25962).method_12273()) {
            ((IPlayerMoveC2SPacket)playerMoveC2SPacket).setOnGround(false);
        }
    }

    private void checkTimers() {
        Player m_2z0E9;
        if (this.isFlag42() && MaceAura.mc.player.onGround() && this.getCount22() == 0) {
            this.setFlag42(false);
            this.getTimer12().reset();
        }
        if ((m_2z0E9 = this.m_2Z0E9(this.HvJVAccess().getValue())) == null) {
            this.sVkz5();
            return;
        }
        if (this.getCount22() == 0) {
            if (!this.isFlag42() && !this.isFlag12() && this.getCount12() <= 0 && this.getTimer12().passedMs(this.getDelay().getValueInt()) && this.m_1k7zt5()) {
                this.wpts2(m_2z0E9);
                this.setFlag42(true);
                this.getTimer22().reset();
                this.fwAS0(false);
                this.zEi0(false);
                this.setCount2(1);
            }
            return;
        }
        if (!this.isFlag22() && this.getTimer22().passedMs(this.getDelayUntilSwitch().getValueInt())) {
            int item = InventoryUtil.findItem(Items.MACE);
            if (item != -1) {
                if (((item | 1) & 1) == 0) {
                    // empty if block
                }
                if (MaceAura.mc.player.getInventory().selected != item) {
                    MaceAura.mc.player.getInventory().selected = item;
                    MaceAura.mc.player.connection.method_52787((Packet)new ServerboundSetCarriedItemPacket(item));
                }
            }
            this.fwAS0(true);
        }
        if (!this.isFlag32() && this.getTimer22().passedMs(this.getLagDelay().getValueInt())) {
            this.m_0F8p3();
            this.zEi0(true);
        }
        if (this.getTimer22().passedMs(this.getDelayUntilAttack().getValueInt())) {
            if (this.isFlag12() && this.getRef42() != null && this.getRef42().isAlive() && (double)MaceAura.mc.player.method_5739((Entity)this.getRef42()) <= this.HvJVAccess().getValue()) {
                MaceAura.mc.player.connection.method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.getRef42(), (boolean)MaceAura.mc.player.isShiftKeyDown()));
                MaceAura.mc.player.connection.method_52787((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                Vec3 psFwGV = this.PSFwGV();
                if (psFwGV != null) {
                    this.lK2mnDC(psFwGV);
                    return;
                }
            }
            this.sVkz5();
        }
    }

    private void m_0F8p3() {
        switch (((Enum)this.qYhGJIAccess().getValue()).ordinal()) {
            case 0: {
                ArrayList<BlockPos> list = new ArrayList<BlockPos>();
                for (double n = MaceAura.mc.player.getX() - this.getSmartXZ().getValue(); n < MaceAura.mc.player.getX() + this.getSmartXZ().getValue(); n += 1.0) {
                    for (double n2 = MaceAura.mc.player.getZ() - this.getSmartXZ().getValue(); n2 < MaceAura.mc.player.getZ() + this.getSmartXZ().getValue(); n2 += 1.0) {
                        for (double n3 = MaceAura.mc.player.getY() - this.getSmartDown().getValue(); n3 < MaceAura.mc.player.getY() + this.getSmartUp().getValue(); n3 += 1.0) {
                            list.add(BlockPos.method_49637((double)n, (double)n3, (double)n2));
                        }
                    }
                }
                double n4 = Double.MAX_VALUE;
                BlockPos blockPos = null;
                for (BlockPos blockPos2 : list) {
                    double squaredDistanceTo;
                    if (!this.Yod6t(blockPos2) || !((double)Mth.method_15355((float)((float)MaceAura.mc.player.method_5707(blockPos2.method_46558().method_1031(0.0, -0.5, 0.0)))) >= this.E6Xe().getValue()) || (squaredDistanceTo = MaceAura.mc.player.method_5707(blockPos2.method_46558())) >= n4) continue;
                    n4 = squaredDistanceTo;
                    blockPos = blockPos2;
                }
                if (blockPos == null) break;
                this.m_2Z0E9((double)blockPos.method_10263() + 0.5, blockPos.method_10264(), (double)blockPos.getZ() + 0.5, false);
                break;
            }
            case 1: {
                for (int i = 0; i < 20; ++i) {
                    Minecraft ntagHp7U8 = mc;
                    if (((i | 1) & 1) == 0) {
                        // empty if block
                    }
                    this.m_2Z0E9(ntagHp7U8.player.getX(), MaceAura.mc.player.getY() + 1337.0, MaceAura.mc.player.getZ(), false);
                }
                break;
            }
            case 2: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 2.3400880035762786, MaceAura.mc.player.getZ(), false);
                break;
            }
            case 3: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), -70.0, MaceAura.mc.player.getZ(), false);
                break;
            }
            case 4: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), -7.0, MaceAura.mc.player.getZ(), false);
                break;
            }
            case 5: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 1.9, MaceAura.mc.player.getZ(), false);
                break;
            }
            case 6: {
                MaceAura.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot(-180.0f, -90.0f, false));
                MaceAura.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Rot(180.0f, 90.0f, false));
                break;
            }
            case 7: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + this.W06e().getValue(), MaceAura.mc.player.getZ(), false);
                break;
            }
            case 8: {
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 1.1001, MaceAura.mc.player.getZ(), false);
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 1.0605, MaceAura.mc.player.getZ(), false);
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 1.0802, MaceAura.mc.player.getZ(), false);
                this.m_2Z0E9(MaceAura.mc.player.getX(), MaceAura.mc.player.getY() + 1.1127, MaceAura.mc.player.getZ(), false);
            }
        }
    }

    private boolean m_1k7zt5() {
        int selectedSlot;
        boolean b = false;
        if (MaceAura.mc.player.getMainHandItem().method_7909() == Items.WIND_CHARGE) {
            selectedSlot = MaceAura.mc.player.getInventory().selected;
        } else {
            int item = InventoryUtil.findItem(Items.WIND_CHARGE);
            if (item != -1) {
                selectedSlot = item;
            } else {
                int itemInventorySlotFromZero = InventoryUtil.findItemInventorySlotFromZero(Items.WIND_CHARGE);
                if (itemInventorySlotFromZero == -1) {
                    return false;
                }
                selectedSlot = itemInventorySlotFromZero;
                b = true;
            }
        }
        float yaw = MaceAura.mc.player.getYRot();
        Aevum.ROTATION.snapAt(yaw, 90.0f);
        if (MaceAura.mc.player.getMainHandItem().method_7909() == Items.WIND_CHARGE) {
            MaceAura.mc.player.connection.method_52787((Packet)new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, 90.0f));
            MaceAura.mc.player.method_6104(InteractionHand.MAIN_HAND);
        } else if (b) {
            ClientUtil.FgmR(selectedSlot, MaceAura.mc.player.getInventory().selected);
            MaceAura.mc.player.connection.method_52787((Packet)new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, 90.0f));
            MaceAura.mc.player.method_6104(InteractionHand.MAIN_HAND);
            ClientUtil.FgmR(selectedSlot, MaceAura.mc.player.getInventory().selected);
            ClientUtil.Njn83();
        } else {
            int selectedSlot2 = MaceAura.mc.player.getInventory().selected;
            InventoryUtil.switchToSlot(selectedSlot);
            MaceAura.mc.player.connection.method_52787((Packet)new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, yaw, 90.0f));
            MaceAura.mc.player.method_6104(InteractionHand.MAIN_HAND);
            InventoryUtil.switchToSlot(selectedSlot2);
        }
        Aevum.ROTATION.snapBack();
        return true;
    }

    private boolean Yod6t(BlockPos blockPos) {
        return MaceAura.mc.level.method_8320(blockPos).method_26215() && MaceAura.mc.level.method_8320(blockPos.method_10084()).method_26215();
    }

    private void m_2Z0E9(double n, double n2, double n3, boolean b) {
        ClientPacketListener networkHandler = MaceAura.mc.player.connection;
        if (!((b | true) & true)) {
            // empty if block
        }
        networkHandler.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(n, n2, n3, b));
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

    private Player m_2Z0E9(double n) {
        Player playerEntity = null;
        double n2 = Double.MAX_VALUE;
        for (Player playerEntity2 : MaceAura.mc.level.method_18456()) {
            double n3;
            if (playerEntity2 == MaceAura.mc.player || !playerEntity2.isAlive() || Aevum.FRIEND.isFriend(playerEntity2) || (n3 = (double)MaceAura.mc.player.method_5739((Entity)playerEntity2)) > n || n3 >= n2) continue;
            n2 = n3;
            playerEntity = playerEntity2;
        }
        return playerEntity;
    }

    private void g5hXzPW(Vec3 vec3d) {
        boolean b = false;
        double x = MaceAura.mc.player.getX();
        double y = MaceAura.mc.player.getY();
        Minecraft ntagHp7U8 = mc;
        double z = ntagHp7U8.player.getZ();
        double[] ref1 = MaceAura.getRef1();
        int length = ref1.length;
        for (int i = 0; i < length; ++i) {
            this.QVDe9A6(x, y + ref1[i], z);
        }
        Vec3 vec3d2 = new Vec3(x, y + MaceAura.getRef1()[MaceAura.getRef1().length - 1], z);
        this.QVDe9A6(vec3d2, vec3d);
        Vec3 vec3d3 = new Vec3(vec3d.x, vec3d.y + this.getSpoofHeight().getValue(), vec3d.z);
        this.m_2Z0E9(vec3d2, vec3d3);
        this.QVDe9A6(vec3d3.x, vec3d3.y, vec3d3.z, false);
        this.m_2Z0E9(vec3d3, vec3d);
        this.QVDe9A6(vec3d.x, vec3d.y, vec3d.z, true);
    }

    private void QVDe9A6(Vec3 vec3d, Vec3 vec3d2) {
        double x = vec3d.x;
        double z = vec3d.z;
        double y = vec3d.y;
        while (Math.abs(x - vec3d2.x) > 0.25) {
            double n4;
            double n3;
            double n = x;
            if (x > vec3d2.x) {
                double n2 = n;
                n3 = -0.25;
                n4 = n2;
            } else {
                double n5 = n;
                n3 = 0.25;
                n4 = n5;
            }
            x = n4 + n3;
            this.QVDe9A6(x, y, z);
        }
        while (Math.abs(z - vec3d2.z) > 0.25) {
            double n9;
            double n8;
            double n6 = z;
            if (z > vec3d2.z) {
                double n7 = n6;
                n8 = -0.25;
                n9 = n7;
            } else {
                double n10 = n6;
                n8 = 0.25;
                n9 = n10;
            }
            z = n9 + n8;
            this.QVDe9A6(x, y, z);
        }
        if (x == vec3d2.x && z == vec3d2.z) {
            return;
        }
        this.QVDe9A6(vec3d2.x, y, vec3d2.z);
    }

    private void m_2Z0E9(Vec3 vec3d, Vec3 vec3d2) {
        int n = (int)Math.ceil(vec3d.method_1022(vec3d2) / 10.0) - (("\u9090\uf103\u0bc7\ucf77\u4f23\ue57c\ubf11\uf7dc\u5c4b\u02dd\uff98\ufeb8\udb23\u7cde\u2377\ua2df\ua91f\u60b3\u11fe\u0485\u6828\u1aaa\u894f\u6016\u8243\udac9\uc945\u6c37\u28e7\u9387\u0991\ud23e\u3496\uc127\u181d\u52f3\u1971\u1ee7\u4fdf\ue22c\udb3d\u3111\u4c6f\u76f1\u0d96\u1e4c\u6431\u3520\u2cde\u58b5\u4137\u775b\ub299\uc18b\ua218\ufece\ua177\u97e9\ua3ea\u7c28\u4780\udc05\u268a\u6fed\uf452\u6587\u7ba8\ub079\u8ab0\u3391\ue482\uad4d\u89f2\u6b7b\ub863\ue1dd\u3ed5\uc6a2\u0390\ubb64\u566d\u668c\u60a3\uf16e\u8641\u5586\ufee9\u5372\ubb3f\u61a1\u3836\u269f\u5824\u0d88\ucbd1\u11af\u586a\u3aff\uf942\ub976\u3f7b\u1769\ufb49\u5a22\u27d6\u4a3e\u5cdb\uc5f6\u9f51\ub8cb\u19c7\ue411\u9afe\u7fb9\ubf82\uaa7d\u9529\u0bcf\u9fb7\u69ff\u34a3\uecc0\u3035\f\ud276\ucd5b\u2211\uba91\u5241\u8a5c\ue345\u34d6\u6290\ud7ed\ubb18\u2500\u81fa\ucb49\u0a3b\uf5ab\u8693\ud085\u8af2\u79c2\u7893\u83ad\u3d86\ue108\u4671\u587e\u2f6b\ucdef\u2ac6\u039f\ub4b0\u29a8\u6b04\uebfc\ue1f6\u77f6\u3af9\u59a2\u8dcc\u4995\u07e5\u3ae3\ua16c\u0387\udd98\ubd83\u7fc4\ub781\u5257\uf69c\u64b2\u4805\u41cb\u41e5\u161e\u0d70\uad5f\u0505\u39bf\u3fd7\ue68e\u4f13\u73b2\u62c3\u87e4\u872c\ub495\u0be5\u2601\u1cbd\u7e5b\u7dc3\ue4c6\ufccc\u775e\uae45\u8961\u5895\uec82\u02cc\u5794\u37a4\ua3b8\uef02\u0470\uee8d\u75c6\ub070\u1dad\u120e\u6e10\uc814\u8166\ue8db\ufabe\u9eb5\ue07a\ued10\u56b1\u121f\ube15\u36d3\u2b74\ud700\uc007\ud767\ub06d\u4866\u27c0\uec06\u014e\u0615\u8a14\u50fc\u62a8\u3012\u55f4\u197b\u0c41\u8539\u1d0a\u4023\u7437\u0497\u8da9\u6f0e\udd6a\u83b8\u53fb\u2bdd\u85cf\u3ffb\uabe3\udbed\u2b55\ua007\ueef4\ud325\ue62d\ucba0\u7281\ud8b4\u6273\u38e6\u872d\uc1e6\uf3ab\ufd7c\ue02a\u7335\udd23\u10d7\u8d92\u74be\u1c2f\ua31f\ubfd7\u772a\u2a4a\u6e4e\u6e1b\u1ba8\ue911\u475d\u9b29\ua03f\ub23c\u9fa6\u1732\u6de3\u9de9\u8304\ub502\ue58a\u3d99\ud36c\udde8\u408f\uff13\u7548\ubd54\ua609\udaa4\u2ae0\u0f87\u14b7\u9e07\u00bc\uf5e3\u50ed\ud7f1\u8176\ua9c7\u8ea9\u555e\u05be\u4af9\u4efd\u8d7e\u00d7\u0ad5\u52a6\ud4e9\ua8f8\uce0d\u1651\uf236\ud251\udedf\u3235\u15db\u60d0\u92c4\u39c6\u7bb3\ueeb4\uff55\u40aa\u7f27\u84d7\uc173\ue748\u4f6a\u561d\ud463\ud5d8\uea58\uac9e\u8e88\u477c\ufc88\u1e4f\ue2b2\u2039\uc81b\uf772\u1b51\uf2e7\ua20e\ub99a\u3fb5\u60d9\ubf39\u4bce\u1ac6\u42a2\ub847\u8975\u9775\u345d\uea8a\u59b6\u498d\u818f\u70ca\uc629\u747c\ud187\ubad9\u4f32\u4788\ud223\u35bb\ud11d\ubcd1\u53ad\ub116\ud2f2\uda84\u45b2\u3756\ub068\u1720\u6512\u2b1c\u566b\u6d17\u9df6\uc587\uaa20\uc428\u9467\u5444\u0932\uf1cb\ubcab\ub82d\ufab7\u5883\u271c\u5119\u9f87\udce5\u9958\u1eaa\uae1b\u902c\u6268\u72fa\u6df1\u37af\u5c72\u9f84\ud0d4\u83d4\ua14b\ud8cd\u4096\ud8ab\u7337\u054d\uea7b\u0d8c\u3aab\u184b\u10a1\uf289\u07ef\ubc5f\ub364\uc460\ue2ab\u41e0\u8dab\u6cfa\uc556\u9109\u677f\u57bc\u9c72\u1eac\ubf7a\u1b71\u94df\u985f\u26ab\ub157\u6c72\ua6db\u4f7c\uced9\uc03f\u461d\ud69c\u22a0\u1032\u83eb\u7ed2\u3bb9\u54a4\u7908\u159a\u02a9\u2f19\u3817\u9784\u89e1\u7b14\u6bf2\ud7e0\ubd55\ud15e\u2f4a\ue694\u4afd\ub545\u87d1\u857a\u5605\u6780\u1284\u38e4\u6ec8\u58bd\u5729\u59ef\ue483\u2c11\u6dab\u983d\ubb40\uf9e8\ufb26\u62f6\u9ad7\u9346\u2f48\ua18d\uaacd\u198a\uce5f\ub430\u16d9\u4051\u2966\u56c2\u90ba\u175e\u65b3\ua6a7\u37f5\u65a6\udfbc\u6fc5\u28d3\u58a4\u86e9\ud67d\u6c16\u7cbb\u21ee\u03c9\u9183\uf0c8\u2768\uf42f\u4680\ud539\u9940\ufc7b\u8b5d\uc6d7\u05ab\u0315\u3253\u8aef\u766c\u7856\u4c3f\u66a3\u2b14\u9ae1\ue837\ue4ba\u97fb\u03fe8\u5fcd\u5bc5\ud713\u466b\ua5ad\ue37e\u46fa\uc7c7\u5c08Y\u02d9\u5b4a\u75b5\uaba3\uef9c\ud023\uac57\u580f\ubbc0\ue054\u8de6\u569b\u77b9\ub261\u29c7\uc309\u891e\uf701\ue37b\u481c\u96fd\u47e2\ue487\u8fab\u8d14\u94aa\u1fe8\u5e65\u82e1\ub883\ua23f\uebec\u8d2a\ue095\u7e88\ua39a\ud475\ua0fd\u0771\u2a96\u6306\u1437\u7c79\uc0df\ua26b\u06f4\ubc62\uc61d\ua261\ue9cc\ue288\u0f4c\u9999\ub0f2\ud7ce\u17ad\ud134\ud568\u3f39\u29ba\u90c5\u2352\ue5bd\u5153\u49f9\u30e7\u7a6a\u805b\uaffa\ua233\u04bc\u7336\u2947\ue098\u16f6\u2e67\ubb34\u9da7\u8b15\u1bc1\u5c96\u90ea\u1a93\ubb13\uf9a6\u5d89\u3c80\ub694\u2e00\u5d85\ubbe5\u66db\ua3d1\u8b9b\ud342\u255a\u34b6\u8540\uedcf\ucb56\ubef6\u2cca\u0093\ue5f8\u6c5b\u4f2e\uadbd\u3b5a\uc255\u4e14\uee19\u4854\u3cfd\u1f04\u0dab\u7154\u1fd0\uc001\u0f53\u61bf\u3ebd\u576c\u3fba\u300a\u83a8\u29ec\u8ed2\u6895\u31b2\ua8db\u1ff5\ua068\u4e32\u1822\u55b9\u8f52\ue2c3\u6013\u356d\ue453\u5885\ud7f9\udb74\ue566\ub389\ucbad\ufdaf\u07e3\u2861\udc82\u4004\ua7e1\uacc5\u3082\u01ae\u5d03\u28a7\ub884\u6ff3\u717a\ube9d\ufe3d\u7323\ufb0f\u9bed\u0836\ub4c5\u9d46\ub8f7\u43b0\uaf06\u9d03\ue8a7\u2cc8\uf2ca\u37b0\ucef1\u4b39\u05af\ub9c7\u94d0\uf07f\u9eca\u7c23\u9e5b\uc90b\ue519\ub81b\u921e\uc211\ua791\ub596\u92f9\u609b\u5737\u7743\ue966\ucde7\u39a3\u6db0\u8713\u8e70\u8fc0\uea2e\u0502\uea67\ua67a\u8d02\u517b\u3dda\u8089\u9875\u2a9a\u9d51\ua68a\u745b\u2b68\ucfaf\u7803\u0cc7\u53c9\u984f\u5f23\u58f7\u18f7\uea12\uc169\ude2c\ufc41\u7e17\uf79f\ub004\uc00a\uae04\ud229\u7139\u4b3d\u23d2\u0fcf\ub008\ud4c4\u3156\u34a9\ucafd\uaf3e\u7f38\ua983\u4eb4\u4ec7\uc6c1\u9f6f\u5899\ue81d\u702a\uce6f\u8613\u43c5\u71a5\uaaba\ub8a3\u9310\ue22c\uf174\ufa4b\ua9bf\u7ba7\udaed\u62d8\ua4be\u0e43\u08be\u036a\uf4c1\u4bb5\uac98\uf43b\uee19\ue7db\uad49\u0a2e\u5e43\ucb33\u5867\uefb8\u46e8\uf257\uca58\u10eb\u5894\ue4ae\u6353\u4fec\ueb37\u21aa\u5ce1\u8770\u78e6\ue065\ua75d\ue502\u783e\ubc33\u19a8\ua537\ue311\u1f5f\uec41\uea18\uc933\ub2f5\uddb4\u05c3\uc49e\ub4d4\u0632\u21a5\u61c1\u31bc\u0580\ue599\udaa2\ufb01\u5725\u7b6f\u9193\u4138\ucd3a\uce64\u01c6\ud786\u9aae\ud541\u40dd\ud93b\u0ad2\u35b7\u7bb6\uf6eb\u0349\u23fa\u60dc\u7cee\uaf10\u1239\u7a31\uee30\u893f\u1f1e\u708a\u9539\uae7f\uf3cc\ue30c\ubb29\u79de\u5149\u863c\ud700\u8b9c\uf6f5\u0dda\u3efd\uf1e8\u29cf\u8d8f\u1657\u927f\u21b5\udac1\u2a4e\u9282\ub6b1\u00a7\u838b\u82e9\u2548\u2385\u02ed\ucf41\u9b08\u8e5d\u12c8\u8288\u68a3\u4383\u90f2\u3782\ud71b\ucfb6\u5c86\ua3fc\u081f\u24c5\u0a9b\uf3ad\ub96f\u1c17\u9b52\ub1cb\ua91d\u08bd\u1ac3\uc090\ueb56\u5593\ufd71\u2501\uf8c3\ua0c7\u4e3c\u8eeb\u5a75\u5c8b\u3a4e\u7701\u37c6\u550b\u25b5\u737e\u9a0b\ue6f5\u6af4\u1c33\u4b76\u6bd8\u2d7f\u7b0a\u7523\u661e\ue509\u69b5\u9c99\uf5a0\uaca0\u04b7\u4fe3\u70f0\u76f6\ube1e\uee10\ufd63\ubbe8\u4b7a\u73b4\u5fb7\u45ad\uf450\ua3c5\ua433\udf01\ucef7\u3bd0\u8663\uf46a\ue104\u253d\u8782\u0ff4\ud618\u691a\u46db\u88c9\u9e40\u9454\ua36e\ufcb9\u3a55\ucd48\u2705\u6c03\ua1f5\u6946\u97fa\uc83f\ud0d0\u9aea\u70dd\u3a56\u1e43\uf9b9\u2479\ud795\uc9ad\ud069\u5e8b\uc4a8\u91bc\u908d\u34d2\u17ad\u48cb\ude66\ua1c3\u2501\u420d\u2226\ua44e\u4a0f\ubc02\u8a4d\u5f1d\u3df6\u0ed2\u41e6\u085f\u54ec\u8f69\u78d8\u2790\udc31\ub7fc\u2fd7\ucc43\u47b2\u0d63\u6933\u127e\u9814\u1252\u4a40\u3986\uaca9\u434d\u49c7\u9e8c\u5880\uab1c\uae69\u7c4a\u810e\u60ad\u0f52\ue435\u0c98\u5d6a\uf48e\ucfad\u9ee0\uf7aa".charAt(1036) << 16 | "\u9090\uf103\u0bc7\ucf77\u4f23\ue57c\ubf11\uf7dc\u5c4b\u02dd\uff98\ufeb8\udb23\u7cde\u2377\ua2df\ua91f\u60b3\u11fe\u0485\u6828\u1aaa\u894f\u6016\u8243\udac9\uc945\u6c37\u28e7\u9387\u0991\ud23e\u3496\uc127\u181d\u52f3\u1971\u1ee7\u4fdf\ue22c\udb3d\u3111\u4c6f\u76f1\u0d96\u1e4c\u6431\u3520\u2cde\u58b5\u4137\u775b\ub299\uc18b\ua218\ufece\ua177\u97e9\ua3ea\u7c28\u4780\udc05\u268a\u6fed\uf452\u6587\u7ba8\ub079\u8ab0\u3391\ue482\uad4d\u89f2\u6b7b\ub863\ue1dd\u3ed5\uc6a2\u0390\ubb64\u566d\u668c\u60a3\uf16e\u8641\u5586\ufee9\u5372\ubb3f\u61a1\u3836\u269f\u5824\u0d88\ucbd1\u11af\u586a\u3aff\uf942\ub976\u3f7b\u1769\ufb49\u5a22\u27d6\u4a3e\u5cdb\uc5f6\u9f51\ub8cb\u19c7\ue411\u9afe\u7fb9\ubf82\uaa7d\u9529\u0bcf\u9fb7\u69ff\u34a3\uecc0\u3035\f\ud276\ucd5b\u2211\uba91\u5241\u8a5c\ue345\u34d6\u6290\ud7ed\ubb18\u2500\u81fa\ucb49\u0a3b\uf5ab\u8693\ud085\u8af2\u79c2\u7893\u83ad\u3d86\ue108\u4671\u587e\u2f6b\ucdef\u2ac6\u039f\ub4b0\u29a8\u6b04\uebfc\ue1f6\u77f6\u3af9\u59a2\u8dcc\u4995\u07e5\u3ae3\ua16c\u0387\udd98\ubd83\u7fc4\ub781\u5257\uf69c\u64b2\u4805\u41cb\u41e5\u161e\u0d70\uad5f\u0505\u39bf\u3fd7\ue68e\u4f13\u73b2\u62c3\u87e4\u872c\ub495\u0be5\u2601\u1cbd\u7e5b\u7dc3\ue4c6\ufccc\u775e\uae45\u8961\u5895\uec82\u02cc\u5794\u37a4\ua3b8\uef02\u0470\uee8d\u75c6\ub070\u1dad\u120e\u6e10\uc814\u8166\ue8db\ufabe\u9eb5\ue07a\ued10\u56b1\u121f\ube15\u36d3\u2b74\ud700\uc007\ud767\ub06d\u4866\u27c0\uec06\u014e\u0615\u8a14\u50fc\u62a8\u3012\u55f4\u197b\u0c41\u8539\u1d0a\u4023\u7437\u0497\u8da9\u6f0e\udd6a\u83b8\u53fb\u2bdd\u85cf\u3ffb\uabe3\udbed\u2b55\ua007\ueef4\ud325\ue62d\ucba0\u7281\ud8b4\u6273\u38e6\u872d\uc1e6\uf3ab\ufd7c\ue02a\u7335\udd23\u10d7\u8d92\u74be\u1c2f\ua31f\ubfd7\u772a\u2a4a\u6e4e\u6e1b\u1ba8\ue911\u475d\u9b29\ua03f\ub23c\u9fa6\u1732\u6de3\u9de9\u8304\ub502\ue58a\u3d99\ud36c\udde8\u408f\uff13\u7548\ubd54\ua609\udaa4\u2ae0\u0f87\u14b7\u9e07\u00bc\uf5e3\u50ed\ud7f1\u8176\ua9c7\u8ea9\u555e\u05be\u4af9\u4efd\u8d7e\u00d7\u0ad5\u52a6\ud4e9\ua8f8\uce0d\u1651\uf236\ud251\udedf\u3235\u15db\u60d0\u92c4\u39c6\u7bb3\ueeb4\uff55\u40aa\u7f27\u84d7\uc173\ue748\u4f6a\u561d\ud463\ud5d8\uea58\uac9e\u8e88\u477c\ufc88\u1e4f\ue2b2\u2039\uc81b\uf772\u1b51\uf2e7\ua20e\ub99a\u3fb5\u60d9\ubf39\u4bce\u1ac6\u42a2\ub847\u8975\u9775\u345d\uea8a\u59b6\u498d\u818f\u70ca\uc629\u747c\ud187\ubad9\u4f32\u4788\ud223\u35bb\ud11d\ubcd1\u53ad\ub116\ud2f2\uda84\u45b2\u3756\ub068\u1720\u6512\u2b1c\u566b\u6d17\u9df6\uc587\uaa20\uc428\u9467\u5444\u0932\uf1cb\ubcab\ub82d\ufab7\u5883\u271c\u5119\u9f87\udce5\u9958\u1eaa\uae1b\u902c\u6268\u72fa\u6df1\u37af\u5c72\u9f84\ud0d4\u83d4\ua14b\ud8cd\u4096\ud8ab\u7337\u054d\uea7b\u0d8c\u3aab\u184b\u10a1\uf289\u07ef\ubc5f\ub364\uc460\ue2ab\u41e0\u8dab\u6cfa\uc556\u9109\u677f\u57bc\u9c72\u1eac\ubf7a\u1b71\u94df\u985f\u26ab\ub157\u6c72\ua6db\u4f7c\uced9\uc03f\u461d\ud69c\u22a0\u1032\u83eb\u7ed2\u3bb9\u54a4\u7908\u159a\u02a9\u2f19\u3817\u9784\u89e1\u7b14\u6bf2\ud7e0\ubd55\ud15e\u2f4a\ue694\u4afd\ub545\u87d1\u857a\u5605\u6780\u1284\u38e4\u6ec8\u58bd\u5729\u59ef\ue483\u2c11\u6dab\u983d\ubb40\uf9e8\ufb26\u62f6\u9ad7\u9346\u2f48\ua18d\uaacd\u198a\uce5f\ub430\u16d9\u4051\u2966\u56c2\u90ba\u175e\u65b3\ua6a7\u37f5\u65a6\udfbc\u6fc5\u28d3\u58a4\u86e9\ud67d\u6c16\u7cbb\u21ee\u03c9\u9183\uf0c8\u2768\uf42f\u4680\ud539\u9940\ufc7b\u8b5d\uc6d7\u05ab\u0315\u3253\u8aef\u766c\u7856\u4c3f\u66a3\u2b14\u9ae1\ue837\ue4ba\u97fb\u03fe8\u5fcd\u5bc5\ud713\u466b\ua5ad\ue37e\u46fa\uc7c7\u5c08Y\u02d9\u5b4a\u75b5\uaba3\uef9c\ud023\uac57\u580f\ubbc0\ue054\u8de6\u569b\u77b9\ub261\u29c7\uc309\u891e\uf701\ue37b\u481c\u96fd\u47e2\ue487\u8fab\u8d14\u94aa\u1fe8\u5e65\u82e1\ub883\ua23f\uebec\u8d2a\ue095\u7e88\ua39a\ud475\ua0fd\u0771\u2a96\u6306\u1437\u7c79\uc0df\ua26b\u06f4\ubc62\uc61d\ua261\ue9cc\ue288\u0f4c\u9999\ub0f2\ud7ce\u17ad\ud134\ud568\u3f39\u29ba\u90c5\u2352\ue5bd\u5153\u49f9\u30e7\u7a6a\u805b\uaffa\ua233\u04bc\u7336\u2947\ue098\u16f6\u2e67\ubb34\u9da7\u8b15\u1bc1\u5c96\u90ea\u1a93\ubb13\uf9a6\u5d89\u3c80\ub694\u2e00\u5d85\ubbe5\u66db\ua3d1\u8b9b\ud342\u255a\u34b6\u8540\uedcf\ucb56\ubef6\u2cca\u0093\ue5f8\u6c5b\u4f2e\uadbd\u3b5a\uc255\u4e14\uee19\u4854\u3cfd\u1f04\u0dab\u7154\u1fd0\uc001\u0f53\u61bf\u3ebd\u576c\u3fba\u300a\u83a8\u29ec\u8ed2\u6895\u31b2\ua8db\u1ff5\ua068\u4e32\u1822\u55b9\u8f52\ue2c3\u6013\u356d\ue453\u5885\ud7f9\udb74\ue566\ub389\ucbad\ufdaf\u07e3\u2861\udc82\u4004\ua7e1\uacc5\u3082\u01ae\u5d03\u28a7\ub884\u6ff3\u717a\ube9d\ufe3d\u7323\ufb0f\u9bed\u0836\ub4c5\u9d46\ub8f7\u43b0\uaf06\u9d03\ue8a7\u2cc8\uf2ca\u37b0\ucef1\u4b39\u05af\ub9c7\u94d0\uf07f\u9eca\u7c23\u9e5b\uc90b\ue519\ub81b\u921e\uc211\ua791\ub596\u92f9\u609b\u5737\u7743\ue966\ucde7\u39a3\u6db0\u8713\u8e70\u8fc0\uea2e\u0502\uea67\ua67a\u8d02\u517b\u3dda\u8089\u9875\u2a9a\u9d51\ua68a\u745b\u2b68\ucfaf\u7803\u0cc7\u53c9\u984f\u5f23\u58f7\u18f7\uea12\uc169\ude2c\ufc41\u7e17\uf79f\ub004\uc00a\uae04\ud229\u7139\u4b3d\u23d2\u0fcf\ub008\ud4c4\u3156\u34a9\ucafd\uaf3e\u7f38\ua983\u4eb4\u4ec7\uc6c1\u9f6f\u5899\ue81d\u702a\uce6f\u8613\u43c5\u71a5\uaaba\ub8a3\u9310\ue22c\uf174\ufa4b\ua9bf\u7ba7\udaed\u62d8\ua4be\u0e43\u08be\u036a\uf4c1\u4bb5\uac98\uf43b\uee19\ue7db\uad49\u0a2e\u5e43\ucb33\u5867\uefb8\u46e8\uf257\uca58\u10eb\u5894\ue4ae\u6353\u4fec\ueb37\u21aa\u5ce1\u8770\u78e6\ue065\ua75d\ue502\u783e\ubc33\u19a8\ua537\ue311\u1f5f\uec41\uea18\uc933\ub2f5\uddb4\u05c3\uc49e\ub4d4\u0632\u21a5\u61c1\u31bc\u0580\ue599\udaa2\ufb01\u5725\u7b6f\u9193\u4138\ucd3a\uce64\u01c6\ud786\u9aae\ud541\u40dd\ud93b\u0ad2\u35b7\u7bb6\uf6eb\u0349\u23fa\u60dc\u7cee\uaf10\u1239\u7a31\uee30\u893f\u1f1e\u708a\u9539\uae7f\uf3cc\ue30c\ubb29\u79de\u5149\u863c\ud700\u8b9c\uf6f5\u0dda\u3efd\uf1e8\u29cf\u8d8f\u1657\u927f\u21b5\udac1\u2a4e\u9282\ub6b1\u00a7\u838b\u82e9\u2548\u2385\u02ed\ucf41\u9b08\u8e5d\u12c8\u8288\u68a3\u4383\u90f2\u3782\ud71b\ucfb6\u5c86\ua3fc\u081f\u24c5\u0a9b\uf3ad\ub96f\u1c17\u9b52\ub1cb\ua91d\u08bd\u1ac3\uc090\ueb56\u5593\ufd71\u2501\uf8c3\ua0c7\u4e3c\u8eeb\u5a75\u5c8b\u3a4e\u7701\u37c6\u550b\u25b5\u737e\u9a0b\ue6f5\u6af4\u1c33\u4b76\u6bd8\u2d7f\u7b0a\u7523\u661e\ue509\u69b5\u9c99\uf5a0\uaca0\u04b7\u4fe3\u70f0\u76f6\ube1e\uee10\ufd63\ubbe8\u4b7a\u73b4\u5fb7\u45ad\uf450\ua3c5\ua433\udf01\ucef7\u3bd0\u8663\uf46a\ue104\u253d\u8782\u0ff4\ud618\u691a\u46db\u88c9\u9e40\u9454\ua36e\ufcb9\u3a55\ucd48\u2705\u6c03\ua1f5\u6946\u97fa\uc83f\ud0d0\u9aea\u70dd\u3a56\u1e43\uf9b9\u2479\ud795\uc9ad\ud069\u5e8b\uc4a8\u91bc\u908d\u34d2\u17ad\u48cb\ude66\ua1c3\u2501\u420d\u2226\ua44e\u4a0f\ubc02\u8a4d\u5f1d\u3df6\u0ed2\u41e6\u085f\u54ec\u8f69\u78d8\u2790\udc31\ub7fc\u2fd7\ucc43\u47b2\u0d63\u6933\u127e\u9814\u1252\u4a40\u3986\uaca9\u434d\u49c7\u9e8c\u5880\uab1c\uae69\u7c4a\u810e\u60ad\u0f52\ue435\u0c98\u5d6a\uf48e\ucfad\u9ee0\uf7aa".charAt(1037)) - 838548951 ^ 0xCABDFC7F);
        for (int i = 0; i < n; ++i) {
            MaceAura.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(true));
        }
    }

    private void QVDe9A6(double n, double n2, double n3) {
        MaceAura.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(n, n2, n3, false));
    }

    private void QVDe9A6(double n, double n2, double n3, boolean b) {
        ClientPacketListener networkHandler = MaceAura.mc.player.connection;
        if (!((b | true) & true)) {
            // empty if block
        }
        networkHandler.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$PosRot(n, n2, n3, MaceAura.mc.player.getYRot(), MaceAura.mc.player.getXRot(), b));
    }

    private void X1xex5Dvp() {
        if (!(MaceAura.mc.player.isSwimming() || MaceAura.mc.player.onClimbable() || MaceAura.mc.player.isHandsBusy())) {
            if (!MaceAura.mc.player.onGround() && MaceAura.mc.player.getDeltaMovement().y < 0.0) {
                MaceAura maceAura;
                Vec3 ref32;
                if (this.isFlag12()) {
                    return;
                }
                this.setFlag1(true);
                if (this.getP7hd() != null) {
                    ref32 = this.getP7hd();
                    maceAura = this;
                } else {
                    ref32 = MaceAura.mc.player.position();
                    maceAura = this;
                }
                maceAura.setRef32(ref32);
                if (this.getRenderMode().is(IRC.Silent)) {
                    this.setRef5(this.PSFwGV().method_1031(0.0, (double)MaceAura.mc.player.method_18381(MaceAura.mc.player.getPose()), 0.0));
                    this.z239();
                }
                return;
            }
            this.uIyzYN(MaceAura.mc.player.position());
            this.setFlag1(false);
            return;
        }
        this.uIyzYN(MaceAura.mc.player.position());
        this.at7Am();
    }

    private void m_9cD4WRA() {
        boolean b = false;
        boolean is = this.getRenderMode().is(IRC.Silent);
        if (!is) {
            this.setFlag62(false);
            this.setFlag72(false);
            return;
        }
        if (ClientUtil.isMoving()) {
            if (!this.isFlag62()) {
                this.setFlag62(true);
                this.TAV5eDbaq();
            }
        } else {
            this.setFlag62(false);
        }
        boolean b2 = false;
        b2 = ElytraFly.INSTANCE != null && ElytraFly.INSTANCE.isOn() && ElytraFly.INSTANCE.isFallFlying();
        if (b2) {
            if (!this.isFlag72()) {
                this.setFlag72(true);
                this.TAV5eDbaq();
            }
        } else {
            this.setFlag72(false);
        }
    }

    private void TAV5eDbaq() {
        if (this.isFlag12() && this.PSFwGV() != null) {
            this.lK2mnDC(this.PSFwGV());
        }
    }

    public void render(PoseStack matrixStack) {
        if (this.L82Oqt5LX()) {
            Render3DUtil.drawBox(matrixStack, MaceAura.mc.player.method_5829(), this.getSilentPlayerBox().getValue());
        }
        this.m_04giit0(this.U3EM3X());
        this.I80816IY0(this.dWfE());
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent lookDirectionEvent) {
        if (!this.L82Oqt5LX()) {
            return;
        }
        this.setAmount1((float)((double)this.U3EM3X() + lookDirectionEvent.getCursorDeltaX() * (double)0.15f));
        this.BYnFXard(Mth.method_15363((float)((float)((double)this.dWfE() + lookDirectionEvent.getCursorDeltaY() * (double)0.15f)), (float)-90.0f, (float)90.0f));
        lookDirectionEvent.cancel();
    }

    public boolean L82Oqt5LX() {
        return this.isOn() && this.getRenderMode().is(IRC.Silent) && this.isFlag12() && this.m_2o6y() != null && this.W7PV06N5();
    }

    public boolean z3Io() {
        return this.isOn() && this.isFlag12() && this.getRef22() != null && CombatUtil.isValid((Entity)this.getRef22());
    }

    private boolean m_2Z0E9(Entity entity) {
        double n2;
        if (!CombatUtil.isValid(entity)) {
            return false;
        }
        Vec3 nsxQb3 = this.NsxQb3(entity);
        double n = MaceAura.mc.player.getX() - nsxQb3.x;
        return n * n + (n2 = MaceAura.mc.player.getZ() - nsxQb3.z) * n2 <= this.getAttackRange().getValue() * this.getAttackRange().getValue();
    }

    private boolean W7PV06N5() {
        Iterator iterator = MaceAura.mc.level.method_18456().iterator();
        while (iterator.hasNext()) {
            if (!this.m_2Z0E9((Entity)iterator.next())) continue;
            return true;
        }
        return false;
    }

    public Vec3 m_2o6y2() {
        return this.m_2o6y();
    }

    public float m_2hUAl6v() {
        return Mth.method_17821((float)mc.getTimer().method_60637(true), (float)this.guJ1(), (float)this.U3EM3X());
    }

    public float wL0p() {
        return Mth.method_17821((float)mc.getTimer().method_60637(true), (float)this.getAmount42(), (float)this.dWfE());
    }

    private void z239() {
        if (MaceAura.mc.player == null) {
            return;
        }
        this.setAmount1(MaceAura.mc.player.getYRot());
        this.BYnFXard(MaceAura.mc.player.getXRot());
        this.m_04giit0(this.U3EM3X());
        this.I80816IY0(this.dWfE());
    }

    private boolean v95m() {
        if (MaceAura.mc.player.getMainHandItem().method_7909() == Items.MACE) {
            return true;
        }
        return this.getSilent().getValue() && this.m_9F1wHjo() != -1;
    }

    private boolean QVDe9A6(Entity entity) {
        return CombatUtil.isValid(entity) && MaceAura.mc.player.getEyePosition().method_1022(this.NsxQb3(entity)) <= this.getAttackRange().getValue();
    }

    private Vec3 NsxQb3(Entity entity) {
        return MathUtil.getClosestPointToBox(MaceAura.mc.player.getEyePosition(), entity.method_5829());
    }

    private boolean attack() {
        int selectedSlot = MaceAura.mc.player.getInventory().selected;
        int m_9f1wHjo = this.m_9F1wHjo();
        boolean b = false;
        Vec3 nsxQb3 = this.NsxQb3((Entity)this.getRef22());
        if (MaceAura.mc.player.getMainHandItem().method_7909() != Items.MACE) {
            if (this.getSilent().getValue() && m_9f1wHjo != -1) {
                this.hjz5DL9KO(m_9f1wHjo);
                b = true;
            } else {
                return false;
            }
        }
        Aevum.ROTATION.lookAt(nsxQb3);
        MaceAura.mc.player.connection.method_52787((Packet)ServerboundInteractPacket.method_34206((Entity)this.getRef22(), (boolean)MaceAura.mc.player.isShiftKeyDown()));
        MaceAura.mc.player.method_6104(InteractionHand.MAIN_HAND);
        EntityUtil.swingHand(InteractionHand.MAIN_HAND, (SwingSide)((Object)this.getSwing().getValue()));
        Aevum.ROTATION.snapBack();
        if (b) {
            if (this.getInventorySwap().getValue()) {
                this.hjz5DL9KO(m_9f1wHjo);
                ClientUtil.Njn83();
            } else {
                InventoryUtil.switchToSlot(selectedSlot);
            }
        }
        return true;
    }

    private boolean sr6ablJ() {
        long g5hXzPW = this.g5hXzPW();
        return g5hXzPW != Long.MAX_VALUE && g5hXzPW <= (long)(42 + this.aaO455a0());
    }

    private long g5hXzPW() {
        if (MaceAura.mc.player.getDeltaMovement().y >= 0.0) {
            return Long.MAX_VALUE;
        }
        Vec3 add = MaceAura.mc.player.position().method_1031(0.0, 0.01, 0.0);
        BlockHitResult raycast = MaceAura.mc.level.method_17742(new ClipContext(add, add.method_1031(0.0, -256.0, 0.0), ClipContext.ClipContext$Block.COLLIDER, ClipContext.ClipContext$Fluid.WATER, (Entity)MaceAura.mc.player));
        if (raycast.method_17783() == HitResult.HitResult$Type.MISS) {
            return Long.MAX_VALUE;
        }
        double n = MaceAura.mc.player.getY() - raycast.getLocation().y;
        if (n <= 0.0) {
            return 0L;
        }
        double y = MaceAura.mc.player.getDeltaMovement().y;
        double n2 = 0.0;
        double n3 = this.getTimer().getValue() ? 50.0 / this.getTimerSpeed().getValue() : 50.0;
        double n4 = n3;
        for (int i = 1; i <= 200; ++i) {
            if (!((n2 -= (y = (y - 0.08) * 0.98)) >= n)) continue;
            return (long)Math.ceil((double)i * n4);
        }
        return Long.MAX_VALUE;
    }

    private int aaO455a0() {
        PlayerInfo playerListEntry = MaceAura.mc.player.connection.method_2871(MaceAura.mc.player.method_5667());
        int latency = playerListEntry == null ? 0 : playerListEntry.getLatency();
        return latency;
    }

    private void hjz5DL9KO(int n) {
        if (this.getInventorySwap().getValue()) {
            ClientUtil.FgmR(n, MaceAura.mc.player.getInventory().selected);
        } else {
            InventoryUtil.switchToSlot(n);
        }
    }

    private int m_9F1wHjo() {
        int n = this.getInventorySwap().getValue() ? InventoryUtil.findItemInventorySlot(Items.MACE) : InventoryUtil.findItem(Items.MACE);
        return n;
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

    private Player getRef22() {
        return this.ref2;
    }

    private void m_5ka21(Player ref2) {
        this.ref2 = ref2;
    }

    private Vec3 PSFwGV() {
        return this.ref3;
    }

    private void setRef32(Vec3 ref3) {
        this.ref3 = ref3;
    }

    private Vec3 getP7hd() {
        return this.P7hd;
    }

    private void uIyzYN(Vec3 p7hd) {
        this.P7hd = p7hd;
    }

    private int getCount12() {
        return this.count1;
    }

    private void setCount1(int count1) {
        this.count1 = count1;
    }

    private boolean isFlag12() {
        return this.flag1;
    }

    private void setFlag1(boolean flag1) {
        this.flag1 = flag1;
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

    private void setCount2(int count2) {
        this.count2 = count2;
    }

    private boolean isFlag22() {
        return this.flag2;
    }

    private void fwAS0(boolean flag2) {
        this.flag2 = flag2;
    }

    private boolean isFlag32() {
        return this.flag3;
    }

    private void zEi0(boolean flag3) {
        this.flag3 = flag3;
    }

    private boolean isFlag42() {
        return this.flag4;
    }

    private void setFlag42(boolean flag4) {
        this.flag4 = flag4;
    }

    private void Bu81F(boolean flag5) {
        this.flag5 = flag5;
    }

    private Player getRef42() {
        return this.ref4;
    }

    private void wpts2(Player ref4) {
        this.ref4 = ref4;
    }

    private Vec3 m_2o6y() {
        return this.ref5;
    }

    private void setRef5(Vec3 ref5) {
        this.ref5 = ref5;
    }

    private boolean isFlag62() {
        return this.flag6;
    }

    private void setFlag62(boolean flag6) {
        this.flag6 = flag6;
    }

    private boolean isFlag72() {
        return this.flag7;
    }

    private void setFlag72(boolean flag7) {
        this.flag7 = flag7;
    }

    private float U3EM3X() {
        return this.amount1;
    }

    private void setAmount1(float amount1) {
        this.amount1 = amount1;
    }

    private float dWfE() {
        return this.amount2;
    }

    private void BYnFXard(float amount2) {
        this.amount2 = amount2;
    }

    private float guJ1() {
        return this.amount3;
    }

    private void m_04giit0(float amount3) {
        this.amount3 = amount3;
    }

    private float getAmount42() {
        return this.amount4;
    }

    private void I80816IY0(float amount4) {
        this.amount4 = amount4;
    }

    private static Throwable xb(Throwable t) {
        return t;
    }

    public static enum IRC {
        Normal,
        Silent;

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

    }
}

