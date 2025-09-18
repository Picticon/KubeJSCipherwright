package pictisoft.cipherwright.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;

public class ContainerRegistry
{
    public static final DeferredRegister<MenuType<?>> MOD_MENUS = DeferredRegister.create(Registries.MENU, CipherWrightMod.MODID);

    public static void register(IEventBus eventBus)
    {
        MOD_MENUS.register(eventBus);
    }

    public static final RegistryObject<MenuType<KubeJSTableContainer>> KUBEJS_TABLE_CONTAINER = MOD_MENUS
            .register("kubejs_table_container", () -> IForgeMenuType.create(
                    (windowId, inv, data) -> new KubeJSTableContainer(windowId, data.readBlockPos(), inv, inv.player)));
}
