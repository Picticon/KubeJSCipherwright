package pictisoft.cipherwright.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;

public class CreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CipherWrightMod.MODID);

    public static void register(IEventBus modEventBus)
    {
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("kjscw_tab", () -> CreativeModeTab.builder()//
            .icon(() -> new ItemStack(ItemRegistry.DEAD_WATER_BUCKET.get()))//
            .title(Component.translatable("creativetab.kjscw_tab"))//
            .displayItems((params, output) -> {
                output.accept(BlockRegistry.KUBEJS_TABLE_BLOCK.get());
                output.accept(ItemRegistry.DEAD_WATER_BUCKET.get());
            }).build());
}