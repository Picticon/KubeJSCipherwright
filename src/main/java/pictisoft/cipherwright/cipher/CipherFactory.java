package pictisoft.cipherwright.cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static pictisoft.cipherwright.cipher.CipherGridObject.GRID_H;
import static pictisoft.cipherwright.cipher.CipherGridObject.GRID_W;

public class CipherFactory
{
    private static final Logger LOGGER = LogManager.getLogger();


    public static <T extends CipherGridObject> void LoadInputsFromJson(Class<T> clazz, JsonObject json, List<T> collectionArray) throws Exception
    {
        if (json.has("repeat"))
        {
            var repeat = json.getAsJsonObject("repeat");
            String shape = "array";
            if (repeat.has("shape")) shape = repeat.get("shape").getAsString();

            switch (shape)
            {
                case "oval":
                {
                    int count = 1;
                    if (repeat.has("segments")) count = repeat.get("segments").getAsInt();
                    float radiusx = 1;
                    if (repeat.has("radiusx")) radiusx = repeat.get("radiusx").getAsFloat();
                    float radiusy = 1;
                    if (repeat.has("radiusy")) radiusy = repeat.get("radiusy").getAsFloat();
                    var idx = 0;
                    for (var s = 0f; s < Math.PI * 2 - .1f; s += (float) ((Math.PI * 2) / count))
                    {
                        var ret = makeObject(clazz, json);
                        if (ret != null)
                        {
                            ret.x += (int) (Math.sin(s) * radiusx * GRID_W);
                            ret.y -= (int) (Math.cos(s) * radiusy * GRID_H);
                            ret.index = idx++;
                            appendAndIndex(collectionArray, ret);
                        }
                    }
                }
                break;
                case "absolute":
                {
                    JsonArray coordinates = null;
                    var gridmode = false;
                    if (repeat.has("coordinates") && repeat.get("coordinates").isJsonArray())
                    {
                        coordinates = repeat.get("coordinates").getAsJsonArray();
                    }
                    if (repeat.has("gridcoordinates") && repeat.get("gridcoordinates").isJsonArray())
                    {
                        coordinates = repeat.get("gridcoordinates").getAsJsonArray();
                        gridmode = true;
                    }
                    if (coordinates != null)
                    {
                        for (var idx = 0; idx < coordinates.size(); idx += 2)
                        {
                            var x = coordinates.get(idx).getAsInt() * (gridmode ? GRID_W : 1);
                            var y = coordinates.get(idx + 1).getAsInt() * (gridmode ? GRID_H : 1);
                            var ret = makeObject(clazz, json);
                            if (ret != null)
                            {
                                ret.x = x;
                                ret.y = y;
                                ret.index = idx / 2;
                                appendAndIndex(collectionArray, ret);
                            }
                        }
                    }
                }
                break;
                case "array":
                default:
                {
                    int rows = 1;
                    if (repeat.has("rows")) rows = repeat.get("rows").getAsInt();
                    int cols = 1;
                    if (repeat.has("cols")) cols = repeat.get("cols").getAsInt();
                    int rowspacing = 0;
                    if (repeat.has("rowspacing")) rowspacing = repeat.get("rowspacing").getAsInt();
                    int colspacing = 0;
                    if (repeat.has("colspacing")) colspacing = repeat.get("colspacing").getAsInt();
                    var idx = 0;
                    for (var y = 0; y < rows; y++)
                        for (var x = 0; x < cols; x++)
                        {
                            var ret = makeObject(clazz, json);
                            if (ret != null)
                            {
                                ret.x += x * (GRID_W + colspacing);
                                ret.y += y * (GRID_H + rowspacing);
                                ret.index = idx++;
                                ret.rows = rows;
                                ret.cols = cols;
                                appendAndIndex(collectionArray, ret);
                            }
                        }
                }
                break;
            }

        } else
        {
            var ret = makeObject(clazz, json);
            if (ret != null)
            {
                appendAndIndex(collectionArray, ret);
            }
        }
    }

    private static <T extends CipherGridObject> void appendAndIndex(List<T> collectionArray, T ret)
    {
        if (ret instanceof CipherWell)
        {
            if (collectionArray == null) collectionArray = new ArrayList<T>();
            if (ret.index < 0) // if index was already assigned, skip
            {
                var idx = -1;
                for (var ca : collectionArray)
                {
                    if (ca.path.equals(ret.path))
                    {
                        if (ca.index < 0) ca.index = 0; // we found a solo entry, give it an index
                        idx = Math.max(ca.index, idx);
                    }
                }
                if (idx >= 0) ret.index = idx + 1; // append to the internal "collection" making it a scattered "array"
            }
        }
        collectionArray.add(ret);
    }

    private static <T extends CipherGridObject> T makeObject(Class<T> clazz, JsonObject json)
    {
        try
        {
            var ret = clazz.getDeclaredConstructor().newInstance();
            ret.readJson(ret, json);
            return ret;
        } catch (Exception e)
        {
            return null;
        }
    }
}
