package dev.chun.optimizations;

import dev.chun.mod.modules.impl.render.FrameInterp;
import java.nio.ByteBuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FrameInterpolation {
   private static int lastFrameTex = -1;
   private static int lastFrameFbo = -1;
   private static int lastWidth;
   private static int lastHeight;
   private static final MotionShaderProgram shader = new MotionShaderProgram();
   private static final int[] VIEWPORT_BUFFER = new int[4];
   private static boolean initialized = false;

   public FrameInterpolation() {
   }

   public static void interp() {
      if (FrameInterp.INSTANCE != null && FrameInterp.INSTANCE.isOn()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            Screen screen = client.currentScreen;
            if (!(screen instanceof ProgressScreen) && !(screen instanceof DownloadingTerrainScreen) && !(screen instanceof ConnectScreen)) {
               Framebuffer fb = client.getFramebuffer();
               Window window = client.getWindow();
               if (fb != null && window != null) {
                  int w = fb.textureWidth;
                  int h = fb.textureHeight;
                  if (!initialized) {
                     shader.init();
                     initialized = true;
                  }

                  if (lastFrameTex != -1 && w == lastWidth && h == lastHeight) {
                     long handle = window.getHandle();
                     int prevFbo = GL11.glGetInteger(36006);
                     GL11.glGetIntegerv(2978, VIEWPORT_BUFFER);
                     GL30.glBindFramebuffer(36160, 0);
                     GL11.glViewport(0, 0, w, h);
                     shader.render(fb.getColorAttachment(), lastFrameTex, w, h);
                     GLFW.glfwSwapBuffers(handle);
                     GL30.glBindFramebuffer(36008, fb.fbo);
                     GL30.glBindFramebuffer(36009, 0);
                     GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
                     capture(fb, w, h);
                     GL30.glBindFramebuffer(36160, prevFbo);
                     GL11.glViewport(VIEWPORT_BUFFER[0], VIEWPORT_BUFFER[1], VIEWPORT_BUFFER[2], VIEWPORT_BUFFER[3]);
                  } else {
                     init(w, h);
                     capture(fb, w, h);
                  }
               }
            }
         }
      }
   }

   private static void capture(Framebuffer fb, int w, int h) {
      int prevReadFbo = GL11.glGetInteger(36010);
      int prevDrawFbo = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36008, fb.fbo);
      GL30.glBindFramebuffer(36009, lastFrameFbo);
      GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
      GL30.glBindFramebuffer(36008, prevReadFbo);
      GL30.glBindFramebuffer(36009, prevDrawFbo);
      lastWidth = w;
      lastHeight = h;
   }

   private static void init(int w, int h) {
      if (lastFrameTex != -1) {
         GL11.glDeleteTextures(lastFrameTex);
      }

      if (lastFrameFbo != -1) {
         GL30.glDeleteFramebuffers(lastFrameFbo);
      }

      int prevTex = GL11.glGetInteger(32873);
      int prevFbo = GL11.glGetInteger(36006);
      lastFrameTex = GL11.glGenTextures();
      GL11.glBindTexture(3553, lastFrameTex);
      GL11.glTexImage2D(3553, 0, 32856, w, h, 0, 6408, 5121, (ByteBuffer)null);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      GL11.glTexParameteri(3553, 10242, 33071);
      GL11.glTexParameteri(3553, 10243, 33071);
      lastFrameFbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, lastFrameFbo);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, lastFrameTex, 0);
      int status = GL30.glCheckFramebufferStatus(36160);
      if (status != 36053) {
         throw new RuntimeException("FrameInterpolation FBO incomplete: " + status);
      } else {
         GL30.glBindFramebuffer(36160, prevFbo);
         GL11.glBindTexture(3553, prevTex);
         lastWidth = w;
         lastHeight = h;
      }
   }

   public static void cleanup() {
      if (lastFrameTex != -1) {
         GL11.glDeleteTextures(lastFrameTex);
         lastFrameTex = -1;
      }

      if (lastFrameFbo != -1) {
         GL30.glDeleteFramebuffers(lastFrameFbo);
         lastFrameFbo = -1;
      }

      shader.destroy();
      initialized = false;
   }
}
