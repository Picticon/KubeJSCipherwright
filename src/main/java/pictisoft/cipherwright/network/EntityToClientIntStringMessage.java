package pictisoft.cipherwright.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/*
public class EntityToClientIntStringMessage extends BlockEntityMessage
{
    private final int _variableId;
    private final String _value;

    public EntityToClientIntStringMessage(Level level, BlockPos blockEntitypos, int variableId, String value)
    {
        super(level, blockEntitypos);
        _variableId = variableId;
        _value = value;
    }

    public EntityToClientIntStringMessage(FriendlyByteBuf buf)
    {
        super(buf);
        _variableId = buf.readInt();
        var isNull = buf.readBoolean();
        _value = isNull ? null : buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(_variableId);
        buf.writeBoolean(_value == null);
        if (_value != null) buf.writeUtf(_value);
    }

    public void handleOnClient(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level != null)
            {
                if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
                var entity = level.getBlockEntity(blockpos);
                if (entity instanceof IHandleOnClientIntStringMessage)
                {
                    ((IHandleOnClientIntStringMessage) entity).handleOnClientIntString(_variableId, _value);
                }
            }
        });
    }

    public interface IHandleOnClientIntStringMessage
    {
        void handleOnClientIntString(int controlId, String value);
    }

}*/