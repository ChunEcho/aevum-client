/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.InteractionHand
 *  net.minecraft.Entity
 *  net.minecraft.Player
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundPlayerPositionPacket
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$StatusOnly
 *  net.minecraft.Mth
 */
package dev.aevum.mod.modules.impl.combat;

import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.MoveEvent;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.utils.math.MathUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.InteractionHand;
import net.minecraft.Entity;
import net.minecraft.Player;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ClientboundPlayerPositionPacket;
import net.minecraft.ServerboundMovePlayerPacket;
import net.minecraft.Mth;

public class AutoMace
extends Module {
    public static AutoMace INSTANCE;
    public final SliderSetting targetRange = this.add(new SliderSetting("Target Range", 10.0, 10.0, 30.0, 1.0));
    public final SliderSetting attackRange = this.add(new SliderSetting("Attack Range", 6.0, 3.0, 6.0, 0.1));
    public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
    public final BooleanSetting track = this.add(new BooleanSetting("Track", true));
    public final BooleanSetting lagback = this.add(new BooleanSetting("Lagback", true));
    private Player ref1;
    private long time1;
    private boolean flag1;

    public AutoMace() {
        super("AutoMace", "Automatically attacks players with mace after falling", Module.Category.Combat);
        this.EljF(null);
        this.setLength(0L);
        this.setFlag12(false);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (AutoMace.nullCheck()) {
            return;
        }
        if (this.hLIV8S() && AutoMace.mc.player.isFallFlying()) {
            AutoMace.mc.player.stopFallFlying();
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
    public void onTick(ClientTickEvent clientTickEvent) {
        if (AutoMace.nullCheck()) {
            return;
        }
        this.EljF(this.NsxQb3());
        if (this.getRef1() != null && (double)AutoMace.mc.player.method_5739((Entity)this.getRef1()) <= this.attackRange.getValue()) {
            int g5hXzPW = this.g5hXzPW(Items.MACE);
            if (g5hXzPW == -1) {
                return;
            }
            if (this.rotate.getValue()) {
                Aevum.ROTATION.lookAt(MathUtil.getClosestPointToBox(AutoMace.mc.player.getEyePosition(), this.getRef1().method_5829()));
            }
            if (System.currentTimeMillis() - this.getTime12() > 1000L) {
                this.N75Vj(g5hXzPW);
                this.setLength(System.currentTimeMillis());
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive receive) {
        if (receive.getPacket() instanceof ClientboundPlayerPositionPacket && this.isFlag12()) {
            AutoMace.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(AutoMace.mc.player.onGround()));
            this.setFlag12(false);
        }
    }

    @EventListener
    public void onMove(MoveEvent moveEvent) {
        if (this.track.getValue() && !AutoMace.nullCheck() && !AutoMace.mc.player.onGround() && this.getRef1() != null) {
            if ((double)AutoMace.mc.player.method_5739((Entity)this.getRef1()) <= this.attackRange.getValue()) {
                return;
            }
            if (System.currentTimeMillis() - this.getTime12() < 200L) {
                return;
            }
            if (AutoMace.mc.player.method_5649(this.getRef1().getX(), AutoMace.mc.player.getY(), this.getRef1().getZ()) > 36.0) {
                if (!AutoMace.mc.player.isFallFlying()) {
                    this.Mpg1COX();
                }
            } else if (AutoMace.mc.player.isFallFlying()) {
                this.m_6oeO0Kq();
            }
            Vec3 pos = AutoMace.mc.player.position();
            Vec3 pos2 = this.getRef1().position();
            double n = pos2.x - pos.x;
            double n2 = pos2.z - pos.z;
            double sqrt = Math.sqrt(n * n + n2 * n2);
            if (sqrt > 0.0) {
                double n3 = n / sqrt;
                double n4 = n2 / sqrt;
                double n5 = AutoMace.mc.player.isFallFlying() ? 0.15 : 0.2873;
                double n6 = n5;
                moveEvent.setX(n3 * n6);
                moveEvent.setZ(n4 * n6);
            }
        }
    }

    private void N75Vj(int selectedSlot) {
        int selectedSlot2 = AutoMace.mc.player.getInventory().selected;
        Vec3 pos = AutoMace.mc.player.position();
        if (selectedSlot != selectedSlot2) {
            AutoMace.mc.player.getInventory().selected = selectedSlot;
        }
        AutoMace.mc.gameMode.method_2918((Player)AutoMace.mc.player, (Entity)this.getRef1());
        AutoMace.mc.player.method_6104(InteractionHand.MAIN_HAND);
        if (selectedSlot != selectedSlot2) {
            AutoMace.mc.player.getInventory().selected = selectedSlot2;
        }
        if (this.lagback.getValue()) {
            AutoMace.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(false));
            AutoMace.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(AutoMace.mc.player.getX(), AutoMace.mc.player.getY() + 1.0, AutoMace.mc.player.getZ(), false));
            AutoMace.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(pos.x, pos.y, pos.z, false));
            this.setFlag12(true);
        }
        if (this.rotate.getValue()) {
            Aevum.ROTATION.snapBack();
        }
    }

    private Player NsxQb3() {
        Player playerEntity = null;
        double n = Double.MAX_VALUE;
        for (Player playerEntity2 : AutoMace.mc.level.method_18456()) {
            double n2;
            if (playerEntity2 == AutoMace.mc.player || !playerEntity2.isAlive() || playerEntity2.getHealth() <= 0.0f || AutoMace.mc.player.method_5858((Entity)playerEntity2) > Mth.method_33723((double)this.targetRange.getValue()) || (n2 = (double)AutoMace.mc.player.method_5739((Entity)playerEntity2)) >= n) continue;
            n = n2;
            playerEntity = playerEntity2;
        }
        return playerEntity;
    }

    private int g5hXzPW(Item item) {
        for (int i = 0; i < 9; ++i) {
            if (AutoMace.mc.player.getInventory().method_5438(i).method_7909() != item) continue;
            return i;
        }
        return -1;
    }

    private boolean hLIV8S() {
        return AutoMace.mc.player.getInventory().method_7372(2).method_7909() == Items.ELYTRA;
    }

    private void Mpg1COX() {
        for (int i = ("\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99".charAt(168) << 16 | "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99".charAt(169)) - 1449807115 ^ 0x5C6DC3E9; i < 4; ++i) {
            if (AutoMace.mc.player.getInventory().method_7372(i).method_7909() != Items.ELYTRA) continue;
            AutoMace.mc.player.getInventory().armor.set(2, (Object)AutoMace.mc.player.getInventory().method_7372(i));
            AutoMace.mc.player.getInventory().armor.set(i, (Object)ItemStack.EMPTY);
            return;
        }
    }

    private void m_6oeO0Kq() {
        int n = 0;
        while (true) {
            int n2 = ++n;
            String s = "\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99";
            if (!((n | true) & true)) {
                // empty if block
            }
            if (n2 >= (("\ufbf4\u3018\u17b3\ua3bc\u8555\u43e1\u4739\ua96b\u651a\uf47d\u000e\uab56\u81e0\uacac\u4259\u38b2\uc0c1\u63fc\u9512\u544d\u7e59\u41db\u6890\ue415\uab10\u1f1d\ud0f7\u358c\ud72d\u2df8\ua654\u5fde\uc09b\u74eb\u5a59\u739f\u0884\uf517\u35d6\u54c6\uc430\u6801\ub5c6\ua4b0\ub01a\u00b5\ubf0d\u31b9\u1bfc\u7fbf\uc87a\u75cb\ueee6\uae68\ua530\u91d1\udc6d\u41a4\u04a2\udf7c\u6d2a\ufee0\u6173\u2d0d\ubec4\uf40c\uc47d\u4516\ua374\u97ea\u4c0e\u2c8a\u8c88\ue53c\u95ca\u5104\ud1c2\u332e\udeac\u8fd6\ue7b7\u2c1a\ue327\ubea2\uf144\ue180\ua58e\u8558\uc5d5\uc51a\u8c1f\u63cf\u554c\u93e1\uc29e\u6e30\u1c45\u382a\uaa97\u6f3b\uad81\ub408\uda38\uce46\ub25d\u33ee\ud84c\uad5a\uf13c\u8a58\udf39\u142b\u53eb\ua9c6\u5cbc\udd5c\u810a\u82c3\u6582\u08e7\ue815\ue382\uda59\udf81\u4f8a\u9e3b\u2cf4\u512d\ub9e1\ueb17\u02d8P\ub3c7\u137e\uaf0f\ueef4\u3d33\u879c\u07ad\u54fc\u1162\u1e24\u708e\uad67\u6422\ud16a\ud7ed\uc5ac\u970e\u59c2\u650d\u2b22\u97a2\u91e8\uff12\u0695\u05d0\udcd4\u96de\u0bee\u63f0\uf723\u83ff\u1a18\ue161\ud243\u184a\ua02f\ub2d8\u10f4\u62ab\uafcf\u9090\u0d83\u4e3f\uc13c\uda7a\ucb82\uf4ac\u0a6c\uf71b\u7396\u05d8\u7b99".charAt(170) << 16 | 0xAFCF) - 1488784709 ^ 0x9EEA28E)) break;
            if (n == 2 || !AutoMace.mc.player.getInventory().method_7372(n).method_7960()) continue;
            AutoMace.mc.player.getInventory().armor.set(n, (Object)AutoMace.mc.player.getInventory().method_7372(2));
            AutoMace.mc.player.getInventory().armor.set(2, (Object)ItemStack.EMPTY);
            break;
        }
    }

    private Player getRef1() {
        return this.ref1;
    }

    private void EljF(Player ref1) {
        this.ref1 = ref1;
    }

    private long getTime12() {
        return this.time1;
    }

    private void setLength(long time1) {
        this.time1 = time1;
    }

    private boolean isFlag12() {
        return this.flag1;
    }

    private void setFlag12(boolean flag1) {
        this.flag1 = flag1;
    }
}

