package pictisoft.cipherwright.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlock;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableScreen;
import pictisoft.cipherwright.registry.BlockRegistry;
import pictisoft.cipherwright.registry.ItemRegistry;

public class LanguageDataGenerator extends LanguageProvider
{

    public LanguageDataGenerator(PackOutput output, String modid, String locale)
    {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations()
    {
        add(KubeJSTableBlock.KUBEJS_TABLE_TITLE, "Cipher Wright");
        add(BlockRegistry.KUBEJS_TABLE_BLOCK.get(), "Cipher Wright");
        add(ItemRegistry.DEAD_WATER_BUCKET.get(), "Bucket of Dead Water");

    }
}