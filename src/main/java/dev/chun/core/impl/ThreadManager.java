/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  by.radioegor146.nativeobfuscator.Native
 *  com.google.common.collect.Lists
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 */
package dev.chun.core.impl;

import com.google.common.collect.Lists;
import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.UpdateEvent;
import dev.chun.api.utils.Wrapper;
import dev.chun.api.utils.render.JelloUtil;
import dev.chun.api.utils.world.BlockUtil;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.impl.client.ClientSetting;
import dev.chun.mod.modules.impl.combat.AutoAnchor;
import dev.chun.mod.modules.impl.combat.AutoCrystal;
import dev.chun.mod.modules.impl.render.HoleESP;
import dev.chun.mod.modules.impl.render.PlaceRender;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class ThreadManager
implements Wrapper {
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public static ClientService clientService;
    public volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    public volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();
    public volatile boolean tickRunning = false;

    public ThreadManager() {
        this.init();
    }

    public void init() {
        Aevum.EVENT_BUS.subscribe(this);
        clientService = new ClientService();
        clientService.setName("AevumClientService");
        clientService.setDaemon(true);
        clientService.start();
    }

    public Iterable<Entity> getEntities() {
        return this.threadSafeEntityList;
    }

    public List<AbstractClientPlayerEntity> getPlayers() {
        return this.threadSafePlayersList;
    }

    public void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    @EventListener(priority=200)
    public void onEvent(ClientTickEvent event) {
        Aevum.POP.onUpdate();
        Aevum.SERVER.onUpdate();
        if (event.isPre()) {
            JelloUtil.updateJello();
            this.tickRunning = true;
            BlockUtil.placedPos.forEach(pos -> PlaceRender.INSTANCE.create((BlockPos)pos));
            BlockUtil.placedPos.clear();
            Aevum.PLAYER.onUpdate();
            if (!Module.nullCheck()) {
                Aevum.EVENT_BUS.post(UpdateEvent.INSTANCE);
            }
        } else {
            this.tickRunning = false;
            if (ThreadManager.mc.world == null || ThreadManager.mc.player == null) {
                return;
            }
            this.threadSafeEntityList = Lists.newArrayList((Iterable)ThreadManager.mc.world.getEntities());
            this.threadSafePlayersList = Lists.newArrayList((Iterable)ThreadManager.mc.world.getPlayers());
        }
        if (!clientService.isAlive() || clientService.isInterrupted()) {
            clientService = new ClientService();
            clientService.setName("AevumService");
            clientService.setDaemon(true);
            clientService.start();
        }
    }

    public class ClientService
    extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    while (true) {
                        if (ThreadManager.this.tickRunning) {
                            Thread.onSpinWait();
                            continue;
                        }
                        AutoCrystal.INSTANCE.onThread();
                        HoleESP.INSTANCE.onThread();
                        AutoAnchor.INSTANCE.onThread();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (ClientSetting.INSTANCE.debug.getValue()) {
                        CommandManager.sendMessage("\u00a74An error has occurred [Thread] Message: [" + e.getMessage() + "]");
                    }
                }
            }
        }
    }
}
