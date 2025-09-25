package pictisoft.cipherwright.util;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeJsonFetcher
{
    public static String getRecipeJson(IntegratedServer server, ResourceLocation recipeId)
    {
        RecipeManager recipeManager = server.getRecipeManager();
        Optional<? extends Recipe<?>> recipe = recipeManager.byKey(recipeId);

        if (recipe.isPresent())
        {
            ResourceManager resourceManager = server.getResourceManager();
            ResourceLocation filePath = new ResourceLocation(recipeId.getNamespace(), "recipes/" + recipeId.getPath() + ".json");
            try
            {
                Optional<Resource> resource = resourceManager.getResource(filePath);
                if (resource.isPresent())
                {
                    try (var reader = resource.get().openAsReader())
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
        }
        return null;
    }
}