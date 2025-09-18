package pictisoft.cipherwright.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
import pictisoft.cipherwright.cipher.CipherSlot;
import pictisoft.cipherwright.util.Chatter;

import java.util.function.Supplier;

 /*
public class SlotUpdatePacket
{
    private final int containerId;
    private final int slotIndex;
    //    private final CipherSlot.SlotMode mode;
    //    private final ItemStack ghostItem;
    //    private final FluidStack ghostFluid;
    //    private final TagKey<Item> tagKey;
    private final CompoundTag tag;

//    public SlotUpdatePacket(int containerId, CipherSlot slot)
//    {
//        this.containerId = containerId;
//        var tag = new CompoundTag();
//        slot.saveExtra(tag);
//        this.tag = tag;
//        this.slotIndex = slot.index;
//    }

    public SlotUpdatePacket(int containerId, int slotIndex, CompoundTag tag)
    {
        this.containerId = containerId;
        this.slotIndex = slotIndex;
        this.tag = tag;
    }

    public static void encode(SlotUpdatePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeInt(packet.containerId);
        buffer.writeInt(packet.slotIndex);
        buffer.writeNbt(packet.tag);
//        buffer.writeEnum(packet.mode);
//        switch (packet.mode)
//        {
//            case ITEM:
//            {
//                if (!packet.ghostItem.isEmpty())
//                {
//                    Chatter.chat("Not empty!");
//                }
//                buffer.writeItem(packet.ghostItem);
//                break;
//            }
//            case FLUID:
//            {
//                buffer.writeFluidStack(packet.ghostFluid);
//                break;
//            }
//            case TAG:
//            {
//                buffer.writeResourceLocation(packet.tagKey.location());
//                break;
//            }
//        }
    }

    //
    public static SlotUpdatePacket decode(FriendlyByteBuf buffer)
    {
        int containerId = buffer.readInt();
        int slotIndex = buffer.readInt();
        var tag = buffer.readNbt();
        CipherSlot.SlotMode mode = CipherSlot.SlotMode.valueOf(tag.getString("mode"));
        return new SlotUpdatePacket(containerId, slotIndex, tag);
//        return switch (mode)
//        {
//            case ITEM -> new SlotUpdatePacket(containerId, slotIndex, mode, buffer.readItem());
//            case FLUID -> new SlotUpdatePacket(containerId, slotIndex, mode, buffer.readFluidStack());
//            case TAG -> new SlotUpdatePacket(containerId, slotIndex, mode, buffer.readResourceLocation());
//        };
    }

    public static void handle(SlotUpdatePacket message, Supplier<NetworkEvent.Context> ctx)
    {
        //Chatter.chatDebug(ctx.get().getSender().serverLevel(), "handle SlotUpdatePacket");
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient())
            {
                // Handle on client
                handleClient(message);
            } else
            {
                // Handle on server
                handleServer(message, ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(SlotUpdatePacket message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.containerMenu.containerId == message.containerId && minecraft.player.containerMenu instanceof KubeJSTableContainer container)
        {

            // Update the slot in the container
            var slot = container.getSlot(message.slotIndex);
            if (slot instanceof CipherSlot cipherslot)
            {
                cipherslot.sync(message);
            }
        }
    }

    private static void handleServer(SlotUpdatePacket message, ServerPlayer player)
    {
        if (player.containerMenu.containerId == message.containerId && player.containerMenu instanceof KubeJSTableContainer container)
        {

//            // Update the slot in the container
//            if (message.mode == KubeJSTableContainer.SlotMode.ITEM)
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
        }
    }

    public CompoundTag getTag()
    {
        return tag;
    }

//    public CipherSlot.SlotMode getMode()
//    {
//        return mode;
//    }
//
//    public ItemStack getItemStack()
//    {
//        return ghostItem;
//    }
//
//    public FluidStack getFluidStack()
//    {
//        return ghostFluid;
//    }
//
//    public TagKey<Item> getTagKey()
//    {
//        return tagKey;
//    }
}
*/