package dev.chun.api.utils.math;

public class SmoothAnimationUtil {
   private double current;
   private double target;
   private double velocity;
   private final double smoothFactor;
   private final double snapThreshold;

   public SmoothAnimationUtil(double initialValue, double smoothFactor, double snapThreshold) {
      this.current = initialValue;
      this.target = initialValue;
      this.velocity = 0.0;
      this.smoothFactor = smoothFactor;
      this.snapThreshold = snapThreshold;
   }

   public SmoothAnimationUtil(double initialValue, double smoothFactor) {
      this(initialValue, smoothFactor, 0.01);
   }

   public SmoothAnimationUtil(double initialValue) {
      this(initialValue, 0.15, 0.01);
   }

   public SmoothAnimationUtil() {
      this(0.0, 0.15, 0.01);
   }

   public void setTarget(double target) {
      this.target = target;
   }

   public double getTarget() {
      return this.target;
   }

   public double update() {
      double diff = this.target - this.current;
      if (Math.abs(diff) < this.snapThreshold) {
         this.current = this.target;
         this.velocity = 0.0;
      } else {
         this.velocity = diff * this.smoothFactor;
         this.current = this.current + this.velocity;
      }

      return this.current;
   }

   public double update(double deltaTime) {
      double factor = 1.0 - Math.pow(1.0 - this.smoothFactor, deltaTime * 60.0);
      double diff = this.target - this.current;
      if (Math.abs(diff) < this.snapThreshold) {
         this.current = this.target;
         this.velocity = 0.0;
      } else {
         this.velocity = diff * factor;
         this.current = this.current + this.velocity;
      }

      return this.current;
   }

   public double get() {
      return this.current;
   }

   public float getFloat() {
      return (float)this.current;
   }

   public void set(double value) {
      this.current = value;
      this.target = value;
      this.velocity = 0.0;
   }

   public boolean isFinished() {
      return Math.abs(this.target - this.current) < this.snapThreshold;
   }

   public double getVelocity() {
      return this.velocity;
   }
}
