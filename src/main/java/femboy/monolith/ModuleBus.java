package femboy.monolith;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ModuleBus {
    private final List<MonolithModule> modules = new ArrayList<>();

    public void register(MonolithModule module) {
        modules.add(module);
    }

    public List<MonolithModule> modules() {
        return modules;
    }

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        for (MonolithModule module : modules) {
            if (module.enabled) {
                module.tick(client);
            }
        }
    }

    public void toggleChat(Minecraft client) {
        if (client.player == null) {
            return;
        }
        StringBuilder line = new StringBuilder("Monolith");
        for (MonolithModule module : modules) {
            module.enabled = !module.enabled;
            line.append(' ').append(module.name).append(module.enabled ? ":on" : ":off");
        }
        client.player.displayClientMessage(Component.literal(line.toString()), false);
    }
}
