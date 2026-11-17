package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class UiIcons {
    private static final int SUPERSAMPLE = 3;
    private static final Map<String, DynamicTexture> CACHE = new HashMap<>();
    private static final Map<String, String> LABELS = new HashMap<>();

    static {
        LABELS.put("Combat", "combat");
        LABELS.put("Movement", "movement");
        LABELS.put("Utility", "utility");
        LABELS.put("Visuals", "visuals");
        LABELS.put("HUD", "hud");
        LABELS.put("Donut SMP", "donut");
        LABELS.put("Friends", "friends");
        LABELS.put("Scripts", "scripts");
        LABELS.put("Configs", "configs");
        LABELS.put("Themes", "themes");
        LABELS.put("Client", "client");
        LABELS.put("Language", "language");
        LABELS.put("GUI scale", "guiscale");
        LABELS.put("Glass", "glass");
        LABELS.put("Font", "font");
        LABELS.put("Stream proof", "stream");
        LABELS.put("Sounds", "sounds");
        LABELS.put("Blur", "blur");
        LABELS.put("search", "search");
        LABELS.put("close", "close");
    }

    private UiIcons() {
    }

    public static void draw(GuiGraphics g, String label, int x, int y, int size, int argb) {
        String id = LABELS.get(label);
        if (id == null || g == null || size <= 0) {
            return;
        }
        DynamicTexture texture = texture(id, size, argb);
        if (texture == null) {
            return;
        }
        var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        Blit.draw(g, texture.getTextureView(), sampler, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private static DynamicTexture texture(String id, int size, int argb) {
        String key = id + ":" + size + ":" + argb;
        DynamicTexture found = CACHE.get(key);
        if (found != null) {
            return found;
        }
        if (!IconPaths.has(id)) {
            return null;
        }
        int pixels = size * SUPERSAMPLE;
        BufferedImage image = IconPaths.render(id, pixels, argb);
        NativeImage nativeImage = new NativeImage(pixels, pixels, false);
        for (int py = 0; py < pixels; py++) {
            for (int px = 0; px < pixels; px++) {
                int color = image.getRGB(px, py);
                int a = color >>> 24;
                int r = (color >> 16) & 255;
                int gg = (color >> 8) & 255;
                int b = color & 255;
                nativeImage.setPixelABGR(px, py, (a << 24) | (b << 16) | (gg << 8) | r);
            }
        }
        DynamicTexture texture = new DynamicTexture(() -> "monolith-icon", nativeImage);
        texture.upload();
        CACHE.put(key, texture);
        return texture;
    }
}
