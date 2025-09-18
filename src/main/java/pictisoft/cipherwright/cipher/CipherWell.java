package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class CipherWell extends CipherGridObject
{
    protected boolean large;
    protected boolean single;
    protected boolean allowtag = true;
    protected boolean allowfluid = false;

    public boolean getLarge()
    {
        return large;
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


    @Override
    public int getWidth()
    {
        return large ? large_width : width;
    }

    @Override
    public int getHeight()
    {
        return large ? large_height : height;
    }

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherWell cw)
        {
            if (input.has("single"))
                cw.single = input.get("single").getAsBoolean();
            if (input.has("large"))
                cw.large = input.get("large").getAsBoolean();
            if (input.has("allowtag"))
                cw.allowtag = input.get("allowtag").getAsBoolean();
            if (input.has("allowfluid"))
                cw.allowfluid = input.get("allowfluid").getAsBoolean();
        }
    }

}
