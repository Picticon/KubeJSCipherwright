package pictisoft.cipherwright.registry;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;

public class ItemRegistry
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CipherWrightMod.MODID);

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> DEAD_WATER_BUCKET = ITEMS.register("dead_water_bucket",
            () -> new BucketItem(FluidRegistry.DEAD_WATER_SOURCE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> DEADER_WATER_BUCKET = ITEMS.register("deader_water_bucket",
            () -> new BucketItem(FluidRegistry.DEADER_WATER_SOURCE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> DEADEST_WATER_BUCKET = ITEMS.register("deadest_water_bucket",
            () -> new BucketItem(FluidRegistry.DEADEST_WATER_SOURCE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

}
