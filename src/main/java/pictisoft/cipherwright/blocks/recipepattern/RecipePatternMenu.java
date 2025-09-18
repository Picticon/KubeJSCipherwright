//package pictisoft.cipherwright.blocks.recipepattern;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.ContainerData;
//import net.minecraft.world.inventory.SimpleContainerData;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.item.ItemStack;
//import pictisoft.cipherwright.cipher.Cipher;
//import pictisoft.cipherwright.registry.ContainerRegistry;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RecipePatternMenu extends AbstractContainerMenu
//{
//    private final RecipePatternBlockEntity blockEntity;
//    private final ContainerData data;
//
//    // This doesn't use normal slots but rather "ghost" slots
//    private final List<GhostSlot> ghostSlots = new ArrayList<>();
//
//    public RecipePatternMenu(int containerId, Inventory playerInventory, BlockPos pos)
//    {
//        this(containerId, playerInventory, (RecipePatternBlockEntity) playerInventory.player.getCommandSenderWorld().getBlockEntity(pos));
//    }
//
//    public RecipePatternMenu(int containerId, Inventory inventory, RecipePatternBlockEntity recipePatternBlockEntity)
//    {
//        super(ContainerRegistry.RECIPE_PATTERN.get(), containerId);
//        this.blockEntity = recipePatternBlockEntity;
//        this.data = new SimpleContainerData(4); // For values needed on client
//
//        // Add player inventory slots
//        for (int row = 0; row < 3; row++)
//        {
//            for (int col = 0; col < 9; col++)
//            {
//                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
//            }
//        }
//
//        for (int col = 0; col < 9; col++)
//        {
//            this.addSlot(new Slot(inventory, col, 8 + col * 18, 198));
//        }
//
//        this.addDataSlots(data);
//
//        // Setup ghost slots based on recipe type
//        setupGhostSlots();
//    }
//
//    private void setupGhostSlots()
//    {
//        ghostSlots.clear();
//
//        Cipher type = blockEntity.getCipher();
//        if (type == null) return;
//
//        // Add input slots
//        for (var def : type.getInputs())
//        {
//            ghostSlots.add(new GhostSlot(ghostSlots.size(), def.getX(), def.getY(), def, true));
//        }
//
//        // Add output slots
//        for (var def : type.getOutputs())
//        {
//            ghostSlots.add(new GhostSlot(ghostSlots.size(), def.getX(), def.getY(), def, false));
//        }
//    }
//
//    @Override
//    public ItemStack quickMoveStack(Player pPlayer, int pIndex)
//    {
//        return null;
//    }
//
//    @Override
//    public boolean stillValid(Player pPlayer)
//    {
//        return pPlayer.distanceToSqr(
//                blockEntity.getBlockPos().getX() + 0.5,
//                blockEntity.getBlockPos().getY() + 0.5,
//                blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
//    }
//
//    public RecipePatternBlockEntity getBlockEntity()
//    {
//        return this.blockEntity;
//    }
//
//    public List<GhostSlot> getGhostSlots()
//    {
//        return ghostSlots;
//    }
//}