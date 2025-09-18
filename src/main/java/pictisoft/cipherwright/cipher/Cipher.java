package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pictisoft.cipherwright.util.AABBHelper;
import pictisoft.cipherwright.util.RecipeEncoder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Cipher
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected ResourceLocation recipeTypeIdThisIsFor;
    protected List<CipherInput> inputs;
    protected List<CipherOutput> outputs;
    private String name;
    private int padTop;
    private int padBottom;
    private int padLeft;
    private int padRight;
    private List<CipherIllustration> illustrations;
    private List<CipherParameter> parameters;
    private List<CipherTemplate> templates;
    private AABB bounds;
    private String category;
    //protected List<CipherParameters> parameters;
    //protected List<CipherDecorators> decorators;


    public static @Nullable Cipher TryParse(@NotNull JsonObject json)
    {
        if (!json.has("type")) return null;
        var ret = new Cipher();
        ret.inputs = new ArrayList<>();
        ret.outputs = new ArrayList<>();
        ret.recipeTypeIdThisIsFor = new ResourceLocation(json.get("type").getAsString());
        ret.padTop = optionalInt(json, "padtop");
        ret.padBottom = optionalInt(json, "padbottom");
        ret.padLeft = optionalInt(json, "padleft");
        ret.padRight = optionalInt(json, "padright");
        ret.name = optionalString(json, "name", ret.recipeTypeIdThisIsFor.toString());
        ret.category = optionalString(json, "category", ret.recipeTypeIdThisIsFor.toString());
        if (json.has("inputs"))
        {
            var inputs = json.get("inputs").getAsJsonArray();
            for (var input : inputs)
            {
                try
                {
                    CipherFactory.LoadInputsFromJson(CipherInput.class, input.getAsJsonObject(), ret.inputs);
                } catch (Exception e)
                {
                    LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                }
            }
        }
        if (json.has("outputs"))
        {
            var outputs = json.get("outputs").getAsJsonArray();
            for (var output : outputs)
            {
                try
                {
                    CipherFactory.LoadInputsFromJson(CipherOutput.class, output.getAsJsonObject(), ret.outputs);
                } catch (Exception e)
                {
                    LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                }
            }
        }
        ret.illustrations = new ArrayList<>();
        if (json.has("illustrations"))
        {
            var illustrations = json.get("illustrations").getAsJsonArray();
            for (var illustration : illustrations)
            {
                try
                {
                    CipherFactory.LoadInputsFromJson(CipherIllustration.class, illustration.getAsJsonObject(), ret.illustrations);
                } catch (Exception e)
                {
                    LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                }
            }
        }
        ret.parameters = new ArrayList<>();
        if (json.has("parameters"))
        {
            var parameters = json.get("parameters").getAsJsonArray();
            for (var parameter : parameters)
            {
                try
                {
                    CipherFactory.LoadInputsFromJson(CipherParameter.class, parameter.getAsJsonObject(), ret.parameters);
                } catch (Exception e)
                {
                    LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                }
            }
        }

        ret.templates = new ArrayList<>();
        if (json.has("templates"))
        {
            var templates = json.get("templates").getAsJsonArray();
            for (var template : templates)
            {
                try
                {
                    CipherFactory.LoadInputsFromJson(CipherTemplate.class, template.getAsJsonObject(), ret.templates);
                } catch (Exception e)
                {
                    LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                }
            }
        }

        ret.addPadding();
        ret.setCipher(ret);
        ret.calculateBounds();
        ret.templates.sort(Comparator.comparing(a -> a.name));
        //Chatter.chat(ret.recipeTypeIdThisIsFor.toString() + " " + ret.bounds.toString());
        return ret;
    }

    private void setCipher(Cipher ret)
    {
        for (var clot : getAll())
        {
            clot.cipher = ret;
        }
    }

    private void addPadding()
    {
        for (var clot : getAll())
        {
            clot.x += getPadLeft();
            clot.y += getPadTop();
        }
    }

    private void calculateBounds()
    {
        bounds = new AABB(0, 0, 0, 16, 16, 0);
        for (var clot : getAll())
        {
            bounds = AABBHelper.union(clot.getBounds(), bounds);
        }
        bounds = AABBHelper.union(bounds, new AABB(0, 0, 0, bounds.maxX + getPadRight(), bounds.maxY + getPadBottom(), 0));
//        maxx += getPadLeft() + getPadRight();
//        maxy += cipher.getPadTop() + cipher.getPadBottom();
//
//        this.cipherWidth = (int) Math.ceil(maxx);
//        this.cipherHeight = (int) Math.ceil(maxy);
    }

    private static String optionalString(JsonObject json, String member, String defaultvalue)
    {
        if (json.has(member)) return json.get(member).getAsString();
        return defaultvalue;
    }

    private static int optionalInt(JsonObject json, String member)
    {
        if (json.has(member)) return json.get(member).getAsInt();
        return 0;
    }

//    public static Cipher Generic()
//    {
//        var ret = new Cipher();
//        ret.recipeIdThisIsFor = new ResourceLocation("cipherwright:empty");
//        ret.name = "Generic";
//        ret.inputs = new ArrayList<>();
//
//        ret.outputs = new ArrayList<>();
//        ret.outputs.add(CipherOutput.SingleOutput());
//        return ret;
//    }

    public ResourceLocation getRecipeTypeId()
    {
        return recipeTypeIdThisIsFor;
    }

    public List<CipherInput> getInputs()
    {
        return inputs;
    }

    public List<CipherOutput> getOutputs()
    {
        return outputs;
    }

    public List<CipherGridObject> getAll()
    {
        var ret = new ArrayList<CipherGridObject>();
        ret.addAll(getInputs());
        ret.addAll(getOutputs());
        ret.addAll(getIllustrations());
        ret.addAll(getParameters());
        return ret;
    }

    public int getPadTop()
    {
        return this.padTop;
    }

    public int getPadBottom()
    {
        return this.padBottom;
    }

    public int getPadLeft()
    {
        return this.padLeft;
    }

    public int getPadRight()
    {
        return this.padRight;
    }

    public int getSlotCount()
    {
        return getInputs().size() + getOutputs().size();
    }

    public CipherGridObject getSlot(int i)
    {
        if (i < inputs.size()) return inputs.get(i);
        if (i < inputs.size() + outputs.size()) return outputs.get(i - inputs.size());
        return inputs.get(0);
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public List<CipherIllustration> getIllustrations()
    {
        return illustrations;
    }

    public List<CipherParameter> getParameters()
    {
        return parameters;
    }

    // height of all the slots
    public int getHeight()
    {
        return (int) bounds.getYsize();
    }

    // width of all the slots
    public int getWidth()
    {
        return (int) bounds.getXsize();
    }

    // go through json object and communicate with the cipher objects
    public void handleRecipe(JsonObject json, List<CipherSlot> slots, Map<String, String> _parameterValues)
    {
        clearSlots(slots);
        for (var slot : slots)
        {
            try
            {
                slot.setItem(json, this);
            } catch (Exception e)
            {
                //Chatter.chat(e.getMessage());
            }
        }

        // load default values into parameterValues
        for (var parameter : parameters)
        {
            //Chatter.chat("handleRecipe: par:" + parameter.getPath() + "=" + parameter.tryGet(json));
            _parameterValues.put(parameter.getPath(), parameter.tryGet(json));
        }
    }

    public static void clearSlots(List<CipherSlot> slots)
    {
        for (var slot : slots)
        {
            slot.setItem(ItemStack.EMPTY);

        }
    }

    public List<CipherTemplate> getTemplates()
    {
        return templates;
    }

    public CipherParameter getParameter(String path)
    {
        for (var r : parameters)
            if (r.getPath().equals(path)) return r;
        return null;
    }

    public CipherTemplate.Snippet getSnippet(String templateName, String snippetTitle)
    {
        var template = getTemplates().stream().filter(a -> a.name.equals(templateName)).findFirst();
        if (template.isEmpty()) return null;
        var snip = template.get().getSnippets().stream().filter(a -> a.title.equals(snippetTitle)).findFirst();
        return snip.orElse(null);
    }

    public CipherTemplate getTemplate(String title)
    {
        return getTemplates().stream().filter(a -> a.name.equals(title)).findFirst().orElse(null);
    }


    public Map<CWIngredient, String> getShapedMap(String path, ArrayList<CipherSlot> slots)
    {
        var re = getEncoder(path, slots);
        return re.getIngredientMap();
    }

    public String[] getEncodedPattern(String path, ArrayList<CipherSlot> slots)
    {
        var re = getEncoder(path, slots);
        return re.getPattern();
    }

    private RecipeEncoder getEncoder(String path, ArrayList<CipherSlot> slots)
    {
        var maxx = 0;
        var maxy = 0;
        for (var slot : slots)
        {
            if (slot.getPath().equals(path))
            {
                maxx = Math.max(maxx, slot.getCipherobject().getArrayRows());
                maxy = Math.max(maxy, slot.getCipherobject().getArrayColumns());
            }
        }
        var array = new CWIngredient[maxy][maxx];
        for (var slot : slots)
        {
            if (slot.getPath().equals(path))
            {
                array[slot.getCipherobject().getIndexY()][slot.getCipherobject().getIndexX()] = slot.getCWIngredient();
            }
        }
        return new RecipeEncoder(array, true);
    }


//    public List<CipherParameters> getParameters()
//    {
//        return parameters;
//    }
//
//    public List<CipherDecorators> getDecorators()
//    {
//        return decorators;
//    }
}
