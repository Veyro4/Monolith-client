package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;


public final class GlassDraw {
    private static final int SIZE = 256;
    private static final int CORNER = 64;
    private static DynamicTexture mask;
    private static DynamicTexture ring;
    private static DynamicTexture disc;
    private static boolean failed;

    private GlassDraw() {
    }

    public static void gradient(GuiGraphics g, int x, int y, int w, int h, int r, int left, int right) {
        if (!LiquidGlass.gradient(g, x, y, w, h, r, left, right)) {
            paintGradient(g, x, y, w, h, r, left, right);
        }
    }

    public static void wash(GuiGraphics g, int x, int y, int w, int h, int r, int argb) {
        if (!LiquidGlass.wash(g, x, y, w, h, r, argb)) {
            paint(g, mask(), x, y, w, h, r, argb, false);
        }
    }

    public static void box(GuiGraphics g, int x, int y, int w, int h, int r, int argb) {
        if (LiquidGlass.draw(g, x, y, w, h, r, argb, false)) {
            return;
        }
        paint(g, mask(), x, y, w, h, r, argb, false);
    }

    public static void stroke(GuiGraphics g, int x, int y, int w, int h, int r, int argb) {
        if (LiquidGlass.draw(g, x, y, w, h, r, argb, true)) {
            return;
        }
        paint(g, ring(), x, y, w, h, r, argb, true);
    }

    public static void glass(GuiGraphics g, int x, int y, int w, int h, int r, int argb, boolean frost) {
        if (!LiquidGlass.glass(g, x, y, w, h, r, argb, frost)) {
            paint(g, mask(), x, y, w, h, r, argb, false);
        }
    }

    public static void shadow(GuiGraphics g, int x, int y, int w, int h, int r, int argb) {
        // No texture fallback: without the shader the panels just go without a shadow.
        LiquidGlass.shadow(g, x, y, w, h, r, argb);
    }

    public static void flat(GuiGraphics g, int x, int y, int w, int h, int r, int argb) {
        if (!LiquidGlass.flat(g, x, y, w, h, r, argb)) {
            paint(g, mask(), x, y, w, h, r, argb, false);
        }
    }

    public static void circle(GuiGraphics g, int x, int y, int d, int argb) {
        if (d <= 0) {
            return;
        }
        if (LiquidGlass.flat(g, x, y, d, d, d, argb)) {
            return;
        }
        if (failed) {
            return;
        }
        DynamicTexture texture = disc();
        tint(g, texture.getTextureView(), sampler(), x, y, x + d, y + d, 0.0F, 1.0F, 0.0F, 1.0F, argb);
    }

    private static void paint(GuiGraphics g, DynamicTexture texture, int x, int y, int w, int h, int r, int argb, boolean ring) {
        if (w <= 1 || h <= 1 || failed) {
            return;
        }
        int c = Math.max(2, Math.min(r, Math.min(w, h) / 2));
        float u = CORNER / (float) SIZE;
        GpuTextureView view = texture.getTextureView();
        GpuSampler sampler = sampler();
        tint(g, view, sampler, x, y, x + c, y + c, 0.0F, u, 0.0F, u, argb);
        tint(g, view, sampler, x + c, y, x + w - c, y + c, u, 1.0F - u, 0.0F, u, argb);
        tint(g, view, sampler, x + w - c, y, x + w, y + c, 1.0F - u, 1.0F, 0.0F, u, argb);
        tint(g, view, sampler, x, y + c, x + c, y + h - c, 0.0F, u, u, 1.0F - u, argb);
        if (!ring) {
            tint(g, view, sampler, x + c, y + c, x + w - c, y + h - c, u, 1.0F - u, u, 1.0F - u, argb);
        }
        tint(g, view, sampler, x + w - c, y + c, x + w, y + h - c, 1.0F - u, 1.0F, u, 1.0F - u, argb);
        tint(g, view, sampler, x, y + h - c, x + c, y + h, 0.0F, u, 1.0F - u, 1.0F, argb);
        tint(g, view, sampler, x + c, y + h - c, x + w - c, y + h, u, 1.0F - u, 1.0F - u, 1.0F, argb);
        tint(g, view, sampler, x + w - c, y + h - c, x + w, y + h, 1.0F - u, 1.0F, 1.0F - u, 1.0F, argb);
    }

    private static void paintGradient(GuiGraphics g, int x, int y, int w, int h, int r, int left, int right) {
        if (w <= 1 || h <= 1 || failed) {
            return;
        }
        int c = Math.max(2, Math.min(r, Math.min(w, h) / 2));
        float u = CORNER / (float) SIZE;
        GpuTextureView view = mask().getTextureView();
        GpuSampler sampler = sampler();
        tint(g, view, sampler, x, y, x + c, y + c, 0.0F, u, 0.0F, u, left);
        tint(g, view, sampler, x, y + c, x + c, y + h - c, 0.0F, u, u, 1.0F - u, left);
        tint(g, view, sampler, x, y + h - c, x + c, y + h, 0.0F, u, 1.0F - u, 1.0F, left);
        int span = w - 2 * c;
        int strips = Math.max(1, Math.min(24, span / 4));
        for (int i = 0; i < strips; i++) {
            int x0 = x + c + span * i / strips;
            int x1 = x + c + span * (i + 1) / strips;
            int color = lerp(left, right, (i + 0.5F) / strips);
            tint(g, view, sampler, x0, y, x1, y + c, u, 1.0F - u, 0.0F, u, color);
            tint(g, view, sampler, x0, y + c, x1, y + h - c, u, 1.0F - u, u, 1.0F - u, color);
            tint(g, view, sampler, x0, y + h - c, x1, y + h, u, 1.0F - u, 1.0F - u, 1.0F, color);
        }
        tint(g, view, sampler, x + w - c, y, x + w, y + c, 1.0F - u, 1.0F, 0.0F, u, right);
        tint(g, view, sampler, x + w - c, y + c, x + w, y + h - c, 1.0F - u, 1.0F, u, 1.0F - u, right);
        tint(g, view, sampler, x + w - c, y + h - c, x + w, y + h, 1.0F - u, 1.0F, 1.0F - u, 1.0F, right);
    }

    private static int lerp(int a, int b, float t) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = (a >>> shift) & 255;
            int cb = (b >>> shift) & 255;
            out |= Math.round(ca + (cb - ca) * t) << shift;
        }
        return out;
    }

    private static void tint(GuiGraphics g, GpuTextureView view, GpuSampler sampler, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        Blit.draw(g, RenderPipelines.GUI_TEXTURED, view, sampler, x0, y0, x1, y1, u0, u1, v0, v1, argb);
    }

    private static GpuSampler sampler() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    }

    private static DynamicTexture mask() {
        if (mask == null) {
            mask = bake(false);
        }
        return mask;
    }

    private static DynamicTexture ring() {
        if (ring == null) {
            ring = bake(true);
        }
        return ring;
    }

    private static DynamicTexture disc() {
        if (disc == null) {
            NativeImage image = new NativeImage(SIZE, SIZE, false);
            float cx = SIZE / 2.0F;
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    float d = (float) Math.hypot(x + 0.5F - cx, y + 0.5F - cx) - (cx - 1.5F);
                    int a = coverage(d);
                    image.setPixelABGR(x, y, a << 24 | 0x00FFFFFF);
                }
            }
            disc = new DynamicTexture(() -> "monolith-disc", image);
            disc.upload();
        }
        return disc;
    }

    private static DynamicTexture bake(boolean ring) {
        NativeImage image = new NativeImage(SIZE, SIZE, false);
        float rad = CORNER - 0.5F;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                float d = sdf(x + 0.5F, y + 0.5F, rad);
                int a = ring ? Math.min(coverage(d - 3.0F), 255 - coverage(d + 1.0F)) : coverage(d);
                image.setPixelABGR(x, y, a << 24 | 0x00FFFFFF);
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> ring ? "monolith-ring" : "monolith-mask", image);
        texture.upload();
        return texture;
    }

    private static float sdf(float x, float y, float rad) {
        float px = Math.min(x, SIZE - x);
        float py = Math.min(y, SIZE - y);
        float qx = rad - px;
        float qy = rad - py;
        return (float) (Math.min(Math.max(qx, qy), 0.0F) + Math.hypot(Math.max(qx, 0.0F), Math.max(qy, 0.0F)) - rad);
    }

    private static int coverage(float d) {
        float t = Math.max(0.0F, Math.min(1.0F, 0.5F - d));
        return Math.round(t * 255.0F);
    }
}
