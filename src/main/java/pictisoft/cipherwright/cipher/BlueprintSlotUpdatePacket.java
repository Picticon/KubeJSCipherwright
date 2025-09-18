//package pictisoft.cipherwright.cipher;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.network.NetworkEvent;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.function.Supplier;
//
//public class BlueprintSlotUpdatePacket
//{
//    private final int containerId;
//    private final int slotIndex;
//    private final BlueprintContainer.SlotMode mode;
//    private final ItemStack ingredient;
//    private final TagKey<Item> tagKey;
//
//    public BlueprintSlotUpdatePacket(int containerId, int slotIndex, BlueprintContainer.SlotMode mode,
//            ItemStack ingredient, TagKey<Item> tagKey)
//    {
//        this.containerId = containerId;
//        this.slotIndex = slotIndex;
//        this.mode = mode;
//        this.ingredient = ingredient != null ? ingredient.copy() : ItemStack.EMPTY;
//        this.tagKey = tagKey;
//    }
//
//    public static void encode(BlueprintSlotUpdatePacket packet, FriendlyByteBuf buffer)
//    {
//        buffer.writeInt(packet.containerId);
//        buffer.writeInt(packet.slotIndex);
//        buffer.writeEnum(packet.mode);
//        buffer.writeItem(packet.ingredient);
//
//        // Write tag key
//        boolean hasTagKey = packet.tagKey != null;
//        buffer.writeBoolean(hasTagKey);
//        if (hasTagKey)
//        {
//            buffer.writeResourceLocation(packet.tagKey.location());
//        }
//    }
//
//    public static BlueprintSlotUpdatePacket decode(FriendlyByteBuf buffer)
//    {
//        int containerId = buffer.readInt();
//        int slotIndex = buffer.readInt();
//        BlueprintContainer.SlotMode mode = buffer.readEnum(BlueprintContainer.SlotMode.class);
//        ItemStack ingredient = buffer.readItem();
//
//        // Read tag key
//        TagKey<Item> tagKey = null;
//        boolean hasTagKey = buffer.readBoolean();
//        if (hasTagKey)
//        {
//            ResourceLocation tagLocation = buffer.readResourceLocation();
//            tagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagLocation);
//        }
//
//        return new BlueprintSlotUpdatePacket(containerId, slotIndex, mode, ingredient, tagKey);
//    }
//
//    public static void handle(BlueprintSlotUpdatePacket message, Supplier<NetworkEvent.Context> ctx)
//    {
//        ctx.get().enqueueWork(() -> {
//            if (ctx.get().getDirection().getReceptionSide().isClient())
//            {
//                // Handle on client
//                handleClient(message);
//            } else
//            {
//                // Handle on server
//                handleServer(message, ctx.get().getSender());
//            }
//        });
//        ctx.get().setPacketHandled(true);
//    }
//
//    private static void handleClient(BlueprintSlotUpdatePacket message)
//    {
//        Minecraft minecraft = Minecraft.getInstance();
//        if (minecraft.player != null &&
//                minecraft.player.containerMenu.containerId == message.containerId &&
//                minecraft.player.containerMenu instanceof BlueprintContainer container)
//        {
//
//            // Update the slot in the container
//            BlueprintContainer.BlueprintSlot slot = container.getBlueprintSlot(message.slotIndex);
//            if (slot != null)
//            {
//                slot.setMode(message.mode);
//                slot.set(message.ingredient);
//                if (message.tagKey != null)
//                {
//                    slot.setTagKey(message.tagKey);
//                }
//            }
//        }
//    }
//
//    private static void handleServer(BlueprintSlotUpdatePacket message, ServerPlayer player)
//    {
//        if (player.containerMenu.containerId == message.containerId &&
//                player.containerMenu instanceof BlueprintContainer container)
//        {
//
//            // Update the slot in the container
//            if (message.mode == BlueprintContainer.SlotMode.ITEM)
//            {
//                container.setSlotIngredient(message.slotIndex, message.ingredient);
//            } else
//            {
//                container.setSlotMode(message.slotIndex, message.mode);
//                if (message.tagKey != null)
//                {
//                    container.setSlotTag(message.slotIndex, message.tagKey);
//                }
//            }
//
//            // Update block entity
//            container.getBlockEntity().setChanged();
//        }
//    }
//}
