package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class TriggerBot extends MonolithModule {
    public TriggerBot() {
        super("TriggerBot");
        this.enabled = false;
    }

    @Override
    public void tick(Minecraft client) {
        HitResult hit = client.hitResult;
        if (!(hit instanceof EntityHitResult entityHit)) {
            return;
        }
        Entity target = entityHit.getEntity();
        if (!target.isAlive() || target == client.player || !Combat.allow("TriggerBot", target)) {
            return;
        }
        if (client.player.distanceTo(target) > Combat.reach("TriggerBot")) {
            return;
        }
        int delay = Math.max(0, ClickGuiScreen.delay("TriggerBot"));
        if (delay > 0 && client.player.tickCount % (delay + 1) != 0) {
            return;
        }
        int mode = ClickGuiScreen.mode("TriggerBot");
        if (mode == 0) {
            return;
        }
        float need = mode == 2 ? 0.99F : 0.9F;
        if (client.player.getAttackStrengthScale(0.0F) < need) {
            return;
        }
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);
    }
}
