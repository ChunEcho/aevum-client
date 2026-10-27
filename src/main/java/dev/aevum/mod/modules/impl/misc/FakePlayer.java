/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.InteractionHand
 *  net.minecraft.MobEffects
 *  net.minecraft.Entity
 *  net.minecraft.Entity$RemovalReason
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.ItemLike
 *  net.minecraft.Blocks
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ClientGamePacketListener
 *  net.minecraft.ClientboundEntityEventPacket
 *  net.minecraft.ClientboundExplodePacket
 *  net.minecraft.ServerboundInteractPacket
 *  net.minecraft.ServerboundInteractPacket$ActionType
 *  net.minecraft.SoundEvents
 *  net.minecraft.SoundSource
 *  net.minecraft.RemotePlayer
 */
package dev.aevum.mod.modules.impl.misc;

import com.mojang.authlib.GameProfile;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.math.DamageUtils;
import dev.aevum.api.utils.world.BlockPosX;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.asm.accessors.ILivingEntity;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.combat.AutoAnchor;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.combat.Criticals;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.StringSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.InteractionHand;
import net.minecraft.MobEffects;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.ItemLike;
import net.minecraft.Blocks;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ClientGamePacketListener;
import net.minecraft.ClientboundEntityEventPacket;
import net.minecraft.ClientboundExplodePacket;
import net.minecraft.ServerboundInteractPacket;
import net.minecraft.SoundEvents;
import net.minecraft.SoundSource;
import net.minecraft.RemotePlayer;

public class FakePlayer
extends Module {
    public static FakePlayer INSTANCE;
    public static FakePlayerEntity fakePlayer;
    final StringSetting name = this.add(new StringSetting("Name", "IDhammaI"));
    private final BooleanSetting damage = this.add(new BooleanSetting("Damage", true));
    private final BooleanSetting autoTotem = this.add(new BooleanSetting("AutoTotem", true));
    public final BooleanSetting record = this.add(new BooleanSetting("Record", false));
    public final BooleanSetting play = this.add(new BooleanSetting("Play", false));
    final List<PlayerState> positions = new ArrayList<PlayerState>();
    int movementTick;
    boolean lastRecordValue = false;

    public FakePlayer() {
        super("FakePlayer", Module.Category.Misc);
        this.setChinese("\u5047\u4eba");
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.name.getValue();
    }

    @Override
    public void onEnable() {
        if (FakePlayer.nullCheck()) {
            this.disable();
            return;
        }
        fakePlayer = new FakePlayerEntity((Player)FakePlayer.mc.player, this.name.getValue());
        FakePlayer.mc.level.method_53875((Entity)fakePlayer);
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (fakePlayer == null || FakePlayer.fakePlayer.clientLevel != FakePlayer.mc.level) {
            this.disable();
            return;
        }
        if (this.autoTotem.getValue()) {
            if (fakePlayer.getOffhandItem().method_7909() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.method_6122(InteractionHand.OFF_HAND, new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.getMainHandItem().method_7909() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.method_6122(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING));
            }
        }
        if (this.record.getValue() != this.lastRecordValue && this.record.getValue()) {
            this.positions.clear();
        }
        this.lastRecordValue = this.record.getValue();
        if (this.record.getValue()) {
            this.positions.add(new PlayerState(FakePlayer.mc.player.getX(), FakePlayer.mc.player.getY(), FakePlayer.mc.player.getZ(), FakePlayer.mc.player.getYRot(), FakePlayer.mc.player.getXRot()));
        }
        if (this.play.getValue() && !this.positions.isEmpty()) {
            ++this.movementTick;
            if (this.movementTick >= this.positions.size()) {
                this.movementTick = 0;
            }
            PlayerState p = this.positions.get(this.movementTick);
            fakePlayer.method_36456(p.yaw);
            fakePlayer.method_36457(p.pitch);
            fakePlayer.method_5847(p.yaw);
            fakePlayer.method_43391(p.x, p.y, p.z);
            fakePlayer.method_5759(p.x, p.y, p.z, p.yaw, p.pitch, 3);
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer == null) {
            return;
        }
        fakePlayer.kill();
        fakePlayer.method_31745(Entity.Entity$RemovalReason.KILLED);
        fakePlayer.onClientRemoval();
        fakePlayer = null;
    }

    @EventListener
    public void onAttack(PacketEvent.Send event) {
        ServerboundInteractPacket packet;
        Packet<?> packet2 = event.getPacket();
        if (packet2 instanceof ServerboundInteractPacket && Criticals.getInteractType(packet = (ServerboundInteractPacket)packet2) == ServerboundInteractPacket.ServerboundInteractPacket$ActionType.ATTACK && Criticals.getEntity(packet) == fakePlayer) {
            FakePlayer.mc.level.method_43128((Player)FakePlayer.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
            float damage = DamageUtils.getAttackDamage((LivingEntity)FakePlayer.mc.player, (LivingEntity)fakePlayer);
            if (!(!(FakePlayer.mc.player.fallDistance > 0.0f) && (!Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground) || !FakePlayer.mc.player.onGround() && Criticals.INSTANCE.onlyGround.getValue()) || FakePlayer.mc.player.onGround() && (!Criticals.INSTANCE.isOn() || Criticals.INSTANCE.mode.is(Criticals.Mode.Ground)) || FakePlayer.mc.player.onClimbable() || FakePlayer.mc.player.isInWater() || FakePlayer.mc.player.method_6059(MobEffects.BLINDNESS) || FakePlayer.mc.player.isPassenger())) {
                FakePlayer.mc.level.method_43128((Player)FakePlayer.mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                FakePlayer.mc.player.method_7277((Entity)fakePlayer);
            }
            if (FakePlayer.fakePlayer.hurtTime <= 0) {
                fakePlayer.method_48922(FakePlayer.mc.level.damageSources().generic());
                if (fakePlayer.getAbsorptionAmount() >= damage) {
                    fakePlayer.method_6073(fakePlayer.getAbsorptionAmount() - damage);
                } else {
                    float damage2 = damage - fakePlayer.getAbsorptionAmount();
                    fakePlayer.method_6073(0.0f);
                    fakePlayer.method_6033(fakePlayer.getHealth() - damage2);
                }
                if (fakePlayer.isDeadOrDying()) {
                    Aevum.POP.onTotemPop((Player)fakePlayer);
                    if (fakePlayer.method_6095(FakePlayer.mc.level.damageSources().generic())) {
                        fakePlayer.method_6033(10.0f);
                        new ClientboundEntityEventPacket((Entity)fakePlayer, 35).method_11471((ClientGamePacketListener)mc.getConnection());
                    }
                }
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet;
        if (this.damage.getValue() && fakePlayer != null && FakePlayer.fakePlayer.hurtTime <= 0 && (packet = event.getPacket()) instanceof ClientboundExplodePacket) {
            ClientboundExplodePacket explosion = (ClientboundExplodePacket)packet;
            Vec3 vec3d = new Vec3(explosion.getX(), explosion.getY(), explosion.getZ());
            if (Math.sqrt(vec3d.method_1025(fakePlayer.position())) > 10.0) {
                return;
            }
            float damage = BlockUtil.getBlock(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ())) == Blocks.RESPAWN_ANCHOR ? (float)AutoAnchor.INSTANCE.getAnchorDamage(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ()), (Player)fakePlayer, (Player)fakePlayer) : AutoCrystal.INSTANCE.calculateDamage(new Vec3(explosion.getX(), explosion.getY(), explosion.getZ()), (Player)fakePlayer, (Player)fakePlayer);
            fakePlayer.method_48922(FakePlayer.mc.level.damageSources().generic());
            if (fakePlayer.getAbsorptionAmount() >= damage) {
                fakePlayer.method_6073(fakePlayer.getAbsorptionAmount() - damage);
            } else {
                float damage2 = damage - fakePlayer.getAbsorptionAmount();
                fakePlayer.method_6073(0.0f);
                fakePlayer.method_6033(fakePlayer.getHealth() - damage2);
            }
            if (fakePlayer.isDeadOrDying()) {
                Aevum.POP.onTotemPop((Player)fakePlayer);
                if (fakePlayer.method_6095(FakePlayer.mc.level.damageSources().generic())) {
                    fakePlayer.method_6033(10.0f);
                    new ClientboundEntityEventPacket((Entity)fakePlayer, 35).method_11471((ClientGamePacketListener)mc.getConnection());
                }
            }
        }
    }

    public static class FakePlayerEntity
    extends RemotePlayer {
        private final boolean onGround;

        public FakePlayerEntity(Player player, String name) {
            super(Wrapper.mc.level, new GameProfile(UUID.fromString("66666666-6666-6666-6666-666666666666"), name));
            this.method_5719((Entity)player);
            this.xo = player.xo;
            this.zo = player.zo;
            this.yo = player.yo;
            this.yBodyRot = player.yBodyRot;
            this.yHeadRot = player.yHeadRot;
            this.attackAnim = player.attackAnim;
            this.swingTime = player.swingTime;
            this.walkAnimation.method_48567(player.walkAnimation.method_48566());
            this.walkAnimation.position = player.walkAnimation.method_48569();
            ((ILivingEntity)((Object)this)).setLeaningPitch(((ILivingEntity)player).getLeaningPitch());
            ((ILivingEntity)((Object)this)).setLastLeaningPitch(((ILivingEntity)player).getLeaningPitch());
            this.wasTouchingWater = player.isInWater();
            this.method_5660(player.isShiftKeyDown());
            this.method_18380(player.getPose());
            this.method_5729(7, player.isFallFlying());
            this.onGround = player.onGround();
            this.method_24830(this.onGround);
            this.getInventory().method_7377(player.getInventory());
            this.method_52544(player.getAbsorptionAmount());
            this.method_6033(player.getHealth());
            this.method_5857(player.method_5829());
        }

        public boolean onGround() {
            return this.onGround;
        }

        public boolean isSpectator() {
            return false;
        }

        public boolean method_7337() {
            return false;
        }
    }

    private record PlayerState(double x, double y, double z, float yaw, float pitch) {
    }
}

