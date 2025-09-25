package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// This class defines a "well", or something that will hold a fake Slot object
public class CipherWell extends CipherGridObject
{
    public enum STYLE_ENUM
    {
        normal,
        large,
        bucket
    }

    private static final Logger LOGGER = LogManager.getLogger();
    protected STYLE_ENUM style = STYLE_ENUM.normal;
    protected boolean single;
    protected boolean allowtag = true;
    protected boolean allowfluid = false;
    protected boolean allowitem = true;
    private ArrayList<CipherParameter> parameters = new ArrayList<>();
    //private int startIndex = -1;

    public STYLE_ENUM getStyle()
    {
        return style;
    }

    public boolean getSingle()
    {
        return single;
    }

    public boolean allowTag()
    {
        return allowtag;
    }

    public boolean allowFluid()
    {
        return allowfluid;
    }

    public boolean allowItem()
    {
        return allowitem;
    }


    @Override
    public int getWidth()
    {
        return getStyle() == STYLE_ENUM.large ? large_width : width;
    }

    @Override
    public int getHeight()
    {
        return getStyle() == STYLE_ENUM.large ? large_height : height;
    }

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherWell cw)
        {
            if (input.has("index")) cw.index = input.get("index").getAsInt();
            if (input.has("single")) cw.single = input.get("single").getAsBoolean();
            if (input.has("style")) cw.style = convertStyle(input.get("style").getAsString());
            if (input.has("allowtag")) cw.allowtag = input.get("allowtag").getAsBoolean();
            if (input.has("allowfluid")) cw.allowfluid = input.get("allowfluid").getAsBoolean();
            if (input.has("allowitem")) cw.allowitem = input.get("allowitem").getAsBoolean();
            if (input.has("members") && input.get("members").isJsonArray())
            {
                var parray = input.get("members").getAsJsonArray();
                parameters = new ArrayList<>();
                for (var parameter : parray)
                {
                    try
                    {
                        CipherFactory.LoadInputsFromJson(CipherParameter.class, parameter.getAsJsonObject(), parameters);
                    } catch (Exception e)
                    {
                        LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private STYLE_ENUM convertStyle(String style)
    {
        try
        {
            return STYLE_ENUM.valueOf(style.toLowerCase());
        } catch (Exception ignored)
        {
        }
        return STYLE_ENUM.normal;
    }

    public ArrayList<CipherParameter> getWellParameters()
    {
        return parameters;
    }
}
