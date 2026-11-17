package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public final class Combo extends MonolithModule {
    private int tap;
    private boolean stapHold;

    public Combo() {
        super("Combo");
        this.enabled = false;
    }

    @Override
    public void tick(Minecraft client) {
        if (tap > 0) {
            tap--;
            if (tap == 0) {
                if (stapHold) {
                    client.options.keyDown.setDown(false);
                    stapHold = false;
                } else {
                    client.options.keyUp.setDown(true);
                }
            }
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("Combo"));
        if (target == null) {
            return;
        }
        if (client.player.getAttackStrengthScale(0.0F) > 0.2F) {
            return;
        }
        double dist = client.player.distanceTo(target);
        double away = Combat.away(client.player, target);
        int delay = Math.min(4, (Combat.ping(client) + 25) / 50);
        boolean stap = ClickGuiScreen.mode("Combo") == 0;
        if (stap && client.player.hurtTime > 0 && dist < 3.1D) {
            tap = 2 + delay;
            stapHold = true;
            client.options.keyDown.setDown(true);
        } else if (!stap && (dist < 2.45D || away < 0.08D)) {
            tap = 2 + delay;
            client.player.setSprinting(false);
            client.options.keyUp.setDown(false);
        }
    }
}
