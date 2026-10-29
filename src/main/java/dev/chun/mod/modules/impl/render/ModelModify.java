package dev.chun.mod.modules.impl.render;

import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.EnumSetting;

public class ModelModify extends Module {
   public static ModelModify INSTANCE;
   public final EnumSetting<ModelModify.Mode> mode = this.add(new EnumSetting<>("Mode", ModelModify.Mode.Sneak));

   public ModelModify() {
      super("ModelModify", "Modify player model pose", Module.Category.Render);
      this.setChinese("\u6a21\u578b\u4fee\u6539");
      INSTANCE = this;
   }

   public boolean isSneak() {
      return this.isOn() && this.mode.is(ModelModify.Mode.Sneak);
   }

   public boolean isSit() {
      return this.isOn() && this.mode.is(ModelModify.Mode.Sit);
   }

   public boolean isSwim() {
      return this.isOn() && this.mode.is(ModelModify.Mode.Swim);
   }

   public boolean isProne() {
      return this.isOn() && this.mode.is(ModelModify.Mode.Prone);
   }

   public static enum Mode {
      Sneak,
      Sit,
      Swim,
      Prone;

      private Mode() {
      }
   }
}
