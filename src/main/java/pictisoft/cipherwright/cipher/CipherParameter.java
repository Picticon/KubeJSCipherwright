package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.util.GUIElementRenderer;
import pictisoft.cipherwright.util.JsonHelpers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CipherParameter extends CipherGridObject
{
    private String placeholder = "";
    private String label = "";
    private String hint = "";
    private String defaultValue = "";
    private ArrayList<String> options = new ArrayList<>();
    private ArrayList<String> descriptions = new ArrayList<>();
    private String display = "";
    private String tooltip;

    public CipherParameter()
    {
        type = "text";
    }

    //
    // returns TRUE if condition matches.
    // input should be PATH:COMPARER:VALUE, where COMPARER IS:
    // equals
    // not or notequals
    // if there are any syntax errors, returns TRUE.
    //
    public static boolean testCondition(String condition, Map<String, String> parameters)
    {
        if (condition == null) return true;
        var split = condition.split(":");
        if (split.length == 3)
        {
            var path = split[0];
            var bool = split[1];
            var compare = split[2];
            if (parameters.containsKey(path))
            {
                if (bool.equals("not") || bool.equals("notequals"))
                {
                    return !parameters.get(path).equals(compare);
                }
                if (bool.equals("equals"))
                {
                    return parameters.get(path).equals(compare);
                }
            }
        }
        return true;
    }

    @Override
    boolean isSupportedType(String type)
    {
        if (type == null || type.isEmpty()) return false;
        return type.equals("text")
                || type.equals("number")
                || type.equals("combo")
                || type.equals("combo-number")
                || type.equals("boolean")
                ;
    }

    @Override
    public int getHeight()
    {
        if (type.equals("combo")) return 16;
        if (type.equals("combo-number")) return 16;
        if (type.equals("boolean")) return 16;
        if (type.equals("text")) return 13;
        if (type.equals("number")) return 13;
        return 16;
    }

    public void render(GuiGraphics gui, GUIElementRenderer guiRenderer)
    {
        render(gui, guiRenderer, getPosX(), getPosY(), getWidth());
    }

    public void render(GuiGraphics gui, GUIElementRenderer guiRenderer, int xxx, int yyy, int www)
    {
    }

    public Component getLabel()
    {
        return Component.translatableWithFallback(this.label, this.label);
    }

    private String getPlaceholder()
    {
        return placeholder;
    }

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherParameter cw)
        {
            if (input.has("placeholder")) cw.placeholder = input.get("placeholder").getAsString();
            if (input.has("label")) cw.label = input.get("label").getAsString();
            if (input.has("hint")) cw.hint = input.get("hint").getAsString();
            if (input.has("default")) cw.defaultValue = input.get("default").getAsString();
            if (input.has("display")) cw.display = input.get("display").getAsString();
            if (input.has("options")) cw.options = JsonHelpers.JsonArrayToStringArray(input.getAsJsonArray("options"));
            if (input.has("descriptions")) cw.descriptions = JsonHelpers.JsonArrayToStringArray(input.getAsJsonArray("descriptions"));
            if (input.has("tooltip")) cw.tooltip = input.get("tooltip").getAsString();
        }
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String tryGet(JsonObject json)
    {
        var node = JsonHelpers.getNestedObject(json, getPath());
        if (node.isString()) return node.string;
        if (node.isNumber())
        {
            var df = new DecimalFormat("#.###");
            return df.format(node.number);
        }
        if (node.isBool()) return node.bool ? "true" : "false";
        return "";
    }

    public List<String> getComboOptions()
    {
        return options;
    }

    public List<String> getComboDescriptions()
    {
        return descriptions;
    }

    public String getDisplay()
    {
        return display;
    }

    public String getFlags()
    {
        return flags == null ? "" : flags;
    }

    public Component getHint()
    {
        return Component.translatableWithFallback(this.hint, this.hint);
    }

    public String getTooltip()
    {
        return tooltip;
    }
}
