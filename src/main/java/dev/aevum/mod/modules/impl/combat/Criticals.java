/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.EndCrystal
 *  net.minecraft.AABB
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 *  net.minecraft.ServerboundInteractPacket$Handler
 *  net.minecraft.ServerboundMovePlayerPacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.player.MovementUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.IPlayerInteractEntityC2SPacket;
import dev.aevum.asm.accessors.IPlayerMoveC2SPacket;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.exploit.Blink;
import dev.aevum.mod.modules.impl.exploit.BowBomb;
import dev.aevum.mod.modules.impl.exploit.Phase;
import dev.aevum.mod.modules.impl.player.AutoPearl;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.EndCrystal;
import net.minecraft.AABB;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.ServerboundMovePlayerPacket;

public class Criticals
extends Module {
    public static Criticals INSTANCE;
    public final EnumSetting<Mode> mode = this.add(new EnumSetting<Mode>("Mode", Mode.OldNCP));
    public final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true, () -> !this.mode.is(Mode.Ground)));
    private final BooleanSetting setOnGround = this.add(new BooleanSetting("SetNoGround", false, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting blockCheck = this.add(new BooleanSetting("BlockCheck", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting autoJump = this.add(new BooleanSetting("AutoJump", true, () -> this.mode.is(Mode.Ground)).setParent());
    private final BooleanSetting mini = this.add(new BooleanSetting("Mini", true, () -> this.mode.is(Mode.Ground) && this.autoJump.isOpen()));
    private final SliderSetting y = this.add(new SliderSetting("MotionY", 0.05, 0.0, 1.0, 1.0E-10, () -> this.mode.is(Mode.Ground) && this.autoJump.isOpen()));
    private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting crawlingDisable = this.add(new BooleanSetting("CrawlingDisable", true, () -> this.mode.is(Mode.Ground)));
    private final BooleanSetting flight = this.add(new BooleanSetting("Flight", false, () -> this.mode.is(Mode.Ground)));
    boolean requireJump = false;

    public Criticals() {
        super("Criticals", Module.Category.Combat);
        this.setChinese("\u5200\u5200\u66b4\u51fb");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        Entity entity;
        ServerboundInteractPacket packet;
        if (event.isCancelled()) {
            return;
        }
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.mode.is(Mode.Ground)) {
            if (BowBomb.send) {
                return;
            }
            if (AutoPearl.throwing || Phase.INSTANCE.isOn()) {
                return;
            }
            if (!this.setOnGround.getValue()) {
                return;
            }
            if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
                ((IPlayerMoveC2SPacket)event.getPacket()).setOnGround(false);
            }
            return;
        }
        Packet<?> packet2 = event.getPacket();
        if (!(!(packet2 instanceof ServerboundInteractPacket) || Criticals.getInteractType(packet = (ServerboundInteractPacket)packet2) != ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK || (entity = Criticals.getEntity(packet)) instanceof EndCrystal || this.onlyGround.getValue() && !Criticals.mc.player.onGround() && !Criticals.mc.player.getAbilities().flying || Criticals.mc.player.isInLava() || Criticals.mc.player.isInWater() || entity == null)) {
            this.doCrit(entity);
        }
    }

    @Override
    public void onLogout() {
        if (this.mode.is(Mode.Ground) && this.autoDisable.getValue()) {
            this.disable();
        }
    }

    @Override
    public void onEnable() {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        this.requireJump = true;
        if (this.mode.is(Mode.Ground)) {
            if (Criticals.nullCheck()) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
            } else if (MovementUtil.isMoving() && this.autoDisable.getValue()) {
                this.disable();
            } else if (this.crawlingDisable.getValue() && Criticals.mc.player.isVisuallyCrawling()) {
                this.disable();
            } else if (Criticals.mc.player.onGround() && this.autoJump.getValue() && (!this.blockCheck.getValue() || BlockUtil.canCollide((Entity)Criticals.mc.player, new AABB(EntityUtil.getPlayerPos(true).method_10086(2))))) {
                this.jump();
            }
        }
    }

    public void jump() {
        if (this.mini.getValue()) {
            MovementUtil.setMotionY(this.y.getValue());
        } else {
            Criticals.mc.player.jumpFromGround();
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) {
            return;
        }
        if (this.mode.is(Mode.Ground)) {
            if (this.crawlingDisable.getValue() && Criticals.mc.player.isVisuallyCrawling()) {
                this.disable();
            } else if (MovementUtil.isMoving() && this.autoDisable.getValue()) {
                this.disable();
            } else if (this.flight.getValue() && Criticals.mc.player.fallDistance > 0.0f) {
                MovementUtil.setMotionY(0.0);
                MovementUtil.setMotionX(0.0);
                MovementUtil.setMotionZ(0.0);
                this.requireJump = false;
            } else if (this.blockCheck.getValue() && !BlockUtil.canCollide((Entity)Criticals.mc.player, new AABB(EntityUtil.getPlayerPos(true).method_10086(2)))) {
                this.requireJump = true;
            } else if (Criticals.mc.player.onGround() && this.autoJump.getValue() && (this.flight.getValue() || this.requireJump)) {
                this.jump();
                this.requireJump = false;
            }
        }
    }

    public void doCrit(Entity entity) {
        switch (this.mode.getValue().ordinal()) {
            case 7: {
                if (MovementUtil.isMoving() || !MovementUtil.isStatic()) {
                    return;
                }
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), true));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0625, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.045, Criticals.mc.player.getZ(), false));
                break;
            }
            case 1: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.062600301692775, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.07260029960661, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 2: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0625, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 3: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.058293536E-5, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 9.16580235E-6, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.0371854E-7, Criticals.mc.player.getZ(), false));
                break;
            }
            case 0: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 2.71875E-7, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY(), Criticals.mc.player.getZ(), false));
                break;
            }
            case 4: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.0045, Criticals.mc.player.getZ(), true));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.52121E-4, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.3, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 0.025, Criticals.mc.player.getZ(), false));
                break;
            }
            case 5: {
                Criticals.mc.player.method_7277(entity);
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 5.0E-4, Criticals.mc.player.getZ(), false));
                mc.getConnection().method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(Criticals.mc.player.getX(), Criticals.mc.player.getY() + 1.0E-4, Criticals.mc.player.getZ(), false));
            }
        }
    }

    public static Entity getEntity(ServerboundInteractPacket packet) {
        return Criticals.mc.level == null ? null : Criticals.mc.level.method_8469(((IPlayerInteractEntityC2SPacket)packet).getEntityId());
    }

    public static ServerboundInteractPacket.ServerboundInteractPacket$ActionType getInteractType(ServerboundInteractPacket packet) {
        final ServerboundInteractPacket.ServerboundInteractPacket$ActionType[] result = new ServerboundInteractPacket.ServerboundInteractPacket$ActionType[1];
        packet.method_34209(new ServerboundInteractPacket.ServerboundInteractPacket$Handler(){

            public void method_34219(InteractionHand hand) {
                result[0] = ServerboundInteractPacket.ServerboundInteractPacket$ActionType.INTERACT;
            }

            public void method_34220(InteractionHand hand, Vec3 pos) {
                result[0] = ServerboundInteractPacket.ServerboundInteractPacket$ActionType.INTERACT_AT;
            }

            public void method_34218() {
                result[0] = ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK;
            }
        });
        return result[0];
    }

    public static enum Mode {
        UpdatedNCP,
        Strict,
        NCP,
        OldNCP,
        Hypixel2K22,
        Packet,
        Ground,
        BBTT;

    }
}

