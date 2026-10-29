package dev.chun.api.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ProjectionUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public ProjectionUtils() {
   }

   public static Vector3f project(Vec3d worldPos) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d camPos = camera.getPos();
      Vector3f target = new Vector3f((float)(worldPos.x - camPos.x), (float)(worldPos.y - camPos.y), (float)(worldPos.z - camPos.z));
      Quaternionf rotation = new Quaternionf(camera.getRotation());
      rotation.conjugate();
      target.rotate(rotation);
      double fov = (double)((Integer)mc.options.getFov().getValue()).intValue();
      float fovRad = (float)Math.toRadians(fov);
      float aspectRatio = (float)mc.getWindow().getFramebufferWidth() / (float)mc.getWindow().getFramebufferHeight();
      float near = 0.05F;
      float far = 1000.0F;
      float tanHalfFov = (float)Math.tan((double)(fovRad / 2.0F));
      float x = target.x / (tanHalfFov * aspectRatio);
      float y = target.y / tanHalfFov;
      float z = target.z;
      if (z >= 0.0F) {
         return null;
      } else {
         float halfWidth = (float)mc.getWindow().getScaledWidth() / 2.0F;
         float halfHeight = (float)mc.getWindow().getScaledHeight() / 2.0F;
         float screenX = halfWidth + x / -z * halfWidth;
         float screenY = halfHeight - y / -z * halfHeight;
         return new Vector3f(screenX, screenY, -z);
      }
   }
}
