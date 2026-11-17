package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public final class AutoShield extends MonolithModule {
    public AutoShield() {
        super("AutoShield");
    }

    @Override
    public void tick(Minecraft client) {
        if (client.player.getOffhandItem().getItem() != Items.SHIELD) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("AutoShield"));
        boolean incoming = target instanceof Player attacker && attacker.hurtTime == 0 && client.player.distanceTo(attacker) < 3.2D && attacker.getAttackStrengthScale(0.0F) > 0.7F;
        boolean ready = client.player.getAttackStrengthScale(0.0F) > 0.9F && client.options.keyAttack.isDown();
        client.options.keyUse.setDown(incoming && !ready);
        if (incoming && !ready && !client.player.isUsingItem()) {
            client.gameMode.useItem(client.player, InteractionHand.OFF_HAND);
        }
    }
}
