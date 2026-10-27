/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.Player
 *  net.minecraft.ItemStack
 *  net.minecraft.Minecraft
 *  net.minecraft.Gui
 *  net.minecraft.GuiGraphics
 *  net.minecraft.FluidTags
 *  net.minecraft.DeltaTracker
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.Aevum;
import dev.aevum.api.events.impl.PreRender2DEvent;
import dev.aevum.api.utils.player.EntityUtil;
import dev.aevum.api.utils.render.ColorUtil;
import dev.aevum.api.utils.render.TextUtil;
import dev.aevum.core.impl.FontManager;
import dev.aevum.mod.modules.impl.client.ClientSetting;
import dev.aevum.mod.modules.impl.client.HUD;
import dev.aevum.mod.modules.impl.render.Crosshair;
import dev.aevum.mod.modules.impl.render.NoRender;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.LivingEntity;
import net.minecraft.Player;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.Gui;
import net.minecraft.GuiGraphics;
import net.minecraft.FluidTags;
import net.minecraft.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public abstract class MixinInGameHud {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Unique
    final Color minColor = new Color(196, 0, 0);
    @Unique
    final Color maxColor = new Color(0, 227, 0);

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderPortalOverlay(GuiGraphics context, float nauseaStrength, CallbackInfo ci) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.portal.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderStatusEffectOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderStatusEffectOverlay(CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.potionsIcon.getValue()) {
            info.cancel();
        }
    }

    @Inject(method={"renderMainHud"}, at={@At(value="TAIL")})
    private void onRenderMainHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Player player;
        if (HUD.INSTANCE.isOn() && HUD.INSTANCE.armor.getValue() && (player = this.method_1737()) != null) {
            int t;
            int x = context.method_51421() / 2 + 91;
            int y = context.method_51443() - 28 - HUD.INSTANCE.armorOffset.getValueInt();
            if (this.minecraft.gameMode.method_2908()) {
                y -= 16;
            }
            if ((t = this.method_1744(this.method_1734())) == 0) {
                y -= 10;
            }
            int maxAir = player.getMaxAirSupply();
            int air = Math.min(player.getAirSupply(), maxAir);
            if (player.method_5777(FluidTags.WATER) || air < maxAir) {
                int w = this.method_1733(t) - 1;
                y += w * 10;
            }
            for (ItemStack armor : this.minecraft.player.getInventory().armor) {
                x -= 20;
                if (armor.method_7960()) continue;
                context.method_51448().method_22903();
                int damage = EntityUtil.getDamagePercent(armor);
                context.method_51427(armor, x, y);
                context.method_51431(this.minecraft.font, armor, x, y);
                if (HUD.INSTANCE.durability.getValue()) {
                    if (HUD.INSTANCE.font.getValue()) {
                        FontManager.drawSmall(context.method_51448(), damage + "%", x + 1, (float)y - FontManager.small.getFontHeight() / 2.0f, ColorUtil.fadeColor(this.minColor, this.maxColor, (float)damage / 100.0f).getRGB(), HUD.INSTANCE.shadow.getValue());
                    } else {
                        String string = damage + "%";
                        float f = x + 2;
                        float f2 = y;
                        Objects.requireNonNull(this.minecraft.font);
                        TextUtil.drawStringScale(context, string, f, f2 - 2.25f, ColorUtil.fadeColor(this.minColor, this.maxColor, (float)damage / 100.0f).getRGB(), 0.5f, HUD.INSTANCE.shadow.getValue());
                    }
                }
                context.method_51448().method_22909();
            }
        }
    }

    @Shadow
    private int method_1733(int t) {
        return 0;
    }

    @Shadow
    private int method_1744(LivingEntity livingEntity) {
        return 0;
    }

    @Shadow
    private Player method_1737() {
        return null;
    }

    @Shadow
    private LivingEntity method_1734() {
        return null;
    }

    @Shadow
    public abstract void method_1753(GuiGraphics var1, DeltaTracker var2);

    @Inject(at={@At(value="HEAD")}, method={"render"})
    public void renderStart(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Aevum.EVENT_BUS.post(PreRender2DEvent.get(context));
    }

    @Inject(at={@At(value="TAIL")}, method={"render"})
    public void renderHook(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Aevum.MODULE.onRender2D(context);
    }

    @Inject(method={"clear"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V")}, cancellable=true)
    private void onClear(CallbackInfo info) {
        if (ClientSetting.INSTANCE.isOn() && ClientSetting.INSTANCE.keepHistory.getValue()) {
            info.cancel();
        }
    }

    @ModifyArg(method={"renderHotbar"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal=1), index=1)
    private int selectedSlotX(int x) {
        if (ClientSetting.INSTANCE.hotbar()) {
            double hotbarX = ClientSetting.animation.get(x, ClientSetting.INSTANCE.hotbarTime.getValueInt(), ClientSetting.INSTANCE.animEase.getValue());
            return (int)hotbarX;
        }
        return x;
    }

    @Inject(method={"renderCrosshair"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderCrosshairBegin(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (Crosshair.INSTANCE.isOn()) {
            Crosshair.INSTANCE.draw(context);
            ci.cancel();
        }
    }
}

