//package pictisoft.cipherwright.cipher;
//
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.tags.TagKey;
//
//import net.minecraftforge.items.IItemHandler;
//import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
//import pictisoft.cipherwright.registry.ContainerRegistry;
//
//import java.util.*;
//
//public class BlueprintContainer extends AbstractContainerMenu {
//    //. private final BlueprintBlockEntity blockEntity;
//    //. private final List<BlueprintSlot> blueprintSlots = new ArrayList<>();
//    //. private final Player player;
//
//    public BlueprintContainer(int containerId, Inventory playerInventory, BlueprintBlockEntity blockEntity) {
//        super(ContainerRegistry.BLUEPRINT_CONTAINER_TYPE.get(), containerId);
//        this.blockEntity = blockEntity;
//        this.player = playerInventory.player;
//
//        // Add player inventory slots (these don't change)
//        for(int row = 0; row < 3; row++) {
//            for(int col = 0; col < 9; col++) {
//                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
//                        8 + col * 18, 84 + row * 18));
//            }
//        }
//
//        // Add player hotbar slots
//        for(int col = 0; col < 9; col++) {
//            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
//        }
//
//        // Create blueprint slots
//        rebuildBlueprintSlots();
//    }
//
//    public void rebuildBlueprintSlots() {
//        // Remove existing blueprint slots
//        if (!blueprintSlots.isEmpty()) {
//            slots.removeAll(blueprintSlots);
//            blueprintSlots.clear();
//        }
//
//        // Get current layout configuration from block entity
//        BlueprintBlockEntity.LayoutConfig config = blockEntity.getCurrentLayout();
//
//        // Add new slots based on configuration
//        for (int i = 0; i < config.getSlotCount(); i++) {
//            BlueprintSlot slot = new BlueprintSlot(
//                i,
//                config.getSlotX(i),
//                config.getSlotY(i),
//                blockEntity.getSlotMode(i),
//                blockEntity.getSlotIngredient(i),
//                blockEntity.getSlotTagKey(i)
//            );
//            blueprintSlots.add(slot);
//            addSlot(slot);
//        }
//    }
//
//    public void toggleSlotMode(int slotIndex) {
//        if (slotIndex >= 0 && slotIndex < blueprintSlots.size()) {
//            BlueprintSlot slot = blueprintSlots.get(slotIndex);
//            SlotMode newMode = slot.getMode() == SlotMode.ITEM ? SlotMode.TAG : SlotMode.ITEM;
//
//            // Update block entity data
//            blockEntity.setSlotMode(slotIndex, newMode);
//
//            // Update slot
//            slot.setMode(newMode);
//
//            // Send update to client/server
//            if (!player.level().isClientSide()) {
//                // Server -> Client
//                PacketHandler.sendToClient(new BlueprintSlotUpdatePacket(containerId, slotIndex, newMode,
//                        slot.getIngredient(), slot.getTagKey()), player);
//            } else {
//                // Client -> Server
//                PacketHandler.sendToServer(new BlueprintSlotUpdatePacket(containerId, slotIndex, newMode,
//                        slot.getIngredient(), slot.getTagKey()));
//            }
//        }
//    }
//
//    public void setSlotTag(int slotIndex, TagKey<Item> tagKey) {
//        if (slotIndex >= 0 && slotIndex < blueprintSlots.size()) {
//            BlueprintSlot slot = blueprintSlots.get(slotIndex);
//
//            // Only update if we're in TAG mode
//            if (slot.getMode() == SlotMode.TAG) {
//                // Update block entity data
//                blockEntity.setSlotTagKey(slotIndex, tagKey);
//
//                // Update slot
//                slot.setTagKey(tagKey);
//
//                // Send update packet
//                if (!player.level().isClientSide()) {
//                    PacketHandler.sendToClient(new BlueprintSlotUpdatePacket(containerId, slotIndex, slot.getMode(),
//                            slot.getIngredient(), tagKey), player);
//                } else {
//                    PacketHandler.sendToServer(new BlueprintSlotUpdatePacket(containerId, slotIndex, slot.getMode(),
//                            slot.getIngredient(), tagKey));
//                }
//            }
//        }
//    }
//
//    @Override
//    public ItemStack quickMoveStack(Player player, int index) {
//        // Handle shift-clicking - for blueprint slots this should just copy the reference
//        // rather than actually moving items
//        ItemStack itemstack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(index);
//
//        if (slot != null && slot.hasItem()) {
//            ItemStack stackInSlot = slot.getItem();
//            itemstack = stackInSlot.copy();
//
//            if (slot instanceof BlueprintSlot) {
//                // Don't allow shift-clicking out of blueprint slots
//                return ItemStack.EMPTY;
//            } else {
//                // Player inventory slot - just copy to a selected blueprint slot
//                // (you'd need UI to select which blueprint slot is "active")
//                // ... implementation ...
//            }
//        }
//
//        return itemstack;
//    }
//
//    @Override
//    public boolean stillValid(Player player) {
//        return player.distanceToSqr(
//                blockEntity.getBlockPos().getX() + 0.5,
//                blockEntity.getBlockPos().getY() + 0.5,
//                blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
//    }
//
//    // Blueprint-specific slot implementation
//    public static class BlueprintSlot extends Slot {
//        private ItemStack displayStack = ItemStack.EMPTY;
//        private KubeJSTableContainer.SlotMode mode = KubeJSTableContainer.SlotMode.ITEM;
//        private TagKey<Item> tagKey;
//        private final int slotIndex;
//
//        public BlueprintSlot(int slotIndex, int x, int y, KubeJSTableContainer.SlotMode mode, ItemStack ingredient, TagKey<Item> tagKey) {
//            super(new DummyInventory(), 0, x, y);
//            this.slotIndex = slotIndex;
//            this.mode = mode;
//            this.displayStack = ingredient.copy();
//            this.tagKey = tagKey;
//        }
//
//        @Override
//        public boolean mayPlace(ItemStack stack) {
//            // Always allow placing items (they won't actually be stored)
//            return true;
//        }
//
//        @Override
//        public boolean mayPickup(Player player) {
//            // Allow "picking up" just to clear the slot display
//            return true;
//        }
//
//        @Override
//        public void set(ItemStack stack) {
//            // Don't actually store the item, just keep a reference
//            this.displayStack = stack.copy();
//            setChanged();
//        }
//
//        @Override
//        public ItemStack getItem() {
//            // Return a copy of the display stack
//            return this.displayStack.copy();
//        }
//
//        @Override
//        public ItemStack remove(int amount) {
//            // Just clear the displayed item without actually returning it
//            ItemStack oldStack = this.displayStack.copy();
//            this.displayStack = ItemStack.EMPTY;
//            setChanged();
//            return ItemStack.EMPTY; // Return empty since we don't actually give items
//        }
//
//        @Override
//        public boolean isActive() {
//            return true;
//        }
//
//        public KubeJSTableContainer.SlotMode getMode() {
//            return mode;
//        }
//
//        public void setMode(KubeJSTableContainer.SlotMode mode) {
//            this.mode = mode;
//        }
//
//        public TagKey<Item> getTagKey() {
//            return tagKey;
//        }
//
//        public void setTagKey(TagKey<Item> tagKey) {
//            this.tagKey = tagKey;
//        }
//
//        public ItemStack getIngredient() {
//            return displayStack;
//        }
//
//        public int getSlotIndex() {
//            return slotIndex;
//        }
//
//        // Dummy inventory implementation just to satisfy Slot requirements
//        public static class DummyInventory implements IItemHandler {
//            @Override
//            public int getSlots() {
//                return 1;
//            }
//
//            @Override
//            public ItemStack getStackInSlot(int slot) {
//                return ItemStack.EMPTY;
//            }
//
//            @Override
//            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
//                return stack; // Can't insert anything
//            }
//
//            @Override
//            public ItemStack extractItem(int slot, int amount, boolean simulate) {
//                return ItemStack.EMPTY; // Can't extract anything
//            }
//
//            @Override
//            public int getSlotLimit(int slot) {
//                return 64;
//            }
//
//            @Override
//            public boolean isItemValid(int slot, ItemStack stack) {
//                return true;
//            }
//        }
//    }
//
//}
