package dev.chun.optimizations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class MotionShaderProgram {
   private int programId = -1;
   private int vao = -1;
   private int vbo = -1;
   private int uProjMat;
   private int uCurrentSampler;
   private int uPreviousSampler;

   public MotionShaderProgram() {
   }

   public void init() {
      if (this.programId == -1) {
         this.compile();
         this.createQuadMesh();
      }
   }

   private void compile() {
      int vert = this.compileShader(35633, "assets/forever/shaders/motion.vsh");
      int frag = this.compileShader(35632, "assets/forever/shaders/motion.fsh");
      this.programId = GL20.glCreateProgram();
      GL20.glAttachShader(this.programId, vert);
      GL20.glAttachShader(this.programId, frag);
      GL20.glBindAttribLocation(this.programId, 0, "Position");
      GL20.glBindAttribLocation(this.programId, 1, "UV0");
      GL20.glLinkProgram(this.programId);
      if (GL20.glGetProgrami(this.programId, 35714) == 0) {
         throw new RuntimeException("Failed to link motion shader: " + GL20.glGetProgramInfoLog(this.programId, 1024));
      } else {
         GL20.glDeleteShader(vert);
         GL20.glDeleteShader(frag);
         this.uProjMat = GL20.glGetUniformLocation(this.programId, "ProjMat");
         this.uCurrentSampler = GL20.glGetUniformLocation(this.programId, "CurrentSampler");
         this.uPreviousSampler = GL20.glGetUniformLocation(this.programId, "PreviousSampler");
      }
   }

   private void createQuadMesh() {
      this.vao = GL30.glGenVertexArrays();
      GL30.glBindVertexArray(this.vao);
      this.vbo = GL15.glGenBuffers();
      GL15.glBindBuffer(34962, this.vbo);
      FloatBuffer buf = MemoryUtil.memAllocFloat(20);
      buf.put(new float[20]).flip();
      GL15.glBufferData(34962, buf, 35048);
      MemoryUtil.memFree(buf);
      int stride = 20;
      GL20.glEnableVertexAttribArray(0);
      GL20.glVertexAttribPointer(0, 3, 5126, false, stride, 0L);
      GL20.glEnableVertexAttribArray(1);
      GL20.glVertexAttribPointer(1, 2, 5126, false, stride, 12L);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
   }

   public void render(int currentTex, int previousTex, int width, int height) {
      int prevProgram = GL11.glGetInteger(35725);
      int prevVao = GL11.glGetInteger(34229);
      int prevVbo = GL11.glGetInteger(34964);
      int prevActiveTexture = GL11.glGetInteger(34016);
      GL13.glActiveTexture(33984);
      int prevTex0 = GL11.glGetInteger(32873);
      GL13.glActiveTexture(33985);
      int prevTex1 = GL11.glGetInteger(32873);
      boolean prevBlend = GL11.glIsEnabled(3042);
      boolean prevDepthTest = GL11.glIsEnabled(2929);
      boolean prevCullFace = GL11.glIsEnabled(2884);
      GL11.glDisable(3042);
      GL11.glDisable(2929);
      GL11.glDisable(2884);
      float[] vertices = new float[]{
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         (float)width,
         0.0F,
         0.0F,
         1.0F,
         0.0F,
         (float)width,
         (float)height,
         0.0F,
         1.0F,
         1.0F,
         0.0F,
         (float)height,
         0.0F,
         0.0F,
         1.0F
      };
      GL15.glBindBuffer(34962, this.vbo);
      FloatBuffer buf = MemoryUtil.memAllocFloat(vertices.length);
      buf.put(vertices).flip();
      GL15.glBufferSubData(34962, 0L, buf);
      MemoryUtil.memFree(buf);
      GL20.glUseProgram(this.programId);
      GL20.glUniformMatrix4fv(this.uProjMat, false, ortho(0.0F, (float)width, (float)height, 0.0F, -1.0F, 1.0F));
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, currentTex);
      GL20.glUniform1i(this.uCurrentSampler, 0);
      GL13.glActiveTexture(33985);
      GL11.glBindTexture(3553, previousTex);
      GL20.glUniform1i(this.uPreviousSampler, 1);
      GL30.glBindVertexArray(this.vao);
      GL11.glDrawArrays(6, 0, 4);
      GL30.glBindVertexArray(prevVao);
      GL15.glBindBuffer(34962, prevVbo);
      GL13.glActiveTexture(33985);
      GL11.glBindTexture(3553, prevTex1);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, prevTex0);
      GL13.glActiveTexture(prevActiveTexture);
      GL20.glUseProgram(prevProgram);
      if (prevBlend) {
         GL11.glEnable(3042);
      } else {
         GL11.glDisable(3042);
      }

      if (prevDepthTest) {
         GL11.glEnable(2929);
      } else {
         GL11.glDisable(2929);
      }

      if (prevCullFace) {
         GL11.glEnable(2884);
      } else {
         GL11.glDisable(2884);
      }
   }

   public void destroy() {
      if (this.programId != -1) {
         GL20.glDeleteProgram(this.programId);
         this.programId = -1;
      }

      if (this.vao != -1) {
         GL30.glDeleteVertexArrays(this.vao);
         this.vao = -1;
      }

      if (this.vbo != -1) {
         GL15.glDeleteBuffers(this.vbo);
         this.vbo = -1;
      }
   }

   private int compileShader(int type, String resourcePath) {
      String source = this.readResource(resourcePath);
      int shader = GL20.glCreateShader(type);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, 35713) == 0) {
         throw new RuntimeException("Failed to compile " + resourcePath + ": " + GL20.glGetShaderInfoLog(shader, 1024));
      } else {
         return shader;
      }
   }

   private String readResource(String path) {
      try (InputStream is = MinecraftClient.class.getClassLoader().getResourceAsStream(path)) {
         if (is == null) {
            throw new java.io.FileNotFoundException("Resource not found: " + path);
         }
         return new String(is.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
         throw new RuntimeException("Failed to read shader: " + path, e);
      }
   }

   private static float[] ortho(float l, float r, float b, float t, float n, float f) {
      return new float[]{
         2.0F / (r - l),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         2.0F / (t - b),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         -2.0F / (f - n),
         0.0F,
         -(r + l) / (r - l),
         -(t + b) / (t - b),
         -(f + n) / (f - n),
         1.0F
      };
   }
}
