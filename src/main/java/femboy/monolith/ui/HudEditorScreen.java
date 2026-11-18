package femboy.monolith.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private static final String[] MARKS = {"Username", "UID", "FPS", "Server", "Ping", "TPS", "BPS"};
    private final Screen back;
    private HudWidgets.Widget drag;
    private int grabX;
    private int grabY;
    private HudWidgets.Widget menu;
    private boolean elements;

    public HudEditorScreen(Screen back) {
        super(Component.literal("HUD"));
        this.back = back;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int glass = ClickGuiScreen.glassAmount();
        if (glass > 0) {
            this.minecraft.options.menuBackgroundBlurriness().set(Math.max(1, glass / 12));
            g.blurBeforeThisStratum();
        }
        g.fill(0, 0, this.width, this.height, 0x44000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float hud = ClickGuiScreen.hud();
        int mx = Math.round(mouseX / hud);
        int my = Math.round(mouseY / hud);
        HudWidgets.render(g, true);
        SmoothGui.begin(g, this.font);
        paintRail(mx, my);
        if (menu != null) {
            paintMenu(mx, my);
        }
        String hint = "Drag widgets. Right click for options. Esc returns.";
        int hw = SmoothGui.width(hint, 12) + 20;
        SmoothGui.glass((this.width - hw) / 2, this.height - 36, hw, 24, 12, ClickGuiScreen.glassColor(), false);
        SmoothGui.text(hint, (this.width - SmoothGui.width(hint, 12)) / 2, this.height - 29, 12, 0xFFF4F7FA);
        SmoothGui.end(g);
    }

    private void paintRail(int mx, int my) {
        int h = 16 + HudWidgets.all().size() * 26;
        SmoothGui.glass(12, 12, 148, h, 12, ClickGuiScreen.glassColor(), false);
        SmoothGui.text("Widgets", 24, 18, 12, 0xFF9AA4B2);
        int y = 36;
        for (HudWidgets.Widget widget : HudWidgets.all()) {
            boolean hot = mx >= 18 && mx <= 148 && my >= y && my < y + 24;
            if (widget.enabled) {
                SmoothGui.flat(18, y, 3, 22, 2, ClickGuiScreen.accentColor());
            } else if (hot) {
                SmoothGui.flat(18, y, 136, 22, 7, 0x14FFFFFF);
            }
            SmoothGui.text(widget.name, 28, y + 5, 12, widget.enabled ? 0xFFF4F7FA : 0xFF8E97A3);
            y += 26;
        }
    }

    private void paintMenu(int mx, int my) {
        int x = Math.min(this.width - 188, menu.x + HudWidgets.width(menu) + 8);
        int y = Math.max(12, menu.y);
        int rows = "watermark".equals(menu.id) ? 3 + (elements ? MARKS.length : 0) : 2;
        int h = 16 + rows * 26;
        SmoothGui.glass(x, y, 176, h, 12, ClickGuiScreen.glassColor(), false);
        SmoothGui.text(menu.name, x + 12, y + 8, 12, 0xFFF4F7FA);
        int row = y + 28;
        slider(x + 12, row, 152, menu.scale, 50, 150);
        SmoothGui.text(menu.scale + "%", x + 132, row - 12, 11, ClickGuiScreen.accentColor());
        row += 28;
        if ("watermark".equals(menu.id)) {
            boolean hot = inside(mx, my, x + 8, row, 160, 22);
            if (hot || elements) {
                SmoothGui.flat(x + 8, row, 160, 22, 6, 0x16FFFFFF);
            }
            SmoothGui.text("Elements", x + 16, row + 5, 12, 0xFFF4F7FA);
            row += 26;
            if (elements) {
                for (int i = 0; i < MARKS.length; i++) {
                    boolean on = HudWidgets.has(menu, i);
                    SmoothGui.text((on ? "✓  " : "    ") + MARKS[i], x + 20, row + 4, 12, on ? ClickGuiScreen.accentColor() : 0xFFD5DBE3);
                    row += 22;
                }
            }
        }
    }

    private void slider(int x, int y, int w, int value, int min, int max) {
        SmoothGui.text("Scale", x, y - 12, 11, 0xFF9AA4B2);
        SmoothGui.flat(x, y, w, 3, 2, 0xFF2A2A32);
        int filled = (w * (value - min)) / Math.max(1, max - min);
        SmoothGui.flat(x, y, Math.max(4, filled), 3, 2, ClickGuiScreen.accentColor());
        int tx = x + Math.min(w - 14, Math.max(0, filled - 7));
        SmoothGui.flat(tx, y - 3, 14, 8, 3, ClickGuiScreen.accentColor());
        SmoothGui.flat(tx + 1, y - 2, 12, 6, 2, 0xFF101014);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        float hud = ClickGuiScreen.hud();
        double mx = event.x() / hud;
        double my = event.y() / hud;
        boolean right = event.button() == 1;
        if (menu != null && clickMenu(mx, my)) {
            return true;
        }
        if (mx >= 18 && mx <= 154 && my >= 36) {
            int index = (int) ((my - 36) / 26);
            if (index >= 0 && index < HudWidgets.all().size()) {
                HudWidgets.Widget widget = HudWidgets.all().get(index);
                widget.enabled = !widget.enabled;
                HudWidgets.save();
                return true;
            }
        }
        HudWidgets.Widget hit = hitWidget(mx, my);
        if (hit == null) {
            menu = null;
            elements = false;
            return true;
        }
        if (right) {
            menu = hit;
            elements = false;
            return true;
        }
        menu = null;
        drag = hit;
        grabX = (int) mx - hit.x;
        grabY = (int) my - hit.y;
        return true;
    }

    private boolean clickMenu(double mx, double my) {
        int x = Math.min(this.width - 188, menu.x + HudWidgets.width(menu) + 8);
        int y = Math.max(12, menu.y);
        int row = y + 28;
        if (inside(mx, my, x + 12, row, 152, 14)) {
            double t = Math.max(0, Math.min(1, (mx - (x + 12)) / 152.0));
            menu.scale = 50 + (int) Math.round(t * 100);
            HudWidgets.save();
            return true;
        }
        row += 28;
        if (!"watermark".equals(menu.id)) {
            return inside(mx, my, x, y, 176, 80);
        }
        if (inside(mx, my, x + 8, row, 160, 22)) {
            elements = !elements;
            return true;
        }
        row += 26;
        if (elements) {
            for (int i = 0; i < MARKS.length; i++) {
                if (inside(mx, my, x + 12, row, 150, 20)) {
                    HudWidgets.toggle(menu, i);
                    return true;
                }
                row += 22;
            }
        }
        return inside(mx, my, x, y, 176, row - y + 8);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (drag == null) {
            return super.mouseDragged(event, deltaX, deltaY);
        }
        float hud = ClickGuiScreen.hud();
        drag.x = (int) (event.x() / hud) - grabX;
        drag.y = (int) (event.y() / hud) - grabY;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (drag != null) {
            HudWidgets.save();
            drag = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            this.minecraft.setScreen(back);
            return true;
        }
        return super.keyPressed(event);
    }

    private HudWidgets.Widget hitWidget(double mx, double my) {
        HudWidgets.Widget found = null;
        for (HudWidgets.Widget widget : HudWidgets.all()) {
            float s = widget.scale / 100.0F;
            int w = Math.round(HudWidgets.width(widget) * s);
            int h = Math.round(HudWidgets.height(widget) * s);
            if (mx >= widget.x && mx <= widget.x + w && my >= widget.y && my <= widget.y + h) {
                found = widget;
            }
        }
        return found;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
