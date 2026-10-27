/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Component
 */
package dev.aevum.api.interfaces;

import net.minecraft.Component;

public interface IChatHudHook {
    public void aevumClient$addMessage(Component var1, int var2);

    public void aevumClient$addMessage(Component var1);

    public void aevumClient$addMessageOutSync(Component var1, int var2);
}

