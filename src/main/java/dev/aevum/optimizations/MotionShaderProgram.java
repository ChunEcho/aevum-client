/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Minecraft
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.system.MemoryUtil
 */
package dev.aevum.optimizations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import net.minecraft.Minecraft;
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

    public void init() {
        if (this.programId != -1) {
            return;
        }
        this.compile();
        this.createQuadMesh();
    }

    private void compile() {
        int vert = this.compileShader(35633, "assets/forever/shaders/motion.vsh");
        int frag = this.compileShader(35632, "assets/forever/shaders/motion.fsh");
        this.programId = GL20.glCreateProgram();
        GL20.glAttachShader((int)this.programId, (int)vert);
        GL20.glAttachShader((int)this.programId, (int)frag);
        GL20.glBindAttribLocation((int)this.programId, (int)0, (CharSequence)"Position");
        GL20.glBindAttribLocation((int)this.programId, (int)1, (CharSequence)"UV0");
        GL20.glLinkProgram((int)this.programId);
        if (GL20.glGetProgrami((int)this.programId, (int)35714) == 0) {
            throw new RuntimeException("Failed to link motion shader: " + GL20.glGetProgramInfoLog((int)this.programId, (int)1024));
        }
        GL20.glDeleteShader((int)vert);
        GL20.glDeleteShader((int)frag);
        this.uProjMat = GL20.glGetUniformLocation((int)this.programId, (CharSequence)"ProjMat");
        this.uCurrentSampler = GL20.glGetUniformLocation((int)this.programId, (CharSequence)"CurrentSampler");
        this.uPreviousSampler = GL20.glGetUniformLocation((int)this.programId, (CharSequence)"PreviousSampler");
    }

    private void createQuadMesh() {
        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray((int)this.vao);
        this.vbo = GL15.glGenBuffers();
        GL15.glBindBuffer((int)34962, (int)this.vbo);
        FloatBuffer buf = MemoryUtil.memAllocFloat((int)20);
        buf.put(new float[20]).flip();
        GL15.glBufferData((int)34962, (FloatBuffer)buf, (int)35048);
        MemoryUtil.memFree((Buffer)buf);
        int stride = 20;
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)3, (int)5126, (boolean)false, (int)stride, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)stride, (long)12L);
        GL30.glBindVertexArray((int)0);
        GL15.glBindBuffer((int)34962, (int)0);
    }

    public void render(int currentTex, int previousTex, int width, int height) {
        int prevProgram = GL11.glGetInteger((int)35725);
        int prevVao = GL11.glGetInteger((int)34229);
        int prevVbo = GL11.glGetInteger((int)34964);
        int prevActiveTexture = GL11.glGetInteger((int)34016);
        GL13.glActiveTexture((int)33984);
        int prevTex0 = GL11.glGetInteger((int)32873);
        GL13.glActiveTexture((int)33985);
        int prevTex1 = GL11.glGetInteger((int)32873);
        boolean prevBlend = GL11.glIsEnabled((int)3042);
        boolean prevDepthTest = GL11.glIsEnabled((int)2929);
        boolean prevCullFace = GL11.glIsEnabled((int)2884);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2884);
        float[] vertices = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, width, 0.0f, 0.0f, 1.0f, 0.0f, width, height, 0.0f, 1.0f, 1.0f, 0.0f, height, 0.0f, 0.0f, 1.0f};
        GL15.glBindBuffer((int)34962, (int)this.vbo);
        FloatBuffer buf = MemoryUtil.memAllocFloat((int)vertices.length);
        buf.put(vertices).flip();
        GL15.glBufferSubData((int)34962, (long)0L, (FloatBuffer)buf);
        MemoryUtil.memFree((Buffer)buf);
        GL20.glUseProgram((int)this.programId);
        GL20.glUniformMatrix4fv((int)this.uProjMat, (boolean)false, (float[])MotionShaderProgram.ortho(0.0f, width, height, 0.0f, -1.0f, 1.0f));
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)currentTex);
        GL20.glUniform1i((int)this.uCurrentSampler, (int)0);
        GL13.glActiveTexture((int)33985);
        GL11.glBindTexture((int)3553, (int)previousTex);
        GL20.glUniform1i((int)this.uPreviousSampler, (int)1);
        GL30.glBindVertexArray((int)this.vao);
        GL11.glDrawArrays((int)6, (int)0, (int)4);
        GL30.glBindVertexArray((int)prevVao);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL13.glActiveTexture((int)33985);
        GL11.glBindTexture((int)3553, (int)prevTex1);
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)prevTex0);
        GL13.glActiveTexture((int)prevActiveTexture);
        GL20.glUseProgram((int)prevProgram);
        if (prevBlend) {
            GL11.glEnable((int)3042);
        } else {
            GL11.glDisable((int)3042);
        }
        if (prevDepthTest) {
            GL11.glEnable((int)2929);
        } else {
            GL11.glDisable((int)2929);
        }
        if (prevCullFace) {
            GL11.glEnable((int)2884);
        } else {
            GL11.glDisable((int)2884);
        }
    }

    public void destroy() {
        if (this.programId != -1) {
            GL20.glDeleteProgram((int)this.programId);
            this.programId = -1;
        }
        if (this.vao != -1) {
            GL30.glDeleteVertexArrays((int)this.vao);
            this.vao = -1;
        }
        if (this.vbo != -1) {
            GL15.glDeleteBuffers((int)this.vbo);
            this.vbo = -1;
        }
    }

    private int compileShader(int type, String resourcePath) {
        String source = this.readResource(resourcePath);
        int shader = GL20.glCreateShader((int)type);
        GL20.glShaderSource((int)shader, (CharSequence)source);
        GL20.glCompileShader((int)shader);
        if (GL20.glGetShaderi((int)shader, (int)35713) == 0) {
            throw new RuntimeException("Failed to compile " + resourcePath + ": " + GL20.glGetShaderInfoLog((int)shader, (int)1024));
        }
        return shader;
    }

    private String readResource(String path) {
        String string;
        block11: {
            InputStream is;
            block10: {
                is = Minecraft.class.getClassLoader().getResourceAsStream(path);
                try {
                    if (is == null) {
                        throw new IOException("Resource not found: " + path);
                    }
                    string = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    if (is != null) break block10;
                    break block11;
                }
                catch (Throwable throwable) {
                    try {
                        if (is != null) {
                            try {
                                is.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Failed to read shader: " + path, e);
                    }
                }
            }
            try {
                is.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return string;
    }

    private static float[] ortho(float l, float r, float b, float t, float n, float f) {
        return new float[]{2.0f / (r - l), 0.0f, 0.0f, 0.0f, 0.0f, 2.0f / (t - b), 0.0f, 0.0f, 0.0f, 0.0f, -2.0f / (f - n), 0.0f, -(r + l) / (r - l), -(t + b) / (t - b), -(f + n) / (f - n), 1.0f};
    }
}

