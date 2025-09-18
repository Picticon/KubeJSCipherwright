package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.util.GUIElementRenderer;
import pictisoft.cipherwright.util.JsonHelpers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CipherParameter extends CipherGridObject
{
    private String placeholder = "";
    private String label = "";
    private String defaultValue = "";
    private ArrayList<String> options = new ArrayList<>();
    private ArrayList<String> descriptions = new ArrayList<>();

    public CipherParameter()
    {
        type = "text";
    }

    @Override
    boolean isSupportedType(String type)
    {
        if (type == null || type.isEmpty()) return false;
        return type.equals("text")
                || type.equals("number")
                || type.equals("combo")
                ;
    }

    public void render(GuiGraphics gui, GUIElementRenderer guiRenderer)
    {
        if (label != null && !label.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            var textwidth = font.width(this.label);
            var x = this.getPosX() - textwidth - 4;
            if (this.flags.contains("right"))
            {
                x = this.getPosX() + this.getWidth() + 4;
            }
            gui.drawString(font, this.label, x, this.getPosY() + 1, 0x404040, false);
        }
    }

    public Component getLabel()
    {
        return Component.translatableWithFallback(getPlaceholder(), getPlaceholder());
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
            if (input.has("default")) cw.defaultValue = input.get("default").getAsString();
            if (input.has("options")) cw.options = JsonHelpers.JsonArrayToStringArray(input.getAsJsonArray("options"));
            if (input.has("descriptions")) cw.descriptions = JsonHelpers.JsonArrayToStringArray(input.getAsJsonArray("descriptions"));
        }
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
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
}
