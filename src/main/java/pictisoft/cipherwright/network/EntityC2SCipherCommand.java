package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import pictisoft.cipherwright.cipher.CipherSlot;

import java.util.function.Supplier;

public class EntityC2SCipherCommand extends BlockEntityMessage
{
    private final String _serial;
    private final int _command;
    private final String _string;

    public EntityC2SCipherCommand(Level level, BlockPos blockEntitypos, int commandId, CipherSlot cipherslot, String commandString)
    {
        super(level, blockEntitypos);
        _serial = cipherslot.getSerialKey();
        _command = commandId;
        _string = commandString;
    }

    public EntityC2SCipherCommand(FriendlyByteBuf buf)
    {
        super(buf);
        _serial = buf.readUtf();
        _command = buf.readInt();
        _string = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeUtf(_serial);
        buf.writeInt(_command);
        buf.writeUtf(_string);
    }

    public void handleOnServer(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            var level = supplier.get().getSender().level();
            if (level.dimension() != dimension) return; // player isn't in same dimension... ignore
            var entity = level.getBlockEntity(blockpos);
            if (entity instanceof EntityC2SCipherCommand.IHandleEntityC2SMessage)
            {
                ((EntityC2SCipherCommand.IHandleEntityC2SMessage) entity).handleC2SMessageForCipher(_serial, _command, _string);
            }
        });
    }

    public interface IHandleEntityC2SMessage
    {
        void handleC2SMessageForCipher(String serial, int command, String string);
    }
}
