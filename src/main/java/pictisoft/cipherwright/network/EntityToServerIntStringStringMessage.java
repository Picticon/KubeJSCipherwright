package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityToServerIntStringStringMessage extends BlockEntityMessage
{
    private final int _variableId;
    private final String _value1;
    private final String _value2;

    public EntityToServerIntStringStringMessage(Level level, BlockPos blockEntitypos, int variableId, String value1, String value2)
    {
        super(level, blockEntitypos);
        _variableId = variableId;
        _value1 = value1;
        _value2 = value2;
    }

    public EntityToServerIntStringStringMessage(FriendlyByteBuf buf)
    {
        super(buf);
        _variableId = buf.readInt();
        if (buf.readBoolean())
            _value1 = buf.readUtf();
        else _value1 = null;
        if (buf.readBoolean())
            _value2 = buf.readUtf();
        else _value2 = null;
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(_variableId);
        if (_value1 == null)
            buf.writeBoolean(false);
        else
        {
            buf.writeBoolean(true);
            buf.writeUtf(_value1);
        }
        if (_value2 == null)
            buf.writeBoolean(false);
        else
        {
            buf.writeBoolean(true);
            buf.writeUtf(_value2);
        }
    }

    public void handleOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = supplier.get().getSender().level();
            if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
            var entity = level.getBlockEntity(blockpos);
            if (entity instanceof EntityToServerIntStringMessage.IToServerIntStringMessageHandler)
            {
                ((IToServerIntStringStringMessageHandler) entity).handleIntStringStringFromClient(_variableId, _value1, _value2);
            }
        });
    }
    public interface IToServerIntStringStringMessageHandler
    {
        void handleIntStringStringFromClient(int controlId, String value1, String value2);
    }

}
