package femboy.monolith.module;

import femboy.monolith.MonolithClient;
import femboy.monolith.MonolithModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Modules {
    public static final String[] CATEGORIES = {"Combat", "Movement", "Player", "Render", "Misc"};
    private static final Map<String, Entry> BY_NAME = new LinkedHashMap<>();
    private static final List<Entry> ALL = new ArrayList<>();

    static {
        combat("AimAssist", "Turns toward a player while you attack.", mode("Legit", "Blatant"), slider("range", "Range", 42, 24, 60), slider("fov", "FOV", 35, 8, 90), slider("hspeed", "Horizontal", 22, 4, 80), slider("vspeed", "Vertical", 20, 4, 80), players(true), bind());
        combat("TriggerBot", "Attacks the entity under your crosshair.", mode("None", "Normal", "Precise"), slider("delay", "Delay", 4, 0, 20), slider("range", "Range", 36, 24, 48), players(true), bind());
        combat("AutoShield", "Raises the offhand shield against a close player.", slider("range", "Range", 36, 24, 48), players(true), bind());
        combat("ShieldBreak", "Swaps to an axe when a player is blocking.", slider("range", "Range", 34, 24, 48), bind());
        combat("HitWeb", "Places a cobweb on the player you hit.", slider("range", "Range", 42, 24, 56), slider("delay", "Cooldown", 8, 2, 20), bind());
        combat("Lunge", "Steps toward the player you just hit.", slider("range", "Range", 38, 24, 52), mode("Step", "Dash"), bind());
        combat("FallSmash", "Attacks at the bottom of a fall.", slider("range", "Range", 34, 24, 48), bind());
        combat("Combo", "S-taps or W-taps to keep the trade.", mode("S-tap", "W-tap"), slider("range", "Range", 34, 24, 48), bind());
        combat("PearlCatch", "Aims a rod at your own pearl.", slider("range", "Range", 40, 24, 56), bind());
        move("Sprint", "Holds sprint while you move.", bind());
        move("KeepSprint", "Puts sprint back after a hit.", bind());
        move("Flight", "Lets you fly.", slider("range", "Speed", 50, 10, 90), mode("Normal", "Fast"), bind());
        move("CatFly", "Flight with a faster vertical step.", slider("range", "Speed", 55, 10, 90), mode("Normal", "Fast"), bind());
        move("DragonFly", "Flight that keeps height while you strafe.", slider("range", "Speed", 60, 10, 90), mode("Normal", "Fast"), bind());
        move("Speed", "Raises walk speed.", slider("range", "Boost", 40, 10, 90), mode("Normal", "Blatant"), bind());
        move("Strafe", "Keeps speed while you change direction.", slider("range", "Boost", 40, 10, 90), mode("Normal", "Blatant"), bind());
        move("Timer", "Speeds the walk timer.", slider("range", "Boost", 30, 10, 80), bind());
        move("Spider", "Climbs walls you walk into.", slider("range", "Climb", 40, 10, 90), bind());
        move("HighJump", "Raises jump height.", slider("range", "Height", 40, 10, 90), bind());
        move("NoFall", "Clears fall distance.", bind());
        move("Jesus", "Pushes you up in water.", slider("range", "Lift", 30, 10, 70), bind());
        move("WaterSpeed", "Moves faster in water.", slider("range", "Speed", 30, 10, 70), bind());
        move("NoSlow", "Keeps sprint while you use an item.", bind());
        move("NoWeb", "Pushes you through cobweb.", bind());
        move("Velocity", "Cuts knockback from hits.", mode("Reduce", "Cancel"), slider("range", "Amount", 20, 0, 100), bind());
        player("Panic", "Turns combat and movement off and closes the menu.", bind());
        render("Gamma", "Raises brightness.", bind());
        render("FullBright", "Locks brightness on.", bind());
    }

    private Modules() {
    }

    private static void combat(String name, String desc, Setting... settings) {
        add("Combat", name, desc, settings);
    }

    private static void move(String name, String desc, Setting... settings) {
        add("Movement", name, desc, settings);
    }

    private static void player(String name, String desc, Setting... settings) {
        add("Player", name, desc, settings);
    }

    private static void render(String name, String desc, Setting... settings) {
        add("Render", name, desc, settings);
    }

    private static Setting mode(String... options) {
        Setting setting = new Setting("mode", "Mode", 0, options.length - 1, options);
        return setting;
    }

    private static Setting slider(String key, String label, int value, int min, int max) {
        Setting setting = new Setting(key, label, min, max, null);
        setting.value = value;
        return setting;
    }

    private static Setting players(boolean on) {
        Setting setting = new Setting("players", "Targets", 0, 1, new String[]{"All", "Players"});
        setting.value = on ? 1 : 0;
        return setting;
    }

    private static Setting bind() {
        Setting setting = new Setting("bind", "Bind", -1, 400, null);
        setting.value = -1;
        return setting;
    }

    private static void add(String category, String name, String desc, Setting... settings) {
        MonolithModule live = null;
        for (MonolithModule module : MonolithClient.BUS.modules()) {
            if (module.name.equals(name)) {
                live = module;
                break;
            }
        }
        Entry entry = new Entry(category, name, desc, live, List.of(settings));
        BY_NAME.put(name, entry);
        ALL.add(entry);
    }

    public static List<Entry> all() {
        return ALL;
    }

    public static Entry get(String name) {
        return BY_NAME.get(name);
    }

    public static boolean on(String name) {
        Entry entry = BY_NAME.get(name);
        return entry != null && entry.enabled();
    }

    public static int range(String name) {
        return value(name, "range", 40);
    }

    public static int mode(String name) {
        return value(name, "mode", 0);
    }

    public static int delay(String name) {
        return value(name, "delay", 0);
    }

    public static boolean playersOnly(String name) {
        return value(name, "players", 0) == 1;
    }

    public static Setting control(String key, String label, int value, int min, int max, String[] options) {
        Setting setting = new Setting(key, label, min, max, options);
        setting.value = value;
        return setting;
    }

    public static int setting(String name, String key, int fallback) {
        return value(name, key, fallback);
    }

    private static int value(String name, String key, int fallback) {
        Entry entry = BY_NAME.get(name);
        if (entry == null) {
            return fallback;
        }
        for (Setting setting : entry.settings) {
            if (setting.key.equals(key)) {
                return setting.value;
            }
        }
        return fallback;
    }

    public static void set(String name, boolean enabled) {
        Entry entry = BY_NAME.get(name);
        if (entry != null) {
            entry.setEnabled(enabled);
        }
    }

    public static void panic() {
        for (Entry entry : ALL) {
            if ("Combat".equals(entry.category) || "Movement".equals(entry.category)) {
                entry.setEnabled(false);
            }
        }
    }

    public static final class Entry {
        public final String category;
        public final String name;
        public final String desc;
        public final MonolithModule live;
        public final List<Setting> settings;
        private boolean stored;

        Entry(String category, String name, String desc, MonolithModule live, List<Setting> settings) {
            this.category = category;
            this.name = name;
            this.desc = desc;
            this.live = live;
            this.settings = settings;
            this.stored = live != null && live.enabled;
        }

        public boolean enabled() {
            return live == null ? stored : live.enabled;
        }

        public void setEnabled(boolean value) {
            stored = value;
            if (live != null) {
                live.enabled = value;
            }
        }

        public void toggle() {
            setEnabled(!enabled());
        }
    }

    public static final class Setting {
        public final String key;
        public final String label;
        public final int min;
        public final int max;
        public final String[] options;
        public int value;
        public boolean wasDown;

        Setting(String key, String label, int min, int max, String[] options) {
            this.key = key;
            this.label = label;
            this.min = min;
            this.max = max;
            this.options = options;
        }

        public String text() {
            if ("bind".equals(key)) {
                if (value < 0) {
                    return "None";
                }
                return switch (value) {
                    case 256 -> "Esc";
                    case 340, 344 -> "Shift";
                    case 341, 345 -> "Ctrl";
                    case 342, 346 -> "Alt";
                    default -> value >= 65 && value <= 90 ? String.valueOf((char) value) : "K" + value;
                };
            }
            if (options != null) {
                int index = Math.max(0, Math.min(options.length - 1, value));
                return options[index];
            }
            return Integer.toString(value);
        }
    }
}
