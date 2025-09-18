package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CipherInput extends CipherWell
{
    private static final Logger LOGGER = LogManager.getLogger();
    @Override
    boolean isSupportedType(String type)
    {
        if (type == null || type.isEmpty()) return false;
        return type.equals("ingredient") || type.equals("fluid") || type.equals("item") || type.equals("shaped");
    }
}
