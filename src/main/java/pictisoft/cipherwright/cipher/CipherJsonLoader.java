package pictisoft.cipherwright.cipher;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pictisoft.cipherwright.CipherWrightMod;

import java.util.*;

@Mod.EventBusSubscriber(modid = CipherWrightMod.MODID)
public class CipherJsonLoader extends SimpleJsonResourceReloadListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private final Map<ResourceLocation, JsonObject> loadedData = new HashMap<>();
    private final Map<ResourceLocation, Cipher> cipherData = new HashMap<>();
    private static final String FOLDER_NAME = "ciphers";

    public CipherJsonLoader()
    {
        super(GSON, FOLDER_NAME);
    }

//    /// This is a UNIQUE template for a Cipher
//    public static Cipher getCipherByLocationId(ResourceLocation templateId)
//    {
//        for (Map.Entry<ResourceLocation, Cipher> entry : getCiphers().entrySet())
//        {
//            ResourceLocation key = entry.getKey();
//            var element = entry.getValue();
//            if (key == templateId) return element;
//        }
//        LOGGER.info("Could not locate a match for template {}", templateId.toString());
//        return getCipherByRecipeId(new ResourceLocation("minecraft:smelting"));
//    }

    /// This returns the first Cipher for a recipe type ID, e.g. "minecraft:crafting_shaped"
    public static @Nullable Cipher getCipherByRecipeId(ResourceLocation recipeTypeId)
    {
        for (Map.Entry<ResourceLocation, Cipher> entry : getCiphers().entrySet())
        {
            var element = entry.getValue();
            if (element.getRecipeTypeId().equals(recipeTypeId)) return element;
        }
        // LOGGER.info("Could not locate a match for recipe type {}", recipeTypeId.toString());
        return null;
        //return getCipherByRecipeId(new ResourceLocation("minecraft:smelting"));
    }

    public static List<String> getSortedCipherCategoryNames()
    {
        var ret = new ArrayList<String>();
        for (var cipher : getSortedCiphers())
        {
            if (ret.contains(cipher.getCategory())) continue;
            ret.add(cipher.getCategory());
        }
        ret.sort(String::compareTo);
        return ret;
    }

    public static List<Cipher> getSortedCipherChildren(String s)
    {
        var ret = new ArrayList<Cipher>();
        for (var c : getSortedCiphers())
        {
            if (c.getCategory().equals(s))
                ret.add(c);
        }
        ret.sort(Comparator.comparing(Cipher::getName));
        return ret;
    }

    public static Cipher getDefaultCipher()
    {
        if (getCipherByRecipeId(new ResourceLocation("minecraft", "crafting_shaped")) != null)
            return getCipherByRecipeId(new ResourceLocation("minecraft", "crafting_shaped"));
        if (getCipherByRecipeId(new ResourceLocation("minecraft", "crafting_shapeless")) != null)
            return getCipherByRecipeId(new ResourceLocation("minecraft", "crafting_shapeless"));
        if (getCiphers().isEmpty()) return new Cipher();
        return getCiphers().entrySet().stream().findFirst().get().getValue();
    }


    // this is called when the resources are loading or reloading.
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler)
    {
        loadedData.clear();
        cipherData.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet())
        {
            ResourceLocation key = entry.getKey();
            JsonElement element = entry.getValue();

            try
            {
                if (element.isJsonObject())
                {
                    loadedData.put(key, element.getAsJsonObject());
                    var cipher = Cipher.TryParse(element.getAsJsonObject());
                    if (cipher != null)
                        cipherData.put(key, cipher);

                    LOGGER.debug("Loaded JSON file: {}", key);
                } else
                {
                    LOGGER.warn("Skipping non-JSON-object file: {}", key);
                }
            } catch (Exception e)
            {
                LOGGER.error("Error loading JSON file {}: {}", key, e.getMessage());
            }
        }
        LOGGER.info("Loaded {} Cipher recipe types from {}", loadedData.size(), FOLDER_NAME);
    }

    // Getter for accessing the loaded data
    protected static Map<ResourceLocation, JsonObject> getLoadedData()
    {
        return cipherJsonLoader.loadedData;
    }

    // Getter for accessing the loaded data
    public static Map<ResourceLocation, Cipher> getCiphers()
    {
        return cipherJsonLoader.cipherData;
    }

    // Getter for accessing the loaded data
    public static List<String> getSortedCipherNames()
    {
        List<String> options = new ArrayList<>();
        //for (var i = 0; i < 10; i++)
        {
            for (var entry : CipherJsonLoader.getCiphers().entrySet())
            {
                options.add(entry.getValue().getName());
            }
        }
        options.sort(String::compareTo);
        return options;
    }

    // Getter for accessing the loaded data
    public static List<Cipher> getSortedCiphers() // sorted by name
    {
        List<Cipher> options = new ArrayList<>();
        //for (var i = 0; i < 10; i++)
        {
            for (var entry : CipherJsonLoader.getCiphers().entrySet())
            {
                options.add(entry.getValue());
            }
        }
        options.sort(Comparator.comparing(Cipher::getName));
        return options;
    }

    public static CipherJsonLoader cipherJsonLoader = new CipherJsonLoader();

    // Register the listener with the event bus
    @SubscribeEvent
    public static void registerReloadListener(AddReloadListenerEvent event)
    {
        event.addListener(cipherJsonLoader);
    }
}