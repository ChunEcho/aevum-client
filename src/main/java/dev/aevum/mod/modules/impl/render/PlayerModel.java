/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.LivingEntity
 *  net.minecraft.GuiGraphics
 *  net.minecraft.InventoryScreen
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package dev.aevum.mod.modules.impl.render;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import net.minecraft.LivingEntity;
import net.minecraft.GuiGraphics;
import net.minecraft.InventoryScreen;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerModel
extends Module {
    public static PlayerModel INSTANCE;
    public final SliderSetting x = this.add(new SliderSetting("X", 80.0, 0.0, 1920.0, 1.0));
    public final SliderSetting y = this.add(new SliderSetting("Y", 140.0, 0.0, 1080.0, 1.0));
    public final SliderSetting size = this.add(new SliderSetting("Size", 30.0, 10.0, 120.0, 1.0));
    public final BooleanSetting followMouse = this.add(new BooleanSetting("FollowMouse", false));
    public final BooleanSetting mirror = this.add(new BooleanSetting("Mirror", false));

    public PlayerModel() {
        super("PlayerModel", Module.Category.Client);
        this.setChinese("\u73a9\u5bb6\u6a21\u578b");
        INSTANCE = this;
    }

    @Override
    public void onRender2D(GuiGraphics DrawContext, float n) {
        float n6;
        if (PlayerModel.nullCheck()) {
            return;
        }
        float n2 = (float)this.size.getValue();
        float n3 = (float)this.x.getValue() + n2;
        float n4 = (float)this.y.getValue() + n2 * 2.0f;
        float n5 = this.followMouse.getValue() ? (float)((double)n3 - PlayerModel.mc.mouseHandler.method_1603()) : 0.0f;
        float f = n6 = this.followMouse.getValue() ? (float)((double)n4 - PlayerModel.mc.mouseHandler.method_1604()) : 0.0f;
        if (this.mirror.getValue()) {
            n5 = -n5;
        }
        InventoryScreen.method_48472((GuiGraphics)DrawContext, (float)n3, (float)n4, (float)n2, (Vector3f)new Vector3f(0.0f, PlayerModel.mc.player.getBbHeight() / 2.0f, 0.0f), (Quaternionf)new Quaternionf().rotateZ((float)Math.PI), (Quaternionf)new Quaternionf().rotateZ((float)Math.PI).rotateX(n6 * 0.01f).rotateY(n5 * 0.01f), (LivingEntity)PlayerModel.mc.player);
    }
}

