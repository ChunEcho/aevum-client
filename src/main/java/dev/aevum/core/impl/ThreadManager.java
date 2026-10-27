/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.Entity
 *  net.minecraft.BlockPos
 *  net.minecraft.AbstractClientPlayer
 */
package dev.aevum.core.impl;

import com.google.common.collect.Lists;
import dev.aevum.Aevum;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.ClientTickEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.JelloUtil;
import dev.aevum.api.utils.world.BlockUtil;
import dev.aevum.core.impl.CommandManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.combat.AutoAnchor;
import dev.aevum.mod.modules.impl.combat.AutoCrystal;
import dev.aevum.mod.modules.impl.render.HoleESP;
import dev.aevum.mod.modules.impl.render.PlaceRender;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.Entity;
import net.minecraft.BlockPos;
import net.minecraft.AbstractClientPlayer;

public class ThreadManager
implements Wrapper {
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public static ClientService clientService;
    public volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    public volatile List<AbstractClientPlayer> threadSafePlayersList = Collections.emptyList();
    public volatile boolean tickRunning = false;

    public ThreadManager() {
        this.init();
    }

    public void init() {
        Aevum.EVENT_BUS.subscribe(this);
        clientService = new ClientService();
        clientService.setName("aevumClientService");
        clientService.setDaemon(true);
        clientService.start();
    }

    public Iterable<Entity> getEntities() {
        return this.threadSafeEntityList;
    }

    public List<AbstractClientPlayer> getPlayers() {
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
            if (ThreadManager.mc.level == null || ThreadManager.mc.player == null) {
                return;
            }
            this.threadSafeEntityList = Lists.newArrayList((Iterable)ThreadManager.mc.level.method_18112());
            this.threadSafePlayersList = Lists.newArrayList((Iterable)ThreadManager.mc.level.method_18456());
        }
        if (!clientService.isAlive() || clientService.isInterrupted()) {
            clientService = new ClientService();
            clientService.setName("aevumService");
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
                        if (!ThreadManager.this.tickRunning) {
                            AutoCrystal.INSTANCE.onThread();
                            HoleESP.INSTANCE.onThread();
                            AutoAnchor.INSTANCE.onThread();
                        }
                        Thread.sleep(2L);
                    }
                }
                catch (InterruptedException interruptedException) {
                    return;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (!ClientSetting.INSTANCE.debug.getValue()) continue;
                    CommandManager.sendMessage("\u00a74An error has occurred [Thread] Message: [" + e.getMessage() + "]");
                    continue;
                }
                break;
            }
        }
    }
}

