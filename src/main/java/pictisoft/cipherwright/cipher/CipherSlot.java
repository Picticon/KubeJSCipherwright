package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import pictisoft.cipherwright.util.*;

import java.io.IOException;
import java.util.List;

public class CipherSlot
{
    //private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    //private final int invSlot;
    private final boolean large;
    private final CipherGridObject cipherobject;
    private boolean _dirty;
    private ItemStack ghostStack = ItemStack.EMPTY;
    private FluidStack ghostFluid = FluidStack.EMPTY;
    private SlotMode _mode = SlotMode.ITEM;
    private boolean single;
    private TagKey<Item> tagKey;
    private int tagCount;
    private int x;
    private int y;
    private int _ticker;
    private int _lasttick;

//    public void sync(SlotUpdatePacket message)
//    {
//        loadExtra(message.getTag());
//    }

    public String getPath()
    {
        return cipherobject.getPathWithoutIndex();
    }

    public CipherGridObject getCipherobject()
    {
        return cipherobject;
    }

    public boolean isDirty()
    {
        return _dirty;
    }

    public int getX()
    {
        return cipherobject.getPosX();
    }

    public int getY()
    {
        return cipherobject.getPosY();
    }

    public JsonObject toJson() throws IOException
    {
        JsonObject ret = new JsonObject();
        switch (_mode)
        {
            case ITEM:
                if (getItemStack() != null && !getItemStack().isEmpty())
                {
                    // For single item ingredient
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(getItemStack().getItem());
                    ret.addProperty("item", itemId.toString());

                    // Add count if greater than 1
                    if (getItemStack().getCount() > 1)
                    {
                        ret.addProperty("count", getItemStack().getCount());
                    }
                    return ret;
                }
                break;
            case TAG:
                if (getTag() != null)
                {
                    // For tag ingredient
                    ret.addProperty("tag", getTag().location().toString());
                    return ret;
                }
                break;
            case FLUID:
            {
                // TODO
            }
            break;
        }
        return null;
    }

    public boolean canBeTag()
    {
        if (cipherobject instanceof CipherWell well) if (!well.allowTag()) return false;
        return _mode == SlotMode.ITEM && !this.getItemStack().isEmpty(); // this disables the "convert to tag" option... bad code
    }

    public boolean canBeFluid()
    {
        if (cipherobject instanceof CipherWell well) return well.allowFluid();
        return false;
    }

    public Ingredient getIngredient()
    {
        if (getMode() == SlotMode.ITEM) return Ingredient.of(getItemStack().copy());
        if (getMode() == SlotMode.TAG) return Ingredient.of(getTag());
        return Ingredient.EMPTY;
    }

    public CWIngredient getCWIngredient()
    {
        if (getMode() == SlotMode.ITEM) return CWIngredient.of(getItemStack().copy());
        if (getMode() == SlotMode.TAG) return CWIngredient.of(getTag());
        return CWIngredient.EMPTY;
    }

    public int adjustCount(int adjustment)
    {
        if (this.getMode() == SlotMode.ITEM)
        {
            this.getItemStack().setCount(
                    Math.max(1, Math.min(isSingle() ? 1 : this.getItemStack().getMaxStackSize(), this.getItemStack().getCount() + adjustment)));
            this.setDirty();
            return this.getItemStack().getCount();
        }
        if (this.getMode() == SlotMode.TAG)
        {
            if (!this.isSingle())
            {
                this.tagCount = Math.max(1, Math.min(64, tagCount + adjustment));
                this.setDirty();
                return this.tagCount;
            }
        }
        if (this.getMode() == SlotMode.FLUID)
        {
            this.getFluid().setAmount(Math.max(1, Math.min(1000, this.getFluid().getAmount() + adjustment)));
            this.setDirty();
            return this.getFluid().getAmount();
        }
        return 0;
    }

    public String getSerialKey()
    {
        return cipherobject.getPathWithIndex() + "[" + this.getX() + "," + this.getY() + "]";
    }

    public enum SlotMode
    {
        ITEM, TAG, FLUID
    }

    public CipherSlot(CipherWell cipherslot)
    {
        //super(inventory, slotIndex, x, y); // Dummy inventory
        //this.invSlot = slotIndex;
        this.single = cipherslot.getSingle();
        this.large = cipherslot.getLarge();
        this.cipherobject = cipherslot;
        //this.isInput = cipherslot instanceof CipherInput;
        this.x = cipherslot.getPosX();
        this.y = cipherslot.getPosY();
    }
/*
    @Override
    public void onTake(Player player, ItemStack stack)
    {
        // no takes
    }

    @Override
    public ItemStack remove(int amount)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return true;
    }

    @Override
    public ItemStack safeInsert(ItemStack pStack, int pIncrement)
    {
        if (!pStack.isEmpty() && this.mayPlace(pStack))
        {
            ItemStack itemstack = this.getItem();
            int i = Math.min(Math.min(pIncrement, pStack.getCount()), this.getMaxStackSize(pStack) - itemstack.getCount());
            if (itemstack.isEmpty())
            {
                this.setByPlayer(pStack);
            } else if (ItemStack.isSameItemSameTags(itemstack, pStack))
            {
                // pStack.shrink(i);
                // itemstack.grow(i);
                var newstack = ItemStackHelpers.mergeStacks(itemstack, pStack);
                this.setByPlayer(newstack);
            }
            return pStack;
        } else
        {
            return pStack;
        }
    }
  */

    private void setDirty()
    {
        _dirty = true;
    }

    public void setClean()
    {
        _dirty = false;
    }

    public void setItem(ItemStack stack)
    {
        if (this.single) stack.setCount(1);
        // Store the ghost item instead of putting it in an inventory
//        if (stack.isEmpty())
//            Chatter.chat("stackempty");
        this.ghostStack = stack.copy();
        this.ghostFluid = FluidStack.EMPTY;
        this.tagKey = null;
        this.tagCount = 0;
        this.setMode(SlotMode.ITEM);
        //Chatter.chat("setitem: " + stack.toString());
        this.setDirty();
    }

    public void setFluid(FluidStack fluid)
    {
        this.ghostFluid = fluid.copy();
        this.ghostStack = ItemStack.EMPTY;
        this.tagKey = null;
        this.tagCount = 0;
        this.setMode(SlotMode.FLUID);
        //Chatter.chat("setfluid: " + fluid.toString());
        this.setDirty();
    }

    public void setTagKey(String string)
    {

    }

    public void setTagKey(TagKey<Item> tagKey)
    {
        this.ghostStack = ItemStack.EMPTY; // ^ first
        this.ghostFluid = FluidStack.EMPTY;
        setMode(SlotMode.TAG);
        this.tagKey = tagKey;
        this.tagCount = 1;
        //Chatter.chat("settag: " + tagKey.toString());
        this.setDirty();
    }

    //    @Override
//    public boolean mayPickup(Player player)
//    {
//        return false;
//    }
//
//    @Override
    public ItemStack getItemStack()
    {
        return this.ghostStack;
    }

//    @Override
//    public void setChanged()
//    {
//        setDirty(); // not sure if needed
//        // Do nothing, since this slot does not interact with an inventory
//    }

//    @Override
//    public int getMaxStackSize()
//    {
//        return 1;
//    }

    public FluidStack getFluid()
    {
        return this.ghostFluid;
    }

    public SlotMode getMode()
    {
        return _mode == null ? SlotMode.ITEM : _mode;
    }

    public void setMode(SlotMode newMode)
    {
        _mode = newMode;
        this.setDirty();
    }

    public boolean isLarge()
    {
        return large;
    }

    public boolean isSingle()
    {
        return single;
    }

    public void render(GuiGraphics gui, GUIElementRenderer guiRenderer, int partialTicks)
    {
        _ticker += partialTicks - _lasttick;
        _lasttick = partialTicks;
        gui.pose().pushPose();
        if (this.getMode() == SlotMode.ITEM)
        {
            renderItemStack(gui, this.ghostStack);
        }
        if (this.getMode() == SlotMode.TAG)
        {
            try
            {
                if (Minecraft.getInstance().level != null)
                {
                    HolderLookup<Item> itemLookup = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ITEM);
                    List<Item> items = itemLookup.getOrThrow(tagKey) // returns a HolderSet<Item>
                            .stream().map(holder -> holder.value()).toList();
                    var idx = (_ticker / 10) % items.size();
                    var itemstack = new ItemStack(items.get(idx), this.tagCount);
                    renderItemStack(gui, itemstack);
                }
            } catch (Exception ignored)
            {
            }
        }
        if (this.getMode() == SlotMode.FLUID)
        {
            var fh = new FluidHelper();
            fh.drawFluid(gui, 16, 16, this.ghostFluid, this.x, this.y);
            var font = Minecraft.getInstance().font;
            gui.pose().pushPose();
            gui.pose().translate(this.x + 8, this.y + 11, 1000);
            gui.pose().scale(.5f, .5f, 1f);
            var w = font.width(ghostFluid.getAmount() + "");
            gui.pose().translate(-w / 2f, 0, 1000);
            gui.drawString(font, ghostFluid.getAmount() + "", 0, 0, 0xffffff);
            gui.pose().popPose();
        }
        gui.pose().popPose();
    }

    private void renderItemStack(GuiGraphics gui, ItemStack itemstack)
    {
        if (!itemstack.isEmpty()) gui.renderItem(itemstack, this.x, this.y);
        {
            var font = IClientItemExtensions.of(itemstack).getFont(itemstack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font == null) font = Minecraft.getInstance().font;
            gui.renderItemDecorations(font, itemstack, this.x, this.y, null);
        }
    }

    public void setItem(JsonObject json, Cipher cipher)
    {
        if (json == null)
        {
            this.setItem(ItemStack.EMPTY);
            return;
        }
        // use the "type, "path" and "index" members to locate the correct item.
        // shaped crafting requires special handling
        // shapeless crafting is an array, and should work as-is
        if (this.cipherobject.type.equals("shaped"))
        {
            var gridrecipe = RecipeHelper.decodeRecipeGrid(cipherobject.rows, cipherobject.cols,
                    JsonHelpers.getNestedObject(json, cipherobject.getPathWithoutIndex()), JsonHelpers.getNestedObject(json, cipherobject.getKey()), true);
            gridrecipe = RecipeHelper.center(gridrecipe, cipherobject.rows, cipherobject.cols);

            var row = cipherobject.index / cipherobject.cols;
            var col = cipherobject.index - (row * cipherobject.cols);
            setIngredient(gridrecipe[row][col]);
//            var jobj = JsonHelpers.getNestedObject(json, cipherobject.getPathWithIndex(row));
//            var mobj = JsonHelpers.getNestedObject(json, cipherobject.getKey());
//            if (jobj.isString() && mobj.isJsonObject()) // need something like "###"
//            {
//                var chr = jobj.string.substring(col, col + 1);
//                if (mobj.jobject.has(chr))
//                {
//                    this.setIngredient(mobj.jobject.getAsJsonObject(chr));
//                }
////                Map<String, Ingredient> map =CipherGridObject.keyFromJson(ikey.getAsJsonObject());
////                var ingredients = cipherobject.parseShaped(jobj.getAsJsonObject());
//            }
        } else
        {
            var child = JsonHelpers.getNestedObject(json, cipherobject.getPathWithIndex());
            if (child == null || child.isNull())
            {
                this.setItem(ItemStack.EMPTY);
                return;
            }
            switch (cipherobject.getType())
            {
                case "itemstack":
                {
                    if (child.isString())
                    {
                        setSlotFromString(child.string);
                    }
                    if (child.isJsonObject())
                    {
                        setslotfromjson(child.jobject);
                    }
                }
                break;
                case "ingredient":
                {
                    if (child.isJsonObject())
                    {
                        setIngredient(child.jobject);
                    }
                }
                break;
            }
        }
        if (getCipherobject().countfield != null && !getCipherobject().countfield.isEmpty())
        {
            var t = JsonHelpers.getNestedObject(json, getCipherobject().countfield);
            if (t.isNumber() && this.ghostStack != null) this.ghostStack.setCount((int) t.number);
        }
    }


    // it is very hard to tell if the ingredient passed in has NBT or TAG or what...
//    private void setIngredientWithNBT(Ingredient ingredient)
//    {
//    }

//    private void setIngredient(Ingredient ing)
//    {
//        if (ing.isEmpty()) return;
//        setIngredient(ing.toJson().getAsJsonObject());
//    }


    // Ingredient class does not handle NBT.
    private void setIngredient(CWIngredient cwIngredient)
    {
        if (cwIngredient instanceof CWIngredient.CWIngredientTag tag)
        {
            this.setTagKey(tag.getTagKey());
        }
        if (cwIngredient instanceof CWIngredient.CWIngredientItem item)
        {
            this.setItem(item.getItemStack());
        }
    }

    // Ingredient class does not handle NBT.
    private void setIngredient(JsonObject json)
    {
        if (json.has("fluid"))
        {
            var fs = ItemAndIngredientHelpers.jsonToFluidStack(json);
            this.setFluid(fs);
        } else if (json.has("tag") || json.has("item"))
        {
            var ig2 = Ingredient.valueFromJson(json);
            var ingredient = ItemAndIngredientHelpers.ingredientWithNBTFromJson(json);
            if (ig2 instanceof Ingredient.ItemValue iv2)
            {
                var arr = ingredient.getItems();
                if (arr.length > 0)
                {
                    var is = arr[0];
                    this.setItem(is);
                } else this.setItem(ItemStack.EMPTY);
            }
            if (ig2 instanceof Ingredient.TagValue tv)
            {
                var jo2 = tv.serialize(); // {tag:tagname}
                var tagstring = JsonHelpers.getNestedObject(jo2, "tag");
                if (tagstring.isString())
                {
                    this.setTagKey(TagKey.create(Registries.ITEM, new ResourceLocation(tagstring.string)));
                }
            }
        }
    }

    private void setslotfromjson(JsonObject jobject)
    {
        if (jobject.has("tag"))
        {
            return;
        } else if (jobject.has("fluid"))
        {
            var fs = ItemAndIngredientHelpers.jsonToFluidStack(jobject);
            if (!fs.isEmpty()) this.setFluid(fs);
        } else
        {
            var is = ShapedRecipe.itemStackFromJson(jobject);
            this.setItem(is);
        }
    }

    private void setSlotFromString(String id)
    {
        setSlotFromResourceLocation(new ResourceLocation(id));
    }

    private void setSlotFromResourceLocation(ResourceLocation rl)
    {
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) this.setItem(ItemStack.EMPTY);
        else this.setItem(new ItemStack(item, 1));
    }

    public TagKey<Item> getTag()
    {
        return tagKey;
    }

    public int getTagCount()
    {
        return tagCount;
    }

    public void saveExtra(CompoundTag tag)
    {
        tag.putString("mode", getMode().name());
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        //tag.putInt("index", this.index);
        switch (getMode())
        {
            case ITEM:
                var itemtag = new CompoundTag();
                this.ghostStack.save(itemtag);
                //this.getItem().save(itemtag);
                tag.put("item", itemtag);
                break;
            case FLUID:
                var fluidtag = new CompoundTag();
                this.ghostFluid.writeToNBT(fluidtag);
                tag.put("fluid", fluidtag);
                break;
            case TAG:
//                var tagtag = new CompoundTag();
//                tagtag.putString("tag", this.getTag().location().toString());
//                tag.put("tag", tagtag);
                tag.putString("tag", this.getTag().location().toString());
                tag.putInt("tagcount", this.tagCount);
                break;
        }
    }

    public void loadExtra(CompoundTag tag)
    {
//        var item = Items.AIR;
//        var count = 1;
        this.setMode(SlotMode.valueOf(tag.getString("mode")));
        this.x = tag.getInt("x");
        this.y = tag.getInt("y");
//        var index = tag.getInt("index");
        switch (getMode())
        {
            case ITEM:
                this.ghostStack = ItemStack.of(tag.getCompound("item"));
                if (this.ghostStack.getTag() != null)
                {
                    if (tag.contains("item") && tag.getCompound("item").contains("tag"))
                    {
                        for (var key : this.ghostStack.getTag().getAllKeys())
                        {
                            if (!tag.getCompound("item").getCompound("tag").contains(key)) // restore keys to the passed in keys
                                this.ghostStack.removeTagKey(key);
                        }
                    }
                    if (tag.contains("item") && !tag.getCompound("item").contains("tag"))
                    {
                        // remove all keys
                        for (var key : this.ghostStack.getTag().getAllKeys())
                        {
                            this.ghostStack.removeTagKey(key);
                        }
                    }
                }
                this.ghostFluid = FluidStack.EMPTY;
                this.tagKey = null;
                this.tagCount = 0;
                break;
            case FLUID:
                this.ghostStack = ItemStack.EMPTY;
                ghostFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluid"));
                this.tagKey = null;
                this.tagCount = 0;
                break;
            case TAG:
                this.ghostStack = ItemStack.EMPTY;
                this.ghostFluid = FluidStack.EMPTY;
                this.tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(tag.getString("tag")));
                this.tagCount = tag.getInt("tagcount");
                break;
        }
    }

//    // Compare if client state is out-of-date
//    public boolean needsSync(CompoundTag lastSynced)
//    {
//        var savetag = new CompoundTag();
//        saveExtra(savetag);
//        return !savetag.equals(lastSynced);
//    }
}

