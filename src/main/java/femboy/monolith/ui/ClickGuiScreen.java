package femboy.monolith.ui;

import com.mojang.blaze3d.platform.InputConstants;
import femboy.monolith.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class ClickGuiScreen extends Screen {
    private static int accent = 0xFF4DA3FF;
    private static final int INK = 0xFFF4F7FA;
    private static final int MUTED = 0xFF93A4B7;
    // Strength of the enabled-module wash (0-255): light, but clearly the theme colour.
    private static final int MODULE_GLOW = 0x66;
    private static String search = "";
    private static boolean searchOn;
    private static String category = "Combat";
    private static float scroll;
    private static float sideScroll;
    private static final Set<String> open = new HashSet<>();
    private static Modules.Setting binding;
    private static Modules.Setting dragging;
    private static int dragX;
    private static int dragW;
    private static boolean paneDrag;
    private static boolean profileOpen;
    private static String pane;
    private static boolean sounds = true;
    private static boolean stream;
    private static int glass = 70;
    private static int blurAmt = 8;
    private static int uiScale = 100;
    private static String fontName = "SansSerif";
    private static String lang = "English";
    private static String theme = "Arctic Blue";
    private static String themeFilter = "All";
    private static final Map<String, Float> catHover = new HashMap<>();
    private static String hoverId = "";
    private static long lastHover;
    private static long hoverQuiet;
    private net.minecraft.client.resources.sounds.SoundInstance hoverSound;
    private static long pageAt;
    // Animations: GUI open pop, category hover lift, module cards bouncing in on a category switch.
    private static final long OPEN_MS = 380L;
    private static final long CARD_MS = 420L;
    private static final long CARD_STAGGER_MS = 26L;
    private static final int CARD_CLIP_MARGIN = 12;
    private static final Map<String, Motion.Spring> catLift = new HashMap<>();
    // Caps one frame's step so a hitch (texture builds, GC) can't skip an animation,
    // while normal low frame rates (10-30 fps in-world on weak GPUs) still play in real time.
    private static final float MAX_FRAME_S = 0.1F;
    private static float cardsMs;
    private static long cardsDelay;
    private float openMs;
    // True while warm() draws the whole GUI invisibly once at the title screen.
    private static boolean warming;
    private long lastFrame;
    private float frameDt;
    private static float hoverDt;
    private static int drawnSig = Integer.MIN_VALUE;
    private Integer savedBlur;

    public ClickGuiScreen() {
        super(Component.literal("Monolith"));
    }

    /**
     * Draws the GUI once, fully transparent, so the first real open doesn't spend its first frames
     * building text/icon textures and running cold code (that made the open pop play at ~5 fps).
     */
    public static void warm(GuiGraphics g) {
        float savedCards = cardsMs;
        warming = true;
        try {
            new ClickGuiScreen().render(g, -10000, -10000, 0.0F);
        } catch (RuntimeException ignored) {
        } finally {
            warming = false;
            cardsMs = savedCards;
            SmoothGui.end(g);
        }
    }

    public static boolean on(String name) { return Modules.on(name); }
    public static int range(String name) { return Modules.range(name); }
    public static int mode(String name) { return Modules.mode(name); }
    public static int delay(String name) { return Modules.delay(name); }
    public static boolean playersOnly(String name) { return Modules.playersOnly(name); }
    public static void set(String name, boolean enabled) { Modules.set(name, enabled); }
    public static void panic() { Modules.panic(); }

    public static void pollBinds() {
        if (Minecraft.getInstance().screen instanceof ClickGuiScreen) {
            return;
        }
        for (Modules.Entry entry : Modules.all()) {
            for (Modules.Setting setting : entry.settings) {
                if (!"bind".equals(setting.key) || setting.value < 0) {
                    continue;
                }
                boolean down = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), setting.value);
                if (down && !setting.wasDown) {
                    entry.toggle();
                }
                setting.wasDown = down;
            }
        }
    }

    @Override
    public void added() {
        openMs = 0.0F;
        cardsMs = 0.0F;
        cardsDelay = 90L;
        lastFrame = 0L;
        savedBlur = this.minecraft.options.getMenuBackgroundBlurriness();
        this.minecraft.options.menuBackgroundBlurriness().set(backdrop());
    }

    @Override
    public void removed() {
        if (savedBlur != null) {
            this.minecraft.options.menuBackgroundBlurriness().set(savedBlur);
            savedBlur = null;
        }
    }

    @Override
    public void tick() {
        int radius = backdrop();
        if (this.minecraft.options.getMenuBackgroundBlurriness() != radius) {
            this.minecraft.options.menuBackgroundBlurriness().set(radius);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.minecraft.options.menuBackgroundBlurriness().set(backdrop());
        g.blurBeforeThisStratum();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.width = g.guiWidth();
        this.height = g.guiHeight();
        noteHover(hoverAt(mouseX, mouseY));
        long anim = searchOn || search.isEmpty() ? System.currentTimeMillis() / 70L : 0L;
        int sig = Objects.hash(category, search, searchOn, (int) scroll, (int) sideScroll, open, glass, blurAmt, accent, theme, profileOpen, pane, stream, fontName, lang, uiScale, mouseX / 6, mouseY / 6, anim);
        long now = System.nanoTime();
        frameDt = lastFrame == 0L ? 0.0F : Math.min(MAX_FRAME_S, (now - lastFrame) / 1.0E9F);
        lastFrame = now;
        hoverDt = frameDt;
        openMs += frameDt * 1000.0F;
        cardsMs += frameDt * 1000.0F;
        float open = warming ? 1.0F : Motion.clamp01(openMs / OPEN_MS);
        boolean opening = open < 1.0F;
        if (!warming) {
            g.fill(0, 0, this.width, this.height, Math.round(0x3A * Motion.outCubic(open)) << 24);
        }
        Layout lay = layout();
        SmoothGui.begin(g, this.font);
        if (warming) {
            SmoothGui.alpha(0.0F);
        }
        if (opening) {
            // Pops up from slightly smaller and lower, overshoots a touch, settles.
            float scale = 0.88F + 0.12F * Motion.outBack(open, 1.9F);
            float cx = lay.f.x + lay.f.w / 2.0F;
            float cy = lay.f.y + lay.f.h / 2.0F;
            g.pose().pushMatrix();
            g.pose().translate(cx, cy + (1.0F - Motion.outCubic(open)) * 14.0F);
            g.pose().scale(scale, scale);
            g.pose().translate(-cx, -cy);
            SmoothGui.alpha(Motion.outCubic(Math.min(1.0F, open * 1.8F)));
        }
        SmoothGui.shadow(lay.f.x, lay.f.y, lay.f.w, lay.f.h, 22, 0x70000000);
        SmoothGui.glass(lay.f.x, lay.f.y, lay.f.w, lay.f.h, 22, shell(), false);
        SmoothGui.stroke(lay.f.x, lay.f.y, lay.f.w, lay.f.h, 22, 1.0F, 0x1AFFFFFF);

        SmoothGui.fill(lay.f.x + lay.pad, lay.f.y + lay.pad, lay.f.side - lay.pad * 2, lay.f.h - lay.pad * 2, 16, 0x75121212);
        SmoothGui.mark(lay.f.x + lay.pad + 8, lay.f.y + lay.pad + 4, accent);
        SmoothGui.text("Monolith", lay.f.x + lay.pad + 40, lay.f.y + lay.pad + 8, 16, INK);
        SmoothGui.text(word("client"), lay.f.x + lay.pad + 40, lay.f.y + lay.pad + 26, 11, MUTED);

        SmoothGui.clip(lay.f.x + lay.pad, lay.catsTop, lay.f.side - lay.pad, lay.catsH);
        int y = lay.catsTop - (int) sideScroll;
        boolean first = true;
        for (String[] group : GuiPages.GROUPS) {
            if (!first) {
                y += 12;
            }
            first = false;
            SmoothGui.text(word(group[0]), lay.f.x + lay.pad + 16, y, 11, MUTED);
            y += 18;
            for (int i = 1; i < group.length; i++) {
                boolean on = group[i].equals(category) && search.isEmpty();
                boolean hot = mouseX >= lay.f.x + lay.pad + 8 && mouseX <= lay.f.x + lay.f.side - lay.pad && mouseY >= y && mouseY < y + 30;
                float hover = hoverStep(group[i], hot && !on);
                float lift = catLift.computeIfAbsent(group[i], k -> new Motion.Spring()).step(hot ? 1.0F : 0.0F, frameDt);
                boolean lifted = Math.abs(lift) > 0.002F;
                if (lift > 0.01F) {
                    // Soft shadow left behind as the row rises.
                    SmoothGui.fill(lay.f.x + lay.pad + 10, y + 3, lay.rowW - 4, 30, 8, Math.round(0x40 * Math.min(1.0F, lift)) << 24);
                }
                if (lifted) {
                    float cx = lay.f.x + lay.pad + 8 + lay.rowW / 2.0F;
                    float cy = y + 15.0F;
                    float scale = 1.0F + 0.06F * lift;
                    g.pose().pushMatrix();
                    g.pose().translate(cx, cy - 3.0F * lift);
                    g.pose().scale(scale, scale);
                    g.pose().translate(-cx, -cy);
                }
                if (on) {
                    SmoothGui.fill(lay.f.x + lay.pad + 8, y, lay.rowW, 30, 8, tone(0xFF15161B, accent, 0.24F));
                } else if (hover > 0.02F) {
                    SmoothGui.fill(lay.f.x + lay.pad + 8, y, lay.rowW, 30, 8, (Math.round(0x1C * Math.min(1.0F, hover)) << 24) | 0xFFFFFF);
                }
                UiIcons.draw(g, group[i], lay.f.x + lay.pad + 14, y + 7, 16, accent);
                SmoothGui.text(word(group[i]), lay.f.x + lay.pad + 40, y + 8, 13, INK);
                if (lifted) {
                    g.pose().popMatrix();
                }
                y += 34;
            }
        }
        SmoothGui.unclip();

        boolean profileHot = inside(mouseX, mouseY, lay.profileX, lay.profileY, lay.profileW, 36);

        ring(lay.profileX, lay.profileY, lay.profileW, 36, 10, accent, 0xFF121212);
        if (profileHot || profileOpen) {
            SmoothGui.fill(lay.profileX + 1, lay.profileY + 1, lay.profileW - 2, 34, 9, 0x10FFFFFF);
        }
        SmoothGui.fill(lay.profileX + 8, lay.profileY + 6, 24, 24, 8, 0xFF1A1A1A);
        String name = stream ? "----" : profile();
        SmoothGui.text(clip(name, lay.profileW - 46), lay.profileX + 38, lay.profileY + 6, 12, INK);
        SmoothGui.text(stream ? "----" : "UID #1", lay.profileX + 38, lay.profileY + 18, 10, MUTED);


        String title = search.isEmpty() ? word(category) : word("Search");
        List<GuiPages.Card> rows = visible();
        SmoothGui.text(title, lay.mainX + 4, lay.f.y + lay.pad + 2, 18, INK);
        SmoothGui.text(rows.size() + " " + word("modules"), lay.mainX + 10 + SmoothGui.width(title, 18), lay.f.y + lay.pad + 8, 12, MUTED);
        paintSearch(g, lay);

        if ("Themes".equals(category) && search.isEmpty()) {
            paintThemes(lay, mouseX, mouseY);
        } else {
            paintCards(lay, rows, mouseX, mouseY);
        }
        if (profileOpen) {
            paintDock(lay, mouseX, mouseY);
        }
        if (opening) {
            g.pose().popMatrix();
        }
        SmoothGui.end(g);
    }

    private void paintCards(Layout lay, List<GuiPages.Card> rows, int mouseX, int mouseY) {
        int ink = INK;
        float cardClock = cardsMs - cardsDelay;
        float baseAlpha = SmoothGui.alpha();
        // Clip starts a little above the first row so its rounded top, rim light, shadow and
        // bounce overshoot aren't sliced flat (the gap under the search box has room for it).
        SmoothGui.clip(lay.mainX, lay.listTop - CARD_CLIP_MARGIN, lay.f.w - lay.f.side - 8, lay.listH + CARD_CLIP_MARGIN);
        int[] colY = new int[] {lay.listTop - (int) scroll, lay.listTop - (int) scroll, lay.listTop - (int) scroll};
        for (int i = 0; i < rows.size(); i++) {
            GuiPages.Card card = rows.get(i);
            int col = i % 3;
            int x = lay.mainX + 8 + col * (lay.colW + 8);
            int cy = colY[col];
            int cardH = cardHeight(card);
            float in = warming ? 1.0F : Motion.clamp01((cardClock - i * CARD_STAGGER_MS) / (float) CARD_MS);
            if (in <= 0.0F) {
                colY[col] += cardH + 10;
                continue;
            }
            boolean bouncing = in < 1.0F;
            if (bouncing) {
                // Each card pops up from below, overshoots slightly and settles, one after another.
                float scale = 0.84F + 0.16F * Motion.outBack(in, 2.2F);
                float cx = x + lay.colW / 2.0F;
                float ccy = cy + cardH / 2.0F;
                SmoothGui.graphics().pose().pushMatrix();
                SmoothGui.graphics().pose().translate(cx, ccy + (1.0F - Motion.outCubic(in)) * 12.0F);
                SmoothGui.graphics().pose().scale(scale, scale);
                SmoothGui.graphics().pose().translate(-cx, -ccy);
                SmoothGui.alpha(baseAlpha * Motion.outCubic(Math.min(1.0F, in * 2.2F)));
            }
            boolean hot = mouseX >= x && mouseX <= x + lay.colW && mouseY >= cy && mouseY <= cy + cardH;
            boolean on = card.enabled();
            SmoothGui.shadow(x, cy, lay.colW, cardH, 16, 0x38000000);
            SmoothGui.glass(x, cy, lay.colW, cardH, 16, panel(), true);
            if (on) {
                int face = accent & 0xFFFFFF;
                int left = (MODULE_GLOW << 24) | face;
                int right = (MODULE_GLOW << 24) | darken(face);
                SmoothGui.gradient(x, cy, lay.colW, cardH, 16, left, right);
                SmoothGui.stroke(x, cy, lay.colW, cardH, 16, 1.0F, 0xFF000000 | face);
            } else if (hot) {
                SmoothGui.fill(x, cy, lay.colW, cardH, 16, 0x14FFFFFF);
            }
            SmoothGui.text(word(card.name), x + 12, cy + 10, 13, ink);
            SmoothGui.text(clip(word(card.desc), lay.colW - 24), x + 12, cy + 26, 11, MUTED);

            if (open.contains(card.key())) {
                int sy = cy + 48;
                for (Modules.Setting setting : card.settings) {
                    SmoothGui.text(setting.label, x + 12, sy, 11, MUTED);
                    String value = setting == binding && "bind".equals(setting.key) ? "..." : setting.text();
                    SmoothGui.text(value, x + lay.colW - 12 - SmoothGui.width(value, 11), sy, 11, accent);
                    sy += 14;
                    if (isSlider(setting)) {
                        int track = lay.colW - 24;
                        int span = Math.max(1, setting.max - setting.min);
                        int filled = track * (setting.value - setting.min) / span;
                        slider(x + 12, sy, track, filled);
                        sy += 14;
                    }
                }
            }
            if (bouncing) {
                SmoothGui.graphics().pose().popMatrix();
                SmoothGui.alpha(baseAlpha);
            }
            colY[col] += cardH + 10;
        }
        SmoothGui.unclip();
    }

    private void paintThemes(Layout lay, int mouseX, int mouseY) {
        int chipX = lay.mainX + 8;
        for (String filter : GuiPages.FILTERS) {
            int w = SmoothGui.width(filter, 11) + 16;
            boolean on = filter.equals(themeFilter);
            SmoothGui.flat(chipX, lay.listTop, w, 26, 13, on ? tone(0xFF0B0F13, accent, 0.28F) : 0xFF1B1E26);
            SmoothGui.text(filter, chipX + 8, lay.listTop + 6, 11, on ? INK : MUTED);
            chipX += w + 4;
            if (chipX > lay.f.x + lay.f.w - 40) {
                break;
            }
        }
        SmoothGui.clip(lay.mainX, lay.listTop + 34, lay.f.w - lay.f.side - 8, lay.listH - 34);
        int cols = 4;
        int gap = 8;
        int w = (lay.f.w - lay.f.side - 24 - gap * (cols - 1)) / cols;
        int n = 0;
        for (String[] item : GuiPages.THEMES) {
            if (!"All".equals(themeFilter) && !themeFilter.equals(item[1])) {
                continue;
            }
            int x = lay.mainX + 8 + (n % cols) * (w + gap);
            int y = lay.listTop + 34 - (int) scroll + (n / cols) * 70;
            boolean on = item[0].equals(theme);
            SmoothGui.glass(x, y, w, 62, 12, panel(), true);
            SmoothGui.text(item[0], x + 12, y + 10, 12, INK);
            for (int s = 0; s < 4; s++) {
                SmoothGui.circle(x + 12 + s * 12, y + 36, 8, 0xFF000000 | Integer.parseInt(item[2 + s], 16));
            }
            if (on) {
                SmoothGui.stroke(x, y, w, 62, 12, 1.0F, accent);
                SmoothGui.circle(x + w - 20, y + 10, 10, accent);
            }
            n++;
        }
        SmoothGui.unclip();
    }

    private void paintDock(Layout lay, int mouseX, int mouseY) {
        SmoothGui.shadow(lay.dockX, lay.dockY, lay.dockW, lay.dockH, 14, 0x60000000);
        SmoothGui.glass(lay.dockX, lay.dockY, lay.dockW, lay.dockH, 14, panel(), true);

        for (int i = 0; i < GuiPages.DOCK.length; i++) {
            int y = lay.dockY + 6 + i * 32;
            boolean on = GuiPages.PANES[i].equals(pane);
            boolean hot = mouseX >= lay.dockX + 6 && mouseX <= lay.dockX + lay.dockW - 6 && mouseY >= y && mouseY < y + 30;
            if (on) {
                SmoothGui.fill(lay.dockX + 6, y, lay.dockW - 12, 30, 8, tone(0xFF15161B, accent, 0.20F));
            } else if (hot) {
                SmoothGui.fill(lay.dockX + 6, y, lay.dockW - 12, 30, 8, 0x16FFFFFF);
            }
            UiIcons.draw(SmoothGui.graphics(), GuiPages.DOCK[i], lay.dockX + 14, y + 6, 16, accent);
            SmoothGui.text(word(GuiPages.DOCK[i]), lay.dockX + 40, y + 7, 13, INK);
        }
        if (pane != null) {
            SmoothGui.shadow(lay.flyX, lay.flyY, lay.flyW, lay.flyH, 14, 0x60000000);
            SmoothGui.glass(lay.flyX, lay.flyY, lay.flyW, lay.flyH, 14, panel(), true);

            paintPane(lay, mouseX, mouseY);
        }
    }

    private void paintPane(Layout lay, int mouseX, int mouseY) {
        int x = lay.flyX + 8;
        int y = lay.flyY + 8;
        if ("lang".equals(pane)) {
            chips(x, y, lay.flyW - 16, new String[]{"English", "Deutsch", "Español", "Français", "Русский"}, lang, mouseX, mouseY);
        } else if ("scale".equals(pane)) {
            chips(x, y, lay.flyW - 16, new String[]{"80", "100", "125"}, Integer.toString(uiScale), mouseX, mouseY);
        } else if ("font".equals(pane)) {
            chips(x, y, lay.flyW - 16, new String[]{"Helvetica Neue", "Arial", "Georgia", "Menlo", "Verdana"}, fontName, mouseX, mouseY);
        } else if ("stream".equals(pane) || "sounds".equals(pane)) {
            boolean on = "sounds".equals(pane) ? sounds : stream;
            chips(x, y, lay.flyW - 16, new String[]{"Off", "On"}, on ? "On" : "Off", mouseX, mouseY);
        } else if ("quality".equals(pane) || "blur".equals(pane)) {
            int value = "quality".equals(pane) ? glass : blurAmt * 10;
            SmoothGui.text(("quality".equals(pane) ? "Glass " : "Blur ") + value + "%", x, y, 12, INK);
            slider(x, y + 22, lay.flyW - 16, (lay.flyW - 16) * value / 100);
        }
    }

    private void chips(int x, int y, int w, String[] options, String selected, int mouseX, int mouseY) {
        for (int i = 0; i < options.length; i++) {
            int row = y + i * 28;
            boolean on = options[i].equals(selected);
            SmoothGui.flat(x, row, w, 26, 8, on ? accent : 0xFF1B1E26);
            if (on) {
                SmoothGui.stroke(x, row, w, 26, 8, 1.0F, accent);
            }
            SmoothGui.text(options[i], x + 8, row + 6, 12, on && light(accent) ? 0xFF0C1016 : INK);

        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        Layout lay = layout();
        double mx = event.x();
        double my = event.y();
        boolean right = event.button() == InputConstants.MOUSE_BUTTON_RIGHT;
        if (inSearch(lay, mx, my)) {
            if (!search.isEmpty() && mx >= lay.searchX + lay.searchW - 28) {
                search = "";
                scroll = 0;
                pageAt = System.currentTimeMillis();
            }
            searchOn = true;
            return true;
        }
        searchOn = false;
        if (profileOpen && clickDock(lay, mx, my)) {
            return true;
        }
        if (inside(mx, my, lay.profileX, lay.profileY, lay.profileW, 36)) {
            profileOpen = !profileOpen;
            if (!profileOpen) {
                pane = null;
            }
            play();
            return true;
        }
        profileOpen = false;
        pane = null;
        if (mx >= lay.f.x + lay.pad && mx <= lay.f.x + lay.f.side && my >= lay.catsTop && my <= lay.catsTop + lay.catsH) {
            String hit = categoryAt(lay, my);
            if (hit != null) {
                if (!hit.equals(category) || !search.isEmpty()) {
                    cardsMs = 0.0F;
                    cardsDelay = 0L;
                }
                category = hit;
                search = "";
                scroll = 0;
                pageAt = System.currentTimeMillis();
                play();
            }
            return true;
        }
        if ("Themes".equals(category) && search.isEmpty()) {
            String filter = filterAt(lay, mx, my);
            if (filter != null) {
                themeFilter = filter;
                scroll = 0;
                play();
                return true;
            }
            String picked = themeAt(lay, mx, my);
            if (picked != null) {
                theme = picked;
                accent = 0xFF000000 | Integer.parseInt(colorOf(picked), 16);
                play();
            }
            return true;
        }
        Hit hit = hit(mx, my, lay);
        if (hit == null) {
            return true;
        }
        if (hit.setting != null) {
            if (isSlider(hit.setting)) {
                dragging = hit.setting;
                dragX = hit.x;
                dragW = hit.w;
                setSlider(hit.setting, mx, hit.x, hit.w);
            } else if ("bind".equals(hit.setting.key)) {
                binding = hit.setting;
            } else if (hit.setting.options != null) {
                hit.setting.value = (hit.setting.value + 1) % hit.setting.options.length;
            }
            return true;
        }
        if (right) {
            if (!open.add(hit.card.key())) {
                open.remove(hit.card.key());
            }
        } else {
            hit.card.toggle();
            if ("Panic".equals(hit.card.name)) {
                this.minecraft.setScreen(null);
            }
        }
        play();
        return true;
    }

    private boolean clickDock(Layout lay, double mx, double my) {
        if (pane != null && inside(mx, my, lay.flyX, lay.flyY, lay.flyW, lay.flyH)) {
            applyPane(lay, mx, my);
            return true;
        }
        if (!inside(mx, my, lay.dockX, lay.dockY, lay.dockW, lay.dockH)) {
            return false;
        }
        int index = (int) ((my - lay.dockY - 6) / 32);
        if (index >= 0 && index < GuiPages.PANES.length) {
            pane = GuiPages.PANES[index].equals(pane) ? null : GuiPages.PANES[index];
            play();
        }
        return true;
    }

    private void applyPane(Layout lay, double mx, double my) {
        int x = lay.flyX + 8;
        int y = lay.flyY + 8;
        if ("lang".equals(pane)) {
            lang = pick(mx, my, x, y, lay.flyW - 16, new String[]{"English", "Deutsch", "Español", "Français", "Русский"}, lang);
        } else if ("scale".equals(pane)) {
            String picked = pick(mx, my, x, y, lay.flyW - 16, new String[]{"80", "100", "125"}, Integer.toString(uiScale));
            uiScale = Integer.parseInt(picked);
        } else if ("font".equals(pane)) {
            fontName = pick(mx, my, x, y, lay.flyW - 16, new String[]{"Helvetica Neue", "Arial", "Georgia", "Menlo", "Verdana"}, fontName);
            SmoothGui.font(fontName);
        } else if ("stream".equals(pane)) {
            stream = "On".equals(pick(mx, my, x, y, lay.flyW - 16, new String[]{"Off", "On"}, stream ? "On" : "Off"));
        } else if ("sounds".equals(pane)) {
            sounds = "On".equals(pick(mx, my, x, y, lay.flyW - 16, new String[]{"Off", "On"}, sounds ? "On" : "Off"));
        } else if ("quality".equals(pane) || "blur".equals(pane)) {
            paneDrag = true;
            double t = Math.max(0, Math.min(1, (mx - x) / Math.max(1, lay.flyW - 16)));
            if ("quality".equals(pane)) {
                glass = (int) Math.round(t * 100);
                blurAmt = glass <= 0 ? 0 : Math.max(1, Math.round(glass / 10.0F));
            } else {
                blurAmt = (int) Math.round(t * 10);
            }
            this.minecraft.options.menuBackgroundBlurriness().set(backdrop());
        }
    }

    private static String pick(double mx, double my, int x, int y, int w, String[] options, String current) {
        for (int i = 0; i < options.length; i++) {
            int row = y + i * 28;
            if (mx >= x && mx <= x + w && my >= row && my < row + 26) {
                return options[i];
            }
        }
        return current;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        paneDrag = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        Layout lay = layout();
        if (paneDrag && ("quality".equals(pane) || "blur".equals(pane))) {
            applyPane(lay, event.x(), event.y());
            return true;
        }
        if (dragging == null) {
            return false;
        }
        setSlider(dragging, event.x(), dragX, dragW);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Layout lay = layout();
        if (mouseX < lay.f.x + lay.f.side) {
            sideScroll = Math.max(0, Math.min(sideMax(lay), sideScroll - (float) scrollY * 18.0F));
            return true;
        }
        int max = Math.max(0, contentHeight(lay) - lay.listH);
        scroll = Math.max(0, Math.min(max, scroll - (float) scrollY * 22.0F));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (binding != null) {
            binding.value = event.key() == InputConstants.KEY_ESCAPE ? -1 : event.key();
            binding = null;
            return true;
        }
        if (searchOn && event.isEscape()) {
            if (!search.isEmpty()) {
                search = "";
                scroll = 0;
                pageAt = System.currentTimeMillis();
            } else {
                searchOn = false;
            }
            return true;
        }
        if (event.isEscape() || event.key() == InputConstants.KEY_RSHIFT) {
            if (profileOpen) {
                profileOpen = false;
                pane = null;
                return true;
            }
            this.minecraft.setScreen(null);
            return true;
        }
        if (searchOn && (event.key() == InputConstants.KEY_BACKSPACE)) {
            if (!search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
                pageAt = System.currentTimeMillis();
            }
            return true;
        }
        if (searchOn && event.isPaste()) {
            String clip = this.minecraft.keyboardHandler.getClipboard();
            if (clip != null && !clip.isEmpty()) {
                for (int i = 0; i < clip.length() && search.length() < 32; i++) {
                    char ch = clip.charAt(i);
                    if (ch >= 32 && ch != 127) {
                        search += ch;
                    }
                }
                scroll = 0;
                pageAt = System.currentTimeMillis();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!searchOn || search.length() >= 32 || !event.isAllowedChatCharacter()) {
            return false;
        }
        search += event.codepointAsString();
        scroll = 0;
        pageAt = System.currentTimeMillis();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
        return Collections.emptyList();
    }

    private Layout layout() {
        float scale = Math.min((this.width - 24) / 980.0F, (this.height - 24) / 600.0F);
        scale = Math.max(0.62F, Math.min(scale, 1.05F)) * (uiScale / 100.0F);
        int w = Math.round(980 * scale);
        int h = Math.round(600 * scale);
        Frame f = new Frame((this.width - w) / 2, (this.height - h) / 2, w, h, Math.round(196 * scale));
        int pad = Math.max(8, f.w / 98);
        int searchW = Math.round(240 * (f.w / 980.0F));
        int profileW = f.side - pad * 2 - 16;
        int profileY = f.y + f.h - pad - 44;
        int catsTop = f.y + pad + 50;
        int catsH = profileY - catsTop - 8;
        int mainX = f.x + f.side;
        int listTop = f.y + pad + 46;
        int dockH = 12 + GuiPages.DOCK.length * 32;
        int dockY = f.y + f.h - pad - dockH;
        int flyH = flyHeight();
        int flyY = dockY;
        if (pane != null) {
            int index = 0;
            for (int i = 0; i < GuiPages.PANES.length; i++) {
                if (GuiPages.PANES[i].equals(pane)) {
                    index = i;
                }
            }
            flyY = dockY + 6 + index * 32;
            int limit = f.y + f.h - pad - flyH;
            if (flyY > limit) {
                flyY = Math.max(f.y + pad, limit);
            }
        }
        return new Layout(f, pad, searchW, f.x + f.w - pad - searchW, f.y + pad + 2, mainX, listTop, f.h - pad - 56, (f.w - f.side - 32) / 3, f.x + pad + 8, profileY, profileW, catsTop, catsH, profileW, mainX + 8, dockY, 168, dockH, mainX + 184, flyY, 168, flyH);
    }

    private List<GuiPages.Card> visible() {
        String q = search.toLowerCase(Locale.ROOT);
        List<GuiPages.Card> rows = new ArrayList<>();
        for (GuiPages.Card card : GuiPages.cards()) {
            boolean match = q.isEmpty()
                    ? card.category.equals(category)
                    : card.name.toLowerCase(Locale.ROOT).contains(q)
                    || card.desc.toLowerCase(Locale.ROOT).contains(q)
                    || card.category.toLowerCase(Locale.ROOT).contains(q);
            if (match && !"Themes".equals(card.category)) {
                rows.add(card);
            }
        }
        return rows;
    }

    private int cardHeight(GuiPages.Card card) {
        if (!open.contains(card.key())) {
            return 52;
        }
        int h = 54;
        for (Modules.Setting setting : card.settings) {
            h += isSlider(setting) ? 28 : 16;
        }
        return h;
    }

    private int contentHeight(Layout lay) {
        if ("Themes".equals(category) && search.isEmpty()) {
            return (shownThemes() + 3) / 4 * 70 + 40;
        }
        int[] col = new int[3];
        List<GuiPages.Card> rows = visible();
        for (int i = 0; i < rows.size(); i++) {
            col[i % 3] += cardHeight(rows.get(i)) + 10;
        }
        return Math.max(col[0], Math.max(col[1], col[2]));
    }

    private int sideMax(Layout lay) {
        int h = 0;
        for (int g = 0; g < GuiPages.GROUPS.length; g++) {
            if (g > 0) {
                h += 12;
            }
            h += 18 + (GuiPages.GROUPS[g].length - 1) * 34;
        }
        return Math.max(0, h - lay.catsH);
    }

    private String categoryAt(Layout lay, double my) {
        int y = lay.catsTop - (int) sideScroll;
        boolean first = true;
        for (String[] group : GuiPages.GROUPS) {
            if (!first) {
                y += 12;
            }
            first = false;
            y += 18;
            for (int i = 1; i < group.length; i++) {
                if (my >= y && my < y + 30) {
                    return group[i];
                }
                y += 34;
            }
        }
        return null;
    }

    private int shownThemes() {
        int n = 0;
        for (String[] item : GuiPages.THEMES) {
            if ("All".equals(themeFilter) || themeFilter.equals(item[1])) {
                n++;
            }
        }
        return n;
    }

    private String filterAt(Layout lay, double mx, double my) {
        if (my < lay.listTop || my > lay.listTop + 26) {
            return null;
        }
        int chipX = lay.mainX + 8;
        for (String filter : GuiPages.FILTERS) {
            int w = SmoothGui.width(filter, 11) + 16;
            if (mx >= chipX && mx <= chipX + w) {
                return filter;
            }
            chipX += w + 4;
            if (chipX > lay.f.x + lay.f.w - 40) {
                break;
            }
        }
        return null;
    }

    private String themeAt(Layout lay, double mx, double my) {
        int cols = 4;
        int gap = 8;
        int w = (lay.f.w - lay.f.side - 24 - gap * (cols - 1)) / cols;
        int n = 0;
        for (String[] item : GuiPages.THEMES) {
            if (!"All".equals(themeFilter) && !themeFilter.equals(item[1])) {
                continue;
            }
            int x = lay.mainX + 8 + (n % cols) * (w + gap);
            int y = lay.listTop + 34 - (int) scroll + (n / cols) * 70;
            if (mx >= x && mx <= x + w && my >= y && my <= y + 62 && my >= lay.listTop + 34 && my <= lay.listTop + lay.listH) {
                return item[0];
            }
            n++;
        }
        return null;
    }

    private static int flyHeight() {
        if (pane == null) {
            return 0;
        }
        if ("quality".equals(pane) || "blur".equals(pane)) {
            return 56;
        }
        if ("stream".equals(pane) || "sounds".equals(pane)) {
            return 72;
        }
        if ("scale".equals(pane)) {
            return 100;
        }
        if ("font".equals(pane) || "lang".equals(pane)) {
            return 156;
        }
        return 128;
    }

    private void icon(String name, int x, int y) {
        int c = accent;
        switch (name) {
            case "Combat" -> {
                SmoothGui.circle(x, y + 2, 10, c);
                SmoothGui.fill(x + 4, y + 1, 2, 12, 1, c);
            }
            case "Movement" -> {
                SmoothGui.circle(x, y + 2, 6, c);
                SmoothGui.circle(x + 7, y + 2, 6, c);
            }
            case "Utility" -> SmoothGui.fill(x, y + 1, 12, 12, 3, c);
            case "Visuals" -> {
                SmoothGui.circle(x, y + 1, 12, c);
                SmoothGui.circle(x + 3, y + 4, 6, 0xFF101014);
            }
            case "HUD" -> {
                SmoothGui.fill(x, y + 1, 5, 5, 1, c);
                SmoothGui.fill(x + 7, y + 1, 5, 5, 1, c);
                SmoothGui.fill(x, y + 8, 5, 5, 1, c);
                SmoothGui.fill(x + 7, y + 8, 5, 5, 1, c);
            }
            case "Donut SMP" -> {
                SmoothGui.circle(x, y + 1, 12, c);
                SmoothGui.circle(x + 4, y + 5, 4, 0xFF101014);
            }
            case "Friends" -> {
                SmoothGui.circle(x, y + 3, 7, c);
                SmoothGui.circle(x + 6, y + 1, 7, c);
            }
            case "Scripts" -> {
                SmoothGui.fill(x, y + 3, 10, 2, 1, c);
                SmoothGui.fill(x + 2, y + 7, 10, 2, 1, c);
            }
            case "Configs" -> SmoothGui.fill(x, y + 2, 12, 10, 2, c);
            case "Themes" -> SmoothGui.circle(x + 1, y + 1, 12, c);
            default -> SmoothGui.circle(x + 2, y + 2, 10, c);
        }
    }

    private String hoverAt(int mx, int my) {
        Layout lay = layout();
        if (inside(mx, my, lay.searchX, lay.searchY, lay.searchW, 32)) {
            return "search";
        }
        if (inside(mx, my, lay.profileX, lay.profileY, lay.profileW, 36)) {
            return "profile";
        }
        if (mx >= lay.f.x + lay.pad && mx <= lay.f.x + lay.f.side && my >= lay.catsTop && my <= lay.catsTop + lay.catsH) {
            String cat = categoryAt(lay, my);
            return cat == null ? "" : "cat:" + cat;
        }
        Hit hit = hit(mx, my, lay);
        return hit == null ? "" : hit.card.key();
    }

    private void paintSearch(GuiGraphics g, Layout lay) {
        int x = lay.searchX;
        int y = lay.searchY;
        int w = lay.searchW;
        SmoothGui.fill(x, y, w, 32, 10, 0x61000000);
        if (searchOn) {
            SmoothGui.stroke(x, y, w, 32, 10, 1.0F, accent);
        } else {
            SmoothGui.stroke(x, y, w, 32, 10, 1.0F, 0x14FFFFFF);
            SmoothGui.fill(x + 12, y + 1, w - 24, 1, 0, 0x1FFFFFFF);
        }
        UiIcons.draw(g, "search", x + 10, y + 9, 14, MUTED);
        String shown = search.isEmpty() ? (searchOn ? "" : ghost()) : search;
        SmoothGui.text(shown, x + 32, y + 9, 13, search.isEmpty() ? MUTED : INK);
        if (!search.isEmpty()) {
            UiIcons.draw(g, "close", x + w - 24, y + 9, 14, MUTED);
        }
        boolean blink = (System.currentTimeMillis() / 500L) % 2L == 0L;
        if (blink && (searchOn || search.isEmpty())) {
            String caretText = searchOn ? search : shown;
            int caret = x + 32 + SmoothGui.width(caretText, 13) + 1;
            SmoothGui.fill(caret, y + 9, 1, 13, 0, accent);
        }
    }

    private static boolean inSearch(Layout lay, double mx, double my) {
        return mx >= lay.searchX && mx <= lay.searchX + lay.searchW && my >= lay.searchY && my <= lay.searchY + 32;
    }

    private static String ghost() {
        String[] phrases = {"Search trigger bot", "Search velocity", "Search flight"};
        long time = System.currentTimeMillis();
        int index = 0;
        while (index < 64) {
            String phrase = phrases[index % phrases.length];
            long type = phrase.length() * 68L;
            long hold = 700L;
            long erase = phrase.length() * 34L;
            long gap = 240L;
            long cycle = type + hold + erase + gap;
            if (time < cycle) {
                if (time < type) {
                    return phrase.substring(0, (int) (time / 68L));
                }
                time -= type;
                if (time < hold) {
                    return phrase;
                }
                time -= hold;
                if (time < erase) {
                    int left = phrase.length() - (int) (time / 34L);
                    return left <= 0 ? "" : phrase.substring(0, left);
                }
                return "";
            }
            time -= cycle;
            index++;
        }
        return phrases[0];
    }

    private static String colorOf(String name) {
        for (String[] item : GuiPages.THEMES) {
            if (item[0].equals(name)) {
                return item[2];
            }
        }
        return "4DA3FF";
    }

    private Hit hit(double mx, double my, Layout lay) {
        int[] colY = new int[] {lay.listTop - (int) scroll, lay.listTop - (int) scroll, lay.listTop - (int) scroll};
        List<GuiPages.Card> rows = visible();
        for (int i = 0; i < rows.size(); i++) {
            GuiPages.Card card = rows.get(i);
            int col = i % 3;
            int x = lay.mainX + 8 + col * (lay.colW + 8);
            int cy = colY[col];
            int cardH = cardHeight(card);
            if (mx >= x && mx <= x + lay.colW && my >= Math.max(cy, lay.listTop) && my <= Math.min(cy + cardH, lay.f.y + lay.f.h - 12)) {
                if (open.contains(card.key())) {
                    int sy = cy + 48;
                    for (Modules.Setting setting : card.settings) {
                        int sh = isSlider(setting) ? 28 : 16;
                        if (my >= sy && my < sy + sh) {
                            return new Hit(card, setting, x, lay.colW);
                        }
                        sy += sh;
                    }
                }
                return new Hit(card, null, x, lay.colW);
            }
            colY[col] += cardH + 10;
        }
        return null;
    }

    private void noteHover(String over) {
        long now = System.currentTimeMillis();
        if (over == null || over.isEmpty()) {
            if (now - lastHover > 120L) {
                hoverId = "";
            }
            return;
        }
        if (over.equals(hoverId)) {
            lastHover = now;
            return;
        }
        hoverId = over;
        lastHover = now;
        if (now < hoverQuiet) {
            return;
        }
        hoverQuiet = now + 180L;
        if (!sounds || this.minecraft == null) {
            return;
        }
        var manager = this.minecraft.getSoundManager();
        if (hoverSound != null) {
            manager.stop(hoverSound);
        }
        hoverSound = SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.12F, 0.22F);
        manager.play(hoverSound);
    }

    private void tickHover(String id, boolean hot) {
        noteHover(hot ? id : "");
    }

    private void play() {
        if (!sounds || this.minecraft == null) {
            return;
        }
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.05F, 0.35F));
    }

    private void slider(int x, int y, int track, int filled) {
        int amount = Math.max(0, Math.min(track, filled));
        SmoothGui.flat(x, y, track, 6, 3, 0xFF2C3644);
        if (amount > 4) {
            SmoothGui.flat(x, y, amount, 6, 3, accent);
        }
        int tx = Math.max(x, Math.min(x + track - 14, x + amount - 7));
        SmoothGui.flat(tx, y - 4, 14, 14, 7, accent);
        SmoothGui.flat(tx + 3, y - 1, 8, 8, 4, 0xFF12161D);
    }

    private static boolean isSlider(Modules.Setting setting) {
        return setting.options == null && !"bind".equals(setting.key);
    }

    private static void setSlider(Modules.Setting setting, double mx, int x, int w) {
        double t = Math.max(0, Math.min(1, (mx - (x + 12)) / Math.max(1, w - 24)));
        setting.value = setting.min + (int) Math.round(t * (setting.max - setting.min));
    }

    private String clip(String text, int width) {
        if (text == null) {
            return "";
        }
        if (SmoothGui.width(text, 11) <= width) {
            return text;
        }
        while (text.length() > 1 && SmoothGui.width(text + "..", 11) > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "..";
    }

    private String word(String value) {
        return GuiLang.text(lang, value);
    }

    private static float hoverStep(String name, boolean hot) {
        // Time based, so the hover tint fades at the same speed at 10 fps or 144 fps.
        float cur = catHover.getOrDefault(name, 0.0F);
        float k = 1.0F - (float) Math.exp(-hoverDt * 13.0F);
        float next = cur + ((hot ? 1.0F : 0.0F) - cur) * k;
        if (next < 0.01F) {
            next = 0.0F;
        }
        catHover.put(name, next);
        return next;
    }

    private void ring(int x, int y, int w, int h, int r, int color, int inner) {
        SmoothGui.fill(x - 1, y - 1, w + 2, h + 2, r + 1, color);
        SmoothGui.fill(x, y, w, h, r, inner);
    }

    // Module panels: same glass, a little less see-through than the window so they read smoother.
    private int panel() {
        int a = shell() >>> 24;
        int alpha = Math.min(255, a + Math.round((255 - a) * 0.38F));
        return (alpha << 24) | 0x16171B;
    }

    private int shell() {
        if (glass <= 0) {
            return 0xFF121212;
        }
        int alpha = Math.round(232 - glass * 1.45F);
        alpha = Math.max(72, Math.min(232, alpha));
        return (alpha << 24) | 0x121212;
    }

    private int backdrop() {
        if (glass <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(10, blurAmt));
    }

    private static int darken(int argb) {
        int r = (int) (((argb >> 16) & 255) * 0.42F);
        int g = (int) (((argb >> 8) & 255) * 0.42F);
        int b = (int) ((argb & 255) * 0.42F);
        return (r << 16) | (g << 8) | b;
    }

    private static int tone(int base, int add, float amount) {
        float t = Math.max(0.0F, Math.min(1.0F, amount));
        int r = Math.round(((base >> 16) & 255) * (1 - t) + ((add >> 16) & 255) * t);
        int g = Math.round(((base >> 8) & 255) * (1 - t) + ((add >> 8) & 255) * t);
        int b = Math.round((base & 255) * (1 - t) + (add & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static boolean light(int rgb) {
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        return 0.2126F * r + 0.7152F * g + 0.0722F * b > 0.55F;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static String profile() {
        try {
            return Minecraft.getInstance().getUser().getName();
        } catch (RuntimeException e) {
            return "Player";
        }
    }

    private record Frame(int x, int y, int w, int h, int side) {
    }

    private record Layout(Frame f, int pad, int searchW, int searchX, int searchY, int mainX, int listTop, int listH, int colW, int profileX, int profileY, int profileW, int catsTop, int catsH, int rowW, int dockX, int dockY, int dockW, int dockH, int flyX, int flyY, int flyW, int flyH) {
    }

    private record Hit(GuiPages.Card card, Modules.Setting setting, int x, int w) {
    }
}
