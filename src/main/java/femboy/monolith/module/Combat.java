package femboy.monolith.module;

import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    private Combat() {
    }

    public static int ping(Minecraft client) {
        if (client.player == null || client.player.connection == null) {
            return 0;
        }
        var info = client.player.connection.getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }

    public static double reach(String name) {
        return Math.max(2.0D, ClickGuiScreen.range(name) / 10.0D);
    }

    public static boolean allow(String name, Entity entity) {
        return !ClickGuiScreen.playersOnly(name) || entity instanceof Player;
    }

    public static LivingEntity nearest(Minecraft client, double range) {
        LocalPlayer self = client.player;
        LivingEntity best = null;
        double bestDist = range;
        for (Player player : client.level.players()) {
            if (player == self || !player.isAlive() || player.isSpectator()) {
                continue;
            }
            double dist = self.distanceTo(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    public static double away(LocalPlayer self, LivingEntity target) {
        Vec3 delta = target.position().subtract(self.position());
        Vec3 motion = target.getDeltaMovement();
        return motion.x * delta.x + motion.z * delta.z;
    }
}
