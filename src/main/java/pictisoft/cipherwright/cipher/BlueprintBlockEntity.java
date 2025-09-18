//package pictisoft.cipherwright.cipher;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.tags.TagKey;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BlueprintBlockEntity extends BlockEntity {
//    private final List<ItemStack> slotIngredients = new ArrayList<>();
//    private final List<BlueprintContainer.SlotMode> slotModes = new ArrayList<>();
//    private final List<TagKey<Item>> slotTags = new ArrayList<>();
//    private LayoutConfig currentLayout;
//
//    public BlueprintBlockEntity(BlockPos pos, BlockState state) {
//        super(YourModBlockEntities.BLUEPRINT_BLOCK_ENTITY.get(), pos, state);
//        // Initialize with default layout
//        currentLayout = new LayoutConfig(3, 3); // 3x3 grid
//
//        // Initialize slots
//        for (int i = 0; i < currentLayout.getSlotCount(); i++) {
//            slotIngredients.add(ItemStack.EMPTY);
//            slotModes.add(BlueprintContainer.SlotMode.ITEM);
//            slotTags.add(null);
//        }
//    }
//
//    public LayoutConfig getCurrentLayout() {
//        return currentLayout;
//    }
//
//    public void setLayout(LayoutConfig layout) {
//        this.currentLayout = layout;
//
//        // Resize ingredient lists if needed
//        while (slotIngredients.size() < layout.getSlotCount()) {
//            slotIngredients.add(ItemStack.EMPTY);
//            slotModes.add(BlueprintContainer.SlotMode.ITEM);
//            slotTags.add(null);
//        }
//
//        // Trim if new layout has fewer slots
//        if (slotIngredients.size() > layout.getSlotCount()) {
//            slotIngredients.subList(layout.getSlotCount(), slotIngredients.size()).clear();
//            slotModes.subList(layout.getSlotCount(), slotModes.size()).clear();
//            slotTags.subList(layout.getSlotCount(), slotTags.size()).clear();
//        }
//
//        setChanged();
//    }
//
//    public ItemStack getSlotIngredient(int index) {
//        if (index >= 0 && index < slotIngredients.size()) {
//            return slotIngredients.get(index);
//        }
//        return ItemStack.EMPTY;
//    }
//
//    public void setSlotIngredient(int index, ItemStack stack) {
//        if (index >= 0 && index < slotIngredients.size()) {
//            slotIngredients.set(index, stack.copy());
//            setChanged();
//        }
//    }
//
//    public SlotMode getSlotMode(int index) {
//        if (index >= 0 && index < slotModes.size()) {
//            return slotModes.get(index);
//        }
//        return SlotMode.ITEM;
//    }
//
//    public void setSlotMode(int index, SlotMode mode) {
//        if (index >= 0 && index < slotModes.size()) {
//            slotModes.set(index, mode);
//            setChanged();
//        }
//    }
//
//    public TagKey<Item> getSlotTagKey(int index) {
//        if (index >= 0 && index < slotTags.size()) {
//            return slotTags.get(index);
//        }
//        return null;
//    }
//
//    public void setSlotTagKey(int index, TagKey<Item> tagKey) {
//        if (index >= 0 && index < slotTags.size()) {
//            slotTags.set(index, tagKey);
//            setChanged();
//        }
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        super.load(tag);
//
//        // Load layout
//        if (tag.contains("Layout")) {
//            CompoundTag layoutTag = tag.getCompound("Layout");
//            int rows = layoutTag.getInt("Rows");
//            int cols = layoutTag.getInt("Cols");
//            currentLayout = new LayoutConfig(rows, cols);
//        }
//
//        // Clear existing data
//        slotIngredients.clear();
//        slotModes.clear();
//        slotTags.clear();
//
//        // Load slot data
//        ListTag slotsTag = tag.getList("Slots", 10); // 10 = CompoundTag
//        for (int i = 0; i < slotsTag.size(); i++) {
//            CompoundTag slotTag = slotsTag.getCompound(i);
//
//            // Load ingredient
//            ItemStack ingredient = ItemStack.EMPTY;
//            if (slotTag.contains("Ingredient")) {
//                ingredient = ItemStack.of(slotTag.getCompound("Ingredient"));
//            }
//            slotIngredients.add(ingredient);
//
//            // Load mode
//            SlotMode mode = slotTag.getBoolean("IsTag") ? SlotMode.TAG : SlotMode.ITEM;
//            slotModes.add(mode);
//
//            // Load tag if applicable
//            TagKey<Item> tagKey = null;
//            if (slotTag.contains("TagKey")) {
//                String tagKeyStr = slotTag.getString("TagKey");
//                ResourceLocation tagLocation = new ResourceLocation(tagKeyStr);
//                tagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagLocation);
//            }
//            slotTags.add(tagKey);
//        }
//
//        // Make sure lists are properly sized
//        int slotCount = currentLayout.getSlotCount();
//        while (slotIngredients.size() < slotCount) {
//            slotIngredients.add(ItemStack.EMPTY);
//            slotModes.add(SlotMode.ITEM);
//            slotTags.add(null);
//        }
//    }
//
//    @Override
//    protected void saveAdditional(CompoundTag tag) {
//        super.saveAdditional(tag);
//
//        // Save layout
//        CompoundTag layoutTag = new CompoundTag();
//        layoutTag.putInt("Rows", currentLayout.getRows());
//        layoutTag.putInt("Cols", currentLayout.getCols());
//        tag.put("Layout", layoutTag);
//
//        // Save slot data
//        ListTag slotsTag = new ListTag();
//        for (int i = 0; i < slotIngredients.size(); i++) {
//            CompoundTag slotTag = new CompoundTag();
//
//            // Save ingredient
//            if (!slotIngredients.get(i).isEmpty()) {
//                CompoundTag ingredientTag = new CompoundTag();
//                slotIngredients.get(i).save(ingredientTag);
//                slotTag.put("Ingredient", ingredientTag);
//            }
//
//            // Save mode
//            slotTag.putBoolean("IsTag", slotModes.get(i) == SlotMode.TAG);
//
//            // Save tag if applicable
//            if (slotTags.get(i) != null) {
//                slotTag.putString("TagKey", slotTags.get(i).location().toString());
//            }
//
//            slotsTag.add(slotTag);
//        }
//        tag.put("Slots", slotsTag);
//    }
//
//    @Override
//    public ClientboundBlockEntityDataPacket getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        CompoundTag tag = new CompoundTag();
//        saveAdditional(tag);
//        return tag;
//    }
//
//    // Helper class to represent the layout configuration
//    public static class LayoutConfig {
//        private final int rows;
//        private final int cols;
//
//        public LayoutConfig(int rows, int cols) {
//            this.rows = rows;
//            this.cols = cols;
//        }
//
//        public int getRows() {
//            return rows;
//        }
//
//        public int getCols() {
//            return cols;
//        }
//
//        public int getSlotCount() {
//            return rows * cols;
//        }
//
//        public int getSlotX(int index) {
//            return 8 + (index % cols) * 18;
//        }
//
//        public int getSlotY(int index) {
//            return 20 + (index / cols) * 18;
//        }
//    }
//}
