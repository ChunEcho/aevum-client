/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.mod.modules.impl.misc;

import dev.aevum.Aevum;
import dev.aevum.core.impl.FPSManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.util.Random;

public class FakeFPS
extends Module {
    public static FakeFPS INSTANCE;
    private FPSManager originalFPS;
    private FPSManager fakeFPS;
    private final Random random = new Random();
    private long lastUpdate;
    private int currentFakeFPS;
    private final SliderSetting minFPS = this.add(new SliderSetting("MinFPS", 60.0, 0.0, 10000.0, 1.0));
    private final SliderSetting maxFPS = this.add(new SliderSetting("MaxFPS", 500.0, 0.0, 10000.0, 1.0));

    public FakeFPS() {
        super("FakeFPS", Module.Category.Misc);
        this.setChinese("\u5047FPS");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (FakeFPS.nullCheck()) {
            return;
        }
        this.originalFPS = Aevum.FPS;
        this.lastUpdate = System.currentTimeMillis();
        this.currentFakeFPS = this.getRandomFPS();
        Aevum.FPS = this.fakeFPS = new FPSManager(){

            @Override
            public int getFps() {
                long now = System.currentTimeMillis();
                if (now - FakeFPS.this.lastUpdate >= 100L) {
                    FakeFPS.this.lastUpdate = now;
                    FakeFPS.this.currentFakeFPS = FakeFPS.this.getRandomFPS();
                }
                return FakeFPS.this.currentFakeFPS;
            }
        };
    }

    @Override
    public void onDisable() {
        if (this.originalFPS != null) {
            Aevum.FPS = this.originalFPS;
            this.originalFPS = null;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(this.currentFakeFPS);
    }

    private int getRandomFPS() {
        int max;
        int min = this.minFPS.getValueInt();
        if (min >= (max = this.maxFPS.getValueInt())) {
            return Math.max(min, 1);
        }
        return Math.max(this.random.nextInt(max - min + 1) + min, 1);
    }
}

