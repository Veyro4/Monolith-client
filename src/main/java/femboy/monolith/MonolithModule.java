package femboy.monolith;

import net.minecraft.client.Minecraft;

public abstract class MonolithModule {
    public final String name;
    public boolean enabled;

    protected MonolithModule(String name) {
        this.name = name;
    }

    public abstract void tick(Minecraft client);
}
