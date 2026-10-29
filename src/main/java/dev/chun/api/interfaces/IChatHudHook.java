/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package dev.chun.api.interfaces;

import net.minecraft.text.Text;

public interface IChatHudHook {
    public void aevumClient$addMessage(Text var1, int var2);

    public void aevumClient$addMessage(Text var1);

    public void aevumClient$addMessageOutSync(Text var1, int var2);
}
