package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TextCache {
    private static final int SCALE = 3;
    private static final Map<String, Glyph> GLYPHS = new HashMap<>();
    private static Font face;
    // Width lookups only need font metrics; building a texture for them made the first open and
    // every first visit to a category stall (clip() measures each shortened candidate string).
    private static final Map<String, Integer> WIDTHS = new ConcurrentHashMap<>();
    private static final Map<String, FontMetrics> METRICS = new ConcurrentHashMap<>();
    private static final Map<String, Font> FACES = new ConcurrentHashMap<>();

    private TextCache() {
    }

    public static void draw(GuiGraphics g, String text, int x, int y, int size, int argb) {
        if (g == null || text == null || text.isEmpty()) {
            return;
        }
        Glyph glyph = glyph(text, size, argb);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        Blit.draw(g, glyph.texture.getTextureView(), sampler, x, y, x + glyph.w, y + glyph.h, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    public static int width(String text, int size) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String family = SmoothGui.font();
        return WIDTHS.computeIfAbsent(family + ":" + size + ":" + text, k -> {
            FontMetrics metrics = metrics(family, size);
            int pw = Math.max(1, metrics.stringWidth(text) + SCALE * 2);
            return Math.max(1, pw / SCALE);
        });
    }

    /** Loads the font system and the GUI font off the render thread, so the first GUI open doesn't stall on it. */
    public static void prewarm() {
        Thread thread = new Thread(() -> {
            try {
                String family = SmoothGui.font();
                for (int size : new int[] {10, 11, 12, 13, 16, 18}) {
                    metrics(family, size).stringWidth("Monolith");
                }
                BufferedImage image = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D draw = image.createGraphics();
                draw.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                draw.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                draw.setFont(face(family).deriveFont(13.0F * SCALE));
                draw.drawString("Monolith", 0, 24);
                draw.dispose();
            } catch (Throwable ignored) {
            }
        }, "Monolith text warmup");
        thread.setDaemon(true);
        thread.start();
    }

    private static FontMetrics metrics(String family, int size) {
        return METRICS.computeIfAbsent(family + ":" + size, k -> {
            BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D measure = probe.createGraphics();
            measure.setFont(face(family).deriveFont((float) size * SCALE));
            FontMetrics metrics = measure.getFontMetrics();
            measure.dispose();
            return metrics;
        });
    }

    private static Glyph glyph(String text, int size, int argb) {
        String key = SmoothGui.font() + ":" + size + ":" + argb + ":" + text;
        Glyph found = GLYPHS.get(key);
        if (found != null) {
            return found;
        }
        Font font = face(SmoothGui.font());
        FontMetrics metrics = metrics(SmoothGui.font(), size);
        int pw = Math.max(1, metrics.stringWidth(text) + SCALE * 2);
        int ph = Math.max(1, metrics.getHeight() + SCALE);
        BufferedImage image = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D draw = image.createGraphics();
        draw.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        draw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        draw.setFont(font.deriveFont((float) size * SCALE));
        draw.setColor(new Color(argb, true));
        draw.drawString(text, SCALE, draw.getFontMetrics().getAscent());
        draw.dispose();
        NativeImage pixels = new NativeImage(pw, ph, false);
        for (int y = 0; y < ph; y++) {
            for (int x = 0; x < pw; x++) {
                int color = image.getRGB(x, y);
                int a = color >>> 24;
                int r = (color >> 16) & 255;
                int gg = (color >> 8) & 255;
                int b = color & 255;
                pixels.setPixelABGR(x, y, (a << 24) | (b << 16) | (gg << 8) | r);
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> "monolith-text", pixels);
        texture.upload();
        Glyph glyph = new Glyph(texture, Math.max(1, pw / SCALE), Math.max(1, ph / SCALE));
        GLYPHS.put(key, glyph);
        return glyph;
    }

    private static Font face(String name) {
        return FACES.computeIfAbsent(name, TextCache::pick);
    }

    private static Font pick(String name) {
        Font picked = new Font(name, Font.PLAIN, 12);
        if (!name.equals(picked.getFamily()) && !"Dialog".equals(name)) {
            picked = new Font("Helvetica Neue", Font.PLAIN, 12);
            if (!"Helvetica Neue".equals(picked.getFamily())) {
                picked = new Font("SansSerif", Font.PLAIN, 12);
            }
        }
        face = picked;
        return picked;
    }

    private record Glyph(DynamicTexture texture, int w, int h) {
    }
}
