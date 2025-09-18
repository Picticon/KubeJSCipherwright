package pictisoft.cipherwright.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import pictisoft.cipherwright.CipherWrightMod;

public final class MessageRegistry
{

    public static SimpleChannel INSTANCE;

    public static void register()
    {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(CipherWrightMod.MODID, "wd40"), //
                () -> "1.0", //
                s -> true, //
                s -> true);

        INSTANCE.messageBuilder(EntityC2SCipherCommand.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(EntityC2SCipherCommand::new) //
                .encoder(EntityC2SCipherCommand::toBytes) //
                .consumerMainThread(EntityC2SCipherCommand::handleOnServer) //
                .add();
//        INSTANCE.messageBuilder(EntityToServerCipherIntMessage.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
//                .decoder(EntityToServerCipherIntMessage::new) //
//                .encoder(EntityToServerCipherIntMessage::toBytes) //
//                .consumerMainThread(EntityToServerCipherIntMessage::handleOnServer) //
//                .add();
        INSTANCE.messageBuilder(EntityToServerIntIntMessage.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(EntityToServerIntIntMessage::new) //
                .encoder(EntityToServerIntIntMessage::toBytes) //
                .consumerMainThread(EntityToServerIntIntMessage::handleOnServer) //
                .add();
        INSTANCE.messageBuilder(EntityToServerIntStringMessage.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(EntityToServerIntStringMessage::new) //
                .encoder(EntityToServerIntStringMessage::toBytes) //
                .consumerMainThread(EntityToServerIntStringMessage::handleOnServer) //
                .add();
        INSTANCE.messageBuilder(EntityToServerIntStringStringMessage.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(EntityToServerIntStringStringMessage::new) //
                .encoder(EntityToServerIntStringStringMessage::toBytes) //
                .consumerMainThread(EntityToServerIntStringStringMessage::handleOnServer) //
                .add();

        //        INSTANCE.messageBuilder(EntityToClientIntStringMessage.class, nextid(), NetworkDirection.PLAY_TO_CLIENT) //
//                .decoder(EntityToClientIntStringMessage::new) //
//                .encoder(EntityToClientIntStringMessage::toBytes) //
//                .consumerMainThread(EntityToClientIntStringMessage::handleOnClient) //
//                .add();
        INSTANCE.messageBuilder(RecipeMessage.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(RecipeMessage::new) //
                .encoder(RecipeMessage::toBytes) //
                .consumerMainThread(RecipeMessage::handleOnServer) //
                .add();

        INSTANCE.messageBuilder(ReopenContainerPacket.class, nextid(), NetworkDirection.PLAY_TO_SERVER) //
                .decoder(ReopenContainerPacket::new) //
                .encoder(ReopenContainerPacket::encode) //
                .consumerMainThread(ReopenContainerPacket::handle) //
                .add();

        INSTANCE.messageBuilder(SyncParameterTrigger.class, nextid(), NetworkDirection.PLAY_TO_CLIENT) //
                .decoder(SyncParameterTrigger::new) //
                .encoder(SyncParameterTrigger::encode) //
                .consumerMainThread(SyncParameterTrigger::handleOnClient) //
                .add();

//        INSTANCE.messageBuilder(SyncColorPacket.class, nextid(), NetworkDirection.PLAY_TO_CLIENT) //
//                .decoder(SyncColorPacket::new) //
//                .encoder(SyncColorPacket::encode) //
//                .consumerMainThread(SyncColorPacket::handle) //
//                .add();

//        INSTANCE.messageBuilder(SlotUpdatePacket.class, nextid(), NetworkDirection.PLAY_TO_CLIENT)//
//                .decoder(SlotUpdatePacket::decode) //
//                .encoder(SlotUpdatePacket::encode) //
//                .consumerMainThread(SlotUpdatePacket::handle) //
//                .add();

    }

    public static <MSG> void sendToServer(MSG message)
    {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.isClientSide)
            INSTANCE.sendToServer(message);
    }

    // public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
    //     INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    // }

    private static int packetId = 0;

    private static int nextid()
    {
        return packetId++;
    }

    public static void sendToClient(ServerPlayer player, Object packet)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToClientsNear(BlockPos pos, Level level, Object packet)
    {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }
}
