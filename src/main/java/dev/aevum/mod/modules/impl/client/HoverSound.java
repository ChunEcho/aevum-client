/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SimpleSoundInstance
 *  net.minecraft.SoundInstance
 *  net.minecraft.Registry
 *  net.minecraft.ResourceLocation
 *  net.minecraft.SoundEvent
 *  net.minecraft.BuiltInRegistries
 */
package dev.aevum.mod.modules.impl.client;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.EnumSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.SimpleSoundInstance;
import net.minecraft.SoundInstance;
import net.minecraft.Registry;
import net.minecraft.ResourceLocation;
import net.minecraft.SoundEvent;
import net.minecraft.BuiltInRegistries;

public class HoverSound
extends Module {
    public static HoverSound INSTANCE;
    public final EnumSetting<Tone> sound = this.add(new EnumSetting<Tone>("Sound", Tone.Mio));
    private final SliderSetting pitch = this.add(new SliderSetting("Pitch", 1.8, 0.5, 2.0, 0.1));
    private final SliderSetting volume = this.add(new SliderSetting("Volume", 0.25, 0.0, 1.0, 0.05));
    public final BooleanSetting expandSound = this.add(new BooleanSetting("ExpandSound", true));
    public final EnumSetting<Tone> expandTone = this.add(new EnumSetting<Tone>("ExpandTone", Tone.Debug));
    private final SliderSetting expandPitch = this.add(new SliderSetting("ExpandPitch", 1.2, 0.5, 2.0, 0.1));
    private final SliderSetting expandVolume = this.add(new SliderSetting("ExpandVolume", 0.3, 0.0, 1.0, 0.05));
    public final BooleanSetting toggleSound = this.add(new BooleanSetting("ToggleSound", true));
    public final EnumSetting<Tone> toggleTone = this.add(new EnumSetting<Tone>("ToggleTone", Tone.Lowercase));
    private final SliderSetting togglePitch = this.add(new SliderSetting("TogglePitch", 1.2, 0.5, 2.0, 0.1));
    private final SliderSetting toggleVolume = this.add(new SliderSetting("ToggleVolume", 0.3, 0.0, 1.0, 0.05));
    public static final SoundEvent MIO;
    public static final SoundEvent MIO2;
    public static final SoundEvent MIO3;

    public HoverSound() {
        super("HoverSound", Module.Category.Client);
        this.setChinese("\u754c\u9762\u97f3\u6548");
        INSTANCE = this;
    }

    private static SoundEvent registerSound(String name) {
        ResourceLocation id = ResourceLocation.method_60655((String)"aevum", (String)name);
        return (SoundEvent)Registry.method_10230((Registry)BuiltInRegistries.SOUND_EVENT, (ResourceLocation)id, (Object)SoundEvent.method_47908((ResourceLocation)id));
    }

    public static void hover() {
        HoverSound m = INSTANCE;
        if (m == null || !m.isOn()) {
            return;
        }
        m.play(m.sound.getValue(), m.pitch.getValueFloat(), m.volume.getValueFloat());
    }

    public static boolean expand() {
        HoverSound m = INSTANCE;
        if (m == null || !m.isOn() || !m.expandSound.getValue()) {
            return false;
        }
        m.play(m.expandTone.getValue(), m.expandPitch.getValueFloat(), m.expandVolume.getValueFloat());
        return true;
    }

    public static boolean playToggle() {
        HoverSound m = INSTANCE;
        if (m == null || !m.isOn() || !m.toggleSound.getValue()) {
            return false;
        }
        m.play(m.toggleTone.getValue(), m.togglePitch.getValueFloat(), m.toggleVolume.getValueFloat());
        return true;
    }

    private void play(Tone tone, float pitch, float volume) {
        if (tone == null || volume <= 0.0f || mc.getSoundManager() == null) {
            return;
        }
        mc.getSoundManager().method_4873((SoundInstance)SimpleSoundInstance.method_4757((SoundEvent)tone.event(), (float)pitch, (float)volume));
    }

    static {
        MIO = HoverSound.registerSound("mio");
        MIO2 = HoverSound.registerSound("mio2");
        MIO3 = HoverSound.registerSound("mio3");
    }

    public static enum Tone {
        Mio("mio"),
        Debug("mio2"),
        Lowercase("mio3");

        private final String path;

        private Tone(String path) {
            this.path = path;
        }

        public SoundEvent event() {
            switch (this.ordinal()) {
                case 1: {
                    return MIO2;
                }
                case 2: {
                    return MIO3;
                }
            }
            return MIO;
        }
    }
}

