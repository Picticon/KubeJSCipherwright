package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/// --
/// this sends a simple message containing two integers from the client to the server
/// block entities can use it by implementing the IHandleClientIntMessage
/// --

public class EntityToServerIntIntMessage extends BlockEntityMessage
{
    private final int _variableId;
    private final int _value;

    public EntityToServerIntIntMessage(Level level, BlockPos blockEntitypos, int variableId, int value)
    {
        super(level, blockEntitypos);
        _variableId = variableId;
        _value = value;
    }

    public EntityToServerIntIntMessage(FriendlyByteBuf buf)
    {
        super(buf);
        _variableId = buf.readInt();
        _value = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(_variableId);
        buf.writeInt(_value);
    }

    public void handleOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = supplier.get().getSender().level();
            ServerPlayer player = ctx.getSender();
            if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
            var entity = level.getBlockEntity(blockpos);
            if (entity instanceof IToServerIntIntMessageHandler)
            {
                ((IToServerIntIntMessageHandler) entity).handleFromClientIntInt(_variableId, _value);
            }
            if (entity instanceof IToServerIntIntMessageHandlerWithPlayer)
            {
                ((IToServerIntIntMessageHandlerWithPlayer) entity).handleFromClientIntInt(_variableId, _value, player);
            }
        });
    }

    public interface IToServerIntIntMessageHandler
    {
        void handleFromClientIntInt(int controlId, int value);
    }

    public interface IToServerIntIntMessageHandlerWithPlayer
    {
        void handleFromClientIntInt(int controlId, int value, ServerPlayer player);
    }

}

