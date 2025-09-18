package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import pictisoft.cipherwright.cipher.CipherSlot;

import java.util.function.Supplier;

// unused...
public class EntityToServerCipherIntMessage extends BlockEntityMessage
{
    private final int _variableId;
    private final CipherCopy _passenger;

    // prepares the packet
    public EntityToServerCipherIntMessage(Level level, BlockPos blockEntitypos, int variableId, CipherSlot cipherslot)
    {
        super(level, blockEntitypos);
        _variableId = variableId;
        _passenger = new CipherCopy(cipherslot);
    }

    // decodes the packet
    public EntityToServerCipherIntMessage(FriendlyByteBuf buf)
    {
        super(buf);
        _variableId = buf.readInt();
        _passenger = new CipherCopy(buf);

    }

    // encodes the packet
    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(_variableId);
        buf.writeNbt(_passenger._tag);
        buf.writeUtf(_passenger._serial);
    }

    public void handleOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = supplier.get().getSender().level();
            if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
            var entity = level.getBlockEntity(blockpos);
            if (entity instanceof EntityToServerCipherIntMessage.IToServerIntIntMessageHandler)
            {
                ((EntityToServerCipherIntMessage.IToServerIntIntMessageHandler) entity).handleFromClientCipherInt(_variableId, _passenger);
            }
        });
    }

    public interface IToServerIntIntMessageHandler
    {
        void handleFromClientCipherInt(int controlId, CipherCopy CipherSlot);
    }


    public class CipherCopy
    {
        private final CompoundTag _tag;
        private final String _serial;

        public CipherCopy(CipherSlot cipherslot)
        {
            _tag = new CompoundTag();
            cipherslot.saveExtra(_tag);
            _serial = cipherslot.getSerialKey();
        }

        public CipherCopy(FriendlyByteBuf buf)
        {
            _tag = buf.readNbt();
            _serial = buf.readUtf();
        }
    }
}
