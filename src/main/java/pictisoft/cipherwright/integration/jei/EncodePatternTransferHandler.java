package pictisoft.cipherwright.integration.jei;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
import pictisoft.cipherwright.cipher.*;
import pictisoft.cipherwright.network.EntityToServerIntStringMessage;
import pictisoft.cipherwright.network.MessageRegistry;
import pictisoft.cipherwright.network.RecipeMessage;
import pictisoft.cipherwright.registry.ContainerRegistry;
import pictisoft.cipherwright.util.*;

import java.util.Objects;
import java.util.Optional;

public class EncodePatternTransferHandler implements IUniversalRecipeTransferHandler<KubeJSTableContainer>
{
    private final IRecipeTransferHandlerHelper helper;

    public EncodePatternTransferHandler(IRecipeTransferHandlerHelper transferHelper)
    {
        this.helper = transferHelper;
    }

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
        if (recipe instanceof Recipe craftingRecipe)
        {
            try
            {
                var recipeId = craftingRecipe.getId();
                if (!Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getSingleplayerServer() == null)
                {
                    return helper.createUserErrorWithTooltip(Component.literal("JEI Integration only available on local servers."));
                }

                var jsonString = RecipeJsonFetcher.getRecipeJson(Minecraft.getInstance().getSingleplayerServer(), recipeId);
                //jsonString = null; // testing
                if (jsonString != null)
                {
                    //if (doTransfer) Chatter.chat("json found for " + craftingRecipe.getId());
                    JsonElement jsonRoot = JsonParser.parseString(jsonString);
                    if (jsonRoot.isJsonObject())
                    {
                        JsonObject json = jsonRoot.getAsJsonObject();
                        if (json.has("type"))
                        {
                            var possibletype = new ResourceLocation(json.get("type").getAsString());
                            var cipher = CipherJsonLoader.getCipherByRecipeId(possibletype);
                            if (cipher != null && !cipher.getInputs().isEmpty())
                            {
                                // valid cipher
                                if (doTransfer)
                                {
                                    //Chatter.chat("Found " + cipher.getName());
                                    sendRecipe(menu, recipeId);
                                }
                                return null;
                            }
                        }
                    }
                    return helper.createUserErrorWithTooltip(Component.literal("Recipe type not supported."));
                } else
                {
                    // no json found. it is most likely generated, possibly kubejs.
                    if (recipe instanceof ShapelessRecipe shapeless)
                    {
                        // we'll fake a shapeless recipe and passing a generated json
                        var template = """
                                {
                                  "type": "minecraft:crafting_shapeless",
                                  "ingredients": <<ingredients>>,
                                  "result": <<result>>
                                }
                                """;
                        var ingredients = shapeless.getIngredients();
                        template = template.replace("<<ingredients>>", ItemAndIngredientHelpers.ingredientsWithNBTToJson(ingredients).toString());
                        template = template.replace("<<result>>",
                                ItemAndIngredientHelpers.itemStackToJson(shapeless.getResultItem(RegistryAccess.EMPTY)).toString());
                        if (doTransfer)
                        {
                            //Chatter.chat("Doing shapeless");
                            sendRecipeJson(menu, template);
                        }
                        return null;
                    }
                    if (recipe instanceof ShapedRecipe shaped)
                    {
                        // we'll fake a shaped recipe and passing a generated json
                        var template = """
                                {
                                    "type": "minecraft:crafting_shaped",
                                    "key": <<key>>,
                                    "pattern": <<pattern>>,
                                    "result": <<result>>
                                  }
                                """;
                        var ingredients = shaped.getIngredients(); // these are inserted top-left to top-right in order
                        if (ingredients.size() > 9) return helper.createInternalError();
                        var result = shaped.getResultItem(RegistryAccess.EMPTY);
                        var array = new CWIngredient[shaped.getHeight()][shaped.getWidth()];
                        var idx = 0;
                        for (var y = 0; y < shaped.getHeight(); y++)
                        {
                            for (var x = 0; x < shaped.getWidth(); x++)
                            {
                                if (idx < ingredients.size())
                                {
                                    array[y][x] = CWIngredient.of(ingredients.get(idx));
                                }
                                idx++;
                            }
                        }
                        var re = new RecipeEncoder(array);
                        template = template.replace("<<result>>", ItemAndIngredientHelpers.itemStackToJson(result).toString());
                        template = template.replace("<<key>>", RecipeHelper.encodePatternKey(re.getIngredientMap()));
                        template = template.replace("<<pattern>>", RecipeHelper.encodePattern(re.getPattern()));
                        if (doTransfer)
                        {
                            //Chatter.chat("Doing shaped");
                            sendRecipeJson(menu, template);
                        }
                        return null;
                    }
                    if (doTransfer)
                    {
                        Component start = Component.literal("Failed to find JSON for: ");
                        Component clickable = Component.literal(recipeId.toString())
                                .setStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, recipeId.toString()))
                                        .withColor(ChatFormatting.AQUA)
                                        .withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal("Click to copy")
                                        ))
                                );
                        Component end = Component.literal(" This is most likely a generated recipe.");
                        Component fullMessage = start.copy().append(clickable).append(end);
                        Chatter.chat(fullMessage);
                    }
                    return helper.createUserErrorWithTooltip(Component.literal("JSON not found. Recipe entry was most likely generated at startup. If this was a KubeJS script, remove the script and reload recipes, or paste the modified JSON into the table."));
                }
            } catch (Exception e)
            {
                // Handle errors (e.g., resource not found)
                //System.err.println("Failed to load recipe JSON" + e.getMessage());
            }
        }
        return helper.createInternalError();
    }

    private void sendRecipe(KubeJSTableContainer menu, ResourceLocation recipeId)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null)
        {
            // Tell server we need to change the recipe id changed
            MessageRegistry.INSTANCE.sendToServer(
                    new RecipeMessage(menu.getBlockEntity().getLevel(), menu.getBlockEntity().getBlockPos(), recipeId));
        }
    }

    private void sendRecipeJson(KubeJSTableContainer menu, String json)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null)
        {
            MessageRegistry.INSTANCE.sendToServer(
                    new EntityToServerIntStringMessage(menu.getBlockEntity().getLevel(), menu.getBlockEntity().getBlockPos(),
                            KubeJSTableBlockEntity.HANDLE_JSON_AS_RECIPE, json));
        }
    }
}
