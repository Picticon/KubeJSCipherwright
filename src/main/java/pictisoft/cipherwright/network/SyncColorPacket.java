//package pictisoft.cipherwright.network;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.core.BlockPos;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraftforge.network.NetworkEvent;
//import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;
//
//import java.util.function.Supplier;
//
//public class SyncColorPacket
//{
//    private final BlockPos pos;
//    private final int color;
//
//    public SyncColorPacket(BlockPos pos, int color)
//    {
//        this.pos = pos;
//        this.color = color;
//    }
//
//    public SyncColorPacket(FriendlyByteBuf friendlyByteBuf)
//    {
//        this.pos = friendlyByteBuf.readBlockPos();
//        this.color = friendlyByteBuf.readInt();
//    }
//
//    public static void encode(SyncColorPacket msg, FriendlyByteBuf buffer)
//    {
//        buffer.writeBlockPos(msg.pos);
//        buffer.writeInt(msg.color);
//    }
//
//    public static SyncColorPacket decode(FriendlyByteBuf buffer)
//    {
//        return new SyncColorPacket(buffer.readBlockPos(), buffer.readInt());
//    }
//
//    public static void handle(SyncColorPacket msg, Supplier<NetworkEvent.Context> ctx)
//    {
//        ctx.get().enqueueWork(() -> {
//            Minecraft mc = Minecraft.getInstance();
//            ClientLevel level = mc.level;
//            if (level != null)
//            {
//                BlockEntity be = level.getBlockEntity(msg.pos);
//                if (be instanceof KubeJSTableBlockEntity customBE)
//                {
//                    customBE.setColor(msg.color); // Apply new color
//                }
//            }
//        });
//        ctx.get().setPacketHandled(true);
//    }
//}