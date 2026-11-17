package femboy.monolith.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

/**
 * 1.21.11 has no public GuiGraphics.blit that takes a texture view + sampler (26.3 does),
 * so submit the same BlitRenderState that GuiGraphics would build itself.
 */
public final class Blit {
    private Blit() {
    }

    public static void draw(GuiGraphics g, GpuTextureView view, GpuSampler sampler, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        draw(g, RenderPipelines.GUI_TEXTURED, view, sampler, x0, y0, x1, y1, u0, u1, v0, v1, SmoothGui.tint());
    }

    public static void draw(GuiGraphics g, RenderPipeline pipeline, GpuTextureView view, GpuSampler sampler, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int argb) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        g.guiRenderState.submitGuiElement(new BlitRenderState(pipeline, TextureSetup.singleTexture(view, sampler), new Matrix3x2f(g.pose()), x0, y0, x1, y1, u0, u1, v0, v1, argb, g.scissorStack.peek()));
    }
}
