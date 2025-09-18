package pictisoft.cipherwright.integration.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
import pictisoft.cipherwright.network.MessageRegistry;
import pictisoft.cipherwright.network.RecipeMessage;
import pictisoft.cipherwright.registry.ContainerRegistry;
import pictisoft.cipherwright.util.RecipeJsonFetcher;

import java.util.Optional;

public class EncodePatternTransferHandler implements IUniversalRecipeTransferHandler<KubeJSTableContainer>
{
    @Override
    public Class<KubeJSTableContainer> getContainerClass()
    {
        return KubeJSTableContainer.class;
    }

    @Override
    public Optional<MenuType<KubeJSTableContainer>> getMenuType()
    {
        return Optional.of(ContainerRegistry.KUBEJS_TABLE_CONTAINER.get());
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(KubeJSTableContainer menu, Object recipe, IRecipeSlotsView recipeSlots,
            Player player, boolean maxTransfer, boolean doTransfer)
    {
        if (!doTransfer) return null;
        if (recipe instanceof Recipe isrecipe)
        {
            try
            {
                var recipeId = isrecipe.getId();

                var json = RecipeJsonFetcher.getRecipeJson(Minecraft.getInstance().getSingleplayerServer(), recipeId);

                if (json != null)
                {
                    //Chatter.chat(json);

                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null)
                    {
                        // Tell server we need to change the recipe id changed
                        MessageRegistry.INSTANCE.sendToServer(new RecipeMessage(menu.getBlockEntity().getLevel(), menu.getBlockEntity().getBlockPos(), recipeId));
                    }
                }


                // Get the RecipeManager (optional, for validation)
                //RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

                // Get the ResourceManager from the server's data pack registries
//                ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
//
//                // Construct the path to the recipe JSON
//                String jsonPath2 = "data/" + recipeId.getNamespace() + "/recipes/" + recipeId.getPath() + ".json";
//                String jsonPath = recipeId.getPath() + ".json";
//                ResourceLocation jsonLocation = new ResourceLocation(recipeId.getNamespace(), jsonPath);
//
//                //var ser = ((Recipe<?>) recipe).getSerializer();
//
//                // Fetch the resource
//                var resource = resourceManager.getResource(jsonLocation);
//                if (resource.isPresent())
//                {
//                    // Read the JSON content
//                    try (BufferedReader reader = resource.get().openAsReader())
//                    {
//                        StringBuilder jsonContent = new StringBuilder();
//                        String line;
//                        while ((line = reader.readLine()) != null)
//                        {
//                            jsonContent.append(line);
//                        }
//                        Chatter.chat(jsonContent.toString());
//                    }
//                }
            } catch (Exception e)
            {
                // Handle errors (e.g., resource not found)
                System.err.println("Failed to load recipe JSON" + e.getMessage());
                return null;
            }
        }
        return null;
    }
}
