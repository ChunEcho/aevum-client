/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Items
 *  net.minecraft.GuiGraphics
 *  net.minecraft.PoseStack
 */
package dev.aevum.mod.modules.impl.hud;

import dev.aevum.Aevum;
import dev.aevum.api.utils.player.InventoryUtil;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.Items;
import net.minecraft.GuiGraphics;
import net.minecraft.PoseStack;

public class Totem
extends Module {
    public static Totem INSTANCE;
    public final BooleanSetting lockOnHotbar = this.add(new BooleanSetting("LockOnHotbar", true));
    public final SliderSetting posX = this.add(new SliderSetting("PosX", 0.0, 0.0, 1920.0, 1.0));
    public final SliderSetting posY = this.add(new SliderSetting("PosY", 150.0, 0.0, 1080.0, 1.0));
    private final int width = 16;
    private final int height = 16;

    public Totem() {
        super("Totem", Module.Category.HUD);
        this.setChinese("\u56fe\u817e");
        INSTANCE = this;
        this.posX.setValue(Aevum.CONFIG.getInt("totem_x", 0));
        this.posY.setValue(Aevum.CONFIG.getInt("totem_y", 150));
        this.posX.injectTask(() -> Aevum.CONFIG.set("totem_x", (int)this.posX.getValue()));
        this.posY.injectTask(() -> Aevum.CONFIG.set("totem_y", (int)this.posY.getValue()));
    }

    @Override
    public void onRender2D(GuiGraphics drawContext, float tickDelta) {
        int renderY;
        int renderX;
        if (Totem.mc.player == null) {
            return;
        }
        PoseStack matrixStack = drawContext.method_51448();
        if (this.lockOnHotbar.getValue()) {
            renderX = mc.getWindow().getScreenWidth() / 4 - 8;
            renderY = mc.getWindow().getGuiScaledHeight() - 16;
            renderY = Totem.mc.player.method_7337() ? renderY - 28 : renderY - 38;
        } else {
            renderX = (int)this.posX.getValue();
            renderY = (int)this.posY.getValue();
        }
        matrixStack.method_22903();
        int count = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        drawContext.method_51427(Items.TOTEM_OF_UNDYING.getDefaultInstance(), renderX + 16 - 16, renderY + 16 - 16);
        String countText = String.valueOf(count);
        int textX = renderX + 16 - 16 + 16 - Totem.mc.font.method_1727(countText) + 2;
        int textY = renderY + 16 - 16 + 16 - 9 + 2 + 2;
        drawContext.method_51433(Totem.mc.font, countText, textX, textY, new Color(255, 255, 255, 255).getRGB(), true);
        matrixStack.method_22909();
    }
}

