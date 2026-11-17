package femboy.monolith.module;

import femboy.monolith.MonolithModule;
import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class AimAssist extends MonolithModule {
    public AimAssist() {
        super("AimAssist");
    }

    @Override
    public void tick(Minecraft client) {
        if (ClickGuiScreen.mode("AimAssist") == 0 && !client.options.keyAttack.isDown()) {
            return;
        }
        LivingEntity target = Combat.nearest(client, Combat.reach("AimAssist"));
        if (target == null || !Combat.allow("AimAssist", target)) {
            return;
        }
        float step = ClickGuiScreen.mode("AimAssist") == 0
                ? Modules.setting("AimAssist", "hspeed", 22) / 10.0F
                : 8.0F;
        Vec3 eye = client.player.getEyePosition();
        Vec3 point = target.position().add(0.0D, target.getBbHeight() * 0.72D, 0.0D);
        double dx = point.x - eye.x;
        double dy = point.y - eye.y;
        double dz = point.z - eye.z;
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
        float yawDelta = Mth.wrapDegrees(yaw - client.player.getYRot());
        if (Math.abs(yawDelta) > Modules.setting("AimAssist", "fov", 35)) {
            return;
        }
        float vertical = ClickGuiScreen.mode("AimAssist") == 0
                ? Modules.setting("AimAssist", "vspeed", 20) / 10.0F
                : 6.0F;
        client.player.setYRot(client.player.getYRot() + Mth.clamp(yawDelta, -step, step));
        client.player.setXRot(Mth.clamp(client.player.getXRot() + Mth.clamp(pitch - client.player.getXRot(), -vertical, vertical), -89.0F, 89.0F));
    }
}
