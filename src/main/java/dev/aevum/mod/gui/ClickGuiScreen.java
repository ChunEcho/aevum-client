/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Vec3
 *  net.minecraft.Component
 *  net.minecraft.GuiGraphics
 *  net.minecraft.InputConstants
 *  net.minecraft.Screen
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.Aevum;
import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.i18n.Lang;
import dev.aevum.api.utils.math.AnimateUtil;
import dev.aevum.api.utils.math.SmoothAnimationUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.Mod;
import dev.aevum.mod.gui.items.Component;
import dev.aevum.mod.gui.items.Item;
import dev.aevum.mod.gui.items.buttons.ModuleButton;
import dev.aevum.mod.modules.HudElement;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.client.ClickGui;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.client.HudEditor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Vec3;
import net.minecraft.Component;
import net.minecraft.GuiGraphics;
import net.minecraft.InputConstants;
import net.minecraft.Screen;
import net.minecraft.PoseStack;

public class ClickGuiScreen
extends Screen {
    private static ClickGuiScreen INSTANCE = new ClickGuiScreen();
    private final ArrayList<Component> components = new ArrayList();
    private float mouseMoveOffsetX;
    private float mouseMoveOffsetY;
    private float walkShakeOffsetX;
    private float walkShakeOffsetY;
    private float walkShakeTime;
    private boolean layoutCorrected = false;
    private final Random snowRandom = new Random();
    private final ArrayList<Snowflake> snowflakes = new ArrayList();
    private final ArrayList<HudElement> hudElements = new ArrayList();
    private final Map<HudElement, SmoothAnimationUtil> hudAnimX = new HashMap<HudElement, SmoothAnimationUtil>();
    private final Map<HudElement, SmoothAnimationUtil> hudAnimY = new HashMap<HudElement, SmoothAnimationUtil>();
    private HudElement draggingHud = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean showHudGuides = true;

    public ClickGuiScreen() {
        super((Component)Component.method_43470((String)"Aevum"));
        this.setInstance();
        this.load();
    }

    public static ClickGuiScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGuiScreen();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
        int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
        int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
        int spacing = layoutWidth + 1;
        int count = Module.Category.values().length;
        int startX = 10;
        int startY = 4;
        if (Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
            int screenWidth = Wrapper.mc.getWindow().getGuiScaledWidth();
            int screenHeight = Wrapper.mc.getWindow().getGuiScaledHeight();
            int totalWidth = count * layoutWidth + (count - 1);
            startX = Math.round(((float)screenWidth - (float)totalWidth) / 2.0f);
            startY = Math.round((float)screenHeight / 6.0f);
            this.layoutCorrected = true;
        }
        int offsetX = Math.round(((float)layoutWidth - (float)moduleButtonWidth) / 2.0f);
        int x = startX - spacing;
        for (final Module.Category category : Module.Category.values()) {
            this.components.add(new Component(this, Lang.tr(category.name()), category, (x += spacing) + offsetX, startY, true){

                @Override
                public void setupItems() {
                    for (Module module : Aevum.MODULE.getModules()) {
                        if (!module.getCategory().equals((Object)category)) continue;
                        this.addButton(new ModuleButton(module));
                    }
                }
            });
        }
        this.components.forEach(components -> components.getItems().sort(Comparator.comparing(Mod::getName)));
    }

    public void method_25394(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int alpha;
        float walkSmooth;
        float smooth;
        ClickGui clickGui;
        float keyCodec = (float)ClickGui.getInstance().alphaValue;
        float scale = 0.92f + 0.08f * keyCodec;
        float slideY = (1.0f - keyCodec) * 20.0f;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)keyCodec);
        Item.context = context;
        this.method_25420(context, mouseX, mouseY, delta);
        if (this.isHudEditorMode()) {
            this.refreshHudElements();
            this.renderHudOverlay(context, mouseX, mouseY, delta);
        }
        if ((clickGui = ClickGui.getInstance()) != null && clickGui.colorMode.getValue() == ClickGui.ColorMode.Spectrum) {
            clickGui.updateSpectrumLut(context.method_51443());
        }
        if (!this.layoutCorrected && Wrapper.mc != null && Wrapper.mc.getWindow() != null) {
            int categoryWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().categoryWidth.getValueInt() : 101;
            int moduleButtonWidth = ClickGui.getInstance() != null ? ClickGui.getInstance().moduleButtonWidth.getValueInt() : 93;
            int layoutWidth = Math.max(categoryWidth, moduleButtonWidth);
            int spacing = layoutWidth + 1;
            int count = this.components.size();
            if (count > 0) {
                int screenWidth = Wrapper.mc.getWindow().getGuiScaledWidth();
                int screenHeight = Wrapper.mc.getWindow().getGuiScaledHeight();
                int totalWidth = count * layoutWidth + (count - 1);
                int startX = Math.round(((float)screenWidth - (float)totalWidth) / 2.0f);
                int startY = Math.round((float)screenHeight / 6.0f);
                int offsetX = Math.round(((float)layoutWidth - (float)moduleButtonWidth) / 2.0f);
                int x = startX - spacing;
                for (Component component : this.components) {
                    component.setX((x += spacing) + offsetX);
                    component.setY(startY);
                }
            }
            this.layoutCorrected = true;
        }
        boolean dragging = false;
        for (Component c2 : this.components) {
            if (!c2.drag) continue;
            dragging = true;
            break;
        }
        float targetOffsetX = 0.0f;
        float targetOffsetY = 0.0f;
        if (ClickGui.getInstance().mouseMove.getValue() && !dragging) {
            float strength = ClickGui.getInstance().mouseMoveStrength.getValueFloat() * (float)ClickGui.getInstance().alphaValue;
            float cx = (float)context.method_51421() / 2.0f;
            float cy = (float)context.method_51443() / 2.0f;
            float nx = cx <= 0.0f ? 0.0f : ((float)mouseX - cx) / cx;
            float ny = cy <= 0.0f ? 0.0f : ((float)mouseY - cy) / cy;
            nx = Math.max(-1.0f, Math.min(1.0f, nx));
            ny = Math.max(-1.0f, Math.min(1.0f, ny));
            targetOffsetX = nx * strength;
            targetOffsetY = ny * strength;
        }
        if ((smooth = ClickGui.getInstance().mouseMoveSmooth.getValueFloat()) <= 0.0f) {
            this.mouseMoveOffsetX = targetOffsetX;
            this.mouseMoveOffsetY = targetOffsetY;
        } else {
            float a = AnimateUtil.deltaTime() * smooth;
            if (a < 0.0f) {
                a = 0.0f;
            }
            if (a > 0.35f) {
                a = 0.35f;
            }
            this.mouseMoveOffsetX += (targetOffsetX - this.mouseMoveOffsetX) * a;
            this.mouseMoveOffsetY += (targetOffsetY - this.mouseMoveOffsetY) * a;
        }
        float targetWalkX = 0.0f;
        float targetWalkY = 0.0f;
        float maxWalk = 0.0f;
        if (ClickGui.getInstance().walkShake.getValue() && !dragging && Wrapper.mc.player != null) {
            Vec3 v = Wrapper.mc.player.getDeltaMovement();
            float horizontalSpeed = (float)Math.sqrt(v.x * v.x + v.z * v.z);
            float moving = horizontalSpeed > 0.003f ? Math.min(1.0f, horizontalSpeed * 18.0f) : 0.0f;
            float dt = AnimateUtil.deltaTime();
            if (moving > 0.0f) {
                float speed = ClickGui.getInstance().walkShakeSpeed.getValueFloat();
                this.walkShakeTime += dt * speed * (0.4f + 0.6f * moving);
            }
            float strength = ClickGui.getInstance().walkShakeStrength.getValueFloat() * moving * (float)ClickGui.getInstance().alphaValue;
            maxWalk = ClickGui.getInstance().walkShakeMax.getValueFloat() * (float)ClickGui.getInstance().alphaValue;
            targetWalkX = (float)Math.sin(this.walkShakeTime) * strength;
            targetWalkY = (float)Math.cos(this.walkShakeTime * 2.0f) * strength * 0.35f;
            targetWalkX = Math.max(-maxWalk, Math.min(maxWalk, targetWalkX));
            targetWalkY = Math.max(-maxWalk, Math.min(maxWalk, targetWalkY));
        }
        if ((walkSmooth = ClickGui.getInstance().walkShakeSmooth.getValueFloat()) <= 0.0f) {
            this.walkShakeOffsetX = targetWalkX;
            this.walkShakeOffsetY = targetWalkY;
        } else {
            float a = AnimateUtil.deltaTime() * walkSmooth;
            if (a < 0.0f) {
                a = 0.0f;
            }
            if (a > 0.35f) {
                a = 0.35f;
            }
            this.walkShakeOffsetX += (targetWalkX - this.walkShakeOffsetX) * a;
            this.walkShakeOffsetY += (targetWalkY - this.walkShakeOffsetY) * a;
        }
        if (maxWalk > 0.0f) {
            this.walkShakeOffsetX = Math.max(-maxWalk, Math.min(maxWalk, this.walkShakeOffsetX));
            this.walkShakeOffsetY = Math.max(-maxWalk, Math.min(maxWalk, this.walkShakeOffsetY));
        }
        float totalOffsetX = this.mouseMoveOffsetX + this.walkShakeOffsetX;
        float totalOffsetY = this.mouseMoveOffsetY + this.walkShakeOffsetY;
        this.components.forEach(c -> c.setMouseMoveOffset(totalOffsetX, totalOffsetY));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Component c3 : this.components) {
            minX = Math.min(minX, c3.getX());
            minY = Math.min(minY, c3.getY());
            maxX = Math.max(maxX, c3.getX() + c3.getWidth());
            maxY = Math.max(maxY, c3.getY() + c3.getHeight());
        }
        int margin = 16;
        int panelX = Math.max(8, minX - margin);
        int panelY = Math.max(6, minY - margin);
        int panelW = Math.min(context.method_51421() - panelX - 8, maxX - minX + margin * 2);
        int panelH = Math.min(context.method_51443() - panelY - 6, maxY - minY + margin * 2 + 24);
        boolean focused = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH;
        int n = alpha = focused ? (int)Math.round(242.25) : (int)Math.round(226.95000000000002);
        if (ClickGui.getInstance().blur.getValue()) {
            float blurRadius = 1.0f + (ClickGui.getInstance().radius.getValueFloat() - 1.0f) * (float)ClickGui.getInstance().alphaValue;
            Aevum.BLUR.applyBlur(blurRadius, 0.0f, 0.0f, context.method_51421(), context.method_51443(), ClickGui.getInstance().blurType.getValue().ordinal());
        }
        this.renderSnow(context);
        context.method_51448().method_22903();
        context.method_51448().method_46416((float)panelX + (float)panelW / 2.0f, (float)panelY + (float)panelH / 2.0f + slideY, 0.0f);
        context.method_51448().method_22905(scale, scale, 1.0f);
        context.method_51448().method_46416(-((float)panelX + (float)panelW / 2.0f), -((float)panelY + (float)panelH / 2.0f), 0.0f);
        int strokeA = Math.max(0, Math.min(255, (int)Math.round((double)alpha * 0.22)));
        context.method_51448().method_22909();
        context.method_51448().method_22903();
        context.method_51448().method_46416(0.0f, slideY, 0.0f);
        context.method_51448().method_22905(scale, scale, 1.0f);
        this.components.forEach(components -> components.drawScreen(context, mouseX, mouseY, delta));
        context.method_51448().method_22909();
        ClickGui gui = ClickGui.getInstance();
        if (gui != null && gui.tips.getValue()) {
            boolean customFont = FontManager.isCustomFontEnabled();
            boolean shadow = FontManager.isShadowEnabled();
            float lineHeight = customFont ? FontManager.ui.getFontHeight() : TextUtil.getHeight();
            float marginBottom = 6.0f;
            int lines = 5;
            float baseY = (float)context.method_51443() - marginBottom - lineHeight * (float)lines;
            int tipX = 6;
            int tipY = Math.round(baseY);
            boolean chinese = ClientSetting.INSTANCE != null && ClientSetting.INSTANCE.chinese.getValue();
            String tip1 = chinese ? "\u5de6\u952e\u62d6\u52a8\u5217 \u53f3\u952e\u5c55\u5f00/\u6298\u53e0" : "LMB drag, RMB expand/collapse";
            String tip2 = chinese ? "\u6eda\u8f6e\u662f\u4e0a\u4e0b\u79fb\u52a8 SHIFT+\u6eda\u8f6e\u662f\u5de6\u53f3\u79fb\u52a8" : "Scroll up/down, SHIFT+scroll left/right";
            String tip3 = chinese ? "SHIFT+\u5de6\u952e \u5feb\u6377\u952e\u6309\u94ae \u5207\u6362\u529f\u80fd(\u6309\u4f4f/\u677e\u5f00)\u89e6\u53d1" : "SHIFT+LMB: toggle hold/release";
            String tip4 = chinese ? "SHIFT+\u5de6\u952e \u529f\u80fd\u6309\u94ae \u91cd\u7f6e\u8bbe\u7f6e" : "SHIFT+LMB: reset this setting";
            String tip5 = chinese ? "\u6587\u672c\u8bbe\u7f6e \u53f3\u952e\u7f16\u8f91" : "RMB on String setting: edit";
            boolean spectrumTips = gui.colorMode.getValue() == ClickGui.ColorMode.Spectrum;
            double delay1 = spectrumTips ? (double)tipY * 0.25 : (double)tipY / 10.0;
            Color c1 = gui.getActiveColor(delay1);
            int color1 = ColorUtil.injectAlpha(c1, alpha).getRGB();
            TextUtil.drawString(context, tip1, tipX, tipY, color1, customFont, shadow);
            int tipY2 = (int)((float)tipY + lineHeight);
            double delay2 = spectrumTips ? (double)tipY2 * 0.25 : (double)tipY2 / 10.0;
            Color c2 = gui.getActiveColor(delay2);
            int color2 = ColorUtil.injectAlpha(c2, alpha).getRGB();
            TextUtil.drawString(context, tip2, tipX, tipY2, color2, customFont, shadow);
            int tipY3 = (int)((float)tipY + lineHeight * 2.0f);
            double delay3 = spectrumTips ? (double)tipY3 * 0.25 : (double)tipY3 / 10.0;
            Color c3 = gui.getActiveColor(delay3);
            int color3 = ColorUtil.injectAlpha(c3, alpha).getRGB();
            TextUtil.drawString(context, tip3, tipX, tipY3, color3, customFont, shadow);
            int tipY4 = (int)((float)tipY + lineHeight * 3.0f);
            double delay4 = spectrumTips ? (double)tipY4 * 0.25 : (double)tipY4 / 10.0;
            Color c4 = gui.getActiveColor(delay4);
            int color4 = ColorUtil.injectAlpha(c4, alpha).getRGB();
            TextUtil.drawString(context, tip4, tipX, tipY4, color4, customFont, shadow);
            int tipY5 = (int)((float)tipY + lineHeight * 4.0f);
            double delay5 = spectrumTips ? (double)tipY5 * 0.25 : (double)tipY5 / 10.0;
            Color c5 = gui.getActiveColor(delay5);
            int color5 = ColorUtil.injectAlpha(c5, alpha).getRGB();
            TextUtil.drawString(context, tip5, tipX, tipY5, color5, customFont, shadow);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public boolean method_25402(double mouseX, double mouseY, int clickedButton) {
        if (this.isHudEditorMode()) {
            this.refreshHudElements();
            int mx = (int)mouseX;
            int my = (int)mouseY;
            HudElement clicked = this.getHoveredHud(mx, my);
            if (clickedButton == 0 && clicked != null && clicked.isDraggable()) {
                this.draggingHud = clicked;
                this.dragOffsetX = mx - clicked.getElementX();
                this.dragOffsetY = my - clicked.getElementY();
                return true;
            }
            if (clickedButton == 1 && clicked instanceof Module) {
                ((Module)((Object)clicked)).toggle();
                return true;
            }
        }
        this.components.forEach(components -> components.mouseClicked((int)mouseX, (int)mouseY, clickedButton));
        return super.method_25402(mouseX, mouseY, clickedButton);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int releaseButton) {
        if (this.isHudEditorMode() && releaseButton == 0 && this.draggingHud != null) {
            this.clampHud(this.draggingHud);
            this.updateAlignMode(this.draggingHud);
            this.draggingHud = null;
            Aevum.CONFIG.save();
            return true;
        }
        this.components.forEach(components -> components.mouseReleased((int)mouseX, (int)mouseY, releaseButton));
        return super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (InputConstants.method_15987((long)Wrapper.mc.getWindow().getWindow(), (int)340)) {
            if (verticalAmount < 0.0) {
                this.components.forEach(component -> component.setX(component.getTargetX() - 15));
            } else if (verticalAmount > 0.0) {
                this.components.forEach(component -> component.setX(component.getTargetX() + 15));
            }
        } else if (verticalAmount < 0.0) {
            this.components.forEach(component -> component.setY(component.getTargetY() - 15));
        } else if (verticalAmount > 0.0) {
            this.components.forEach(component -> component.setY(component.getTargetY() + 15));
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (this.isHudEditorMode()) {
            this.refreshHudElements();
            if (this.draggingHud != null && this.draggingHud.isDraggable()) {
                int step = Screen.hasShiftDown() ? 10 : 1;
                switch (keyCode) {
                    case 263: {
                        this.moveHudLeft(this.draggingHud, step);
                        this.clampHud(this.draggingHud);
                        return true;
                    }
                    case 262: {
                        this.moveHudLeft(this.draggingHud, -step);
                        this.clampHud(this.draggingHud);
                        return true;
                    }
                    case 265: {
                        this.draggingHud.setElementY(this.draggingHud.getElementY() - step);
                        this.clampHud(this.draggingHud);
                        return true;
                    }
                    case 264: {
                        this.draggingHud.setElementY(this.draggingHud.getElementY() + step);
                        this.clampHud(this.draggingHud);
                        return true;
                    }
                    case 71: {
                        this.showHudGuides = !this.showHudGuides;
                        return true;
                    }
                    case 82: {
                        this.forceSetLeftAlignX(this.draggingHud, (this.width - this.draggingHud.getElementWidth()) / 2);
                        this.draggingHud.setElementY((this.height - this.draggingHud.getElementHeight()) / 2);
                        this.clampHud(this.draggingHud);
                        return true;
                    }
                }
            }
        }
        this.components.forEach(component -> component.onKeyPressed(keyCode));
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char chr, int modifiers) {
        this.components.forEach(component -> component.onKeyTyped(chr, modifiers));
        return super.method_25400(chr, modifiers);
    }

    private boolean isHudEditorMode() {
        return HudEditor.INSTANCE != null && HudEditor.INSTANCE.isOn();
    }

    private void refreshHudElements() {
        if (!this.hudElements.isEmpty()) {
            return;
        }
        for (Module module : Aevum.MODULE.getModules()) {
            if (!(module instanceof HudElement)) continue;
            HudElement hud = (HudElement)((Object)module);
            this.hudElements.add(hud);
            this.hudAnimX.put(hud, new SmoothAnimationUtil(hud.getElementX(), 0.28, 0.2));
            this.hudAnimY.put(hud, new SmoothAnimationUtil(hud.getElementY(), 0.28, 0.2));
        }
    }

    private SmoothAnimationUtil getAnimX(HudElement e) {
        return this.hudAnimX.computeIfAbsent(e, k -> new SmoothAnimationUtil(e.getElementX(), 0.28, 0.2));
    }

    private SmoothAnimationUtil getAnimY(HudElement e) {
        return this.hudAnimY.computeIfAbsent(e, k -> new SmoothAnimationUtil(e.getElementY(), 0.28, 0.2));
    }

    private float getRenderHudX(HudElement e) {
        SmoothAnimationUtil anim = this.getAnimX(e);
        anim.setTarget(e.getElementX());
        return (float)anim.update();
    }

    private float getRenderHudY(HudElement e) {
        SmoothAnimationUtil anim = this.getAnimY(e);
        anim.setTarget(e.getElementY());
        return (float)anim.update();
    }

    private void renderHudOverlay(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.showHudGuides && this.draggingHud != null) {
            context.method_25294(this.width / 2, 0, this.width / 2 + 1, this.height, 0x15FFFFFF);
            context.method_25294(0, this.height / 2, this.width, this.height / 2 + 1, 0x15FFFFFF);
        }
        for (HudElement elem : this.hudElements) {
            this.renderHudElement(context, elem, mouseX, mouseY, delta);
        }
        if (this.showHudGuides && this.draggingHud != null) {
            this.renderHudAlignGuides(context);
        }
        context.method_25294(0, this.height - 13, this.width, this.height, -1879048192);
        String alignInfo = this.draggingHud != null ? (this.draggingHud.getAlignMode() == HudElement.AlignMode.RIGHT ? " [\u53f3\u5bf9\u9f50]" : " [\u5de6\u5bf9\u9f50]") : "";
        String info = this.draggingHud != null ? String.format("\u00a7e\u62d6\u62fdHUD \u00a77(%d, %d)%s", this.draggingHud.getElementX(), this.draggingHud.getElementY(), alignInfo) : "\u00a77HudEditor | \u5de6\u952e\u62d6\u62fd | \u53f3\u952e\u5207\u6362 | G\u8f85\u52a9\u7ebf | R\u5c45\u4e2d";
        context.method_25300(this.font, info, this.width / 2, this.height - 10, 0xFFFFFF);
    }

    private void renderHudElement(GuiGraphics context, HudElement elem, int mouseX, int mouseY, float delta) {
        int actualX = elem.getElementX();
        int actualY = elem.getElementY();
        int w = elem.getElementWidth();
        int h = elem.getElementHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float renderX = this.getRenderHudX(elem);
        float renderY = this.getRenderHudY(elem);
        boolean vis = elem.isElementVisible();
        if (vis) {
            try {
                context.method_51448().method_22903();
                context.method_51448().method_46416(renderX - (float)actualX, renderY - (float)actualY, 0.0f);
                elem.renderElement(context, delta);
                context.method_51448().method_22909();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.draggingHud == elem) {
            int rx = Math.round(renderX);
            int ry = Math.round(renderY);
            this.drawDashedRect(context, rx, ry, w, h, -1);
            String label = elem.getElementName();
            int lw = this.font.method_1727(label);
            int lx = rx + w + 4;
            int ly = ry - 2;
            if (lx + lw + 6 > this.width) {
                lx = rx - lw - 6;
            }
            if (ly < 2) {
                ly = ry + h + 2;
            }
            context.method_25294(lx - 2, ly - 1, lx + lw + 2, ly + 9, -1342177280);
            context.method_25303(this.font, label, lx, ly, -1);
        }
    }

    private void drawDashedRect(GuiGraphics context, int x, int y, int w, int h, int color) {
        int end;
        int i;
        int dash = 4;
        int gap = 2;
        for (i = 0; i < w; i += dash + gap) {
            end = Math.min(i + dash, w);
            context.method_25294(x + i, y, x + end, y + 1, color);
            context.method_25294(x + i, y + h - 1, x + end, y + h, color);
        }
        for (i = 0; i < h; i += dash + gap) {
            end = Math.min(i + dash, h);
            context.method_25294(x, y + i, x + 1, y + end, color);
            context.method_25294(x + w - 1, y + i, x + w, y + end, color);
        }
    }

    private void renderHudAlignGuides(GuiGraphics context) {
        int dx = this.draggingHud.getElementX();
        int dy = this.draggingHud.getElementY();
        int dw = this.draggingHud.getElementWidth();
        int dh = this.draggingHud.getElementHeight();
        if (Math.abs(dx + dw / 2 - this.width / 2) <= 2) {
            context.method_25294(this.width / 2, 0, this.width / 2 + 1, this.height, -2147418113);
        }
        if (Math.abs(dy + dh / 2 - this.height / 2) <= 2) {
            context.method_25294(0, this.height / 2, this.width, this.height / 2 + 1, -2147418113);
        }
        for (HudElement o : this.hudElements) {
            if (o == this.draggingHud || o.getElementWidth() <= 0) continue;
            int ox = o.getElementX();
            int oy = o.getElementY();
            int ow = o.getElementWidth();
            int oh = o.getElementHeight();
            int minY = Math.min(dy, oy);
            int maxY = Math.max(dy + dh, oy + oh);
            int minX = Math.min(dx, ox);
            int maxX = Math.max(dx + dw, ox + ow);
            if (Math.abs(dx - ox) <= 2) {
                context.method_25294(ox, minY, ox + 1, maxY, -2147418113);
            }
            if (Math.abs(dx + dw - ox - ow) <= 2) {
                context.method_25294(ox + ow - 1, minY, ox + ow, maxY, -2130737152);
            }
            if (Math.abs(dy - oy) <= 2) {
                context.method_25294(minX, oy, maxX, oy + 1, -2147418113);
            }
            if (Math.abs(dy + dh - oy - oh) <= 2) {
                context.method_25294(minX, oy + oh - 1, maxX, oy + oh, -2147418113);
            }
            if (Math.abs(dx - ox - ow) <= 2) {
                context.method_25294(ox + ow, minY, ox + ow + 1, maxY, -2147418368);
            }
            if (Math.abs(dx + dw - ox) <= 2) {
                context.method_25294(ox - 1, minY, ox, maxY, -2147418368);
            }
            if (Math.abs(dy - oy - oh) <= 2) {
                context.method_25294(minX, oy + oh, maxX, oy + oh + 1, -2147418368);
            }
            if (Math.abs(dy + dh - oy) > 2) continue;
            context.method_25294(minX, oy - 1, maxX, oy, -2147418368);
        }
    }

    private void updateAlignMode(HudElement hud) {
        int hx = hud.getElementX();
        int hw = hud.getElementWidth();
        int rightEdge = hx + hw;
        boolean hasRightAlignPartner = false;
        for (HudElement o : this.hudElements) {
            if (o == hud || o.getElementWidth() <= 0 || Math.abs(rightEdge - (o.getElementX() + o.getElementWidth())) > 1) continue;
            hasRightAlignPartner = true;
            break;
        }
        if (hasRightAlignPartner || hx + hw / 2 > this.width / 2) {
            if (hud.getAlignMode() != HudElement.AlignMode.RIGHT) {
                hud.setAlignMode(HudElement.AlignMode.RIGHT);
            }
        } else if (hud.getAlignMode() != HudElement.AlignMode.LEFT) {
            hud.setAlignMode(HudElement.AlignMode.LEFT);
        }
    }

    private void forceSetLeftAlignX(HudElement hud, int leftX) {
        try {
            if (hud.getAlignMode() == HudElement.AlignMode.RIGHT) {
                hud.setRightAnchorX(leftX + hud.getElementWidth());
            } else {
                hud.setElementX(leftX);
            }
        }
        catch (Exception e) {
            hud.setElementX(leftX);
        }
    }

    private void moveHudLeft(HudElement hud, int delta) {
        try {
            if (hud.getAlignMode() == HudElement.AlignMode.RIGHT) {
                hud.setRightAnchorX(hud.getRightAnchorX() - delta);
            } else {
                hud.setElementX(hud.getElementX() - delta);
            }
        }
        catch (Exception e) {
            hud.setElementX(hud.getElementX() - delta);
        }
    }

    private HudElement getHoveredHud(int mx, int my) {
        HudElement r = null;
        int min = Integer.MAX_VALUE;
        for (HudElement e : this.hudElements) {
            int a;
            if (!this.isOverHud(e, mx, my) || (a = e.getElementWidth() * e.getElementHeight()) <= 0 || a >= min) continue;
            min = a;
            r = e;
        }
        return r;
    }

    private boolean isOverHud(HudElement e, int mx, int my) {
        int m = 4;
        int x = e.getElementX();
        int y = e.getElementY();
        int w = e.getElementWidth();
        int h = e.getElementHeight();
        return w > 0 && h > 0 && mx >= x - m && mx <= x + w + m && my >= y - m && my <= y + h + m;
    }

    private void clampHud(HudElement e) {
        int clampedX = Math.max(0, Math.min(e.getElementX(), this.width - e.getElementWidth()));
        int clampedY = Math.max(0, Math.min(e.getElementY(), this.height - e.getDragBoundsHeight()));
        this.forceSetLeftAlignX(e, clampedX);
        e.setElementY(clampedY);
    }

    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isHudEditorMode() && button == 0 && this.draggingHud != null) {
            int oh;
            int ow;
            int oy;
            int ox;
            int nx = (int)mouseX - this.dragOffsetX;
            int ny = (int)mouseY - this.dragOffsetY;
            int dw = this.draggingHud.getElementWidth();
            int dh = this.draggingHud.getElementHeight();
            int dragDh = this.draggingHud.getDragBoundsHeight();
            if (Math.abs(nx) < 2) {
                nx = 0;
            }
            if (Math.abs(ny) < 2) {
                ny = 0;
            }
            if (Math.abs(nx + dw - this.width) < 2) {
                nx = this.width - dw;
            }
            if (Math.abs(ny + dragDh - this.height) < 2) {
                ny = this.height - dragDh;
            }
            int cx = (this.width - dw) / 2;
            int cy = (this.height - dh) / 2;
            if (Math.abs(nx - cx) < 2) {
                nx = cx;
            }
            if (Math.abs(ny - cy) < 2) {
                ny = cy;
            }
            boolean snappedX = false;
            boolean snappedY = false;
            for (HudElement o : this.hudElements) {
                if (o == this.draggingHud || o.getElementWidth() <= 0) continue;
                ox = o.getElementX();
                oy = o.getElementY();
                ow = o.getElementWidth();
                oh = o.getElementHeight();
                boolean vertAdj = Math.abs(ny - oy - oh) < 2 || Math.abs(ny + dh - oy) < 2;
                boolean bl = vertAdj;
                if (vertAdj && !snappedX) {
                    int rsr = 6;
                    if (Math.abs(nx + dw - (ox + ow)) < rsr) {
                        nx = ox + ow - dw;
                        snappedX = true;
                    }
                    if (!snappedX && Math.abs(nx - ox) < rsr) {
                        nx = ox;
                        snappedX = true;
                    }
                }
                if (snappedY) continue;
                if (Math.abs(ny - oy - oh) < 2) {
                    ny = oy + oh;
                    snappedY = true;
                }
                if (snappedY || Math.abs(ny + dh - oy) >= 2) continue;
                ny = oy - dh;
                snappedY = true;
            }
            for (HudElement o : this.hudElements) {
                if (o == this.draggingHud || o.getElementWidth() <= 0) continue;
                ox = o.getElementX();
                oy = o.getElementY();
                ow = o.getElementWidth();
                oh = o.getElementHeight();
                if (!snappedX) {
                    if (Math.abs(nx - ox) < 2) {
                        nx = ox;
                        snappedX = true;
                    }
                    if (!snappedX && Math.abs(nx + dw - ox - ow) < 2) {
                        nx = ox + ow - dw;
                        snappedX = true;
                    }
                    if (!snappedX && Math.abs(nx - ox - ow) < 2) {
                        nx = ox + ow;
                        snappedX = true;
                    }
                    if (!snappedX && Math.abs(nx + dw - ox) < 2) {
                        nx = ox - dw;
                        snappedX = true;
                    }
                }
                if (snappedY) continue;
                if (Math.abs(ny - oy) < 2) {
                    ny = oy;
                    snappedY = true;
                }
                if (!snappedY && Math.abs(ny + dh - oy - oh) < 2) {
                    ny = oy + oh - dh;
                    snappedY = true;
                }
                if (!snappedY && Math.abs(ny - oy - oh) < 2) {
                    ny = oy + oh;
                    snappedY = true;
                }
                if (snappedY || Math.abs(ny + dh - oy) >= 2) continue;
                ny = oy - dh;
                snappedY = true;
            }
            if (this.draggingHud.getAlignMode() == HudElement.AlignMode.RIGHT) {
                this.forceSetLeftAlignX(this.draggingHud, nx);
            } else {
                this.draggingHud.setElementX(nx);
            }
            this.draggingHud.setElementY(ny);
            return true;
        }
        return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return this.components;
    }

    public int getTextOffset() {
        return -ClickGui.getInstance().textOffset.getValueInt() - 6;
    }

    private void renderSnow(GuiGraphics context) {
        ClickGui gui = ClickGui.getInstance();
        if (gui == null || !gui.snow.getValue()) {
            if (!this.snowflakes.isEmpty()) {
                this.snowflakes.clear();
            }
            return;
        }
        float fade = (float)gui.alphaValue;
        if (fade <= 0.01f) {
            return;
        }
        int w = context.method_51421();
        int h = context.method_51443();
        int target = Math.max(0, gui.snowAmount.getValueInt());
        while (this.snowflakes.size() < target) {
            this.snowflakes.add(this.spawnSnowflake(w, h, true));
        }
        while (this.snowflakes.size() > target) {
            this.snowflakes.remove(this.snowflakes.size() - 1);
        }
        float dt = AnimateUtil.deltaTime();
        if (dt <= 0.0f) {
            dt = 0.016f;
        }
        float baseSpeed = gui.snowSpeed.getValueFloat();
        float baseSize = gui.snowSize.getValueFloat();
        float wind = gui.snowWind.getValueFloat();
        int a = (int)Math.round((double)gui.snowAlpha.getValueInt() * (double)fade);
        a = Math.max(0, Math.min(255, a));
        Color c = new Color(255, 255, 255, a);
        for (int i = 0; i < this.snowflakes.size(); ++i) {
            Snowflake f = this.snowflakes.get(i);
            f.y += baseSpeed * f.speedMul * dt;
            f.x += wind * dt + (float)Math.sin(f.y * 0.02f + f.phase) * f.drift * dt;
            float size = Math.max(0.3f, baseSize * f.sizeMul);
            if (f.y > (float)h + size + 2.0f || f.x < -10.0f || f.x > (float)w + 10.0f) {
                this.snowflakes.set(i, this.spawnSnowflake(w, h, false));
                continue;
            }
            if (gui.snowShape.getValue() == ClickGui.SnowShape.Circle) {
                Render2DUtil.drawCircle(context.method_51448(), f.x, f.y, size, c, 16);
                continue;
            }
            this.drawSnowflake(context.method_51448(), f.x, f.y, size, c.getRGB(), f.phase + f.y * 0.01f);
        }
    }

    private void drawSnowflake(PoseStack matrices, float x, float y, float r, int color, float rotation) {
        float branchLen = r * 0.45f;
        float branchOffset = 0.65f;
        for (int i = 0; i < 3; ++i) {
            double a = (double)rotation + (double)i * 1.0471975511965976;
            float dx = (float)Math.cos(a) * r;
            float dy = (float)Math.sin(a) * r;
            Render2DUtil.drawLine(matrices, x - dx, y - dy, x + dx, y + dy, color);
            float fx1 = x + dx * branchOffset;
            float fy1 = y + dy * branchOffset;
            Render2DUtil.drawLine(matrices, fx1, fy1, fx1 + (float)Math.cos(a + 0.5235987755982988) * branchLen, fy1 + (float)Math.sin(a + 0.5235987755982988) * branchLen, color);
            Render2DUtil.drawLine(matrices, fx1, fy1, fx1 + (float)Math.cos(a - 0.5235987755982988) * branchLen, fy1 + (float)Math.sin(a - 0.5235987755982988) * branchLen, color);
            double a2 = a + Math.PI;
            float fx2 = x - dx * branchOffset;
            float fy2 = y - dy * branchOffset;
            Render2DUtil.drawLine(matrices, fx2, fy2, fx2 + (float)Math.cos(a2 + 0.5235987755982988) * branchLen, fy2 + (float)Math.sin(a2 + 0.5235987755982988) * branchLen, color);
            Render2DUtil.drawLine(matrices, fx2, fy2, fx2 + (float)Math.cos(a2 - 0.5235987755982988) * branchLen, fy2 + (float)Math.sin(a2 - 0.5235987755982988) * branchLen, color);
        }
    }

    private Snowflake spawnSnowflake(int w, int h, boolean randomY) {
        float x = this.snowRandom.nextInt(Math.max(1, w));
        float y = randomY ? (float)this.snowRandom.nextInt(Math.max(1, h)) : -5.0f - this.snowRandom.nextFloat() * 20.0f;
        float phase = this.snowRandom.nextFloat() * ((float)Math.PI * 2);
        float drift = 10.0f + this.snowRandom.nextFloat() * 40.0f;
        float speedMul = 0.55f + this.snowRandom.nextFloat() * 1.05f;
        float sizeMul = 0.6f + this.snowRandom.nextFloat() * 1.2f;
        return new Snowflake(x, y, phase, drift, speedMul, sizeMul);
    }

    private static final class Snowflake {
        private float x;
        private float y;
        private final float phase;
        private final float drift;
        private final float speedMul;
        private final float sizeMul;

        private Snowflake(float x, float y, float phase, float drift, float speedMul, float sizeMul) {
            this.x = x;
            this.y = y;
            this.phase = phase;
            this.drift = drift;
            this.speedMul = speedMul;
            this.sizeMul = sizeMul;
        }
    }
}

