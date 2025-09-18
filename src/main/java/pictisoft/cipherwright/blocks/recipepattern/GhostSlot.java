//package pictisoft.cipherwright.blocks.recipepattern;
//
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.registries.ForgeRegistries;
//import pictisoft.cipherwright.cipher.Cipher;
//import pictisoft.cipherwright.cipher.ICipherGridObject;
//
//public class GhostSlot
//{
//    private final int id;
//    public final int x;
//    public final int y;
//    private final ICipherGridObject definition;
//    private final boolean isInput;
//
//    private ItemStack rememberedStack = ItemStack.EMPTY;
//    private ResourceLocation tagId = null;
//
//    public GhostSlot(int id, int x, int y, ICipherGridObject definition, boolean isInput)
//    {
//        this.id = id;
//        this.x = x;
//        this.y = y;
//        this.definition = definition;
//        this.isInput = isInput;
//    }
//
//    public int getId()
//    {
//        return id;
//    }
//
//    public boolean isInput()
//    {
//        return isInput;
//    }
//
//    public ItemStack getStack()
//    {
//        return rememberedStack;
//    }
//
//    public void setStack(ItemStack stack)
//    {
//        if (stack.isEmpty())
//        {
//            this.rememberedStack = ItemStack.EMPTY;
//        } else
//        {
//            // Only remember one item, not the count
//            ItemStack copy = stack.copy();
//            copy.setCount(1);
//            this.rememberedStack = copy;
//
//            // Clear tag if we set a new item
//            this.tagId = null;
//        }
//    }
//
//    public boolean hasTag()
//    {
//        return tagId != null;
//    }
//
//    public ResourceLocation getTagId()
//    {
//        return tagId;
//    }
//
//    public void setTagId(ResourceLocation tagId)
//    {
//        this.tagId = tagId;
//    }
//
//    public boolean isValidItem(ItemStack stack)
//    {
//        if (stack.isEmpty()) return true;
//
//        // Check against slot limitations
////        for (ItemPredicate limitation : definition.getLimitations()) {
////            if (!limitation.test(stack)) {
////                return false;
////            }
////        }
//
//        return true;
//    }
//
//    /**
//     * Checks if the given screen coordinates are within this slot
//     */
//    public boolean isMouseOver(double mouseX, double mouseY, int guiLeft, int guiTop)
//    {
//        return mouseX >= guiLeft + x && mouseX < guiLeft + x + 18 &&
//                mouseY >= guiTop + y && mouseY < guiTop + y + 18;
//    }
//
//    /**
//     * Handle mouse click on this ghost slot
//     *
//     * @return true if the slot accepted the item
//     */
//    public boolean handleClick(ItemStack heldStack, boolean isRightClick)
//    {
//        if (isRightClick && !rememberedStack.isEmpty())
//        {
//            // Right-click to clear slot or open tag selection
//            if (hasTag())
//            {
//                setTagId(null);
//            } else
//            {
//                setStack(ItemStack.EMPTY);
//            }
//            return true;
//        } else if (!isRightClick && !heldStack.isEmpty())
//        {
//            // Left-click with item to set slot
//            if (isValidItem(heldStack))
//            {
//                setStack(heldStack.copy());
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * Get encoded data for saving
//     */
//    public CompoundTag serializeNBT()
//    {
//        CompoundTag tag = new CompoundTag();
//        tag.putInt("Slot", id);
//
//        if (!rememberedStack.isEmpty())
//        {
//            CompoundTag itemTag = new CompoundTag();
//            rememberedStack.save(itemTag);
//            tag.put("Item", itemTag);
//        }
//
//        if (tagId != null)
//        {
//            tag.putString("Tag", tagId.toString());
//        }
//
//        return tag;
//    }
//
//    /**
//     * Load data from NBT
//     */
//    public void deserializeNBT(CompoundTag tag)
//    {
//        if (tag.contains("Item"))
//        {
//            rememberedStack = ItemStack.of(tag.getCompound("Item"));
//        } else
//        {
//            rememberedStack = ItemStack.EMPTY;
//        }
//
//        if (tag.contains("Tag"))
//        {
//            tagId = new ResourceLocation(tag.getString("Tag"));
//        } else
//        {
//            tagId = null;
//        }
//    }
//
//    /**
//     * Get a description of what this slot contains for use in templates
//     */
//    public String getContentDescription()
//    {
//        if (tagId != null)
//        {
//            return "#" + tagId;
//        } else if (!rememberedStack.isEmpty())
//        {
//            return ForgeRegistries.ITEMS.getKey(rememberedStack.getItem()).toString();
//        } else
//        {
//            return "minecraft:air";
//        }
//    }
//
//    /**
//     * Convert an item into slot pattern character for crafting recipes
//     */
//    public char getPatternChar()
//    {
//        if (rememberedStack.isEmpty() && tagId == null)
//        {
//            return ' '; // Empty slot
//        }
//
//        // For crafting grid, return a character based on slot ID
//        return (char) ('A' + id);
//    }
//}