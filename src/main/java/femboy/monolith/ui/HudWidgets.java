package femboy.monolith.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudWidgets {
    public static final class Widget {
        public final String id;
        public final String name;
        public int x;
        public int y;
        public boolean enabled = true;
        public int scale = 100;
        public int flags;

        Widget(String id, String name, int x, int y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    private static final List<Widget> WIDGETS = new ArrayList<>();
    private static final Path FILE = Path.of(System.getProperty("user.home"), ".monolith", "hud-v3.txt");
    private static volatile String spotify = "Not playing";
    private static long nextPoll;
    private static double lastX;
    private static double lastZ;
    private static long lastMove;
    private static double bps;
    private static boolean arranged;

    static {
        WIDGETS.add(new Widget("watermark", "Watermark", 12, 12));
        WIDGETS.add(new Widget("fps", "FPS", 12, 52));
        WIDGETS.add(new Widget("spotify", "Spotify", 12, 84));
        WIDGETS.add(new Widget("coords", "Coords", 12, 124));
        WIDGETS.add(new Widget("armor", "Armor", 12, 156));
        WIDGETS.add(new Widget("potions", "Potions", 12, 188));
        WIDGETS.add(new Widget("array", "Array list", 12, 228));
        WIDGETS.add(new Widget("target", "Target", 12, 268));
        load();
    }

    private HudWidgets() {
    }

    public static void arrange(int viewW, int viewH) {
        if (arranged) {
            return;
        }
        arranged = true;
        Widget mark = get("watermark");
        if (mark != null && mark.x > 180) {
            return;
        }
        place(get("watermark"), (viewW - 280) / 2, 16);
        place(get("fps"), viewW - 132, 16);
        place(get("spotify"), viewW - 240, 56);
        place(get("array"), viewW - 150, 104);
        place(get("potions"), viewW - 150, 210);
        place(get("target"), (viewW - 140) / 2, viewH - 88);
        place(get("coords"), 188, viewH - 42);
        place(get("armor"), 188, viewH - 78);
        save();
    }

    private static void place(Widget widget, int x, int y) {
        if (widget == null) {
            return;
        }
        widget.x = Math.max(8, x);
        widget.y = Math.max(8, y);
    }

    public static List<Widget> all() {
        return WIDGETS;
    }

    public static Widget get(String id) {
        for (Widget widget : WIDGETS) {
            if (widget.id.equals(id)) {
                return widget;
            }
        }
        return null;
    }

    public static void render(GuiGraphics g, boolean editor) {
        Minecraft client = Minecraft.getInstance();
        pollSpotify();
        sampleSpeed(client);
        float hud = ClickGuiScreen.hud();
        int viewW = Math.max(1, Math.round(g.guiWidth() / hud));
        int viewH = Math.max(1, Math.round(g.guiHeight() / hud));
        arrange(viewW, viewH);
        g.pose().pushMatrix();
        g.pose().scale(hud, hud);
        SmoothGui.begin(g, client.font);
        for (Widget widget : WIDGETS) {
            if (!widget.enabled) {
                continue;
            }
            draw(g, client, widget, viewW, viewH);
        }
        SmoothGui.end(g);
        g.pose().popMatrix();
    }

    public static int width(Widget widget) {
        return Math.max(96, SmoothGui.width(line(Minecraft.getInstance(), widget), 12) + ("spotify".equals(widget.id) ? 28 : 22));
    }

    public static int height(Widget widget) {
        if ("array".equals(widget.id) || "potions".equals(widget.id)) {
            return Math.max(28, 16 + lines(Minecraft.getInstance(), widget).size() * 14);
        }
        return 28;
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            StringBuilder out = new StringBuilder();
            for (Widget widget : WIDGETS) {
                out.append(widget.id).append(' ')
                        .append(widget.x).append(' ')
                        .append(widget.y).append(' ')
                        .append(widget.enabled ? 1 : 0).append(' ')
                        .append(widget.scale).append(' ')
                        .append(widget.flags).append('\n');
            }
            Files.writeString(FILE, out.toString());
        } catch (IOException ignored) {
        }
    }

    private static void load() {
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(FILE)) {
                String[] part = line.split(" ");
                if (part.length < 6) {
                    continue;
                }
                Widget widget = get(part[0]);
                if (widget == null) {
                    continue;
                }
                widget.x = Integer.parseInt(part[1]);
                widget.y = Integer.parseInt(part[2]);
                widget.enabled = "1".equals(part[3]);
                widget.scale = Integer.parseInt(part[4]);
                widget.flags = Integer.parseInt(part[5]);
            }
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static void draw(GuiGraphics g, Minecraft client, Widget widget, int viewW, int viewH) {
        int w = width(widget);
        int h = height(widget);
        widget.x = Math.max(4, Math.min(viewW - w - 4, widget.x));
        widget.y = Math.max(4, Math.min(viewH - h - 4, widget.y));
        float s = widget.scale / 100.0F;
        g.pose().pushMatrix();
        g.pose().translate(widget.x, widget.y);
        g.pose().scale(s, s);
        g.pose().translate(-widget.x, -widget.y);
        SmoothGui.glass(widget.x, widget.y, w, h, 12, ClickGuiScreen.glassColor(), false);
        SmoothGui.flat(widget.x + 8, widget.y + 8, 3, h - 16, 2, ClickGuiScreen.accentColor());
        int y = widget.y + 8;
        for (String row : lines(client, widget)) {
            SmoothGui.text(row, widget.x + 16, y, 12, 0xFFF2F0EB);
            y += 14;
        }
        g.pose().popMatrix();
    }

    private static int tone(int base, int add, float amount) {
        int r = Math.round(((base >> 16) & 255) * (1 - amount) + ((add >> 16) & 255) * amount);
        int g = Math.round(((base >> 8) & 255) * (1 - amount) + ((add >> 8) & 255) * amount);
        int b = Math.round((base & 255) * (1 - amount) + (add & 255) * amount);
        return (r << 16) | (g << 8) | b;
    }

    private static String line(Minecraft client, Widget widget) {
        List<String> rows = lines(client, widget);
        String longest = "";
        for (String row : rows) {
            if (SmoothGui.width(row, 12) > SmoothGui.width(longest, 12)) {
                longest = row;
            }
        }
        return longest;
    }

    private static List<String> lines(Minecraft client, Widget widget) {
        List<String> rows = new ArrayList<>();
        LocalPlayer player = client.player;
        switch (widget.id) {
            case "watermark" -> {
                rows.add("Monolith");
                if (has(widget, 0) && client.player != null) {
                    rows.add(client.player.getName().getString());
                }
            }
            case "fps" -> rows.add(client.getFps() + " fps");
            case "spotify" -> rows.add(clip(spotify, 28));
            case "coords" -> rows.add(player == null ? "0 0 0" : (int) player.getX() + "  " + (int) player.getY() + "  " + (int) player.getZ());
            case "armor" -> rows.add("Armor  " + armor(player) + "%");
            case "target" -> rows.add(target(client));
            case "potions" -> {
                if (player == null || player.getActiveEffects().isEmpty()) {
                    rows.add("No effects");
                } else {
                    player.getActiveEffects().forEach(effect -> rows.add(effect.getEffect().value().getDisplayName().getString()));
                }
            }
            case "array" -> {
                boolean any = false;
                for (var entry : femboy.monolith.module.Modules.all()) {
                    if (entry.enabled()) {
                        rows.add(entry.name);
                        any = true;
                    }
                }
                if (!any) {
                    rows.add("No modules");
                }
            }
            default -> rows.add(widget.name);
        }
        return rows;
    }

    private static String watermark(Minecraft client, Widget widget) {
        List<String> bits = new ArrayList<>();
        bits.add("Monolith");
        if (has(widget, 0)) {
            bits.add(client.player == null ? "Player" : client.player.getName().getString());
        }
        if (has(widget, 1)) {
            bits.add("1 UID");
        }
        if (has(widget, 2)) {
            bits.add(client.getFps() + " fps");
        }
        if (has(widget, 3)) {
            bits.add(client.getCurrentServer() == null ? "Singleplayer" : client.getCurrentServer().name);
        }
        if (has(widget, 4)) {
            bits.add(ping(client) + " ms");
        }
        if (has(widget, 5)) {
            bits.add("20 tps");
        }
        if (has(widget, 6)) {
            bits.add(String.format(Locale.ROOT, "%.1f bps", bps));
        }
        return String.join("  ·  ", bits);
    }

    static boolean has(Widget widget, int bit) {
        return (widget.flags & (1 << bit)) != 0;
    }

    static void toggle(Widget widget, int bit) {
        widget.flags ^= 1 << bit;
        save();
    }

    private static int ping(Minecraft client) {
        if (client.player == null || client.player.connection == null) {
            return 0;
        }
        var info = client.player.connection.getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    private static int armor(LocalPlayer player) {
        if (player == null) {
            return 0;
        }
        return Math.min(100, player.getArmorValue() * 5);
    }

    private static String target(Minecraft client) {
        if (client.hitResult instanceof EntityHitResult hit) {
            return hit.getEntity().getName().getString();
        }
        return "No target";
    }

    private static String clip(String text, int max) {
        if (text == null || text.length() <= max) {
            return text == null ? "" : text;
        }
        return text.substring(0, max - 1) + "…";
    }

    private static void sampleSpeed(Minecraft client) {
        if (client.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastMove == 0L) {
            lastX = client.player.getX();
            lastZ = client.player.getZ();
            lastMove = now;
            return;
        }
        if (now - lastMove < 250L) {
            return;
        }
        double dx = client.player.getX() - lastX;
        double dz = client.player.getZ() - lastZ;
        bps = Math.hypot(dx, dz) / ((now - lastMove) / 1000.0);
        lastX = client.player.getX();
        lastZ = client.player.getZ();
        lastMove = now;
    }

    private static void pollSpotify() {
        long now = System.currentTimeMillis();
        if (now < nextPoll) {
            return;
        }
        nextPoll = now + 2000L;
        Thread thread = new Thread(() -> {
            try {
                Process process = new ProcessBuilder("osascript", "-e",
                        "tell application \"Spotify\" to if player state is playing then (artist of current track as text) & \"  —  \" & (name of current track as text) else \"Not playing\"")
                        .redirectErrorStream(true)
                        .start();
                String text = new String(process.getInputStream().readAllBytes()).trim();
                if (!text.isEmpty() && !text.toLowerCase(Locale.ROOT).contains("error")) {
                    spotify = text;
                } else {
                    spotify = "Not playing";
                }
            } catch (IOException e) {
                spotify = "Not playing";
            }
        }, "monolith-spotify");
        thread.setDaemon(true);
        thread.start();
    }
}
