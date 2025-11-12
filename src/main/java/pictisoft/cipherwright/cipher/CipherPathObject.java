package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class CipherPathObject extends CipherGridObject
{
    protected String path; // JSON path to the variable that we are interested in
    protected String countfield;
    protected String itemfield;

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherPathObject cpo)
        {

            // general path to the object
            if (input.has("path")) cpo.path = input.get("path").getAsString();

            // optional path to count member
            if (input.has("path:count")) cpo.countfield = input.get("path:count").getAsString();

            // optional path to item member
            if (input.has("path:item"))
                cpo.itemfield = input.get("path:item").getAsString();
            if (path == null) path = "xxx";
        }
    }

    public String getItemPath()
    {
        if (itemfield == null || itemfield.isBlank()) return getPathWithIndex();
        return getPathWithIndex() + "." + itemfield;
    }

    public String getCountPath()
    {
        if (countfield == null || countfield.isBlank()) return getPathWithIndex();
        return /*getPathWithIndex() + "." +*/ countfield;
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

    public String getPath()
    {
        if (path == null)
        {
            path = "xxx";
        }
        return path;
    }

    public boolean hasCountField()
    {
        return countfield != null && !countfield.isBlank();
    }
}
