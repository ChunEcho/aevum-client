/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Packet
 *  net.minecraft.ClientboundSetTimePacket
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.PacketEvent;
import dev.aevum.api.events.impl.PreRender2DEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.core.impl.ShaderManager;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.Packet;
import net.minecraft.ClientboundSetTimePacket;

public class Ambience
extends Module {
    public static Ambience INSTANCE;
    public final ColorSetting filter = this.add(new ColorSetting("Filter", new Color(255, 255, 255, 20)).injectBoolean(false));
    public final ColorSetting worldColor = this.add(new ColorSetting("WorldColor", new Color(-1, true)).injectBoolean(true));
    public final BooleanSetting customTime = this.add(new BooleanSetting("CustomTime", false).setParent());
    public final SliderSetting time = this.add(new SliderSetting("Time", 0, 0, 24000, this.customTime::isOpen));
    public final ColorSetting fog = this.add(new ColorSetting("FogColor", new Color(13401557)).injectBoolean(false));
    public final ColorSetting sky = this.add(new ColorSetting("SkyColor", new Color(0)).injectBoolean(false));
    public final ColorSetting cloud = this.add(new ColorSetting("CloudColor", new Color(0)).injectBoolean(false));
    public final ColorSetting dimensionColor = this.add(new ColorSetting("DimensionColor", new Color(0)).injectBoolean(false));
    public final EnumSetting<SkyMode> skyMode = this.add(new EnumSetting<SkyMode>("SkyMode", SkyMode.Off));
    public final SliderSetting skySpeed = this.add(new SliderSetting("SkySpeed", 4.0, 0.0, 20.0, 0.1, () -> this.skyMode.getValue() != SkyMode.Off));
    public final ColorSetting skyShaderColor = this.add(new ColorSetting("SkyShaderColor", new Color(255, 255, 255, 200), () -> this.skyMode.getValue() != SkyMode.Off));
    public final ColorSetting skyBackgroundColor = this.add(new ColorSetting("SkyBackground", new Color(16, 24, 48, 255), () -> this.skyMode.getValue() == SkyMode.Local));
    public final BooleanSetting fogDistance = this.add(new BooleanSetting("FogDistance", false).setParent());
    public final SliderSetting fogStart = this.add(new SliderSetting("FogStart", 50, 0, 1000, this.fogDistance::isOpen));
    public final SliderSetting fogEnd = this.add(new SliderSetting("FogEnd", 100, 0, 1000, this.fogDistance::isOpen));
    public final BooleanSetting fullBright = this.add(new BooleanSetting("FullBright", false));
    public final BooleanSetting customLuminance = this.add(new BooleanSetting("CustomLuminance", false).setParent().injectTask(() -> {
        if (!Ambience.nullCheck()) {
            Ambience.mc.levelRenderer.allChanged();
        }
    }));
    public final SliderSetting luminance = this.add(new SliderSetting("Luminance", 15, 0, 15, this.customLuminance::isOpen).injectTask(() -> {
        if (!Ambience.nullCheck() && this.customLuminance.getValue()) {
            Ambience.mc.levelRenderer.allChanged();
        }
    }));
    public final BooleanSetting customWeather = this.add(new BooleanSetting("CustomWeather", false).setParent());
    public final EnumSetting<WeatherType> weatherType = this.add(new EnumSetting<WeatherType>("WeatherType", WeatherType.CLEAR, this.customWeather::isOpen));
    public final SliderSetting weatherIntensity = this.add(new SliderSetting("Intensity", 1.0, 0.0, 1.0, () -> this.customWeather.isOpen() && this.weatherType.getValue() != WeatherType.CLEAR));
    public final BooleanSetting weatherPenetrate = this.add(new BooleanSetting("Penetrate", false, () -> this.customWeather.isOpen() && this.weatherType.getValue() != WeatherType.CLEAR));
    long oldTime;
    private float oldRainGradient;
    private float oldThunderGradient;

    public Ambience() {
        super("Ambience", "Custom ambience", Module.Category.Render);
        this.setChinese("\u81ea\u5b9a\u4e49\u73af\u5883");
        INSTANCE = this;
    }

    @EventListener
    public void onRender2D(PreRender2DEvent event) {
        if (this.filter.booleanValue) {
            event.drawContext.method_25294(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), this.filter.getValue().getRGB());
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (this.customTime.getValue()) {
            Ambience.mc.level.method_8435((long)this.time.getValue());
        }
        if (this.customWeather.getValue() && !Ambience.nullCheck()) {
            this.applyWeather();
        }
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> class_25962 = event.getPacket();
        if (class_25962 instanceof ClientboundSetTimePacket) {
            ClientboundSetTimePacket packet = (ClientboundSetTimePacket)class_25962;
            if (this.customTime.getValue()) {
                this.oldTime = packet.getGameTime();
                event.cancel();
            }
        }
    }

    @Override
    public void onEnable() {
        if (!Ambience.nullCheck()) {
            this.oldTime = Ambience.mc.level.getDayTime();
            if (this.customTime.getValue()) {
                Ambience.mc.level.method_8435((long)this.time.getValue());
            }
            this.oldRainGradient = Ambience.mc.level.method_8430(1.0f);
            this.oldThunderGradient = Ambience.mc.level.method_8478(1.0f);
            if (this.customWeather.getValue()) {
                this.applyWeather();
            }
        }
    }

    @Override
    public void onDisable() {
        if (!Ambience.nullCheck()) {
            Ambience.mc.level.method_8435(this.oldTime);
            this.restoreWeather();
        }
    }

    private void applyWeather() {
        float intensity = (float)this.weatherIntensity.getValue();
        switch (this.weatherType.getValue().ordinal()) {
            case 0: {
                Ambience.mc.level.setRainLevel(0.0f);
                Ambience.mc.level.method_8496(0.0f);
                break;
            }
            case 1: {
                Ambience.mc.level.setRainLevel(intensity);
                Ambience.mc.level.method_8496(0.0f);
                break;
            }
            case 2: {
                Ambience.mc.level.setRainLevel(intensity);
                Ambience.mc.level.method_8496(0.0f);
                break;
            }
            case 3: {
                Ambience.mc.level.setRainLevel(intensity);
                Ambience.mc.level.method_8496(intensity);
            }
        }
    }

    private void restoreWeather() {
        Ambience.mc.level.setRainLevel(this.oldRainGradient);
        Ambience.mc.level.method_8496(this.oldThunderGradient);
    }

    public boolean shouldPenetrate() {
        return this.isOn() && this.customWeather.getValue() && this.weatherPenetrate.getValue() && this.weatherType.getValue() != WeatherType.CLEAR;
    }

    public WeatherType getWeatherType() {
        return this.weatherType.getValue();
    }

    public float getWeatherIntensity() {
        return (float)this.weatherIntensity.getValue();
    }

    public boolean shouldRenderSkyShader() {
        return this.isOn() && this.skyMode.getValue() != SkyMode.Off;
    }

    public ShaderManager.Shader getSkyShader() {
        return switch (this.skyMode.getValue().ordinal()) {
            case 1 -> ShaderManager.Shader.Cine;
            case 2 -> ShaderManager.Shader.Aurora;
            case 3 -> ShaderManager.Shader.Local;
            default -> ShaderManager.Shader.Cine;
        };
    }

    public static enum SkyMode {
        Off,
        Cine,
        Aurora,
        Local;

    }

    public static enum WeatherType {
        CLEAR("\u6674\u5929"),
        RAIN("\u96e8\u5929"),
        SNOW("\u96ea\u5929"),
        STORM("\u96f7\u66b4");

        private final String chinese;

        private WeatherType(String chinese) {
            this.chinese = chinese;
        }

        public String toString() {
            return this.chinese;
        }
    }
}

