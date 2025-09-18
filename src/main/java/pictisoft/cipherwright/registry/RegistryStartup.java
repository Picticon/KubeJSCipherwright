package pictisoft.cipherwright.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public class RegistryStartup
{
    public static void register(IEventBus modEventBus)
    {
        BlockRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        ContainerRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        FluidRegistry.register(modEventBus);
    }
}