package pictisoft.cipherwright.cipher;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CipherGridObject
{
    public static final int GRID_W = 18;
    public static final int GRID_H = 18;

    private static final Logger LOGGER = LogManager.getLogger();
    public Cipher cipher; // pointer to container
    protected int x;
    protected int y;
    protected int width = GRID_W;
    protected int large_width = 26;
    protected int height = GRID_H;
    protected int large_height = 26;
    protected String path; // JSON path to the variable that we are interested in
    protected String type; // The type of thing, e.g. ingredient, item, tag
    protected String key; //  used in case of shaped crafting, to decode an ingredient
    protected String flags = "";

    protected int index = -1; // This is the index inside the array
    protected int rows; // this is the number of rows in the containing object, e.g. 3 for vanilla shaped recipe
    protected int cols; // this is the number of columns in the containing object, e.g. 3 for vanilla shaped recipe
    protected String countfield;


    protected static void TryParse(JsonObject input, CipherInput ret)
    {
    }

    public int getArrayRows()
    {
        return rows;
    }

    public int getArrayColumns()
    {
        return cols;
    }

    public int getIndexX()
    {
        return index % cols;
    }

    public int getIndexY()
    {
        return index/cols;
    }

    public int getPosX()
    {
        return x;
    }

    public int getPosY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public String getType()
    {
        return type;
    }

    public String getKey()
    {
        return key;
    }

    String getPathWithIndex()
    {
        if (index >= 0 && !path.endsWith("]"))
            return path + "[" + index + "]";
        else
            return path;
    }

    String getPathWithIndex(int idx)
    {
        return path + "[" + idx + "]";
    }

    public String getPathWithoutIndex()
    {
        return path;
    }

    public AABB getBounds()
    {
        return new AABB(x, y, 0, x + getWidth(), y + getHeight(), 0);
    }

    public String getPath()
    {
        return path;
    }

    public int getIndex()
    {
        return index;
    }

    boolean isSupportedType(String type)
    {
        return false;
    }

//    public Ingredient[][] parseShaped(JsonObject json)
//    {
//        var ikey = JsonHelpers.getPath(json, key);
//        //var igrid = JsonHelpers.getPath(json, path);
//        if (ikey == null) return new Ingredient[0][0];
//        Map<String, Ingredient> map = keyFromJson(ikey.getAsJsonObject());
//        Ingredient[][] grid = new Ingredient[rows][cols];
//        for (int y = 0; y < height; y++)
//        {
//            var pattern = JsonHelpers.getPath(json, path + "[" + y + "]");
//            if (pattern != null)
//            {
//                for (int x = 0; x < width; x++)
//                {
//                    var prow = pattern.getAsString();
//                    //   grid[y][x] = flat.get(y * width + x);
//                }
//            }
//        }
//        return grid;
//    }

    static Map<String, Ingredient> keyFromJson(JsonObject pKeyEntry)
    {
        Map<String, Ingredient> map = Maps.newHashMap();

        for (Map.Entry<String, JsonElement> entry : pKeyEntry.entrySet())
        {
            if (entry.getKey().length() != 1)
            {
                throw new JsonSyntaxException("Invalid key entry: '" + (String) entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey()))
            {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), Ingredient.fromJson(entry.getValue(), false));
        }

        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        if (input.has("key")) ret.key = input.get("key").getAsString();
        if (input.has("type")) ret.type = input.get("type").getAsString();
        if (input.has("path")) ret.path = input.get("path").getAsString();
        if (input.has("path:count")) ret.countfield = input.get("path:count").getAsString();
        if (input.has("flags")) ret.flags = input.get("flags").getAsString();

        if (!ret.isSupportedType(ret.type))
        {
            LOGGER.debug("Unsupported Input Type for Cipher: {}", ret.type);
            ret.type = "ingredient";
        }

        if (input.has("left")) ret.x = input.get("left").getAsInt();
        if (input.has("gridx")) ret.x = Math.round(input.get("gridx").getAsFloat() * GRID_W);
        if (input.has("top")) ret.y = input.get("top").getAsInt();
        if (input.has("gridy")) ret.y = Math.round(input.get("gridy").getAsFloat() * GRID_H);
        if (input.has("w")) ret.width = Math.round(input.get("w").getAsFloat());
        if (input.has("gridw")) ret.width = Math.round(input.get("gridw").getAsFloat() * GRID_W);
        if (input.has("h")) ret.height = Math.round(input.get("h").getAsFloat());
        if (input.has("gridh")) ret.height = Math.round(input.get("gridh").getAsFloat() * GRID_W);
    }
}
