package dev.chun.mod.modules;

import net.minecraft.client.gui.DrawContext;

public interface HudElement {
   String getElementName();

   int getElementX();

   void setElementX(int var1);

   int getElementY();

   void setElementY(int var1);

   int getElementWidth();

   int getElementHeight();

   default int getDragBoundsHeight() {
      return this.getElementHeight();
   }

   boolean isElementVisible();

   void renderElement(DrawContext var1, float var2);

   default boolean isDraggable() {
      return true;
   }

   default HudElement.AlignMode getAlignMode() {
      return HudElement.AlignMode.LEFT;
   }

   default void setAlignMode(HudElement.AlignMode mode) {
   }

   default int getRightAnchorX() {
      return this.getElementX() + this.getElementWidth();
   }

   default void setRightAnchorX(int rightX) {
   }

   public static enum AlignMode {
      LEFT,
      RIGHT;

      private AlignMode() {
      }
   }
}
