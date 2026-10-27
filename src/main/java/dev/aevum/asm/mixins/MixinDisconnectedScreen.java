/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  net.minecraft.Component
 *  net.minecraft.Minecraft
 *  net.minecraft.ConnectScreen
 *  net.minecraft.Button
 *  net.minecraft.Button$Builder
 *  net.minecraft.DisconnectedScreen
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerData
 *  net.minecraft.LayoutElement
 *  net.minecraft.LinearLayout
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.aevum.asm.mixins;

import dev.aevum.api.utils.Wrapper;
import dev.aevum.mod.modules.impl.misc.AutoReconnect;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.Component;
import net.minecraft.Minecraft;
import net.minecraft.ConnectScreen;
import net.minecraft.Button;
import net.minecraft.DisconnectedScreen;
import net.minecraft.Screen;
import net.minecraft.TitleScreen;
import net.minecraft.ServerAddress;
import net.minecraft.ServerData;
import net.minecraft.LayoutElement;
import net.minecraft.LinearLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DisconnectedScreen.class})
public abstract class MixinDisconnectedScreen
extends Screen {
    @Shadow
    @Final
    private LinearLayout layout;
    @Unique
    private Button reconnectBtn;
    @Unique
    private double time;

    protected MixinDisconnectedScreen(Component title) {
        super(title);
        this.time = AutoReconnect.INSTANCE.delay.getValue() * 20.0;
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift=At.Shift.BEFORE)})
    private void addButtons(CallbackInfo ci) {
        if (AutoReconnect.INSTANCE.lastServerConnection != null) {
            this.reconnectBtn = new Button.Button$Builder((Component)Component.method_43470((String)this.getText()), button -> this.tryConnecting()).method_46431();
            this.layout.method_52736((LayoutElement)this.reconnectBtn);
        }
    }

    public void method_25393() {
        if (!AutoReconnect.INSTANCE.rejoin() || AutoReconnect.INSTANCE.lastServerConnection == null) {
            return;
        }
        if (this.time <= 0.0) {
            this.tryConnecting();
        } else {
            this.time -= 1.0;
            if (this.reconnectBtn != null) {
                this.reconnectBtn.method_25355((Component)Component.method_43470((String)this.getText()));
            }
        }
    }

    @Unique
    private String getText() {
        Object reconnectText = "Reconnect";
        if (AutoReconnect.INSTANCE.rejoin()) {
            reconnectText = (String)reconnectText + " " + String.format("(%.1f)", this.time / 20.0);
        }
        return reconnectText;
    }

    @Unique
    private void tryConnecting() {
        Pair<ServerAddress, ServerData> lastServer = AutoReconnect.INSTANCE.lastServerConnection;
        ConnectScreen.method_36877((Screen)new TitleScreen(), (Minecraft)Wrapper.mc, (ServerAddress)((ServerAddress)lastServer.left()), (ServerData)((ServerData)lastServer.right()), (boolean)false, null);
    }
}

