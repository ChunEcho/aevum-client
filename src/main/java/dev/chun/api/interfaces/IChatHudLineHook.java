/*
 * Decompiled with CFR 0.152.
 */
package dev.chun.api.interfaces;

import dev.chun.api.utils.math.FadeUtils;

public interface IChatHudLineHook {
    public int aevumClient$getMessageId();

    public void aevumClient$setMessageId(int var1);

    public boolean aevumClient$getSync();

    public void aevumClient$setSync(boolean var1);

    public FadeUtils aevumClient$getFade();

    public void aevumClient$setFade(FadeUtils var1);
}

