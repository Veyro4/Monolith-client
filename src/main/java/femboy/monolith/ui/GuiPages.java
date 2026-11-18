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
            {"Arctic Blue", "Blues", "3DA0FF", "5CFFFF", "8AB4FF", "04101C"},
            {"Glacier", "Blues", "2EE6FF", "7AF0FF", "B8FBFF", "042028"},
            {"Skyline", "Blues", "1CB8FF", "4DDCFF", "7A9BFF", "041424"},
            {"Cobalt Night", "Blues", "3B6BFF", "6C9BFF", "2F4DFF", "050814"},
            {"Emerald", "Greens", "00E39A", "3DFFC2", "00C47A", "001810"},
            {"Forest Glass", "Greens", "2BFF6A", "7DFF9A", "12D94A", "001408"},
            {"Seafoam", "Greens", "2DFFE0", "7DFFE8", "A8FFD8", "001816"},
            {"Violet Prism", "Purples", "9A4DFF", "E070FF", "6E5CFF", "120018"},
            {"Lavender Haze", "Purples", "C9A6FF", "E4D4FF", "B07CFF", "140820"},
            {"Rose Quartz", "Pinks", "FF4D7A", "FF8AD4", "FF6A8A", "1C0610"},
            {"Blush", "Pinks", "FF7A9A", "FFB0C4", "FF4D78", "1C0814"},
            {"Cherry", "Reds", "FF2A2A", "FF5A3C", "FF7A6A", "1A0000"},
            {"Solar Gold", "Yellows", "FFC21A", "FFE56A", "FF9A00", "1A1000"},
            {"Tangerine", "Oranges", "FF7A1A", "FFB15A", "FF4D00", "1A0800"},
            {"Obsidian", "Monochrome", "8AA0B8", "C5D4E4", "5C7088", "020408"},
            {"Silver Mist", "Monochrome", "E8EEF4", "FFFFFF", "B8C4D0", "101418"},
            {"Neon Cyan", "Neon", "00F0FF", "5CFFFF", "A8FCFF", "001418"},
            {"Neon Pink", "Neon", "FF2BD6", "FF6AE0", "D07CFF", "1A0014"},
            {"Arctic Rose", "Pastel", "FF8AD4", "5CFFFF", "C9A6FF", "100818"},
            {"Frost White", "Pastel", "F2F7FF", "E8FBFF", "D4C6FF", "101418"}
    };
    public static final String[] DOCK = {"Language", "Menu scale", "HUD scale", "Glass", "Font", "Stream proof", "Sounds", "Blur"};
    public static final String[] PANES = {"lang", "scale", "hud", "quality", "font", "stream", "sounds", "blur"};
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
