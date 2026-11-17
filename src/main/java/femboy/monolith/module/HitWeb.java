package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HitWeb extends MonolithModule {
    private long last;

    public HitWeb() {
        super("HitWeb");
    }

    @Override
    public void tick(Minecraft client) {
        int delay = Math.max(2, ClickGuiScreen.delay("HitWeb"));
        if (client.player.getAttackStrengthScale(0.0F) > 0.2F || client.player.tickCount % delay != 0 || System.currentTimeMillis() - last < delay * 50L) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("HitWeb"));
        if (target == null || target.hurtTime < 8) {
            return;
        }
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getItem(i).getItem() == Items.COBWEB) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return;
        }
        BlockPos pos = target.blockPosition();
        int old = client.player.getInventory().getSelectedSlot();
        client.player.getInventory().setSelectedSlot(slot);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, 0.5, 0), Direction.UP, pos.below(), false);
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
        client.player.swing(InteractionHand.MAIN_HAND);
        client.player.getInventory().setSelectedSlot(old);
        last = System.currentTimeMillis();
    }
}
