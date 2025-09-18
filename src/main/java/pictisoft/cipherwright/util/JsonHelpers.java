package pictisoft.cipherwright.util;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsonHelpers
{
    public static ArrayList<String> JsonArrayToStringArray(JsonArray options)
    {
        var ret = new ArrayList<String>();
        for(var i=0;i<options.size();i++)
        {
            var s = options.get(i);
            ret.add(s.getAsString());
        }
        return ret;
    }

    public static class JsonNode
    {
        private boolean isNull;
        private boolean isbool;
        private boolean isnumber;
        public String string;
        public double number;
        public boolean bool;
        public JsonObject jobject;
        public JsonArray jsonarray;

        public JsonNode(double asDouble)
        {
            number = asDouble;
            isnumber = true;
        }

        public JsonNode(String asString)
        {
            string = asString;
        }

        public JsonNode(boolean asBoolean)
        {
            bool = asBoolean;
            isbool = true;
        }

        public JsonNode(JsonObject current)
        {
            jobject = current;
        }

        public JsonNode()
        {
            isNull = true;
        }

        public JsonNode(JsonArray asJsonArray)
        {
            jsonarray = asJsonArray;
        }

        public boolean isJsonArray()
        {
            return jsonarray != null;
        }

        public static JsonNode jsonNodeFromPrimitive(JsonPrimitive jp)
        {
            if (jp.isNumber()) return new JsonNode(jp.getAsDouble());
            if (jp.isString()) return new JsonNode(jp.getAsString());
            if (jp.isBoolean()) return new JsonNode(jp.getAsBoolean());
            if (jp.isJsonArray()) return new JsonNode(jp.getAsJsonArray());
            return new JsonNode();
        }

        public boolean isString()
        {
            return string != null;
        }

        public boolean isJsonObject()
        {
            return jobject != null;
        }

        public boolean isNumber()
        {
            return isnumber;
        }

        public boolean isBool()
        {
            return isbool;
        }

        public boolean isNull()
        {
            return isNull;
        }
    }

    public static JsonHelpers.JsonNode getNestedObject(JsonObject root, String path)
    {
        if (path == null || path.isEmpty())
        {
            //Chatter.chat("Missing path to get json object");
            return new JsonNode();
        }
        String[] parts = path.split("\\.");
        JsonObject current = root;

        for (String key : parts)
        {
            int bracket = key.indexOf('[');
            if (bracket != -1 && key.endsWith("]"))
            {
                String bkey = key.substring(0, bracket);
                var acurrent = current.getAsJsonArray(bkey);
                int index = Integer.parseInt(key.substring(bracket + 1, key.length() - 1));
                // needs to be an array
                if (acurrent != null && acurrent.isJsonArray())
                {
                    JsonArray arr = acurrent.getAsJsonArray();
                    if (index >= 0 && index < arr.size())
                    {
                        if (arr.get(index).isJsonPrimitive())
                        {
                            return JsonNode.jsonNodeFromPrimitive(arr.get(index).getAsJsonPrimitive());
                        }
                        if (arr.get(index).isJsonObject())
                            current = arr.get(index).getAsJsonObject();

                    } else
                    {
                        return new JsonNode(); // index out of bounds
                    }
                } else
                {
                    return new JsonNode(); // not an array
                }
            } else
            {
                if (!current.has(key))
                {
                    return new JsonNode();
                }
                if (current.get(key).isJsonPrimitive())
                {
                    return JsonNode.jsonNodeFromPrimitive(current.get(key).getAsJsonPrimitive());
                }
                if (current.get(key).isJsonArray())
                    return new JsonNode(current.get(key).getAsJsonArray());
                if (current.get(key).isJsonObject())
                    current = current.getAsJsonObject(key);
            }
        }
        return new JsonNode(current);
    }

    public static JsonElement getPath(JsonObject obj, String path)
    {
        String[] parts = path.split("\\.");
        JsonElement current = obj;

        for (String part : parts)
        {
            if (current == null || current.isJsonNull())
            {
                return JsonNull.INSTANCE;
            }

            // Check if part has an array index: e.g. items[2]
            int bracket = part.indexOf('[');
            if (bracket != -1 && part.endsWith("]"))
            {
                String key = part.substring(0, bracket);
                int index = Integer.parseInt(part.substring(bracket + 1, part.length() - 1));

                if (current.isJsonObject())
                {
                    current = current.getAsJsonObject().get(key);
                } else
                {
                    return JsonNull.INSTANCE;
                }

                if (current != null && current.isJsonArray())
                {
                    JsonArray arr = current.getAsJsonArray();
                    if (index >= 0 && index < arr.size())
                    {
                        current = arr.get(index);
                    } else
                    {
                        return JsonNull.INSTANCE; // index out of bounds
                    }
                } else
                {
                    return JsonNull.INSTANCE; // not an array
                }
            } else
            {
                // Normal object key
                if (current.isJsonObject())
                {
                    current = current.getAsJsonObject().get(part);
                } else
                {
                    return JsonNull.INSTANCE;
                }
            }
        }

        return current == null ? JsonNull.INSTANCE : current;
    }
}
