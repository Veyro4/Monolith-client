package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class PearlCatch extends MonolithModule {
    private int caught = -1;

    public PearlCatch() {
        super("PearlCatch");
    }

    @Override
    public void tick(Minecraft client) {
        int rod = -1;
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getItem(i).getItem() == Items.FISHING_ROD) {
                rod = i;
                break;
            }
        }
        if (rod < 0) {
            return;
        }
        ThrownEnderpearl pearl = null;
        double best = Math.max(8.0D, ClickGuiScreen.range("PearlCatch") / 2.0D);
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof ThrownEnderpearl thrown && thrown.getOwner() == client.player && client.player.distanceTo(thrown) < best) {
                best = client.player.distanceTo(thrown);
                pearl = thrown;
            }
        }
        if (pearl == null || pearl.getId() == caught) {
            return;
        }
        Vec3 pos = pearl.position().add(pearl.getDeltaMovement().scale(4.0D));
        Vec3 eye = client.player.getEyePosition();
        double dx = pos.x - eye.x;
        double dy = pos.y - eye.y;
        double dz = pos.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = Mth.clamp((float) (-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)))), -89.0F, 89.0F);
        client.player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, client.player.onGround(), client.player.horizontalCollision));
        int old = client.player.getInventory().getSelectedSlot();
        client.player.getInventory().setSelectedSlot(rod);
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        client.player.getInventory().setSelectedSlot(old);
        caught = pearl.getId();
    }
}
