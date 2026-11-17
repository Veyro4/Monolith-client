package femboy.monolith;

import femboy.monolith.module.AimAssist;
import femboy.monolith.module.AutoShield;
import femboy.monolith.module.Combo;
import femboy.monolith.module.FallSmash;
import femboy.monolith.module.HitWeb;
import femboy.monolith.module.Lunge;
import femboy.monolith.module.ModuleEffects;
import femboy.monolith.module.PearlCatch;
import femboy.monolith.module.ShieldBreak;
import femboy.monolith.module.TriggerBot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import femboy.monolith.ui.MonolithHud;
import femboy.monolith.ui.ClickGuiScreen;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

public final class MonolithClient implements ClientModInitializer {
    public static final ModuleBus BUS = new ModuleBus();
    public static KeyMapping menuKey;
    private static boolean shiftWasDown;
    private static boolean guiWarmed;

    @Override
    public void onInitializeClient() {
        femboy.monolith.ui.LiquidGlass.install();
        femboy.monolith.ui.TextCache.prewarm();
        // Warm the click GUI once, invisibly, the first time the title screen draws.
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!guiWarmed && screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((s, graphics, mx, my, delta) -> {
                    if (!guiWarmed) {
                        guiWarmed = true;
                        ClickGuiScreen.warm(graphics);
                    }
                });
            }
        });
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.monolith.menu",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RSHIFT,
                KeyMapping.Category.MISC
        ));
        BUS.register(new TriggerBot());
        BUS.register(new AimAssist());
        BUS.register(new Combo());
        BUS.register(new AutoShield());
        BUS.register(new ShieldBreak());
        BUS.register(new HitWeb());
        BUS.register(new Lunge());
        BUS.register(new PearlCatch());
        BUS.register(new FallSmash());
        try {
            HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("monolith", "hud"), (graphics, delta) -> {
                // Joining a world straight from launch skips the title screen; warm the GUI here then.
                if (!guiWarmed) {
                    guiWarmed = true;
                    ClickGuiScreen.warm(graphics);
                }
                MonolithHud.render(graphics);
            });
        } catch (Throwable ignored) {
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean shiftDown = InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_RSHIFT);
            if ((shiftDown && !shiftWasDown) || menuKey.consumeClick()) {
                if (client.screen instanceof ClickGuiScreen) {
                    client.setScreen(null);
                } else if (client.screen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            shiftWasDown = shiftDown;
            ClickGuiScreen.pollBinds();
            BUS.tick(client);
            ModuleEffects.tick(client);
            DevPreview.tick(client);
        });
    }
}
