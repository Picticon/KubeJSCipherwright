package pictisoft.cipherwright.cipher;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.util.Chatter;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CipherTemplate extends CipherGridObject
{
    public String name = "";
    private ArrayList<Snippet> _snippets = new ArrayList<>();
    private String format = "json";
    protected DataLoad data;

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherTemplate cw)
        {
            if (input.has("name")) cw.name = input.get("name").getAsString();
            if (input.has("format")) cw.format = input.get("format").getAsString();
            if (input.has("snippets"))
            {
                var jarray = input.getAsJsonArray("snippets");
                for (var a : jarray)
                {
                    if (a.isJsonObject())
                    {
                        var snippet = new CipherTemplate.Snippet(a);
                        if (!snippet.fragments.isEmpty()) _snippets.add(snippet);
                    }
                }
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<Snippet> getSnippets()
    {
        return _snippets;
    }

    public void clipboard(DataLoad data)
    {
        var string = buildString(data);
        Minecraft.getInstance().keyboardHandler.setClipboard(string);
        Chatter.chat(data.cipher.getName() + " / " + this.name + " " + " copied to clipboard.");
    }

    private String buildString(DataLoad data)
    {
        StringBuilder ret = new StringBuilder();
        this.data = data;
        for (var tt : getSnippets())
        {
            if (!tt.use(data)) continue;

            var string = buildSnippet(data, tt.title);
            if (!ret.isEmpty() && format.equals("kubejs")) ret.append("\r\n");
            ret.append(string);
        }
        if (flags.contains("prettyjson"))
        {
            return prettyJson(ret.toString());
        }
        return ret.toString();
    }

    private String buildSnippet(DataLoad data, String title)
    {
        var snip = findSnippet(title);
        if (snip == null) return "";

        return buildSnippet(data, snip);
    }

    private String buildSnippet(DataLoad data, Snippet snippet)
    {
        this.data = data;
        var cipher = data.cipher;

        StringBuilder ret = new StringBuilder();
        if (snippet.loopPath != null)
        {
            // look for any path matching the looppath, and index
            var outer = new StringBuilder();
            if (snippet.prefix != null) outer.append(snippet.prefix);
            var added = false;
            for (var idx = 0; idx < 100; idx++)
            {
                for (var s : data.slots)
                {
                    if (s.getCipherobject().getIndex() == idx
                            && s.getCipherobject().getPath().equals(snippet.loopPath))
                    {
                        for (var fragment : snippet.fragments)
                        {
                            var inner = new StringBuilder();
                            if (added && snippet.join != null) inner.append(snippet.join);
                            var newfragment = fragment.replace("<<loop>>", "" + idx);
                            buildOneFragment(data, cipher, newfragment, inner);
                            outer.append(inner);
                            added = true;
                        }
                    }
                }
            }
            if (snippet.postfix != null) outer.append(snippet.postfix);
            ret.append(outer);
        } else
        {
            for (var fragment : snippet.fragments)
            {
                buildOneFragment(data, cipher, fragment, ret);
            }
        }
        if (flags.contains("prettyjson"))
        {
            return prettyJson(ret.toString());
        }
        return ret.toString();

    }

    private void buildOneFragment(DataLoad data, Cipher cipher, String fragment, StringBuilder ret)
    {
        Pattern pattern = Pattern.compile("<<([^>]+)>>");
        Matcher matcher = pattern.matcher(fragment);

        StringBuilder result = new StringBuilder();


        // the following should be handled by some set of parser classes... oh well
        // giant SWITCH it is...
        while (matcher.find())
        {
            String placeholder = matcher.group(1); // the text inside << >>
            String replacement = "??";

            if (placeholder.startsWith("parameter:"))
            {
                var path = placeholder.replace("parameter:", "");
                if (data.parameters.containsKey(path)) replacement = CipherEncoderBase.encodeParameter(format, path, data);
            } else if (placeholder.startsWith("key:"))
            {
                var path = placeholder.replace("key:", "");
                var map = data.cipher.getShapedMap(path, data.slots);
                replacement = CipherEncoderBase.mapToPatternKey(format, map, data);
            } else if (placeholder.startsWith("pattern:"))
            {
                var path = placeholder.replace("pattern:", "");
                var encoded = data.cipher.getEncodedPattern(path, data.slots);
                replacement = CipherEncoderBase.mapToPattern(format, encoded, data);
            } else if (placeholder.startsWith("ingredient:"))
            {
                var path = placeholder.replace("ingredient:", "");
                var slots = data.slots.stream().filter(a -> a.pathMatch(path)).toList();
                if (!slots.isEmpty()) replacement = CipherEncoderBase.slotToIngredient(format, slots.get(0), data);
            } else if (placeholder.startsWith("ingredients:"))
            {
                var path = placeholder.replace("ingredients:", "");
                var slots = data.slots.stream().filter(a -> a.getPath().equals(path)).toList();
                replacement = CipherEncoderBase.slotsToIngredients(format, slots, data);
            } else if (placeholder.startsWith("itemstack:"))
            {
                var path = placeholder.replace("itemstack:", "");
                var slot = data.slots.stream().filter(a -> a.pathMatch(path)).findFirst();
                if (slot.isPresent()) replacement = CipherEncoderBase.slotToItemStack(slot.get(), format, data);
            } else if (placeholder.startsWith("fluidstack:"))
            {
                var path = placeholder.replace("fluidstack:", "");
                var slot = data.slots.stream().filter(a -> a.pathMatch(path)).findFirst();
                if (slot.isPresent()) replacement = CipherEncoderBase.slotToFluidStack(slot.get(), format, data);
            } else if (placeholder.startsWith("item-id:"))
            {
                var path = placeholder.replace("item-id:", "");
                var slot = data.slots.stream().filter(a -> a.pathMatch(path)).findFirst();
                if (slot.isPresent()) replacement = CipherEncoderBase.slotToItemStackId(slot.get(), format, data);
            } else if (placeholder.startsWith("item-count:"))
            {
                var path = placeholder.replace("item-count:", "");
                var slot = data.slots.stream().filter(a -> a.pathMatch(path)).findFirst();
                if (slot.isPresent()) replacement = CipherEncoderBase.slotToItemStackCount(slot.get(), format, data);
            } else if (placeholder.startsWith("itemstacks:"))
            {
                var path = placeholder.replace("itemstacks:", "");
                var slots = data.slots.stream().filter(a -> a.getPath().equals(path)).toList();
                replacement = CipherEncoderBase.slotToItemStacks(format, slots, data);
            } else if (placeholder.startsWith("template:"))
            {
                placeholder = placeholder.replace("template:", "");
                var replacementTemplate = cipher.getTemplate(placeholder);
                if (replacementTemplate != null && !this.name.equals(replacementTemplate.name)) replacement = replacementTemplate.buildString(data);
            } else if (placeholder.startsWith("snippet:"))
            {
                placeholder = placeholder.replace("snippet:", "");
                var places = placeholder.split(":");
                if (places.length == 2)
                {
                    var template_name = places[0];
                    var snippet_title = places[1];
                    var replacementSnippet = cipher.getSnippet(template_name, snippet_title);
                    if (replacementSnippet != null)
                    {
                        replacement = buildSnippet(data, replacementSnippet); // recursive possibility?
                        if (flags != null && flags.contains("prettyjson")) replacement = prettyJson(replacement);
                    }
                }
            } else if (placeholder.equals("oldid"))
            {
                replacement = data.removeId;
            } else if (placeholder.contains("description:")) // used for comments
            {
                placeholder = placeholder.replace("description:", "");
                replacement = makeDescription(placeholder);
            } else
            {
                switch (placeholder)
                {
                    case "type" -> replacement = data.cipher.recipeTypeIdThisIsFor.toString();
                    default -> replacement = "'??" + placeholder + "??'";
                }
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        ret.append(result);
    }

    // make "description:"
    private String makeDescription(String path)
    {
        for (var r : data.slots)
        {
            if (r.getCipherobject().getPathWithIndex().equals(path) || r.getCipherobject().getPathWithoutIndex().equals(path))
            {
                return r.getItemStack().getHoverName().getString();
            }
        }
        return "Unknown";
    }

    private String getJSON(List<CipherSlot> list)
    {
        var json = new JsonArray();
        for (var slot : list)
        {
            JsonObject j = null;
            try
            {
                j = slot.toJson();
                if (j != null) json.add(j);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return json.toString();
    }


    private String getJSON(ItemStack stack)
    {
        JsonElement element = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow();
        //String json = new GsonBuilder().setPrettyPrinting().create().toJson(element);
        return new GsonBuilder().create().toJson(element);
    }

    public String prettyJson(String jsonString)
    {
        try
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            return gson.toJson(jsonElement);
        } catch (Exception e)
        {
            // Handle invalid JSON
            return jsonString;
        }
    }

    private Snippet findSnippet(String title)
    {
        for (var s : _snippets)
            if (s.title.equals(title)) return s;
        return null;
    }

    public static class DataLoad
    {
        public final boolean includeComments;

        public DataLoad(Cipher cipher, String removeId, ArrayList<CipherSlot> slots, Map<String, String> parameters, boolean includeComments)
        {
            this.cipher = cipher;
            this.removeId = removeId;
            this.slots = slots;
            this.parameters = parameters;
            this.includeComments = includeComments;
        }

        public String removeId = null;
        public Cipher cipher;
        public ArrayList<CipherSlot> slots = new ArrayList<>();
        public Map<String, String> parameters = new HashMap<>();
    }

    public class Snippet
    {
        private final String loopPath;
        private final String join;
        private final String prefix;
        private final String postfix;
        public String title = "Unnamed";
        public String condition;
        public String flags = "";
        private ArrayList<String> fragments = new ArrayList<>();

        public Snippet(JsonElement a)
        {
            var b = a.getAsJsonObject();
            if (b.has("title")) this.title = b.get("title").getAsString();
            if (b.has("flags")) this.flags = b.get("flags").getAsString();
            if (b.has("condition")) this.condition = b.get("condition").getAsString();

            this.loopPath = (b.has("loop")) ? b.get("loop").getAsString() : null;
            this.join = (b.has("join")) ? b.get("join").getAsString() : null;
            this.prefix = (b.has("prefix")) ? b.get("prefix").getAsString() : null;
            this.postfix = (b.has("postfix")) ? b.get("postfix").getAsString() : null;

            if (b.has("fragment") && b.get("fragment").isJsonPrimitive() && b.get("fragment").getAsJsonPrimitive().isString())
            {
                this.fragments.add(b.get("fragment").getAsString());
            }
            if (b.has("fragments"))
            {
                var f = b.get("fragments").getAsJsonArray();
                for (var frag : f)
                {
                    // single entry
                    if (frag.isJsonPrimitive() && frag.getAsJsonPrimitive().isString())
                    {
                        var ftext = frag.getAsString();
                        this.addFragment(ftext);
                    }
//                                // conditional entry .. not implemented yet
//                                if (frag.isJsonArray())
//                                {
//                                    farray = frag.getAsJsonArray();
//                                }
                }
            }
        }

        public void addFragment(String ftext)
        {
            fragments.add(ftext);
        }

        public boolean use(DataLoad dataload)
        {
            if (condition != null)
            {
                if (condition.contains("remove")) return dataload.removeId != null;
                if (condition.contains("comment")) return dataload.includeComments;
                if (condition.contains("item-count:"))
                {
                    return (CipherParameter.testItemCount(condition.replace("item-count:", ""), dataload.slots));
                }
                if (condition.startsWith("present:"))
                {
                    return (CipherParameter.testPresent(condition.replace("present:", ""), dataload.slots));
                }
                if (condition.startsWith("parameter:"))
                {
                    return (CipherParameter.testCondition(condition.replace("parameter:", ""), dataload.parameters));
                }

            }
            return true;
        }
    }
}
