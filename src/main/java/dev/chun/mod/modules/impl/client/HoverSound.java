package dev.chun.mod.modules.impl.client;

import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class HoverSound extends Module {
   public static HoverSound INSTANCE;
   public final EnumSetting<HoverSound.Tone> sound = this.add(new EnumSetting<>("Sound", HoverSound.Tone.Mio));
   private final SliderSetting pitch = this.add(new SliderSetting("Pitch", 1.8, 0.5, 2.0, 0.1));
   private final SliderSetting volume = this.add(new SliderSetting("Volume", 0.25, 0.0, 1.0, 0.05));
   public final BooleanSetting expandSound = this.add(new BooleanSetting("ExpandSound", true));
   public final EnumSetting<HoverSound.Tone> expandTone = this.add(new EnumSetting<>("ExpandTone", HoverSound.Tone.Debug));
   private final SliderSetting expandPitch = this.add(new SliderSetting("ExpandPitch", 1.2, 0.5, 2.0, 0.1));
   private final SliderSetting expandVolume = this.add(new SliderSetting("ExpandVolume", 0.3, 0.0, 1.0, 0.05));
   public final BooleanSetting toggleSound = this.add(new BooleanSetting("ToggleSound", true));
   public final EnumSetting<HoverSound.Tone> toggleTone = this.add(new EnumSetting<>("ToggleTone", HoverSound.Tone.Lowercase));
   private final SliderSetting togglePitch = this.add(new SliderSetting("TogglePitch", 1.2, 0.5, 2.0, 0.1));
   private final SliderSetting toggleVolume = this.add(new SliderSetting("ToggleVolume", 0.3, 0.0, 1.0, 0.05));
   public static final SoundEvent MIO = registerSound("mio");
   public static final SoundEvent MIO2 = registerSound("mio2");
   public static final SoundEvent MIO3 = registerSound("mio3");

   public HoverSound() {
      super("HoverSound", Module.Category.Client);
      this.setChinese("\u754c\u9762\u97f3\u6548");
      INSTANCE = this;
   }

   private static SoundEvent registerSound(String name) {
      Identifier id = Identifier.of("aevum", name);
      return (SoundEvent)Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
   }

   public static void hover() {
      HoverSound m = INSTANCE;
      if (m != null && m.isOn()) {
         m.play(m.sound.getValue(), m.pitch.getValueFloat(), m.volume.getValueFloat());
      }
   }

   public static boolean expand() {
      HoverSound m = INSTANCE;
      if (m != null && m.isOn() && m.expandSound.getValue()) {
         m.play(m.expandTone.getValue(), m.expandPitch.getValueFloat(), m.expandVolume.getValueFloat());
         return true;
      } else {
         return false;
      }
   }

   public static boolean playToggle() {
      HoverSound m = INSTANCE;
      if (m != null && m.isOn() && m.toggleSound.getValue()) {
         m.play(m.toggleTone.getValue(), m.togglePitch.getValueFloat(), m.toggleVolume.getValueFloat());
         return true;
      } else {
         return false;
      }
   }

   private void play(HoverSound.Tone tone, float pitch, float volume) {
      if (tone != null && !(volume <= 0.0F) && mc.getSoundManager() != null) {
         mc.getSoundManager().play(PositionedSoundInstance.master(tone.event(), pitch, volume));
      }
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
         switch (this) {
            case Debug:
               return HoverSound.MIO2;
            case Lowercase:
               return HoverSound.MIO3;
            default:
               return HoverSound.MIO;
         }
      }
   }
}
