package pictisoft.cipherwright.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pictisoft.cipherwright.CipherWrightMod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = CipherWrightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
        PackOutput output = generator.getPackOutput();

        // client
        generator.addProvider(event.includeClient(), new BlockstateDataGenerator(output, fileHelper));
        generator.addProvider(event.includeClient(), new ItemModelDataGenerator(output, fileHelper));
        generator.addProvider(event.includeClient(), new LanguageDataGenerator(output, CipherWrightMod.MODID, "en_us"));
        //generator.addProvider(event.includeClient(), new SoundDataGenerator(output, fileHelper));

        // Server Data
        //generator.addProvider(event.includeServer(), new RecipeDataGenerator(output));
        //generator.addProvider(event.includeServer(), LootTableDataGenerator.create(output));
        //BlockTagDataGenerator blockTagGenerator = generator.addProvider(event.includeServer(), new BlockTagDataGenerator(output, provider, fileHelper));
        //generator.addProvider(event.includeServer(), new ItemTagDataGenerator(output, provider, blockTagGenerator.contentsGetter(), fileHelper));

        //generator.addProvider(event.includeServer(), new WorldGenProvider(output, provider));

        //generator.addProvider(event.includeServer(), new FluidTagDataGenerator(output, provider, fileHelper));

        //
        //generator.addProvider(event.includeServer(), new DatapackEntriesGenerator(output, provider));
    }
}