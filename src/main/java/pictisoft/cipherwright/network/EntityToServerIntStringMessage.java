package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityToServerIntStringMessage extends BlockEntityMessage
{
    private final int _variableId;
    private final String _value;

    public EntityToServerIntStringMessage(Level level, BlockPos blockEntitypos, int variableId, String value)
    {
        super(level, blockEntitypos);
        _variableId = variableId;
        _value = value;
    }

    public EntityToServerIntStringMessage(FriendlyByteBuf buf)
    {
        super(buf);
        _variableId = buf.readInt();
        if (buf.readBoolean())
            _value = buf.readUtf();
        else _value = null;
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(_variableId);
        if (_value == null)
            buf.writeBoolean(false);
        else
        {
            buf.writeBoolean(true);
            buf.writeUtf(_value);
        }
    }

    public void handleOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = supplier.get().getSender().level();
            if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
            var entity = level.getBlockEntity(blockpos);
            if (entity instanceof IToServerIntStringMessageHandler)
            {
                ((IToServerIntStringMessageHandler) entity).handleFromClientIntString(_variableId, _value);
            }
        });
    }

    public interface IToServerIntStringMessageHandler
    {
        void handleFromClientIntString(int controlId, String value);
    }

}