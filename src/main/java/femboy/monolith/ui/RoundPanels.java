package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class RoundPanels {
    private static final int SIZE = 48;
    private static final float UV = 12.0F / SIZE;
    private static final Map<Integer, DynamicTexture> TEXTURES = new HashMap<>();
    private static boolean broken;

    private RoundPanels() {
    }

    public static void box(GuiGraphics g, int x, int y, int x2, int y2, int argb) {
        int w = x2 - x;
        int h = y2 - y;
        if (w < 8 || h < 8 || broken) {
            g.fill(x, y, x2, y2, argb);
            return;
        }
        try {
            DynamicTexture texture = texture(argb);
            int c = Math.min(8, Math.min(w, h) / 2);
            blit(g, texture, x, y, x + c, y + c, 0.0F, UV, 0.0F, UV);
            blit(g, texture, x + c, y, x2 - c, y + c, UV, 1.0F - UV, 0.0F, UV);
            blit(g, texture, x2 - c, y, x2, y + c, 1.0F - UV, 1.0F, 0.0F, UV);
            blit(g, texture, x, y + c, x + c, y2 - c, 0.0F, UV, UV, 1.0F - UV);
            blit(g, texture, x + c, y + c, x2 - c, y2 - c, UV, 1.0F - UV, UV, 1.0F - UV);
            blit(g, texture, x2 - c, y + c, x2, y2 - c, 1.0F - UV, 1.0F, UV, 1.0F - UV);
            blit(g, texture, x, y2 - c, x + c, y2, 0.0F, UV, 1.0F - UV, 1.0F);
            blit(g, texture, x + c, y2 - c, x2 - c, y2, UV, 1.0F - UV, 1.0F - UV, 1.0F);
            blit(g, texture, x2 - c, y2 - c, x2, y2, 1.0F - UV, 1.0F, 1.0F - UV, 1.0F);
        } catch (Throwable ignored) {
            broken = true;
            g.fill(x, y, x2, y2, argb);
        }
    }

    private static void blit(GuiGraphics g, DynamicTexture texture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        Blit.draw(g, texture.getTextureView(), texture.getSampler(), x0, y0, x1, y1, u0, u1, v0, v1);
    }

    private static DynamicTexture texture(int argb) {
        DynamicTexture found = TEXTURES.get(argb);
        if (found != null) {
            return found;
        }
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D draw = image.createGraphics();
        draw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw.setColor(new Color(argb, true));
        draw.fillRoundRect(0, 0, SIZE, SIZE, 24, 24);
        draw.dispose();
        NativeImage pixels = new NativeImage(SIZE, SIZE, false);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int color = image.getRGB(x, y);
                int a = color >>> 24;
                int r = (color >> 16) & 255;
                int gg = (color >> 8) & 255;
                int b = color & 255;
                pixels.setPixelABGR(x, y, (a << 24) | (b << 16) | (gg << 8) | r);
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> "monolith-round", pixels);
        texture.upload();
        GpuSampler linear = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try {
            Field field = AbstractTexture.class.getDeclaredField("sampler");
            field.setAccessible(true);
            field.set(texture, linear);
        } catch (ReflectiveOperationException ignored) {
        }
        TEXTURES.put(argb, texture);
        return texture;
    }
}
