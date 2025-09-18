package pictisoft.cipherwright.util;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeJsonFetcher
{
    private static final Gson GSON = new Gson();

    public static String getRecipeJson(MinecraftServer server, ResourceLocation recipeId)
    {
        RecipeManager recipeManager = server.getRecipeManager();
        Optional<? extends Recipe<?>> recipe = recipeManager.byKey(recipeId);

        if (recipe.isPresent())
        {
            Recipe<?> foundRecipe = recipe.get();
            RecipeSerializer<?> serializer = foundRecipe.getSerializer();
            ResourceManager resourceManager = server.getResourceManager();

            ResourceLocation filePath = new ResourceLocation(recipeId.getNamespace(), "recipes/" + recipeId.getPath() + ".json");
            try
            {
                Optional<Resource> resource = resourceManager.getResource(filePath);
                if (resource.isPresent())
                {
                    try (BufferedReader reader = resource.get().openAsReader())
                    {
                        return reader.lines().collect(Collectors.joining("\n"));
                    } catch (IOException e)
                    {
                        System.out.println("Failed to read recipe JSON: " + e.getMessage());
                        return null;
                    }
                }
            } catch (Exception e)
            {
                System.out.println("Failed to read recipe JSON: " + e.getMessage());
                return null;
            }

//            ResourceLocation serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
//            if (serializerId == null) return Optional.empty(); // Avoid null issues
//
//            // Manually serialize the recipe if the serializer does not provide a direct method
//            JsonObject json = new JsonObject();
//            json.addProperty("type", serializerId.toString());
//            json.addProperty("id", foundRecipe.getId().toString());
//
//            // Convert the recipe object to JSON using Gson
//            JsonElement recipeJson = GSON.toJsonTree(foundRecipe);
//            json.add("data", recipeJson);
//
//            return Optional.of(json);
//        }
//
//        return Optional.empty();
        }
        return null;
    }
}