package pictisoft.cipherwright.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class BlockEntityMessage
{
    //protected final String dimensionType;
    protected final ResourceKey<Level> dimension;
    protected BlockPos blockpos;

    public BlockEntityMessage(Level level, BlockPos blockEntitypos)
    {
//        this.dimensionType = level.dimension().registry().toString();
        this.dimension = level.dimension();
        this.blockpos = blockEntitypos;
    }

    public BlockEntityMessage(FriendlyByteBuf buf)
    {
        //      this.dimensionType = Registries.DIMENSION_TYPE.registry()
        this.dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.blockpos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        //    buf.writeWithCodec(DimensionType.CODEC, this.dimensionType);
        buf.writeResourceLocation(this.dimension.location());
        buf.writeInt(this.blockpos.getX());
        buf.writeInt(this.blockpos.getY());
        buf.writeInt(this.blockpos.getZ());
    }

}
