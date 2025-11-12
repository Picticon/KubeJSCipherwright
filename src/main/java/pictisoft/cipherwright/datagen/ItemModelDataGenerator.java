package pictisoft.cipherwright.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.registry.ItemRegistry;

public class ItemModelDataGenerator extends ItemModelProvider
{
    public ItemModelDataGenerator(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, CipherWrightMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        simpleItem(ItemRegistry.DEAD_WATER_BUCKET);
        simpleItem(ItemRegistry.DEADER_WATER_BUCKET);
        simpleItem(ItemRegistry.DEADEST_WATER_BUCKET);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item)
    {
        return withExistingParent(item.getId().getPath(), new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(CipherWrightMod.MODID, "item/" + item.getId().getPath()));
    }

}
