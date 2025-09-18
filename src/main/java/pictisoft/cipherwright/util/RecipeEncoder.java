package pictisoft.cipherwright.util;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import pictisoft.cipherwright.cipher.CWIngredient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipeEncoder
{
    private final Map<CWIngredient, String> ingredientMap;
    private final String[] pattern;

    public RecipeEncoder(CWIngredient[][] ingredientGrid)
    {
        this.ingredientMap = new HashMap<>();
        this.pattern = encodeRecipe(ingredientGrid);
    }

    public RecipeEncoder(CWIngredient[][] ingredientGrid, boolean shrink)
    {
        this.ingredientMap = new HashMap<>();
        this.pattern = encodeRecipe(shrink ? trim(ingredientGrid) : ingredientGrid);
    }

    public static JsonElement ingredientFromTag(TagKey<Item> tagitem) // no counts...
    {
        JsonObject result = new JsonObject();
        result.addProperty("tag", tagitem.location().toString());
//        if (stack.getCount() > 1)
//        {
//            result.addProperty("count", stack.getCount());
//        }
        return result;
    }

    private String[] encodeRecipe(CWIngredient[][] grid)
    {
        // Build map of unique ingredients to characters
        Set<CWIngredient> uniqueIngredients = new HashSet<>();

        for (CWIngredient[] row : grid)
        {
            for (CWIngredient ingredient : row)
            {
                var found = findIngredient(uniqueIngredients, ingredient);
                if (ingredient != null && !ingredient.isEmpty() && found == null)
                {
                    uniqueIngredients.add(ingredient);
                }
            }
        }

        // Assign characters to ingredients
        assignCharacters(uniqueIngredients);

        if (grid.length == 0 || grid[0].length == 0) return new String[0];

        // Build pattern array
        String[] result = new String[grid.length];
        for (int x = 0; x < grid.length; x++)
        {
            StringBuilder rowBuilder = new StringBuilder();
            for (int y = 0; y < grid[0].length; y++)
            {
                CWIngredient ingredient = grid[x][y];
                if (ingredient == null || ingredient.isEmpty())
                {
                    rowBuilder.append(" ");
                } else
                {
                    var added = false;
                    for (var r : ingredientMap.entrySet())
                    {
                        if (r.getKey().isEqual(ingredient))
                        {
                            rowBuilder.append(r.getValue());
                            added = true;
                            break;
                        }
                    }
                    if (!added) rowBuilder.append("?");
                }
            }
            result[x] = rowBuilder.toString();
        }

        return result;
    }

    private CWIngredient findIngredient(Set<CWIngredient> uniqueIngredients, CWIngredient ingredient)
    {
        for (var alreadyAdded : uniqueIngredients)
        {
            if (alreadyAdded.isEqual(ingredient))
                return alreadyAdded;
        }
        return null;
    }

    private void assignCharacters(Set<CWIngredient> ingredients)
    {
        // Use common starting letters for ingredients, then move to other characters
        String availableChars = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        int charIndex = 0;

        for (CWIngredient ingredient : ingredients)
        {
            if (charIndex < availableChars.length())
            {
                String character = String.valueOf(availableChars.charAt(charIndex));
                ingredientMap.put(ingredient, character);
                charIndex++;
            } else
            {
                // Fallback if we somehow have more than 62 unique ingredients
                ingredientMap.put(ingredient, String.valueOf(charIndex));
                charIndex++;
            }
        }
    }

    /**
     * Gets the map of ingredients to their character representations
     *
     * @return Map of Ingredient to String character
     */
    public Map<CWIngredient, String> getIngredientMap()
    {
        return new HashMap<>(ingredientMap);
    }

    /**
     * Gets the pattern array representing the recipe layout
     *
     * @return Array of strings representing the recipe pattern
     */
    public String[] getPattern()
    {
        return pattern.clone();
    }

    /**
     * Gets a specific ingredient's character representation
     *
     * @param ingredient The ingredient to look up
     * @return The character representation, or null if not found
     */
    public String getCharacterForIngredient(Ingredient ingredient)
    {
        return ingredientMap.get(ingredient);
    }

    public static CWIngredient[][] trim(CWIngredient[][] grid)
    {
        if (grid.length == 0) return new CWIngredient[0][0];
        int rows = grid.length;
        int cols = grid[0].length;

        int top = 0, bottom = rows - 1, left = 0, right = cols - 1;

        while (top <= bottom && isRowEmpty(grid, top)) top++;
        while (bottom >= top && isRowEmpty(grid, bottom)) bottom--;
        while (left <= right && isColEmpty(grid, left)) left++;
        while (right >= left && isColEmpty(grid, right)) right--;

        int newRows = Math.max(0, bottom - top + 1);
        int newCols = Math.max(0, right - left + 1);

        CWIngredient[][] trimmed = new CWIngredient[newRows][newCols];
        for (int r = 0; r < newRows; r++)
        {
            System.arraycopy(grid[top + r], left, trimmed[r], 0, newCols);
        }

        return trimmed;
    }

    public static boolean isRowEmpty(CWIngredient[][] grid, int row)
    {
        for (CWIngredient ing : grid[row])
        {
            if (!ing.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Checks if a column is completely empty (all Ingredients empty).
     */
    public static boolean isColEmpty(CWIngredient[][] grid, int col)
    {
        for (CWIngredient[] row : grid)
        {
            if (!row[col].isEmpty()) return false;
        }
        return true;
    }

}
