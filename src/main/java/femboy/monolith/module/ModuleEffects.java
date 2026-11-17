package femboy.monolith.module;

import femboy.monolith.ui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class ModuleEffects {
    private static Double savedGamma;
    private static float savedWalk = 0.1F;
    private static boolean speedApplied;

    private ModuleEffects() {
    }

    private static String flyName() {
        if (ClickGuiScreen.on("DragonFly")) {
            return "DragonFly";
        }
        if (ClickGuiScreen.on("CatFly")) {
            return "CatFly";
        }
        return "Flight";
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        if (ClickGuiScreen.on("Sprint") || (ClickGuiScreen.on("KeepSprint") && player.hurtTime > 0)) {
            player.setSprinting(true);
        }
        boolean fly = ClickGuiScreen.on("Flight") || ClickGuiScreen.on("CatFly") || ClickGuiScreen.on("DragonFly");
        if (fly) {
            float speed = 0.02F + ClickGuiScreen.range(flyName()) / 500.0F;
            if (ClickGuiScreen.mode(flyName()) == 1) {
                speed += 0.04F;
            }
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(speed);
            player.onUpdateAbilities();
            Vec3 flyMotion = player.getDeltaMovement();
            if (ClickGuiScreen.on("CatFly") && client.options.keyJump.isDown()) {
                player.setDeltaMovement(flyMotion.x, Math.max(flyMotion.y, 0.18D), flyMotion.z);
            } else if (ClickGuiScreen.on("DragonFly") && !client.options.keyJump.isDown() && !client.options.keyShift.isDown()) {
                player.setDeltaMovement(flyMotion.x, 0.0D, flyMotion.z);
            }
        } else if (!player.getAbilities().instabuild) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        boolean speed = ClickGuiScreen.on("Speed") || ClickGuiScreen.on("Timer");
        if (speed) {
            float walk = savedWalk;
            if (ClickGuiScreen.on("Speed")) {
                walk += ClickGuiScreen.range("Speed") / 400.0F;
                if (ClickGuiScreen.mode("Speed") == 1) {
                    walk += 0.04F;
                }
            }
            if (ClickGuiScreen.on("Timer")) {
                walk += ClickGuiScreen.range("Timer") / 250.0F;
            }
            player.getAbilities().setWalkingSpeed(walk);
            player.onUpdateAbilities();
            speedApplied = true;
        } else if (speedApplied) {
            player.getAbilities().setWalkingSpeed(savedWalk);
            player.onUpdateAbilities();
            speedApplied = false;
        }
        if (ClickGuiScreen.on("Strafe")) {
            float forward = (client.options.keyUp.isDown() ? 1.0F : 0.0F) - (client.options.keyDown.isDown() ? 1.0F : 0.0F);
            float strafe = (client.options.keyLeft.isDown() ? 1.0F : 0.0F) - (client.options.keyRight.isDown() ? 1.0F : 0.0F);
            if (forward != 0.0F || strafe != 0.0F) {
                float yaw = player.getYRot() * 0.017453292F;
                float sin = net.minecraft.util.Mth.sin(yaw);
                float cos = net.minecraft.util.Mth.cos(yaw);
                double strafeSpeed = 0.16D + ClickGuiScreen.range("Strafe") / 400.0D;
                if (ClickGuiScreen.mode("Strafe") == 1) {
                    strafeSpeed += 0.04D;
                }
                player.setDeltaMovement((strafe * cos - forward * sin) * strafeSpeed, player.getDeltaMovement().y, (forward * cos + strafe * sin) * strafeSpeed);
            }
        }
        if (ClickGuiScreen.on("Spider") && player.horizontalCollision) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.12D + ClickGuiScreen.range("Spider") / 400.0D, motion.z);
        }
        if (ClickGuiScreen.on("HighJump") && client.options.keyJump.isDown() && player.onGround()) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.42D + ClickGuiScreen.range("HighJump") / 250.0D, motion.z);
        }
        if (ClickGuiScreen.on("NoFall")) {
            player.resetFallDistance();
        }
        if ((ClickGuiScreen.on("Jesus") || ClickGuiScreen.on("WaterSpeed")) && player.isInWater()) {
            Vec3 motion = player.getDeltaMovement();
            double len = Math.hypot(motion.x, motion.z);
            double cap = 0.12D + ClickGuiScreen.range(ClickGuiScreen.on("Jesus") ? "Jesus" : "WaterSpeed") / 400.0D;
            if (len > 0.01D) {
                player.setDeltaMovement(motion.x / len * cap, 0.08D, motion.z / len * cap);
            } else {
                player.setDeltaMovement(motion.x, 0.08D, motion.z);
            }
        }
        if (ClickGuiScreen.on("NoSlow") && player.isUsingItem()) {
            player.setSprinting(true);
        }
        if (ClickGuiScreen.on("Velocity") && player.hurtTime == 9) {
            double scale = ClickGuiScreen.mode("Velocity") == 1 ? 0.0D : ClickGuiScreen.range("Velocity") / 100.0D;
            Vec3 knock = player.getDeltaMovement();
            player.setDeltaMovement(knock.x * scale, knock.y * scale, knock.z * scale);
        }
        if (ClickGuiScreen.on("NoWeb") && client.level.getBlockState(player.blockPosition()).is(net.minecraft.world.level.block.Blocks.COBWEB)) {
            Vec3 motion = player.getDeltaMovement();
            double len = Math.hypot(motion.x, motion.z);
            if (len > 0.01D && len < 0.18D) {
                player.setDeltaMovement(motion.x / len * 0.18D, Math.max(motion.y, 0.05D), motion.z / len * 0.18D);
            }
        }
        if (ClickGuiScreen.on("Panic")) {
            ClickGuiScreen.panic();
            ClickGuiScreen.set("Panic", false);
            if (client.screen instanceof ClickGuiScreen) {
                client.setScreen(null);
            }
        }
        boolean bright = ClickGuiScreen.on("Gamma") || ClickGuiScreen.on("FullBright");
        if (bright) {
            if (savedGamma == null) {
                savedGamma = client.options.gamma().get();
            }
            client.options.gamma().set(12.0D);
        } else if (savedGamma != null) {
            client.options.gamma().set(savedGamma);
            savedGamma = null;
        }
    }
}
