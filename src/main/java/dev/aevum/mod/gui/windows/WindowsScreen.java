/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Component
 *  net.minecraft.GuiGraphics
 *  net.minecraft.Screen
 */
package dev.aevum.mod.gui.windows;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.api.utils.render.Render2DUtil;
import dev.aevum.mod.gui.ClickGuiScreen;
import dev.aevum.mod.gui.windows.WindowBase;
import dev.aevum.mod.modules.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.Component;
import net.minecraft.GuiGraphics;
import net.minecraft.Screen;

public class WindowsScreen
extends Screen {
    public static WindowBase lastClickedWindow;
    public static WindowBase draggingWindow;
    private List<WindowBase> windows = new ArrayList<WindowBase>();

    public WindowsScreen(WindowBase ... windows) {
        super(Component.method_30163((String)"CustomWindows"));
        this.windows.clear();
        lastClickedWindow = null;
        this.windows = Arrays.stream(windows).toList();
    }

    public void method_25394(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (Module.nullCheck()) {
            this.method_25420(context, mouseX, mouseY, delta);
        }
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> {
            if (w != lastClickedWindow) {
                w.render(context, mouseX, mouseY);
            }
        });
        if (lastClickedWindow != null && lastClickedWindow.isVisible()) {
            lastClickedWindow.render(context, mouseX, mouseY);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.windows.forEach(w -> w.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean method_25402(double mouseX, double mouseY, int button) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseClicked(mouseX, mouseY, button));
        int i = Wrapper.mc.getWindow().getGuiScaledWidth() / 2;
        float offset = (float)this.windows.size() * 20.0f / -2.0f - 23.0f;
        if (Render2DUtil.isHovered(mouseX, mouseY, (float)i + offset + 1.0f, Wrapper.mc.getWindow().getGuiScaledHeight() - 23, 15.0, 15.0)) {
            Wrapper.mc.method_1507((Screen)ClickGuiScreen.getInstance());
        }
        offset += 23.0f;
        for (WindowBase w2 : this.windows) {
            if (Render2DUtil.isHovered(mouseX, mouseY, (float)i + offset, Wrapper.mc.getWindow().getGuiScaledHeight() - 24, 17.0, 17.0)) {
                w2.setVisible(!w2.isVisible());
            }
            offset += 20.0f;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.keyPressed(keyCode, scanCode, modifiers));
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    public boolean method_25400(char key, int keyCode) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.charTyped(key, keyCode));
        return super.method_25400(key, keyCode);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseScrolled((int)(verticalAmount * 5.0)));
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

