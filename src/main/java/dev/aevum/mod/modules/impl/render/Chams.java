/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Entity
 *  net.minecraft.Mob
 *  net.minecraft.Animal
 *  net.minecraft.EndCrystal
 *  net.minecraft.Slime
 *  net.minecraft.Villager
 *  net.minecraft.Player
 *  net.minecraft.WanderingTrader
 */
package dev.aevum.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aevum.api.events.eventbus.EventListener;
import dev.aevum.api.events.impl.Render3DEvent;
import dev.aevum.api.events.impl.UpdateEvent;
import dev.aevum.api.utils.render.CrystalRenderer;
import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.mod.modules.settings.impl.SliderSetting;
import java.awt.Color;
import net.minecraft.Entity;
import net.minecraft.Mob;
import net.minecraft.Animal;
import net.minecraft.EndCrystal;
import net.minecraft.Slime;
import net.minecraft.Villager;
import net.minecraft.Player;
import net.minecraft.WanderingTrader;

public class Chams
extends Module {
    public static Chams INSTANCE;
    public final BooleanSetting crystal = this.add(new BooleanSetting("Crystal", true).setParent());
    public final BooleanSetting cancelVanilla = this.add(new BooleanSetting("CancelVanilla", true, this.crystal::isOpen));
    public final BooleanSetting customRender = this.add(new BooleanSetting("CustomRender", true, this.crystal::isOpen).setParent());
    public final ColorSetting crystalOutline = this.add(new ColorSetting("Outline", new Color(255, 255, 255, 255), () -> this.crystal.isOpen() && this.customRender.isOpen()));
    public final ColorSetting crystalFill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 40), () -> this.crystal.isOpen() && this.customRender.isOpen()));
    public final SliderSetting crystalScale = this.add(new SliderSetting("Scale", 1.0, 0.1, 3.0, 0.01, () -> this.crystal.isOpen() && this.customRender.isOpen()));
    public final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 1.5, 0.5, 5.0, 0.1, () -> this.crystal.isOpen() && this.customRender.isOpen()));
    public final SliderSetting range = this.add(new SliderSetting("Range", 12.0, 0.0, 30.0, 1.0, () -> this.crystal.isOpen() && this.customRender.isOpen()));
    public final BooleanSetting throughWall = this.add(new BooleanSetting("ThroughWall", false).setParent());
    private final BooleanSetting Crystals = this.add(new BooleanSetting("Crystals", true, this.throughWall::isOpen));
    private final BooleanSetting Players = this.add(new BooleanSetting("Players", true, this.throughWall::isOpen));
    private final BooleanSetting Mobs = this.add(new BooleanSetting("Mobs", true, this.throughWall::isOpen));
    private final BooleanSetting Animals = this.add(new BooleanSetting("Animals", true, this.throughWall::isOpen));
    private final BooleanSetting Villagers = this.add(new BooleanSetting("Villagers", true, this.throughWall::isOpen));
    private final BooleanSetting Slimes = this.add(new BooleanSetting("Slimes", true, this.throughWall::isOpen));
    public final ColorSetting hand = this.add(new ColorSetting("Hand", -1).injectBoolean(true));
    public int age;

    public Chams() {
        super("Chams", Module.Category.Render);
        this.setChinese("\u6a21\u578b\u4e0a\u8272");
        INSTANCE = this;
    }

    public boolean shouldRenderCustomCrystal() {
        return this.isOn() && this.crystal.getValue() && this.customRender.getValue();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        ++this.age;
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if (this.hand.booleanValue) {
            this.applyHandColor();
        }
        if (this.shouldRenderCustomCrystal()) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            try {
                this.renderCustomCrystals(event);
            }
            finally {
                if (this.hand.booleanValue) {
                    this.applyHandColor();
                }
            }
        }
    }

    private void applyHandColor() {
        Color color = this.hand.getValue();
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
    }

    private void renderCustomCrystals(Render3DEvent event) {
        if (Chams.mc.level == null || Chams.mc.player == null) {
            return;
        }
        double rangeSq = this.range.getValue() * this.range.getValue();
        for (Entity entity : Chams.mc.level.method_18112()) {
            double d;
            EndCrystal crystal;
            if (!(entity instanceof EndCrystal) || !(crystal = (EndCrystal)entity).isAlive()) continue;
            double distSq = Chams.mc.player.method_5707(entity.position());
            if (d > rangeSq) continue;
            CrystalRenderer.renderCrystal(event, crystal, this);
        }
    }

    public boolean chams(Entity entity) {
        if (entity instanceof EndCrystal) {
            return this.Crystals.getValue();
        }
        if (entity instanceof Slime) {
            return this.Slimes.getValue();
        }
        if (entity instanceof Player) {
            return this.Players.getValue();
        }
        if (entity instanceof Villager || entity instanceof WanderingTrader) {
            return this.Villagers.getValue();
        }
        if (entity instanceof Animal) {
            return this.Animals.getValue();
        }
        return entity instanceof Mob ? this.Mobs.getValue() : false;
    }
}

