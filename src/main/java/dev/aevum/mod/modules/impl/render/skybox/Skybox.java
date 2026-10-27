/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Level
 *  org.joml.Matrix4f
 */
package dev.aevum.mod.modules.impl.render.skybox;

import dev.aevum.mod.modules.Module;
import dev.aevum.mod.modules.impl.render.skybox.CustomSkyRenderer;
import dev.aevum.mod.modules.settings.impl.BooleanSetting;
import dev.aevum.mod.modules.settings.impl.ColorSetting;
import dev.aevum.obf.zkm.NoFlowObfuscate;
import dev.aevum.obf.zkm.NoReferencesObfuscate;
import java.awt.Color;
import net.minecraft.Level;
import org.joml.Matrix4f;

@NoFlowObfuscate
@NoReferencesObfuscate
public class Skybox
extends Module {
    public static Skybox INSTANCE;
    public final ColorSetting horizonColor = this.add(new ColorSetting("Horizon", new Color(196, 79, 186)));
    public final ColorSetting nebula1Color = this.add(new ColorSetting("Nebula1", new Color(196, 79, 186)));
    public final ColorSetting nebula2Color = this.add(new ColorSetting("Nebula2", new Color(196, 79, 186)));
    public final ColorSetting uvStars1Color = this.add(new ColorSetting("UVStars1", new Color(196, 79, 186)));
    public final ColorSetting uvStars2Color = this.add(new ColorSetting("UVStars2", new Color(255, 255, 255)));
    public final ColorSetting stars1Color = this.add(new ColorSetting("Stars1", new Color(255, 255, 255)));
    public final ColorSetting stars2Color = this.add(new ColorSetting("Stars2", new Color(242, 163, 237)));
    public final BooleanSetting stars = this.add(new BooleanSetting("Stars", true));
    private static final CustomSkyRenderer skyRenderer;

    public Skybox() {
        super("Skybox", "Custom skybox", Module.Category.Render);
        INSTANCE = this;
    }

    public void renderSky(Matrix4f positionMatrix, Matrix4f projectionMatrix, float tickDelta) {
        if (Skybox.mc.level == null) {
            return;
        }
        skyRenderer.render((Level)Skybox.mc.level, tickDelta, positionMatrix, projectionMatrix);
    }

    static {
        skyRenderer = new CustomSkyRenderer();
    }
}

