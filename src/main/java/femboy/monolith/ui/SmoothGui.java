package femboy.monolith.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class SmoothGui {
    private static GuiGraphics graphics;
    private static Font font;

    private SmoothGui() {
    }

    private static String fontFamily = "Helvetica Neue";
    private static float alpha = 1.0F;

    /** Fade applied to everything drawn through SmoothGui (and text/icon blits), 0..1. */
    public static void alpha(float value) {
        alpha = Motion.clamp01(value);
    }

    public static float alpha() {
        return alpha;
    }

    /** White tint carrying the current fade, for textured blits. */
    public static int tint() {
        return (Math.round(alpha * 255.0F) << 24) | 0x00FFFFFF;
    }

    private static int fade(int argb) {
        if (alpha >= 1.0F) {
            return argb;
        }
        int a = Math.round((argb >>> 24) * alpha);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static void font(String name) {
        if (name != null && !name.isBlank()) {
            fontFamily = name;
        }
    }

    public static String font() {
        return fontFamily;
    }

    public static void begin(GuiGraphics g, Font uiFont) {
        graphics = g;
        font = uiFont;
    }

    public static GuiGraphics graphics() {
        return graphics;
    }

    public static void begin(int x, int y, int w, int h) {
    }

    public static void begin(int x, int y, int w, int h, int supersample) {
    }

    public static void gradient(int x, int y, int w, int h, int r, int left, int right) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.gradient(graphics, x, y, w, h, Math.max(1, r), fade(left), fade(right));
        }
    }

    public static void wash(int x, int y, int w, int h, int r, int color) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.wash(graphics, x, y, w, h, Math.max(1, r), fade(color));
        }
    }

    public static void flat(int x, int y, int w, int h, int r, int color) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.flat(graphics, x, y, w, h, Math.max(1, r), fade(color));
        }
    }

    public static void fill(int x, int y, int w, int h, int r, int color) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.box(graphics, x, y, w, h, Math.max(1, r), fade(color));
        }
    }

    /** Liquid glass pane; {@code frost} for the frosted, slightly more solid variant. */
    public static void glass(int x, int y, int w, int h, int r, int color, boolean frost) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.glass(graphics, x, y, w, h, Math.max(1, r), fade(color), frost);
        }
    }

    /** Soft drop shadow around a rounded rect (drawn outside it only). */
    public static void shadow(int x, int y, int w, int h, int r, int color) {
        if (graphics != null && w > 0 && h > 0) {
            GlassDraw.shadow(graphics, x, y, w, h, Math.max(1, r), fade(color));
        }
    }

    public static void stroke(int x, int y, int w, int h, int r, float widthPx, int color) {
        if (graphics != null && w > 1 && h > 1) {
            GlassDraw.stroke(graphics, x, y, w, h, Math.max(1, r), fade(color));
        }
    }

    public static void mark(int x, int y, int color) {
        if (graphics == null) {
            return;
        }
        GlassDraw.flat(graphics, x + 8, y + 2, 14, 10, 2, fade(color));
        GlassDraw.flat(graphics, x + 6, y + 12, 18, 16, 2, fade(color));
    }

    public static void icon(int x, int y, int color) {
        if (graphics != null) {
            GlassDraw.flat(graphics, x, y + 3, 12, 2, 1, fade(color));
            GlassDraw.flat(graphics, x, y + 8, 8, 2, 1, fade(color));
        }
    }

    public static void circle(int x, int y, int d, int color) {
        if (graphics != null && d > 0) {
            GlassDraw.circle(graphics, x, y, d, fade(color));
        }
    }

    public static void text(String value, int x, int y, int size, int color) {
        if (graphics == null || value == null || value.isEmpty()) {
            return;
        }
        TextCache.draw(graphics, value, x, y, size, color);
    }

    public static int width(String value, int size) {
        return TextCache.width(value, size);
    }

    public static void clip(int x, int y, int w, int h) {
        if (graphics != null) {
            graphics.enableScissor(x, y, x + w, y + h);
        }
    }

    public static void unclip() {
        if (graphics != null) {
            graphics.disableScissor();
        }
    }

    public static void again(GuiGraphics g) {
    }

    public static void end(GuiGraphics g) {
        graphics = null;
        alpha = 1.0F;
    }
}
