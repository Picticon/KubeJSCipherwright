package pictisoft.cipherwright.util;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import pictisoft.cipherwright.cipher.CWIngredient;

public class RecipeHelper
{
    public static CWIngredient[][] decodeRecipeGrid(int rows, int cols,
            JsonHelpers.JsonNode encoded_path,
            JsonHelpers.JsonNode map_path, boolean shift_and_center)
    {
        var ret = new CWIngredient[rows][cols];
        for (var r = 0; r < rows; r++)
            for (var c = 0; c < cols; c++)
                ret[r][c] = CWIngredient.EMPTY;
        if (encoded_path.isJsonArray())
        {
            var row = 0;
            for (var json : encoded_path.jsonarray)
            {
                if (json.isJsonPrimitive())
                {
                    var jprim = json.getAsJsonPrimitive();
                    if (jprim.isString())
                    {
                        var encoded = jprim.getAsString();
                        for (var col = 0; col < encoded.length(); col++)
                        {
                            var substr = encoded.substring(col, col + 1);
                            if (map_path.isJsonObject() && map_path.jobject.has(substr))
                            {
                                ret[row][col] = CWIngredient.fromJson(map_path.jobject.get(substr).getAsJsonObject());
                            }
                        }
                    }
                }
                row++;
            }
        }
        return ret;
    }

    private static Ingredient getIngredient(JsonObject json)
    {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
        {
            var str = json.getAsString();
            ResourceLocation rl = new ResourceLocation(str);
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != Items.AIR)
            {
                ItemStack stack = new ItemStack(item, 1);
                return Ingredient.of(stack);
            }
        } else
        {
            return ItemAndIngredientHelpers.ingredientWithNBTFromJson(json);
//            if (ig instanceof Ingredient.ItemValue || ig instanceof Ingredient.TagValue)
//            {
//                return Ingredient.fromJson(json);
//            }
        }
        return Ingredient.EMPTY;
    }

    /**
     * Checks if a row is completely empty (all Ingredients empty).
     */
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

    /**
     * Returns the "trimmed" subgrid with all empty outer rows/columns removed.
     */
    public static CWIngredient[][] trim(CWIngredient[][] grid)
    {
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

    /**
     * Shifts the grid by dx, dy inside a new larger array.
     * Cells shifted outside the new bounds are dropped.
     */
    public static CWIngredient[][] shift(CWIngredient[][] grid, int dx, int dy, int newRows, int newCols)
    {
        CWIngredient[][] shifted = new CWIngredient[newRows][newCols];
        // initialize all empty
        for (int r = 0; r < newRows; r++)
        {
            for (int c = 0; c < newCols; c++)
            {
                shifted[r][c] = CWIngredient.EMPTY;
            }
        }

        for (int r = 0; r < grid.length; r++)
        {
            for (int c = 0; c < grid[r].length; c++)
            {
                int nr = r + dy;
                int nc = c + dx;
                if (nr >= 0 && nr < newRows && nc >= 0 && nc < newCols)
                {
                    shifted[nr][nc] = grid[r][c];
                }
            }
        }
        return shifted;
    }

    /**
     * Centers the grid inside a new array of given size.
     */
    public static CWIngredient[][] center(CWIngredient[][] grid, int newRows, int newCols)
    {
        var ac = getActualSize(grid);
        int rowOffset = (newRows - ac[0]) / 2;
        int colOffset = (newCols - ac[1]) / 2;
        return shift(grid, colOffset, rowOffset, newRows, newCols);
    }

    /**
     * Returns the "actual" size after trimming (height, width).
     */
    public static int[] getActualSize(CWIngredient[][] grid)
    {
        CWIngredient[][] trimmed = trim(grid);
        return new int[]{trimmed.length, trimmed.length > 0 ? trimmed[0].length : 0};
    }
}
