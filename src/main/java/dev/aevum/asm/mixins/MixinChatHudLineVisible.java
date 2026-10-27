/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiMessage$Line
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.interfaces.IChatHudLineHook;
import dev.aevum.api.utils.math.FadeUtils;
import net.minecraft.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={GuiMessage.GuiMessage$Line.class})
public class MixinChatHudLineVisible
implements IChatHudLineHook {
    @Unique
    private int id = 0;
    @Unique
    private boolean sync = false;
    @Unique
    private FadeUtils fade;

    @Override
    public int aevumClient$getMessageId() {
        return this.id;
    }

    @Override
    public void aevumClient$setMessageId(int id) {
        this.id = id;
    }

    @Override
    public boolean aevumClient$getSync() {
        return this.sync;
    }

    @Override
    public void aevumClient$setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public FadeUtils aevumClient$getFade() {
        return this.fade;
    }

    @Override
    public void aevumClient$setFade(FadeUtils fade) {
        this.fade = fade;
    }
}

