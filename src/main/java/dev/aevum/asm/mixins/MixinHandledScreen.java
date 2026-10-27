/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.AbstractContainerMenu
 *  net.minecraft.Slot
 *  net.minecraft.ItemStack
 *  net.minecraft.Component
 *  net.minecraft.GuiGraphics
 *  net.minecraft.MenuAccess
 *  net.minecraft.Screen
 *  net.minecraft.AbstractContainerScreen
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.handler.HandledScreenHandler;
import net.minecraft.AbstractContainerMenu;
import net.minecraft.Slot;
import net.minecraft.ItemStack;
import net.minecraft.Component;
import net.minecraft.GuiGraphics;
import net.minecraft.MenuAccess;
import net.minecraft.Screen;
import net.minecraft.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractContainerScreen.class})
public abstract class MixinHandledScreen<T extends AbstractContainerMenu>
extends Screen
implements MenuAccess<T> {
    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;

    protected MixinHandledScreen(Component title) {
        super(title);
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 2 && HandledScreenHandler.onMouseClicked(this.hoveredSlot, ITEMS, this.minecraft)) {
            cir.setReturnValue((Object)true);
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderHead(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (HandledScreenHandler.onRenderHead((Object)this)) {
            ci.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreenHandler.onRenderReturn(context, mouseX, mouseY, this.hoveredSlot, this.minecraft);
    }

    @Shadow
    public abstract void method_25420(GuiGraphics var1, int var2, int var3, float var4);
}

