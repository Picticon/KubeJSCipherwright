//package pictisoft.cipherwright.blocks.recipepattern;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.BaseEntityBlock;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockBehaviour;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraftforge.network.NetworkHooks;
//
//public class RecipePatternBlock extends BaseEntityBlock
//{
//    public RecipePatternBlock()
//    {
//        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3.0F));
//    }
//
//    @Override
//    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
//            InteractionHand hand, BlockHitResult hit)
//    {
//        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
//        {
//            BlockEntity blockEntity = level.getBlockEntity(pos);
//            if (blockEntity instanceof RecipePatternBlockEntity)
//            {
//                NetworkHooks.openScreen(serverPlayer, (RecipePatternBlockEntity) blockEntity, pos);
//            }
//        }
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
//    {
//        return new RecipePatternBlockEntity(pPos, pState);
//    }
//
//}