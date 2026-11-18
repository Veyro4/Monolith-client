package femboy.monolith.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class MonolithHud {
    private MonolithHud() {
    }

    public static void render(GuiGraphics g) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.screen != null) {
            return;
        }
        HudWidgets.render(g, false);
    }
}
