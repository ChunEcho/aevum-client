/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.Vec3
 *  net.minecraft.Packet
 *  net.minecraft.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.ServerboundMovePlayerPacket$StatusOnly
 */
package dev.aevum.api.utils.path;

import com.google.common.collect.Lists;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.path.PathUtils;
import dev.aevum.api.utils.path.Vec3;
import java.util.List;
import net.minecraft.Vec3;
import net.minecraft.Packet;
import net.minecraft.ServerboundMovePlayerPacket;

public class TPUtils
implements Wrapper {
    public static void teleportWithBack(Vec3 newPos, TeleportType type, Runnable runnable) {
        switch (type.ordinal()) {
            case 0: {
                TPUtils.legacyTeleportWithBack(newPos, runnable);
                break;
            }
            case 1: {
                TPUtils.newTeleportWithBack(newPos, runnable);
            }
        }
    }

    public static void legacyTeleportWithBack(Vec3 newPos, Runnable runnable) {
        List<Vec3> tpPath = PathUtils.computePath(newPos);
        tpPath.removeFirst();
        tpPath.forEach(vec3 -> TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(vec3.x(), vec3.y(), vec3.z(), false)));
        runnable.run();
        tpPath = Lists.reverse(tpPath);
        tpPath.removeFirst();
        tpPath.forEach(vec3 -> TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(vec3.x(), vec3.y(), vec3.z(), false)));
        TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(TPUtils.mc.player.getX(), TPUtils.mc.player.getY(), TPUtils.mc.player.getZ(), false));
    }

    public static void newTeleportWithBack(Vec3 newPos, Runnable runnable) {
        int i;
        int packetsRequired = (int)Math.ceil(TPUtils.mc.player.position().method_1022(newPos) / 10.0) - 1;
        for (i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(true));
        }
        TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(newPos.x, newPos.y, newPos.z, true));
        runnable.run();
        for (i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(true));
        }
        TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(TPUtils.mc.player.getX(), TPUtils.mc.player.getY(), TPUtils.mc.player.getZ(), false));
    }

    public static void newTeleport(Vec3 newPos) {
        int packetsRequired = (int)Math.ceil(TPUtils.mc.player.position().method_1022(newPos) / 10.0) - 1;
        for (int i = 0; i < packetsRequired; ++i) {
            TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$StatusOnly(true));
        }
        TPUtils.mc.player.connection.method_52787((Packet)new ServerboundMovePlayerPacket.ServerboundMovePlayerPacket$Pos(newPos.x, newPos.y, newPos.z, true));
    }

    public static enum TeleportType {
        Legacy,
        New;

    }
}

