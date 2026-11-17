package femboy.monolith.ui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NativeGui {
    public static final boolean AVAILABLE = load();
    private static DynamicTexture texture;
    private static int lastW;
    private static int lastH;

    private NativeGui() {
    }

    private static boolean load() {
        String[] paths = {
                System.getProperty("monolith.native"),
                "/Users/sam/Desktop/bliss client/native/build/libmonolithgui.dylib"
        };
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            try {
                System.load(path);
                return true;
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
        try (InputStream in = NativeGui.class.getResourceAsStream("/natives/macos-arm64/libmonolithgui.dylib")) {
            if (in == null) {
                return false;
            }
            Path file = Files.createTempFile("monolithgui", ".dylib");
            Files.write(file, in.readAllBytes());
            System.load(file.toAbsolutePath().toString());
            return true;
        } catch (IOException | UnsatisfiedLinkError ignored) {
            return false;
        }
    }

    public static void begin(int w, int h) {
        lastW = w;
        lastH = h;
        begin0(w, h);
    }

    public static void font(String name) {
        font0(name);
    }

    public static void tri(float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
        tri0(x0, y0, x1, y1, x2, y2, argb);
    }

    public static void oval(float x, float y, float w, float h, int argb) {
        oval0(x, y, w, h, argb);
    }

    public static void round(float x, float y, float w, float h, float r, int argb) {
        roundRect(x, y, w, h, r, argb);
    }

    public static void text(float x, float y, float size, int argb, String value) {
        text0(x, y, size, argb, value);
    }

    public static void stroke(float x, float y, float w, float h, float r, float width, int argb) {
        stroke0(x, y, w, h, r, width, argb);
    }

    public static void clip(float x, float y, float w, float h) {
        clip0(x, y, w, h);
    }

    public static void unclip() {
        unclip0();
    }

    public static void again(GuiGraphics g, int x, int y, int destW, int destH) {
        if (texture == null) {
            return;
        }
        com.mojang.blaze3d.textures.GpuSampler sampler = com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR);
        Blit.draw(g, texture.getTextureView(), sampler, x, y, x + destW, y + destH, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    public static void blit(GuiGraphics g, int x, int y, int destW, int destH) {
        byte[] pixels = end0();
        if (pixels == null || pixels.length < 4) {
            return;
        }
        int pw = lastW;
        int ph = lastH;
        if (texture == null || texture.getPixels().getWidth() != pw || texture.getPixels().getHeight() != ph) {
            if (texture != null) {
                texture.close();
            }
            texture = new DynamicTexture("monolith-gui", pw, ph, false);
        }
        NativeImage image = texture.getPixels();
        for (int py = 0; py < ph; py++) {
            for (int px = 0; px < pw; px++) {
                int i = (py * pw + px) * 4;
                int a = pixels[i + 3] & 255;
                int r = pixels[i] & 255;
                int gg = pixels[i + 1] & 255;
                int b = pixels[i + 2] & 255;
                if (a > 0 && a < 255) {
                    r = Math.min(255, r * 255 / a);
                    gg = Math.min(255, gg * 255 / a);
                    b = Math.min(255, b * 255 / a);
                }
                image.setPixelABGR(px, py, (a << 24) | (b << 16) | (gg << 8) | r);
            }
        }
        texture.upload();
        com.mojang.blaze3d.textures.GpuSampler sampler = com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR);
        Blit.draw(g, texture.getTextureView(), sampler, x, y, x + destW, y + destH, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private static native void begin0(int w, int h);

    private static native void font0(String name);

    private static native void tri0(float x0, float y0, float x1, float y1, float x2, float y2, int argb);

    private static native void oval0(float x, float y, float w, float h, int argb);

    private static native void roundRect(float x, float y, float w, float h, float r, int argb);

    private static native void stroke0(float x, float y, float w, float h, float r, float width, int argb);

    private static native void clip0(float x, float y, float w, float h);

    private static native void unclip0();

    private static native void text0(float x, float y, float size, int argb, String value);

    private static native byte[] end0();
}
