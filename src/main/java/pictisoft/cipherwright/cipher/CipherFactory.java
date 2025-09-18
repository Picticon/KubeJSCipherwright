package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static pictisoft.cipherwright.cipher.CipherGridObject.GRID_H;
import static pictisoft.cipherwright.cipher.CipherGridObject.GRID_W;

public class CipherFactory
{
    private static final Logger LOGGER = LogManager.getLogger();


    public static <T extends CipherGridObject> void LoadInputsFromJson(Class<T> clazz, JsonObject json, List<T> array) throws Exception
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
                    for (var s = 0f; s < Math.PI * 2; s += (float) ((Math.PI * 2) / count))
                    {
                        var r = makeObject(clazz, json);
                        if (r != null)
                        {
                            r.x += (int) (Math.sin(s) * radiusx * GRID_W);
                            r.y -= (int) (Math.cos(s) * radiusy * GRID_H);
                            r.index = idx++;
                            array.add(r);
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
                    var idx = 0;
                    for (var y = 0; y < rows; y++)
                        for (var x = 0; x < cols; x++)
                        {
                            var r = makeObject(clazz, json);
                            if (r != null)
                            {
                                r.x += x * GRID_W;
                                r.y += y * GRID_H;
                                r.index = idx++;
                                r.rows = rows;
                                r.cols = cols;
                                array.add(r);
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
                array.add(ret);
            }
        }
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
