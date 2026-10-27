/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.GuiMessage
 *  net.minecraft.GuiMessage$Line
 *  net.minecraft.ChatComponent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package dev.aevum.asm.accessors;

import java.util.List;
import net.minecraft.GuiMessage;
import net.minecraft.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ChatComponent.class})
public interface IChatHud {
    @Mutable
    @Accessor(value="visibleMessages")
    public void setVisibleMessages(List<GuiMessage.GuiMessage$Line> var1);

    @Mutable
    @Accessor(value="messages")
    public void setMessages(List<GuiMessage> var1);
}

