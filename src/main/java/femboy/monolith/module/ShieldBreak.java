package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public final class ShieldBreak extends MonolithModule {
    private int restore = -1;
    private int hold;

    public ShieldBreak() {
        super("ShieldBreak");
    }

    @Override
    public void tick(Minecraft client) {
        if (hold > 0) {
            if (--hold == 0 && restore >= 0) {
                client.player.getInventory().setSelectedSlot(restore);
                restore = -1;
            }
            return;
        }
        if (!client.options.keyAttack.isDown()) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("ShieldBreak"));
        if (target == null || !target.isBlocking()) {
            return;
        }
        if (client.player.getAttackStrengthScale(0.0F) < 0.84F) {
            return;
        }
        int axe = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (isAxe(stack.getItem())) {
                axe = i;
                break;
            }
        }
        if (axe < 0) {
            return;
        }
        restore = client.player.getInventory().getSelectedSlot();
        client.player.getInventory().setSelectedSlot(axe);
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND);
        hold = 2;
    }

    private static boolean isAxe(Item item) {
        return item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.COPPER_AXE || item == Items.IRON_AXE || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE;
    }
}
