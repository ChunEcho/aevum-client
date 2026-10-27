/*
 * Decompiled with CFR 0.152.
 */
package dev.aevum.api.interfaces;

import dev.aevum.api.utils.math.FadeUtils;

public interface IChatHudLineHook {
    public int aevumClient$getMessageId();

    public void aevumClient$setMessageId(int var1);

    public boolean aevumClient$getSync();

    public void aevumClient$setSync(boolean var1);

    public FadeUtils aevumClient$getFade();

    public void aevumClient$setFade(FadeUtils var1);
}

