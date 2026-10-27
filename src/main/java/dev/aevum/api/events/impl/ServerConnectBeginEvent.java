/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerData
 */
package dev.aevum.api.events.impl;

import net.minecraft.ServerAddress;
import net.minecraft.ServerData;

public class ServerConnectBeginEvent {
    private static final ServerConnectBeginEvent INSTANCE = new ServerConnectBeginEvent();
    public ServerAddress address;
    public ServerData info;

    public static ServerConnectBeginEvent get(ServerAddress address, ServerData info) {
        ServerConnectBeginEvent.INSTANCE.address = address;
        ServerConnectBeginEvent.INSTANCE.info = info;
        return INSTANCE;
    }
}

