package femboy.monolith.ui;

import femboy.monolith.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class MonolithHud {
    private MonolithHud() {
    }

    public static void render(GuiGraphics g) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.screen instanceof ClickGuiScreen) {
            return;
        }
        SmoothGui.begin(g, client.font);
        if (GuiPages.shown("Watermark")) {
            SmoothGui.fill(8, 8, 118, 28, 10, 0xC0101014);
            SmoothGui.text("Monolith", 18, 14, 14, 0xFFF4F7FA);
        }
        if (GuiPages.shown("Array List")) {
            int y = 42;
            boolean right = false;
            for (GuiPages.Card card : GuiPages.cards()) {
                if (card.name.equals("Array List") && !card.settings.isEmpty() && card.settings.get(0).value == 1) {
                    right = true;
                }
            }
            for (Modules.Entry entry : Modules.all()) {
                if (!entry.enabled()) {
                    continue;
                }
                int w = SmoothGui.width(entry.name, 12) + 16;
                int x = right ? g.guiWidth() - w - 8 : 8;
                SmoothGui.fill(x, y, w, 16, 6, 0xC0101014);
                SmoothGui.text(entry.name, x + 8, y + 2, 12, 0xFF7EB6FF);
                y += 18;
            }
        }
        if (GuiPages.shown("Coords")) {
            String line = (int) client.player.getX() + " " + (int) client.player.getY() + " " + (int) client.player.getZ();
            int w = SmoothGui.width(line, 12) + 16;
            int y = g.guiHeight() - 28;
            SmoothGui.fill(8, y, w, 16, 6, 0xC0101014);
            SmoothGui.text(line, 16, y + 2, 12, 0xFFF4F7FA);
        }
        SmoothGui.end(g);
    }
}
