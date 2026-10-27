/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Window
 *  net.minecraft.RenderTarget
 *  net.minecraft.Minecraft
 *  net.minecraft.ConnectScreen
 *  net.minecraft.ReceivingLevelScreen
 *  net.minecraft.ProgressScreen
 *  net.minecraft.Screen
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL30
 */
package dev.aevum.optimizations;

import dev.aevum.mod.modules.impl.render.FrameInterp;
import dev.aevum.optimizations.MotionShaderProgram;
import java.nio.ByteBuffer;
import net.minecraft.Window;
import net.minecraft.RenderTarget;
import net.minecraft.Minecraft;
import net.minecraft.ConnectScreen;
import net.minecraft.ReceivingLevelScreen;
import net.minecraft.ProgressScreen;
import net.minecraft.Screen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FrameInterpolation {
    private static int lastFrameTex = -1;
    private static int lastFrameFbo = -1;
    private static int lastWidth;
    private static int lastHeight;
    private static final MotionShaderProgram shader;
    private static final int[] VIEWPORT_BUFFER;
    private static boolean initialized;

    public static void interp() {
        if (FrameInterp.INSTANCE == null || !FrameInterp.INSTANCE.isOn()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        Screen screen = client.screen;
        if (screen instanceof ProgressScreen || screen instanceof ReceivingLevelScreen || screen instanceof ConnectScreen) {
            return;
        }
        RenderTarget fb = client.getMainRenderTarget();
        Window window = client.getWindow();
        if (fb == null || window == null) {
            return;
        }
        int w = fb.width;
        int h = fb.height;
        if (!initialized) {
            shader.init();
            initialized = true;
        }
        if (lastFrameTex == -1 || w != lastWidth || h != lastHeight) {
            FrameInterpolation.init(w, h);
            FrameInterpolation.capture(fb, w, h);
            return;
        }
        long handle = window.getWindow();
        int prevFbo = GL11.glGetInteger((int)36006);
        GL11.glGetIntegerv((int)2978, (int[])VIEWPORT_BUFFER);
        GL30.glBindFramebuffer((int)36160, (int)0);
        GL11.glViewport((int)0, (int)0, (int)w, (int)h);
        shader.render(fb.method_30277(), lastFrameTex, w, h);
        GLFW.glfwSwapBuffers((long)handle);
        GL30.glBindFramebuffer((int)36008, (int)fb.frameBufferId);
        GL30.glBindFramebuffer((int)36009, (int)0);
        GL30.glBlitFramebuffer((int)0, (int)0, (int)w, (int)h, (int)0, (int)0, (int)w, (int)h, (int)16384, (int)9728);
        FrameInterpolation.capture(fb, w, h);
        GL30.glBindFramebuffer((int)36160, (int)prevFbo);
        GL11.glViewport((int)VIEWPORT_BUFFER[0], (int)VIEWPORT_BUFFER[1], (int)VIEWPORT_BUFFER[2], (int)VIEWPORT_BUFFER[3]);
    }

    private static void capture(RenderTarget fb, int w, int h) {
        int prevReadFbo = GL11.glGetInteger((int)36010);
        int prevDrawFbo = GL11.glGetInteger((int)36006);
        GL30.glBindFramebuffer((int)36008, (int)fb.frameBufferId);
        GL30.glBindFramebuffer((int)36009, (int)lastFrameFbo);
        GL30.glBlitFramebuffer((int)0, (int)0, (int)w, (int)h, (int)0, (int)0, (int)w, (int)h, (int)16384, (int)9728);
        GL30.glBindFramebuffer((int)36008, (int)prevReadFbo);
        GL30.glBindFramebuffer((int)36009, (int)prevDrawFbo);
        lastWidth = w;
        lastHeight = h;
    }

    private static void init(int w, int h) {
        if (lastFrameTex != -1) {
            GL11.glDeleteTextures((int)lastFrameTex);
        }
        if (lastFrameFbo != -1) {
            GL30.glDeleteFramebuffers((int)lastFrameFbo);
        }
        int prevTex = GL11.glGetInteger((int)32873);
        int prevFbo = GL11.glGetInteger((int)36006);
        lastFrameTex = GL11.glGenTextures();
        GL11.glBindTexture((int)3553, (int)lastFrameTex);
        GL11.glTexImage2D((int)3553, (int)0, (int)32856, (int)w, (int)h, (int)0, (int)6408, (int)5121, (ByteBuffer)null);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
        lastFrameFbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer((int)36160, (int)lastFrameFbo);
        GL30.glFramebufferTexture2D((int)36160, (int)36064, (int)3553, (int)lastFrameTex, (int)0);
        int status = GL30.glCheckFramebufferStatus((int)36160);
        if (status != 36053) {
            throw new RuntimeException("FrameInterpolation FBO incomplete: " + status);
        }
        GL30.glBindFramebuffer((int)36160, (int)prevFbo);
        GL11.glBindTexture((int)3553, (int)prevTex);
        lastWidth = w;
        lastHeight = h;
    }

    public static void cleanup() {
        if (lastFrameTex != -1) {
            GL11.glDeleteTextures((int)lastFrameTex);
            lastFrameTex = -1;
        }
        if (lastFrameFbo != -1) {
            GL30.glDeleteFramebuffers((int)lastFrameFbo);
            lastFrameFbo = -1;
        }
        shader.destroy();
        initialized = false;
    }

    static {
        shader = new MotionShaderProgram();
        VIEWPORT_BUFFER = new int[4];
        initialized = false;
    }
}

