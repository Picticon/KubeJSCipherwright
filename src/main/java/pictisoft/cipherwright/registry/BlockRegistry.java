package pictisoft.cipherwright.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlock;

public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CipherWrightMod.MODID);

    public static void register(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
    }

    public static final RegistryObject<Block> KUBEJS_TABLE_BLOCK = registerBlockAndItem("kubejs_table_block",
            KubeJSTableBlock::new);


    // registers the block and the matching item
    private static <T extends Block> RegistryObject<T> registerBlockAndItem(String name, com.google.common.base.Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // registers the block only, no item
    private static <T extends Block> RegistryObject<T> registerBlockWithNoItem(String name, com.google.common.base.Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        return toReturn;
    }

    // registers the item for the block
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block)
    {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

}
