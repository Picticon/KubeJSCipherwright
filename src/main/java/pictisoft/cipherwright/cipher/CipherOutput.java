package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CipherOutput extends CipherWell
{
    @Override
    boolean isSupportedType(String type)
    {
        return (type != null && !type.isEmpty() &&
                (type.equals("itemstack"))
        );
    }

//    public static CipherOutput SingleOutput()
//    {
//        var ret = new CipherOutput();
//        ret.x = 5;
//        ret.y = 1;
//        ret.width = 1;
//        ret.height = 1;
//        ret.large = true;
//        ret.type = "itemstack";
//        return ret;
//    }

}
