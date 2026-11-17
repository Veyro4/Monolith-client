package femboy.monolith.ui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Rounded-rect SDF shader (core/liquid_glass) drawn as GUI elements.
 * Pipelines are added to the vanilla static pipeline list at client init so the
 * resource reload precompiles them with the mod's shader sources.
 */
public final class LiquidGlass {
    private static final int[] RADII = {6, 8, 10, 14, 16, 22, 32, 64};
    private static final RenderPipeline[] FILL = new RenderPipeline[RADII.length];
    private static final RenderPipeline[] STROKE = new RenderPipeline[RADII.length];
    private static final RenderPipeline[] FLAT = new RenderPipeline[RADII.length];
    private static final RenderPipeline[] GLASS = new RenderPipeline[RADII.length];
    private static final RenderPipeline[] FROST = new RenderPipeline[RADII.length];
    private static final RenderPipeline[] SHADOW = new RenderPipeline[RADII.length];
    /** Must match SHADOW_SPREAD in liquid_glass.fsh. */
    public static final int SHADOW_SPREAD = 12;
    private static final Map<RenderPipeline, Boolean> VALID = new IdentityHashMap<>();
    private static boolean installed;

    private LiquidGlass() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        try {
            Field field = RenderPipelines.class.getDeclaredField("PIPELINES_BY_LOCATION");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Identifier, RenderPipeline> map = (Map<Identifier, RenderPipeline>) field.get(null);
            for (int i = 0; i < RADII.length; i++) {
                FILL[i] = pipeline("liquid_fill_" + RADII[i], RADII[i]);
                STROKE[i] = pipeline("liquid_stroke_" + RADII[i], RADII[i], "STROKE", "FLAT");
                FLAT[i] = pipeline("liquid_flat_" + RADII[i], RADII[i], "FLAT");
                GLASS[i] = pipeline("liquid_glass_" + RADII[i], RADII[i], "GLASS");
                FROST[i] = pipeline("liquid_frost_" + RADII[i], RADII[i], "GLASS", "FROST");
                SHADOW[i] = pipeline("liquid_shadow_" + RADII[i], RADII[i], "SHADOW");
                for (RenderPipeline p : new RenderPipeline[] {FILL[i], STROKE[i], FLAT[i], GLASS[i], FROST[i], SHADOW[i]}) {
                    map.put(p.getLocation(), p);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            installed = false;
        }
    }

    public static boolean flat(GuiGraphics g, int x, int y, int w, int h, int radius, int argb) {
        return submit(g, x, y, w, h, radius, argb, argb, FLAT);
    }

    public static boolean gradient(GuiGraphics g, int x, int y, int w, int h, int radius, int left, int right) {
        return submit(g, x, y, w, h, radius, left, right, FLAT);
    }

    public static boolean wash(GuiGraphics g, int x, int y, int w, int h, int radius, int argb) {
        int left = (0x24 << 24) | (argb & 0x00FFFFFF);
        int right = argb & 0x00FFFFFF;
        return submit(g, x, y, w, h, radius, left, right, FILL);
    }

    public static boolean draw(GuiGraphics g, int x, int y, int w, int h, int radius, int argb, boolean stroke) {
        return submit(g, x, y, w, h, radius, argb, argb, stroke ? STROKE : FILL);
    }

    /** Liquid glass pane: edge light, sheen, inner glow and grain; {@code frost} adds a milky veil and more body. */
    public static boolean glass(GuiGraphics g, int x, int y, int w, int h, int radius, int argb, boolean frost) {
        return submit(g, x, y, w, h, radius, argb, argb, frost ? FROST : GLASS, 0);
    }

    /** Soft shadow outside the rounded rect, reaching SHADOW_SPREAD px. */
    public static boolean shadow(GuiGraphics g, int x, int y, int w, int h, int radius, int argb) {
        return submit(g, x, y, w, h, radius, argb, argb, SHADOW, SHADOW_SPREAD);
    }

    private static boolean submit(GuiGraphics g, int x, int y, int w, int h, int radius, int left, int right, RenderPipeline[] set) {
        return submit(g, x, y, w, h, radius, left, right, set, 0);
    }

    private static boolean submit(GuiGraphics g, int x, int y, int w, int h, int radius, int left, int right, RenderPipeline[] set, int pad) {
        install();
        if (!installed || w <= 1 || h <= 1 || set[0] == null) {
            return false;
        }
        RenderPipeline pipeline = set[index(radius, w, h)];
        if (!valid(pipeline)) {
            return false;
        }
        g.guiRenderState.submitGuiElement(new Pane(pipeline, new Matrix3x2f(g.pose()), x, y, x + w, y + h, pad, left, right, g.scissorStack.peek()));
        return true;
    }

    private static boolean valid(RenderPipeline pipeline) {
        Boolean known = VALID.get(pipeline);
        if (known == null) {
            try {
                CompiledRenderPipeline compiled = RenderSystem.getDevice().precompilePipeline(pipeline);
                known = compiled != null && compiled.isValid();
            } catch (RuntimeException e) {
                known = false;
            }
            VALID.put(pipeline, known);
        }
        return known;
    }

    private static int index(int radius, int w, int h) {
        if (radius * 2 >= Math.min(w, h) - 1) {
            return RADII.length - 1;
        }
        int best = 0;
        int dist = Integer.MAX_VALUE;
        for (int i = 0; i < RADII.length - 1; i++) {
            int delta = Math.abs(RADII[i] - radius);
            if (delta < dist) {
                dist = delta;
                best = i;
            }
        }
        return best;
    }

    private static RenderPipeline pipeline(String name, int radius, String... defines) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("monolith", "pipeline/" + name))
                .withVertexShader(Identifier.withDefaultNamespace("core/liquid_glass"))
                .withFragmentShader(Identifier.withDefaultNamespace("core/liquid_glass"))
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withShaderDefine("RADIUS", (float) radius);
        for (String define : defines) {
            builder.withShaderDefine(define);
        }
        return builder.build();
    }

    // Packs the rect size into the vertex z and the rect origin into the UVs; the fragment
    // shader rebuilds the rounded-rect distance field from them.
    // pad grows the quad past the rect (shadows) while the shader still sees the rect's own size.
    private record Pane(RenderPipeline pipeline, Matrix3x2f pose, int x0, int y0, int x1, int y1, int pad, int left, int right, @Nullable ScreenRectangle scissorArea) implements GuiElementRenderState {
        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            int w = Math.abs(x1 - x0);
            int h = Math.abs(y1 - y0);
            float packed = w + h * 4096.0F;
            int band = Math.min(radius(), Math.min(w, h) / 2);
            if (pad == 0 && isStroke() && h - 2 * band > 2 && w - 2 * band > 2) {
                // A 1px outline only touches the corner arcs and the outer 2px of each side;
                // a full quad would shade and blend the whole panel area for nothing.
                int c = band + 1;
                quad(vertexConsumer, x0, y0, x0 + c, y0 + c, packed);
                quad(vertexConsumer, x1 - c, y0, x1, y0 + c, packed);
                quad(vertexConsumer, x0, y1 - c, x0 + c, y1, packed);
                quad(vertexConsumer, x1 - c, y1 - c, x1, y1, packed);
                quad(vertexConsumer, x0 + c, y0, x1 - c, y0 + 2, packed);
                quad(vertexConsumer, x0 + c, y1 - 2, x1 - c, y1, packed);
                quad(vertexConsumer, x0, y0 + c, x0 + 2, y1 - c, packed);
                quad(vertexConsumer, x1 - 2, y0 + c, x1, y1 - c, packed);
                return;
            }
            if (pad > 0 && h - 2 * band > 0 && w - 2 * band > 0) {
                // Shadows only show outside the rect: draw a ring (4 bands reaching the rounded
                // corners) instead of one quad over the whole panel. On weak GPUs the full quad
                // cost a whole screen of invisible blended pixels per shadow.
                quad(vertexConsumer, x0 - pad, y0 - pad, x1 + pad, y0 + band, packed);
                quad(vertexConsumer, x0 - pad, y1 - band, x1 + pad, y1 + pad, packed);
                quad(vertexConsumer, x0 - pad, y0 + band, x0 + band, y1 - band, packed);
                quad(vertexConsumer, x1 - band, y0 + band, x1 + pad, y1 - band, packed);
                return;
            }
            put(vertexConsumer, x0 - pad, y0 - pad, -pad, -pad, packed, left);
            put(vertexConsumer, x0 - pad, y1 + pad, -pad, h + pad, packed, left);
            put(vertexConsumer, x1 + pad, y1 + pad, w + pad, h + pad, packed, right);
            put(vertexConsumer, x1 + pad, y0 - pad, w + pad, -pad, packed, right);
        }

        private boolean isStroke() {
            return pipeline.getLocation().getPath().contains("liquid_stroke_");
        }

        private int radius() {
            String path = pipeline.getLocation().getPath();
            try {
                return Integer.parseInt(path.substring(path.lastIndexOf('_') + 1));
            } catch (RuntimeException e) {
                return 64;
            }
        }

        private void quad(VertexConsumer vertexConsumer, int ax, int ay, int bx, int by, float packed) {
            put(vertexConsumer, ax, ay, ax - x0, ay - y0, packed, left);
            put(vertexConsumer, ax, by, ax - x0, by - y0, packed, left);
            put(vertexConsumer, bx, by, bx - x0, by - y0, packed, left);
            put(vertexConsumer, bx, ay, bx - x0, ay - y0, packed, left);
        }

        // UV = position inside the rect in unscaled units, so the shader stays exact under any pose scale.
        private void put(VertexConsumer vertexConsumer, int x, int y, float u, float v, float packed, int color) {
            org.joml.Vector2f pos = pose.transformPosition(x, y, new org.joml.Vector2f());
            vertexConsumer.addVertex(pos.x(), pos.y(), packed).setUv(u, v).setColor(color);
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            ScreenRectangle bounds = new ScreenRectangle(x0 - pad, y0 - pad, x1 - x0 + pad * 2, y1 - y0 + pad * 2).transformMaxBounds(pose);
            return scissorArea == null ? bounds : scissorArea.intersection(bounds);
        }
    }
}
