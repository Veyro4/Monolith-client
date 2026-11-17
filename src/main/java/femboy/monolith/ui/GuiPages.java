package femboy.monolith.ui;

import femboy.monolith.module.Modules;

import java.util.ArrayList;
import java.util.List;

public final class GuiPages {
    public static final String[][] GROUPS = {
            {"Main", "Combat", "Movement", "Utility", "Visuals", "HUD"},
            {"Server", "Donut SMP"},
            {"Social", "Friends"},
            {"Menu", "Scripts", "Configs", "Themes", "Client"}
    };
    public static final String[] FILTERS = {"All", "Blues", "Greens", "Purples", "Pinks", "Reds", "Oranges", "Yellows", "Monochrome", "Pastel", "Neon"};
    public static final String[][] THEMES = {
            {"Arctic Blue", "Blues", "4DA3FF", "67E8F9", "C4B5FD", "0B1B33"},
            {"Glacier", "Blues", "67E8F9", "BAE6FD", "E0F2FE", "0C4A6E"},
            {"Skyline", "Blues", "38BDF8", "7DD3FC", "A5B4FC", "082F49"},
            {"Cobalt Night", "Blues", "2563EB", "60A5FA", "1D4ED8", "0B1220"},
            {"Emerald", "Greens", "10B981", "34D399", "059669", "022C22"},
            {"Forest Glass", "Greens", "22C55E", "86EFAC", "166534", "052E16"},
            {"Seafoam", "Greens", "5EEAD4", "99F6E4", "A7F3D0", "134E4A"},
            {"Violet Prism", "Purples", "8B5CF6", "F0ABFC", "6366F1", "1A0B24"},
            {"Lavender Haze", "Purples", "C4B5FD", "DDD6FE", "A78BFA", "312E81"},
            {"Rose Quartz", "Pinks", "FB7185", "F9A8D4", "FDA4AF", "3F1220"},
            {"Blush", "Pinks", "FDA4AF", "FECDD3", "FB7185", "4A1942"},
            {"Cherry", "Reds", "EF4444", "F87171", "FB7185", "450A0A"},
            {"Solar Gold", "Yellows", "FBBF24", "FDE68A", "F59E0B", "451A03"},
            {"Tangerine", "Oranges", "FB923C", "FDBA74", "F97316", "431407"},
            {"Obsidian", "Monochrome", "475569", "64748B", "1E293B", "020617"},
            {"Silver Mist", "Monochrome", "CBD5E1", "E2E8F0", "94A3B8", "1E293B"},
            {"Neon Cyan", "Neon", "22D3EE", "67E8F9", "A5F3FC", "083344"},
            {"Neon Pink", "Neon", "FF4FD8", "F472B6", "C4B5FD", "500724"},
            {"Arctic Rose", "Pastel", "F9A8D4", "67E8F9", "C4B5FD", "1E1B4B"},
            {"Frost White", "Pastel", "DDEBFF", "E0F2FE", "C4B5FD", "1E293B"}
    };
    public static final String[] DOCK = {"Language", "GUI scale", "Glass", "Font", "Stream proof", "Sounds", "Blur"};
    public static final String[] PANES = {"lang", "scale", "quality", "font", "stream", "sounds", "blur"};
    private static final List<Card> CARDS = new ArrayList<>();

    static {
        combat("Aim Assist", "AimAssist", "Turns toward the player you are fighting.");
        combat("Trigger Bot", "TriggerBot", "Attacks under the crosshair.");
        combat("Velocity", "Velocity", "Cuts knockback.");
        combat("Auto Shield", "AutoShield", "Raises the offhand shield.");
        combat("Shield Break", "ShieldBreak", "Swaps to an axe on a block.");
        combat("Hit Web", "HitWeb", "Places a cobweb on the player you hit.");
        combat("Lunge", "Lunge", "Steps toward the player you just hit.");
        combat("Fall Smash", "FallSmash", "Attacks at the bottom of a fall.");
        combat("Combo", "Combo", "S-taps or W-taps to keep the trade.");
        combat("Pearl Catch", "PearlCatch", "Aims a rod at your own pearl.");
        combat("Keep Sprint", "KeepSprint", "Keeps sprint after a hit.");
        add("Combat", "Hit Select", "Waits for your hit window.", range("Chance", 60, 0, 100));
        add("Combat", "Reach", "Extends your attack range.", range("Range", 31, 30, 35));
        move("Sprint", "Sprint", "Holds sprint.");
        move("Flight", "Flight", "Lets you fly.");
        move("Cat Fly", "CatFly", "Flight with a faster vertical step.");
        move("Dragon Fly", "DragonFly", "Flight that keeps height while you strafe.");
        move("Speed", "Speed", "Raises walk speed.");
        move("Strafe", "Strafe", "Keeps speed while you change direction.");
        move("Timer", "Timer", "Speeds the walk timer.");
        move("No Fall", "NoFall", "Clears fall distance.");
        move("Spider", "Spider", "Climbs walls you walk into.");
        move("High Jump", "HighJump", "Raises jump height.");
        move("Jesus", "Jesus", "Pushes you up in water.");
        move("Water Speed", "WaterSpeed", "Moves faster in water.");
        move("No Slow", "NoSlow", "Keeps sprint while you use an item.");
        move("No Web", "NoWeb", "Pushes you through cobweb.");
        add("Utility", "Auto Tool", "Swaps to the right tool.", bind());
        add("Utility", "Chest Stealer", "Takes chest items.", range("Delay", 2, 0, 10));
        add("Utility", "Fast Place", "Places blocks faster.", bind());
        add("Utility", "Middle Click", "Friend or pearl.", mode("Action", "Friend", "Pearl"));
        add("Visuals", "ESP", "Outlines players.", mode("Mode", "Box", "Glow"));
        live("Visuals", "Full Bright", "FullBright", "Locks brightness.");
        live("Visuals", "Gamma", "Gamma", "Raises brightness.");
        add("Visuals", "Nametags", "Names through walls.", bind());
        add("Visuals", "Trails", "Draws a movement trail.", range("Length", 12, 4, 40));
        Card watermark = new Card("HUD", "Watermark", "Client name on screen.", null, List.of(bind()));
        watermark.stored = true;
        CARDS.add(watermark);
        Card array = new Card("HUD", "Array List", "Enabled modules.", null, List.of(mode("Side", "Right", "Left")));
        array.stored = true;
        CARDS.add(array);
        add("HUD", "Target", "Info on the player you hit.", bind());
        add("HUD", "Armor", "Armor and durability.", bind());
        add("HUD", "Coords", "Your position.", bind());
        add("HUD", "Potions", "Active effects.", bind());
        add("Donut SMP", "Worth", "Shows item worth.", bind());
        add("Donut SMP", "Spawner Finder", "Marks nearby spawners.", range("Range", 32, 8, 64));
        add("Donut SMP", "Auction", "Highlights cheap listings.", range("Discount", 15, 0, 50));
        add("Friends", "Add Friend", "Type a name, then add.", bind());
        add("Friends", "Middle Add", "Middle click a player to add.", bind());
        add("Friends", "Team Check", "Skip friends in combat.", bind());
        add("Scripts", "Loader", "Runs scripts from the scripts folder.", bind());
        add("Scripts", "Auto Run", "Starts enabled scripts on join.", bind());
        add("Configs", "Default", "Everyday settings.", mode("Action", "Load", "Save"));
        add("Configs", "Legit", "Low settings.", mode("Action", "Load", "Save"));
        add("Configs", "Blatant", "High settings.", mode("Action", "Load", "Save"));
        add("Client", "Notifications", "Toggle toasts.", bind());
        live("Client", "Panic", "Panic", "Turns combat and movement off and closes the menu.");
    }

    private GuiPages() {
    }

    public static List<Card> cards() {
        return CARDS;
    }

    public static boolean shown(String name) {
        for (Card card : CARDS) {
            if (card.name.equals(name)) {
                return card.enabled();
            }
        }
        return false;
    }

    private static void combat(String name, String live, String desc) {
        live("Combat", name, live, desc);
    }

    private static void move(String name, String live, String desc) {
        live("Movement", name, live, desc);
    }

    private static void live(String category, String name, String liveName, String desc) {
        Modules.Entry live = Modules.get(liveName);
        CARDS.add(new Card(category, name, desc, live, live == null ? List.of(bind()) : live.settings));
    }

    private static void add(String category, String name, String desc, Modules.Setting... settings) {
        CARDS.add(new Card(category, name, desc, null, List.of(settings)));
    }

    private static Modules.Setting bind() {
        return Modules.control("bind", "Bind", -1, -1, 400, null);
    }

    private static Modules.Setting range(String label, int value, int min, int max) {
        return Modules.control("range", label, value, min, max, null);
    }

    private static Modules.Setting mode(String label, String... options) {
        return Modules.control("mode", label, 0, 0, options.length - 1, options);
    }

    public static final class Card {
        public final String category;
        public final String name;
        public final String desc;
        public final Modules.Entry live;
        public final List<Modules.Setting> settings;
        public boolean stored;

        Card(String category, String name, String desc, Modules.Entry live, List<Modules.Setting> settings) {
            this.category = category;
            this.name = name;
            this.desc = desc;
            this.live = live;
            this.settings = settings;
        }

        public boolean enabled() {
            return live == null ? stored : live.enabled();
        }

        public void toggle() {
            if (live == null) {
                stored = !stored;
            } else {
                live.toggle();
            }
        }

        public String key() {
            return category + "|" + name;
        }
    }
}
