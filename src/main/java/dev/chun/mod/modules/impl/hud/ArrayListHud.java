package dev.chun.mod.modules.impl.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.chun.Aevum;
import dev.chun.api.events.eventbus.EventListener;
import dev.chun.api.events.impl.ClientTickEvent;
import dev.chun.api.events.impl.InitEvent;
import dev.chun.api.utils.math.Animation;
import dev.chun.api.utils.math.Easing;
import dev.chun.api.utils.render.ColorUtil;
import dev.chun.api.utils.render.Render2DUtil;
import dev.chun.api.utils.render.TextUtil;
import dev.chun.api.utils.skija.SkijaGlowRenderer;
import dev.chun.core.impl.FontManager;
import dev.chun.mod.gui.fonts.FontRenderer;
import dev.chun.mod.modules.HudElement;
import dev.chun.mod.modules.Module;
import dev.chun.mod.modules.settings.impl.BooleanSetting;
import dev.chun.mod.modules.settings.impl.ColorSetting;
import dev.chun.mod.modules.settings.impl.EnumSetting;
import dev.chun.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ArrayListHud extends Module implements HudElement {
   public static ArrayListHud INSTANCE;
   private int hudX = 2;
   private int hudY = 1;
   private HudElement.AlignMode alignMode = HudElement.AlignMode.LEFT;
   private final BooleanSetting sortByLength = this.add(new BooleanSetting("Sort", true));
   private final BooleanSetting renderUp = this.add(new BooleanSetting("RenderUp", false));
   private final BooleanSetting shadow = this.add(new BooleanSetting("Shadow", true));
   private final BooleanSetting lowerCase = this.add(new BooleanSetting("LowerCase", false));
   private final BooleanSetting showSpace = this.add(new BooleanSetting("Space", true));
   private final BooleanSetting forgeHax = this.add(new BooleanSetting("ForgeHax", false));
   private final BooleanSetting listGlow = this.add(new BooleanSetting("Glow", false).setParent());
   private final SliderSetting listGlowRadius = this.add(new SliderSetting("GlowRadius", 3.0, 0.5, 8.0, 0.1, () -> this.listGlow.isOpen()));
   private final SliderSetting listGlowOpacity = this.add(new SliderSetting("GlowOpacity", 0.6, 0.1, 1.0, 0.05, () -> this.listGlow.isOpen()));
   private final SliderSetting interval = this.add(new SliderSetting("Interval", 0.0, 0.0, 15.0, 0.1));
   private final SliderSetting enableLength = this.add(new SliderSetting("EnableLength", 200, 0, 1000));
   private final SliderSetting disableLength = this.add(new SliderSetting("DisableLength", 200, 0, 1000));
   private final SliderSetting fadeLength = this.add(new SliderSetting("FadeLength", 200, 0, 1000));
   private final EnumSetting<Easing> easing = this.add(new EnumSetting<>("Easing", Easing.CircInOut));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(208, 0, 0)));
   private final EnumSetting<ArrayListHud.ColorMode> colorMode = this.add(new EnumSetting<>("ColorMode", ArrayListHud.ColorMode.Pulse));
   private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0, 0.0, 5.0, 0.1));
   private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50));
   private final ColorSetting endColor = this.add(new ColorSetting("SecondColor", new Color(255, 0, 0)).injectBoolean(true));
   private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 4.0, 1.0, 10.0, 0.1));
   private final SliderSetting saturation = this.add(new SliderSetting("Saturation", 130, 1, 255));
   private final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 1000));
   private final BooleanSetting backGround = this.add(new BooleanSetting("BackGround", false).setParent());
   private final SliderSetting bgWidth = this.add(new SliderSetting("BGWidth", 0.0, 0.0, 15.0, () -> this.backGround.isOpen()));
   private final ColorSetting bgColor = this.add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), () -> this.backGround.isOpen()));
   private final ColorSetting rect = this.add(new ColorSetting("Rect", new Color(208, 0, 0)).injectBoolean(false));
   private final ArrayList<ArrayListHud.ModuleInfo> moduleList = new ArrayList<>();
   private boolean initialized = false;
   private final ConcurrentHashMap<String, Identifier> listGlowCache = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, Long> listGlowUpdateTime = new ConcurrentHashMap<>();

   public ArrayListHud() {
      super("ArrayList", Module.Category.HUD);
      this.setChinese("\u6a21\u5757\u5217\u8868");
      INSTANCE = this;
      Aevum.EVENT_BUS.subscribe(new ArrayListHud.InitHandler());
   }

   private boolean isRightSide() {
      return this.alignMode == HudElement.AlignMode.RIGHT;
   }

   private int getColor(double counter) {
      if (this.colorMode.getValue() == ArrayListHud.ColorMode.Pulse) {
         return this.endColor.booleanValue
            ? ColorUtil.pulseColor(this.color.getValue(), this.endColor.getValue(), counter, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue())
               .getRGB()
            : ColorUtil.pulseColor(this.color.getValue(), counter, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue()).getRGB();
      } else if (this.colorMode.getValue() == ArrayListHud.ColorMode.Rainbow) {
         double state = Math.ceil(((double)System.currentTimeMillis() * this.rainbowSpeed.getValue() + counter * this.rainbowDelay.getValue()) / 20.0);
         return Color.getHSBColor((float)(state % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0F, 1.0F).getRGB();
      } else {
         return this.color.getValue().getRGB();
      }
   }

   @EventListener(
      priority = -999
   )
   public void onTick(ClientTickEvent event) {
      if (!nullCheck() && this.initialized) {
         if (event.isPost()) {
            for (ArrayListHud.ModuleInfo info : this.moduleList) {
               info.update();
            }

            if (this.sortByLength.getValue()) {
               this.moduleList.sort(Comparator.comparingInt(i -> i.displayText == null ? 0 : -((int)FontManager.getWidth(i.displayText))));
            }

            if (this.listGlow.getValue()) {
               this.cleanupStaleListGlowEntries();
            }
         }
      }
   }

   @Override
   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.renderElement(drawContext, tickDelta);
   }

   @Override
   public String getElementName() {
      return this.getChinese() != null ? this.getChinese() : this.getName();
   }

   @Override
   public int getElementX() {
      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX - this.getElementWidth() : this.hudX;
   }

   @Override
   public void setElementX(int x) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? x + this.getElementWidth() : x;
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
      int maxW = 60;

      for (ArrayListHud.ModuleInfo info : this.moduleList) {
         int w;
         if (info.displayText != null && (info.isOn || info.animX > 0.0) && (w = (int)FontManager.getWidth(info.displayText)) > maxW) {
            maxW = w;
         }
      }

      return maxW + 4;
   }

   @Override
   public int getElementHeight() {
      int count = 0;

      for (ArrayListHud.ModuleInfo info : this.moduleList) {
         if (info.isOn || info.animX > 0.0) {
            count++;
         }
      }

      return Math.max(20, (int)(((double)FontManager.getFontHeight() + this.interval.getValue()) * (double)Math.max(count, 1)));
   }

   @Override
   public boolean isElementVisible() {
      return this.isOn();
   }

   @Override
   public HudElement.AlignMode getAlignMode() {
      return this.alignMode;
   }

   @Override
   public void setAlignMode(HudElement.AlignMode mode) {
      if (mode != this.alignMode) {
         if (mode == HudElement.AlignMode.RIGHT && this.alignMode == HudElement.AlignMode.LEFT) {
            this.hudX = this.hudX + this.getElementWidth();
         } else if (mode == HudElement.AlignMode.LEFT && this.alignMode == HudElement.AlignMode.RIGHT) {
            this.hudX = this.hudX - this.getElementWidth();
         }

         this.alignMode = mode;
      }
   }

   @Override
   public int getRightAnchorX() {
      return this.alignMode == HudElement.AlignMode.RIGHT ? this.hudX : this.hudX + this.getElementWidth();
   }

   @Override
   public void setRightAnchorX(int rightX) {
      this.hudX = this.alignMode == HudElement.AlignMode.RIGHT ? rightX : rightX - this.getElementWidth();
   }

   @Override
   public void renderElement(DrawContext context, float tickDelta) {
      if (this.initialized) {
         float fontHeight = FontManager.getFontHeight();
         boolean fromUp = this.renderUp.getValue();
         float anchorX = (float)this.getElementX();
         int boxWidth = this.getElementWidth();
         boolean rightSide = this.isRightSide();
         double currentY;
         if (fromUp) {
            currentY = (double)this.hudY + 1.0;
         } else {
            int visCount = 0;

            for (ArrayListHud.ModuleInfo info : this.moduleList) {
               if (info.isOn || info.animX > 0.0) {
                  visCount++;
               }
            }

            currentY = (double)this.hudY + ((double)fontHeight + this.interval.getValue()) * (double)visCount - (double)fontHeight - 1.0;
         }

         double counter = 20.0;

         for (ArrayListHud.ModuleInfo infox : this.moduleList) {
            if (!(infox.animX <= 0.0) || infox.isOn) {
               double textWidth = (double)FontManager.getWidth(infox.displayText);
               infox.animX = infox.animation
                  .get(
                     infox.isOn ? textWidth + 1.0 : 0.0,
                     infox.isOn ? (long)this.enableLength.getValueInt() : (long)this.disableLength.getValueInt(),
                     this.easing.getValue()
                  );
               Animation animation = infox.fadeAnimation;
               double d = infox.isOn ? 1.0 : 0.0;
               double fade = animation.get(d, (long)this.fadeLength.getValueInt(), this.easing.getValue());
               if (fade > 0.04) {
                  int c = ColorUtil.injectAlpha(this.getColor(counter += fromUp ? fade : -fade), (int)((double)this.color.getValue().getAlpha() * fade));
                  double displayWidth = infox.animX;
                  float textX = rightSide ? anchorX + (float)boxWidth - (float)displayWidth : anchorX;
                  if (this.backGround.getValue()) {
                     float bgX = rightSide
                        ? anchorX + (float)boxWidth - (float)displayWidth - this.bgWidth.getValueFloat() / 2.0F
                        : anchorX - this.bgWidth.getValueFloat() / 2.0F;
                     Render2DUtil.drawRect(
                        context.getMatrices(),
                        bgX,
                        (float)currentY - 1.0F - this.interval.getValueFloat() / 2.0F,
                        (float)displayWidth + this.bgWidth.getValueFloat(),
                        fontHeight + this.interval.getValueFloat(),
                        ColorUtil.injectAlpha(
                           this.bgColor.sync ? c : this.bgColor.getValue().getRGB(), (int)((double)this.bgColor.getValue().getAlpha() * fade)
                        )
                     );
                  }

                  boolean textDrawnByGlow = false;
                  if (this.listGlow.getValue() && infox.displayText != null && !infox.displayText.isEmpty()) {
                     Color itemColor = new Color(c, true);
                     textDrawnByGlow = this.renderListItemGlow(
                        context, context.getMatrices(), infox.displayText, textX, (float)currentY, itemColor, (float)fade
                     );
                  }

                  if (!textDrawnByGlow) {
                     TextUtil.drawString(context, infox.displayText, (double)textX, (double)((float)currentY), c, this.shadow.getValue());
                  }

                  if (this.rect.booleanValue) {
                     float rectX = rightSide
                        ? anchorX + (float)boxWidth + this.bgWidth.getValueFloat() / 2.0F
                        : anchorX - this.bgWidth.getValueFloat() / 2.0F - 1.0F;
                     Render2DUtil.drawRect(
                        context.getMatrices(),
                        rectX,
                        (float)currentY - 1.0F - this.interval.getValueFloat() / 2.0F,
                        1.0F,
                        fontHeight + this.interval.getValueFloat(),
                        this.rect.sync ? c : ColorUtil.injectAlpha(this.rect.getValue(), (int)((double)this.rect.getValue().getAlpha() * fade)).getRGB()
                     );
                  }

                  currentY += fromUp ? ((double)fontHeight + this.interval.getValue()) * fade : -((double)fontHeight + this.interval.getValue()) * fade;
               }
            }
         }
      }
   }

   private void drawGlowTexture(
      DrawContext drawContext, MatrixStack matrices, Identifier texture, float screenX, float screenY, float screenW, float screenH, float alpha
   ) {
      if (SkijaGlowRenderer.isTextureReady(texture)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
         matrices.push();
         matrices.translate(screenX, screenY, 0.0F);
         int w = (int)screenW;
         int h = (int)screenH;
         drawContext.drawTexture(texture, 0, 0, 0.0F, 0.0F, w, h, w, h);
         matrices.pop();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private boolean renderListItemGlow(DrawContext drawContext, MatrixStack matrices, String text, float x, float y, Color itemColor, float fade) {
      if (text != null && !text.isEmpty() && !(fade < 0.05F)) {
         String strippedText = FontRenderer.stripControlCodes(text);
         if (strippedText.isEmpty()) {
            return false;
         } else {
            float glowRadius = (float)this.listGlowRadius.getValue();
            float glowOpacity = (float)this.listGlowOpacity.getValue();
            Color glowColor = new Color(
               itemColor.getRed(), itemColor.getGreen(), itemColor.getBlue(), Math.min(255, (int)((float)itemColor.getAlpha() * glowOpacity))
            );
            return this.renderListItemSkijaGlow(drawContext, matrices, strippedText, x, y, itemColor, glowColor, glowRadius, fade);
         }
      } else {
         return false;
      }
   }

   private boolean renderListItemSkijaGlow(
      DrawContext drawContext, MatrixStack matrices, String text, float x, float y, Color textColor, Color glowColor, float glowRadius, float fade
   ) {
      String cacheKey = String.format(
         "list_%s_%.1f_%08x_%08x_%.2f_%s_%.1f",
         text,
         glowRadius,
         textColor.getRGB(),
         glowColor.getRGB(),
         this.listGlowOpacity.getValue(),
         FontManager.getMode(),
         FontManager.getSize()
      );
      Identifier glowTexture = this.listGlowCache.get(cacheKey);
      Long lastUpdate = this.listGlowUpdateTime.get(cacheKey);
      boolean needsUpdate = glowTexture == null || lastUpdate == null || System.currentTimeMillis() - lastUpdate > 5000L;
      if (needsUpdate) {
         if (glowTexture != null) {
            SkijaGlowRenderer.cleanupTexture(glowTexture);
         }

         Identifier newTexture;
         if ((
               newTexture = SkijaGlowRenderer.createGlowTexture(
                  text, FontManager.isCustom() ? FontManager.getSize() : 9.0F, textColor, glowColor, glowRadius, false, null, null
               )
            )
            == null) {
            return false;
         }

         this.listGlowCache.put(cacheKey, newTexture);
         this.listGlowUpdateTime.put(cacheKey, System.currentTimeMillis());
         glowTexture = newTexture;
      }

      if (glowTexture != null && SkijaGlowRenderer.isTextureReady(glowTexture)) {
         float textWidth = FontManager.getWidth(text);
         float textHeight = FontManager.getFontHeight();
         float glowPadding = glowRadius * 3.0F;
         this.drawGlowTexture(
            drawContext, matrices, glowTexture, x - glowPadding, y - glowPadding, textWidth + glowPadding * 2.0F, textHeight + glowPadding * 2.0F, fade
         );
         return true;
      } else {
         return false;
      }
   }

   private void cleanupStaleListGlowEntries() {
      long now = System.currentTimeMillis();
      this.listGlowUpdateTime.entrySet().removeIf(entry -> {
         if (now - entry.getValue() > 30000L) {
            Identifier id = this.listGlowCache.remove(entry.getKey());
            if (id != null) {
               SkijaGlowRenderer.cleanupTexture(id);
            }

            return true;
         } else {
            return false;
         }
      });
   }

   private static enum ColorMode {
      Custom,
      Pulse,
      Rainbow;

      private ColorMode() {
      }
   }

   public class InitHandler {
      public InitHandler() {
      }

      @EventListener
      public void onInit(InitEvent event) {
         ArrayListHud.this.moduleList.clear();

         for (Module m : Aevum.MODULE.getModules()) {
            ArrayListHud.this.moduleList.add(ArrayListHud.this.new ModuleInfo(m));
         }

         ArrayListHud.this.initialized = true;
         Aevum.EVENT_BUS.unsubscribe(this);
      }
   }

   private class ModuleInfo {
      final Module module;
      String displayText;
      boolean isOn;
      double animX = 0.0;
      final Animation animation = new Animation();
      final Animation fadeAnimation = new Animation();

      ModuleInfo(Module module) {
         this.module = module;
         this.displayText = module.getArrayName();
      }

      void update() {
         boolean bl = this.isOn = this.module.isOn() && this.module.drawn.getValue();
         if (this.isOn) {
            String text = this.module.getArrayName();
            if (ArrayListHud.this.showSpace.getValue() && (text = text.replaceAll("([a-z])([A-Z])", "$1 $2")).startsWith(" ")) {
               text = text.replaceFirst(" ", "");
            }

            if (ArrayListHud.this.lowerCase.getValue()) {
               text = text.toLowerCase();
            }

            if (ArrayListHud.this.forgeHax.getValue()) {
               text = text + "\u00a7r<";
            }

            this.displayText = text;
         }
      }
   }
}
