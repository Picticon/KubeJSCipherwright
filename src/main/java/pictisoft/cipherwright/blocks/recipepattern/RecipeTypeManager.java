//package pictisoft.cipherwright.blocks.recipepattern;
//
//import com.google.gson.JsonObject;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.ResourceManager;
//import net.minecraft.util.GsonHelper;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.InputStream;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//public class RecipeTypeManager {
//    private static final Logger LOGGER = LogManager.getLogger();
//    private static final Map<ResourceLocation, RecipeType> TYPES = new HashMap<>();
//
//    public static void loadRecipeTypes(ResourceManager resourceManager) {
//        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
//                "recipe_types", location -> location.getPath().endsWith(".json"));
//
//        TYPES.clear();
//
//        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
//            try (InputStream stream = entry.getValue().getInputStream()) {
//                JsonObject json = GsonHelper.parse(new InputStreamReader(stream));
//                RecipeType type = deserializeRecipeType(json);
//                TYPES.put(type.getId(), type);
//            } catch (Exception e) {
//                LOGGER.error("Failed to load recipe type {}", entry.getKey(), e);
//            }
//        }
//    }
//
//    private static RecipeType deserializeRecipeType(JsonObject json) {
//        // Parse JSON into RecipeType
//        // ...
//    }
//
//    public static RecipeType getRecipeType(ResourceLocation id) {
//        return TYPES.get(id);
//    }
//
//    public static Collection<RecipeType> getAllTypes() {
//        return TYPES.values();
//    }
//}