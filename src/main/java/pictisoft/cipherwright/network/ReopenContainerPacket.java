package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;

import java.util.function.Supplier;

public class ReopenContainerPacket
{
    private final BlockPos pos;
    //private final int customValue;

    public ReopenContainerPacket(FriendlyByteBuf buf)
    {
//        this.containerId = buf.readInt();
//        this.customValue = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public ReopenContainerPacket(BlockPos pos)
    {
//        this.containerId = containerId;
//        this.customValue = customValue;
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf buf)
    {
//        buf.writeInt(containerId);
//        buf.writeInt(customValue);
        buf.writeBlockPos(pos);
    }

    // the open-screen message comes the server, so the trigger has to be handled on the server.
    public void handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null)
            {
                Level level = player.level();
                if (level.getBlockEntity(pos) instanceof MenuProvider blockEntity)
                {
                    //MinecraftServer server = player.server;

//                    server.execute(() -> {
                        player.closeContainer();
//                        server.execute(() -> {
//                            player.openMenu(blockEntity);
//                        });
//                    });
                    NetworkHooks.openScreen((ServerPlayer) player, blockEntity, pos);
                }
            }
            context.setPacketHandled(true);
        });
    }
}
