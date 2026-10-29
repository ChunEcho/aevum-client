package dev.chun.mod.modules.impl.hud;

import dev.chun.api.utils.math.AnimateUtil;
import dev.chun.api.utils.math.Easing;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.core.impl.BlurManager;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class NotificationHud extends Module implements HudElement {
   public static NotificationHud INSTANCE;
   private static final float BASE_CARD_WIDTH = 170.0F;
   private static final float BASE_CARD_HEIGHT = 36.0F;
   private static final float BASE_ACCENT_WIDTH = 2.0F;
   private static final float BASE_PADDING_LEFT = 10.0F;
   private static final float BASE_PADDING_TOP = 12.0F;
   private static final float BASE_LINE_GAP = 2.0F;
   private static final float BASE_CARD_SPACING = 4.0F;
   private static final float BASE_TITLE_FONT_SIZE = 8.5F;
   private static final float BASE_MSG_FONT_SIZE = 9.0F;
   private static final long DISPLAY_TIME = 1000L;
   private static final long ANIM_IN_TIME = 300L;
   private static final long ANIM_TEXT_TIME = 200L;
   private final SliderSetting scale = this.add(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.05));
   private final ColorSetting enableColor = this.add(new ColorSetting("Enable Color", new Color(48, 226, 178, 255)));
   private final ColorSetting disableColor = this.add(new ColorSetting("Disable Color", new Color(220, 50, 50, 255)));
   private final ColorSetting infoColor = this.add(new ColorSetting("Info Color", new Color(99, 150, 145, 255)));
   private final ColorSetting bgColor = this.add(new ColorSetting("Bg Color", new Color(20, 20, 20, 180)));
   private final ColorSetting titleColor = this.add(new ColorSetting("Title Color", new Color(255, 255, 255, 255)));
   private final ColorSetting msgColor = this.add(new ColorSetting("Msg Color", new Color(181, 181, 181, 255)));
   private final BooleanSetting blur = this.add(new BooleanSetting("Blur", true));
   private final SliderSetting blurStrength = this.add(new SliderSetting("Blur Strength", 30.0, 0.0, 100.0, 1.0));
   private final SliderSetting textOffset = this.add(new SliderSetting("Text Offset", 0.0, -10.0, 10.0, 1.0));
   private int hudX = 10;
   private int hudY = 10;
   private final List<NotificationHud.Notification> notifications = new CopyOnWriteArrayList<>();

   public NotificationHud() {
      super("Notification", Module.Category.HUD);
      this.setChinese("\u901a\u77e5\u6d88\u606f");
      INSTANCE = this;
   }

   private static float getCardWidth() {
      return 170.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getCardHeight() {
      return 36.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getAccentWidth() {
      return 2.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getPaddingLeft() {
      return 10.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getLineGap() {
      return 2.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getCardSpacing() {
      return 4.0F * INSTANCE.scale.getValueFloat();
   }

   private static float getTitleFontSize() {
      return 8.5F * INSTANCE.scale.getValueFloat();
   }

   private static float getMsgFontSize() {
      return 9.0F * INSTANCE.scale.getValueFloat();
   }

   public static void send(String title, String message, NotificationHud.NotificationType type) {
      if (INSTANCE != null && INSTANCE.isOn()) {
         INSTANCE.notifications.add(new NotificationHud.Notification(title, message, type));
      }
   }

   @Override
   public void onRender2D(DrawContext context, float tickDelta) {
      this.renderElement(context, tickDelta);
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (!this.notifications.isEmpty()) {
         float currentY = (float)this.hudY;

         for (NotificationHud.Notification notif : this.notifications) {
            notif.updateY(currentY);
            notif.render(context);
            if (!notif.isDead) {
               currentY += (getCardHeight() + getCardSpacing()) * notif.getHeightProgress();
            }
         }

         this.notifications.removeIf(n -> n.isDead);
      }
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = x;
   }

   @Override
   public int getElementY() {
      return this.hudY;
   }

   @Override
   public void setElementY(int y) {
      this.hudY = y;
   }

   @Override
   public int getElementWidth() {
      return (int)getCardWidth();
   }

   @Override
   public int getElementHeight() {
      return (int)getCardHeight();
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   private static class Notification {
      private final String title;
      private final String message;
      private final NotificationHud.NotificationType type;
      private final long startTime;
      private float renderY = -1.0F;
      private float heightProgress = 1.0F;
      public boolean isDead = false;

      public Notification(String title, String message, NotificationHud.NotificationType type) {
         this.title = title;
         this.message = message;
         this.type = type;
         this.startTime = System.currentTimeMillis();
      }

      public float getHeightProgress() {
         return this.heightProgress;
      }

      public void updateY(float targetY) {
         if (this.renderY < 0.0F) {
            this.renderY = targetY;
         }

         this.renderY = AnimateUtil.fast(targetY, this.renderY, 16.0F);
      }

      public void render(DrawContext context) {
         if (NotificationHud.INSTANCE != null) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            long t_s1_end = 300L;
            long t_s2_end = 500L;
            long t_s3_end = 1500L;
            long t_s2r_end = t_s3_end + 200L;
            long t_s1r_end = t_s3_end + 200L + 300L;
            if (elapsed > t_s1r_end) {
               this.isDead = true;
            } else {
               int x = NotificationHud.INSTANCE.hudX;
               int y = Math.round(this.renderY);
               if (elapsed <= t_s1_end) {
                  float delta = (float)elapsed / 300.0F;
                  float progress;
                  this.heightProgress = progress = (float)Easing.CubicOut.ease((double)delta);
                  this.renderStage1(context, x, y, progress);
               } else if (elapsed <= t_s2_end) {
                  float delta = (float)(elapsed - t_s1_end) / 200.0F;
                  float progress = (float)Easing.CubicOut.ease((double)delta);
                  this.heightProgress = 1.0F;
                  this.renderStage2(context, x, y, progress);
               } else if (elapsed <= t_s3_end) {
                  this.heightProgress = 1.0F;
                  this.renderStage3(context, x, y);
               } else if (elapsed <= t_s2r_end) {
                  float delta = (float)(elapsed - t_s3_end) / 200.0F;
                  float progress = (float)Easing.CubicOut.ease((double)delta);
                  this.heightProgress = 1.0F;
                  this.renderStage2(context, x, y, 1.0F - progress);
               } else {
                  float delta = (float)(elapsed - t_s2r_end) / 300.0F;
                  float progress = (float)Easing.CubicOut.ease((double)delta);
                  this.heightProgress = 1.0F - progress;
                  this.renderStage1(context, x, y, 1.0F - progress);
               }
            }
         }
      }

      private void renderStage1(DrawContext context, int x, int y, float progress) {
         Color accent = this.getAccentColor();
         MatrixStack ms = context.getMatrices();
         float cardW = NotificationHud.getCardWidth();
         float cardH = NotificationHud.getCardHeight();
         float drawW = cardW * progress;
         Render2DUtil.drawRect(ms, (float)x, (float)y, drawW, cardH, accent);
      }

      private void renderStage2(DrawContext context, int x, int y, float progress) {
         NotificationHud inst = NotificationHud.INSTANCE;
         Color bg = inst.bgColor.getValue();
         Color accent = this.getAccentColor();
         MatrixStack ms = context.getMatrices();
         float cardW = NotificationHud.getCardWidth();
         float cardH = NotificationHud.getCardHeight();
         float accentW = NotificationHud.getAccentWidth();
         this.applyBlurIfNeeded(context, x, y, (int)cardW, (int)cardH);
         Render2DUtil.drawRect(ms, (float)x, (float)y, cardW, cardH, bg);
         int alpha = (int)(255.0F * progress);
         if (alpha > 4) {
            this.drawNotifText(context, x, y, alpha);
         }

         float accentStartX = (float)x + (cardW - accentW) * progress;
         float accentDrawW = cardW - (accentStartX - (float)x);
         Render2DUtil.drawRect(ms, accentStartX, (float)y, accentDrawW, cardH, accent);
      }

      private void renderStage3(DrawContext context, int x, int y) {
         NotificationHud inst = NotificationHud.INSTANCE;
         Color bg = inst.bgColor.getValue();
         Color accent = this.getAccentColor();
         MatrixStack ms = context.getMatrices();
         float cardW = NotificationHud.getCardWidth();
         float cardH = NotificationHud.getCardHeight();
         float accentW = NotificationHud.getAccentWidth();
         this.applyBlurIfNeeded(context, x, y, (int)cardW, (int)cardH);
         Render2DUtil.drawRect(ms, (float)x, (float)y, cardW, cardH, bg);
         Render2DUtil.drawRect(ms, (float)x + cardW - accentW, (float)y, accentW, cardH, accent);
         this.drawNotifText(context, x, y, 255);
      }

      private void drawNotifText(DrawContext context, int x, int y, int alpha) {
         NotificationHud inst = NotificationHud.INSTANCE;
         int textOffsetY = inst.textOffset.getValueInt();
         float titleSize = NotificationHud.getTitleFontSize();
         float msgSize = NotificationHud.getMsgFontSize();
         FontRenderer titleFont = FontManager.getNotificationFont(titleSize);
         FontRenderer msgFont = FontManager.getNotificationFont(msgSize);
         float titleH = titleFont.getFontHeight("A");
         float msgH = msgFont.getFontHeight("A");
         float lineGap = NotificationHud.getLineGap();
         float totalTextH = titleH + lineGap + msgH;
         float cardH = NotificationHud.getCardHeight();
         float paddingL = NotificationHud.getPaddingLeft();
         float textStartY = (float)y + (cardH - totalTextH) * 0.5F + (float)textOffsetY;
         float textX = (float)x + paddingL;
         Color titleC = inst.titleColor.getValue();
         Color msgC = inst.msgColor.getValue();
         float tA = (float)alpha / 255.0F * ((float)titleC.getAlpha() / 255.0F);
         float mA = (float)alpha / 255.0F * ((float)msgC.getAlpha() / 255.0F);
         MatrixStack ms = context.getMatrices();
         titleFont.drawString(
            ms,
            this.title,
            textX,
            (float)Math.round(textStartY),
            (float)titleC.getRed() / 255.0F,
            (float)titleC.getGreen() / 255.0F,
            (float)titleC.getBlue() / 255.0F,
            tA,
            false
         );
         msgFont.drawString(
            ms,
            this.message,
            textX,
            (float)Math.round(textStartY + titleH + lineGap),
            (float)msgC.getRed() / 255.0F,
            (float)msgC.getGreen() / 255.0F,
            (float)msgC.getBlue() / 255.0F,
            mA,
            false
         );
      }

      private void applyBlurIfNeeded(DrawContext context, int x, int y, int w, int h) {
         NotificationHud inst = NotificationHud.INSTANCE;
         if (inst.blur.getValue()) {
            float percentage = (float)inst.blurStrength.getValueInt() / 100.0F;
            float actualRadius = percentage * 20.0F;
            if (!(actualRadius <= 0.0F)) {
               context.draw();
               new BlurManager().applyBlur(actualRadius, (float)x, (float)y, (float)w, (float)h);
            }
         }
      }

      private Color getAccentColor() {
         if (NotificationHud.INSTANCE == null) {
            return new Color(100, 200, 100);
         } else {
            return switch (this.type) {
               case ENABLE -> NotificationHud.INSTANCE.enableColor.getValue();
               case DISABLE -> NotificationHud.INSTANCE.disableColor.getValue();
               case INFO -> NotificationHud.INSTANCE.infoColor.getValue();
            };
         }
      }
   }

   public static enum NotificationType {
      ENABLE,
      DISABLE,
      INFO;

      private NotificationType() {
      }
   }
}
