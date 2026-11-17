package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class Lunge extends MonolithModule {
    public Lunge() {
        super("Lunge");
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player.getAttackStrengthScale(0.0F) > 0.15F) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("Lunge"));
        if (target == null || target.hurtTime != 10) {
            return;
        }
        Vec3 delta = target.position().subtract(client.player.position());
        double flat = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (flat < 0.2D) {
            return;
        }
        double step = ClickGuiScreen.mode("Lunge") == 0 ? 0.28D : 0.48D;
        client.player.setDeltaMovement(client.player.getDeltaMovement().add(delta.x / flat * step, 0.08D, delta.z / flat * step));
    }
}
