//package pictisoft.cipherwright.blocks.recipepattern;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.MenuProvider;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import pictisoft.cipherwright.cipher.Cipher;
//import pictisoft.cipherwright.cipher.CipherJsonLoader;
//import pictisoft.cipherwright.registry.BlockEntityRegistry;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class RecipePatternBlockEntity extends BlockEntity implements MenuProvider
//{
//    private Cipher currentCipher;
//    private final Map<Integer, ItemStack> rememberedItems = new HashMap<>();
//    private final Map<Integer, ResourceLocation> slotTags = new HashMap<>();
//
//    public RecipePatternBlockEntity(BlockPos pos, BlockState state)
//    {
//        super(BlockEntityRegistry.RECIPE_PATTERN_BLOCK_ENTITY.get(), pos, state);
//    }
//
//    @Override
//    public Component getDisplayName()
//    {
//        return Component.translatable("container.recipe_pattern");
//    }
//
//    @Override
//    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
//    {
//        return new RecipePatternMenu(containerId, inventory, this);
//    }
//
//    public Cipher getCipher()
//    {
//        return this.currentCipher;
//    }
//
//    public void setCipher(Cipher type)
//    {
//        this.currentCipher = type;
//        this.rememberedItems.clear();
//        this.slotTags.clear();
//        setChanged();
//    }
//
//    // Methods to handle remembered items and tags
//    public void setRememberedItem(int slot, ItemStack stack)
//    {
//        if (stack.isEmpty())
//        {
//            rememberedItems.remove(slot);
//        } else
//        {
//            rememberedItems.put(slot, stack.copy());
//        }
//        setChanged();
//    }
//
//    public void setSlotTag(int slot, ResourceLocation tagId)
//    {
//        if (tagId == null)
//        {
//            slotTags.remove(slot);
//        } else
//        {
//            slotTags.put(slot, tagId);
//        }
//        setChanged();
//    }
//
//    // Serialization methods
//    @Override
//    protected void saveAdditional(CompoundTag tag)
//    {
//        super.saveAdditional(tag);
//        if (currentCipher != null)
//        {
//            tag.putString("Cipher", currentCipher.getRecipeId().toString());
//        }
//
//        CompoundTag itemsTag = new CompoundTag();
//        rememberedItems.forEach((slot, stack) -> {
//            CompoundTag slotTag = new CompoundTag();
//            stack.save(slotTag);
//            itemsTag.put(String.valueOf(slot), slotTag);
//        });
//        tag.put("RememberedItems", itemsTag);
//
//        CompoundTag tagsTag = new CompoundTag();
//        slotTags.forEach((slot, tagId) -> {
//            tagsTag.putString(String.valueOf(slot), tagId.toString());
//        });
//        tag.put("SlotTags", tagsTag);
//    }
//
//    @Override
//    public void load(CompoundTag tag)
//    {
//        super.load(tag);
//        if (tag.contains("Cipher"))
//        {
//            this.currentCipher = CipherJsonLoader.getCipherByRecipeId(new ResourceLocation(tag.getString("Cipher")));
//        }
//
//        rememberedItems.clear();
//        CompoundTag itemsTag = tag.getCompound("RememberedItems");
//        for (String key : itemsTag.getAllKeys())
//        {
//            int slot = Integer.parseInt(key);
//            ItemStack stack = ItemStack.of(itemsTag.getCompound(key));
//            rememberedItems.put(slot, stack);
//        }
//
//        slotTags.clear();
//        CompoundTag tagsTag = tag.getCompound("SlotTags");
//        for (String key : tagsTag.getAllKeys())
//        {
//            int slot = Integer.parseInt(key);
//            ResourceLocation tagId = new ResourceLocation(tagsTag.getString(key));
//            slotTags.put(slot, tagId);
//        }
//    }
//}