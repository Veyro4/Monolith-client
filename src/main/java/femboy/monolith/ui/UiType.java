package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class UiType {
    private static final int COLS = 16;
    private static final int CELL = 18;
    private static final Map<Long, Atlas> ATLASES = new HashMap<>();
    private static Font base;
    private static boolean failed;

    private UiType() {
    }

    public static boolean ready() {
        return !failed && font() != null;
    }

    public static int width(String text, int size) {
        if (text == null || text.isEmpty() || !ready()) {
            return 0;
        }
        FontMetrics metrics = metrics(size);
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += metrics.charWidth(text.charAt(i));
        }
        return width;
    }

    public static void draw(GuiGraphics g, String text, int x, int y, int size, int argb) {
        if (text == null || text.isEmpty() || !ready()) {
            return;
        }
        Atlas atlas = atlas(size, argb);
        FontMetrics metrics = metrics(size);
        int cursor = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 || c > 126) {
                cursor += 4;
                continue;
            }
            int index = c - 32;
            int col = index % COLS;
            int row = index / COLS;
            int advance = Math.max(1, metrics.charWidth(c));
            int pad = Math.max(0, (CELL - advance) / 2);
            float u0 = (col * CELL + pad) / (float) (COLS * CELL);
            float u1 = (col * CELL + pad + advance) / (float) (COLS * CELL);
            float v0 = row * CELL / (float) atlas.imageHeight;
            float v1 = (row + 1) * CELL / (float) atlas.imageHeight;
            Blit.draw(g, atlas.texture.getTextureView(), atlas.texture.getSampler(), cursor, y, cursor + advance, y + CELL, u0, u1, v0, v1);
            cursor += advance;
        }
    }

    private static Font font() {
        if (base != null || failed) {
            return base;
        }
        try {
            base = Font.createFont(Font.TRUETYPE_FONT, new File("/System/Library/Fonts/SFNS.ttf"));
        } catch (Exception e) {
            base = new Font("Helvetica", Font.PLAIN, 13);
        }
        if (base == null) {
            failed = true;
        }
        return base;
    }

    private static FontMetrics metrics(int size) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D draw = image.createGraphics();
        draw.setFont(font().deriveFont(Font.PLAIN, size));
        FontMetrics metrics = draw.getFontMetrics();
        draw.dispose();
        return metrics;
    }

    private static Atlas atlas(int size, int argb) {
        long key = ((long) size << 32) | (argb & 0xFFFFFFFFL);
        Atlas found = ATLASES.get(key);
        if (found != null) {
            return found;
        }
        int rows = 6;
        int height = rows * CELL;
        int width = COLS * CELL;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D draw = image.createGraphics();
        draw.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        draw.setFont(font().deriveFont(Font.PLAIN, (float) size));
        draw.setColor(new Color(argb, true));
        FontMetrics metrics = draw.getFontMetrics();
        for (int i = 0; i < 95; i++) {
            int col = i % COLS;
            int row = i / COLS;
            String glyph = String.valueOf((char) (32 + i));
            int gx = col * CELL + (CELL - metrics.charWidth(glyph.charAt(0))) / 2;
            int gy = row * CELL + metrics.getAscent() + 1;
            draw.drawString(glyph, gx, gy);
        }
        draw.dispose();
        NativeImage pixels = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getRGB(x, y);
                int a = color >>> 24;
                int r = (color >> 16) & 255;
                int gg = (color >> 8) & 255;
                int b = color & 255;
                pixels.setPixelABGR(x, y, (a << 24) | (b << 16) | (gg << 8) | r);
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> "monolith-type-" + size, pixels);
        texture.upload();
        linear(texture);
        Atlas atlas = new Atlas(texture, height);
        ATLASES.put(key, atlas);
        return atlas;
    }

    static void linear(DynamicTexture texture) {
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try {
            Field field = AbstractTexture.class.getDeclaredField("sampler");
            field.setAccessible(true);
            field.set(texture, sampler);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private record Atlas(DynamicTexture texture, int imageHeight) {
    }
}
