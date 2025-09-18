package pictisoft.cipherwright.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;

import java.util.Objects;
import java.util.function.Supplier;

public class SyncParameterTrigger extends BlockEntityMessage
{
    private final CompoundTag data;
    private final ResourceLocation originalRecipeID;

    public SyncParameterTrigger(@NotNull KubeJSTableBlockEntity entity)
    {
        super(Objects.requireNonNull(entity.getLevel()), entity.getBlockPos());
        CompoundTag tag = new CompoundTag();
        for (var kvp : entity.getCipherParameters().entrySet())
        {
            tag.putString(kvp.getKey(), kvp.getValue());
        }
        this.data = tag;
        this.originalRecipeID = entity.getOriginalRecipeID();
    }

    public SyncParameterTrigger(FriendlyByteBuf buf)
    {
        super(buf);
        this.data = buf.readNbt();
        if (buf.readBoolean())
            this.originalRecipeID = new ResourceLocation(buf.readUtf());
        else
            this.originalRecipeID = null;
    }

    // Encode (write to buffer)
    public void encode(FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeNbt(data);
        if (this.originalRecipeID == null)
            buf.writeBoolean(false);
        else
        {
            buf.writeBoolean(true);
            buf.writeUtf(this.originalRecipeID.toString());
        }
    }

    // Handle (apply on client)
    public void handleOnClient(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof IHandleSyncParameterTrigger screen)
            {
                screen.updateCustomData(blockpos, data);

            }
        });
    }

    public interface IHandleSyncParameterTrigger
    {
        void updateCustomData(BlockPos blockpos, CompoundTag data);
    }
}
