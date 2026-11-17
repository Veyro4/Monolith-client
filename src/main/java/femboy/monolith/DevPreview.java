package femboy.monolith;

import femboy.monolith.ui.ClickGuiScreen;
import femboy.monolith.ui.GuiPages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Dev-only check. Only active with -Dmonolith.preview (runPreview: over the title screen) or
 * -Dmonolith.previewWorld (runPreviewWorld: inside run/saves/New World). It opens the click GUI
 * with a few Combat modules on and saves screenshots to run/screenshots: open-NN (GUI pop),
 * settled, hover-NN (Movement row lift), switch-NN (cards bouncing in after clicking Utility),
 * back-NN (Combat again) and dock (profile panel), and logs the fps with the GUI open and closed.
 * The pointer is moved inside Minecraft's mouse handler only, never the real cursor.
 * Normal launches never touch this.
 */
final class DevPreview {
    static final boolean WORLD = Boolean.getBoolean("monolith.previewWorld");
    static final boolean ON = WORLD || Boolean.getBoolean("monolith.preview");
    // Positions in GUI units at GUI scale 2 on a 1920x1080 window (the preview run setup).
    private static final double ROW_X = 125.0;
    private static final double COMBAT_Y = 104.0;
    private static final double MOVEMENT_Y = 138.0;
    private static final double UTILITY_Y = 172.0;
    private static final double PROFILE_X = 142.0;
    private static final double PROFILE_Y = 493.0;
    private static int ticks = -1;
    private static int ready;

    private DevPreview() {
    }

    static void tick(Minecraft client) {
        if (!ON || ticks == Integer.MAX_VALUE) {
            return;
        }
        if (ticks < 0) {
            if (!start(client)) {
                return;
            }
            List<GuiPages.Card> combat = GuiPages.cards().stream().filter(c -> "Combat".equals(c.category)).toList();
            for (int i = 0; i < combat.size(); i += 2) {
                if (!combat.get(i).enabled()) {
                    combat.get(i).toggle();
                }
            }
            fps(client, "gui closed");
            pointer(client, 900.0, 520.0);
            client.setScreen(new ClickGuiScreen());
            ticks = 0;
            return;
        }
        ticks++;
        if (ticks <= 12) {
            shot(client, "open-%02d", ticks);
        } else if (ticks == 24) {
            shot(client, "settled", 0);
        } else if (ticks >= 30 && ticks <= 36) {
            // Re-applied every tick: in a world something resets the pointer after we set it.
            pointer(client, ROW_X, MOVEMENT_Y);
            if (ticks > 30) {
                shot(client, "hover-%02d", ticks - 30);
            }
        } else if (ticks == 50) {
            click(client, ROW_X, UTILITY_Y);
        } else if (ticks > 50 && ticks <= 60) {
            shot(client, "switch-%02d", ticks - 50);
        } else if (ticks == 70) {
            click(client, ROW_X, COMBAT_Y);
        } else if (ticks > 70 && ticks <= 76) {
            shot(client, "back-%02d", ticks - 70);
        } else if (ticks == 90) {
            click(client, PROFILE_X, PROFILE_Y);
        } else if (ticks == 100) {
            shot(client, "dock", 0);
        } else if (ticks == 110) {
            click(client, PROFILE_X, PROFILE_Y);
            pointer(client, 900.0, 520.0);
        } else if (ticks == 150) {
            // Two quiet seconds (no screenshots) so the counter isn't skewed by screenshot stalls.
            fps(client, "gui open");
            ticks = Integer.MAX_VALUE;
        }
    }

    private static boolean start(Minecraft client) {
        if (client.getOverlay() != null) {
            return false;
        }
        if (!WORLD) {
            return client.screen instanceof TitleScreen;
        }
        // In the world with nothing open; give the terrain a few seconds to load in.
        if (client.player == null || client.level == null || client.screen != null) {
            ready = 0;
            return false;
        }
        return ++ready >= 100;
    }

    private static void fps(Minecraft client, String when) {
        System.out.println("[monolith-preview] fps " + when + ": " + client.getFps());
    }

    private static void click(Minecraft client, double x, double y) {
        pointer(client, x, y);
        if (client.screen != null) {
            client.screen.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)), false);
        }
    }

    private static void shot(Minecraft client, String name, int n) {
        Screenshot.grab(client.gameDirectory, "monolith-" + String.format(name, n) + ".png", client.getMainRenderTarget(), 1, msg -> {
        });
    }

    private static void pointer(Minecraft client, double guiX, double guiY) {
        try {
            double sx = client.getWindow().getScreenWidth() / (double) client.getWindow().getGuiScaledWidth();
            double sy = client.getWindow().getScreenHeight() / (double) client.getWindow().getGuiScaledHeight();
            set(client.mouseHandler, "xpos", guiX * sx);
            set(client.mouseHandler, "ypos", guiY * sy);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void set(MouseHandler handler, String name, double value) throws ReflectiveOperationException {
        Field field = MouseHandler.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setDouble(handler, value);
    }
}
