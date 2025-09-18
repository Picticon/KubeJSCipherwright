package pictisoft.cipherwright.blocks.kubejstable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class KubeJSTableBlock extends BaseEntityBlock
{
    public KubeJSTableBlock()
    {
        super(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE).strength(1f).sound(SoundType.WOOD));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new KubeJSTableBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public static final String KUBEJS_TABLE_TITLE = "screen.kubejs.table.title";

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
    {
        if (handIn != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!world.isClientSide && world.getBlockEntity(pos) instanceof KubeJSTableBlockEntity tile)
        {
            MenuProvider containerProvider = new MenuProvider()
            {
                @Override
                public Component getDisplayName()
                {
                    return Component.translatable(KUBEJS_TABLE_TITLE);
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
                {
                    return new KubeJSTableContainer(windowId, pos, playerInventory, playerEntity);
                }
            };
            NetworkHooks.openScreen((ServerPlayer) player, containerProvider, tile.getBlockPos());
        }
        return InteractionResult.SUCCESS;
    }

}