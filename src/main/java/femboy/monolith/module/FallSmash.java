package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;


public final class FallSmash extends MonolithModule {
    private boolean hit;

    public FallSmash() {
        super("FallSmash");
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player.onGround()) {
            hit = false;
            return;
        }
        if (hit || client.player.fallDistance < 1.5D) {
            return;
        }
        if (client.player.getDeltaMovement().y > -0.08D) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("FallSmash"));
        if (target == null || client.player.getAttackStrengthScale(0.0F) < 0.8F) {
            return;
        }
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);
        hit = true;
    }
}
