package femboy.monolith.ui;

/** Easing curves and a small spring for the click GUI animations. Time based, so frame rate doesn't change the feel. */
public final class Motion {
    private Motion() {
    }

    public static float clamp01(float t) {
        return t < 0.0F ? 0.0F : Math.min(1.0F, t);
    }

    public static float outCubic(float t) {
        t = clamp01(t);
        float u = 1.0F - t;
        return 1.0F - u * u * u;
    }

    /** Ease out with one soft overshoot past 1 before settling; {@code s} sets how far it overshoots. */
    public static float outBack(float t, float s) {
        t = clamp01(t);
        float u = t - 1.0F;
        return 1.0F + (s + 1.0F) * u * u * u + s * u * u;
    }

    /** Under-damped spring: follows its target with a small, quick overshoot ("pop"). */
    public static final class Spring {
        private static final float STIFFNESS = 420.0F;
        private static final float DAMPING = 24.0F;
        private float value;
        private float velocity;

        public float step(float target, float dt) {
            // A few sub-steps keep it stable on slow frames.
            int steps = Math.max(1, (int) Math.ceil(dt / 0.004F));
            float h = dt / steps;
            for (int i = 0; i < steps; i++) {
                float accel = STIFFNESS * (target - value) - DAMPING * velocity;
                velocity += accel * h;
                value += velocity * h;
            }
            if (Math.abs(target - value) < 0.0005F && Math.abs(velocity) < 0.0005F) {
                value = target;
                velocity = 0.0F;
            }
            return value;
        }
    }
}
