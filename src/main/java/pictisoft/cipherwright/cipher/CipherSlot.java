package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import pictisoft.cipherwright.util.*;

import java.io.IOException;
import java.util.List;

// This class holds an item,fluid,ingredient,tag
// this is the main user interaction object
public class CipherSlot
{
    //private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    //private final int invSlot;
    private final CipherPathObject cipherobject;
    private final CipherWell.STYLE_ENUM style;
    private boolean _dirty;
    private ItemStack ghostStack = ItemStack.EMPTY;
    private FluidStack ghostFluid = FluidStack.EMPTY;
    private SlotMode _mode = SlotMode.ITEM;
    private boolean single;
    private TagKey<Item> tagKey;
    private int tagCount;
    private TagKey<Fluid> fluidTagKey;
    private int fluidTagAmount;
    private int x;
    private int y;
    private int _ticker;
    private int _lasttick;
    private CompoundTag parameters = new CompoundTag();

//    public void sync(SlotUpdatePacket message)
//    {
//        loadExtra(message.getTag());
//    }

    public String getPath()
    {
        return cipherobject.getPathWithoutIndex();
    }

    public String getPathWithIndex()
    {
        return cipherobject.getPathWithIndex();
    }

    public CipherPathObject getCipherobject()
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
            case FLUIDTAG:
            {
                // TODO
            }
            break;
        }
        return null;
    }

    public boolean canBeItem()
    {
        if (cipherobject instanceof CipherWell well) return well.allowItem();
        return false;
    }

    public boolean canBeTag()
    {
        if (cipherobject instanceof CipherWell well) if (!well.allowTag()) return false;
        return _mode == SlotMode.ITEM && !this.getItemStack().isEmpty();
    }

    public boolean canBeFluidTag()
    {
        if (cipherobject instanceof CipherWell well) if (!well.allowFluidTag()) return false;
        return _mode == SlotMode.FLUID && !this.getFluidStack().isEmpty();
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
        //if (getMode() == SlotMode.FLUID) return Ingredient.of(getFluidStack());
        //if (getMode() == SlotMode.FLUIDTAG) return Ingredient.of(getFluidTag());
        return Ingredient.EMPTY;
    }

    public CWIngredient getCWIngredient()
    {
        if (getMode() == SlotMode.ITEM) return CWIngredient.of(getItemStack().copy());
        if (getMode() == SlotMode.TAG) return CWIngredient.of(getTag());
        //if (getMode() == SlotMode.FLUID) return CWIngredient.of(getFluidStack());
        //if (getMode() == SlotMode.FLUIDTAG) return CWIngredient.of(getFluidTag());
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
            var max = 1000;
            if (cipherobject instanceof CipherWell well) max = well.getMaxFluidAmount();
            this.getFluidStack().setAmount(Math.max(1, Math.min(max, this.getFluidStack().getAmount() + adjustment)));
            this.setDirty();
            return this.getFluidStack().getAmount();
        }
        if (this.getMode() == SlotMode.FLUIDTAG)
        {
            var max = 1000;
            if (cipherobject instanceof CipherWell well) max = well.getMaxFluidAmount();
            fluidTagAmount = Math.max(1, Math.min(max, fluidTagAmount + adjustment));
            this.setDirty();
            return fluidTagAmount;
        }
        return 0;
    }

    public String getSerialKey()
    {
        return cipherobject.getPathWithIndex() + "[" + this.getX() + "," + this.getY() + "]";
    }

    public void setWellParameter(String string)
    {
        var breaks = string.split(":", 2);
        if (breaks.length == 2)
        {
            parameters.putString(breaks[0], breaks[1]);
        }
    }

    private void setWellParameter(String path, String asString)
    {
        parameters.putString(path, asString);
    }

    public String getWellParameter(CipherParameter p)
    {
        if (parameters.contains(p.path)) return parameters.getString(p.path);
        return "";
    }

    public String getWellMember(CipherParameter p)
    {
        return p.path;
    }

    public void clear()
    {
        setItem(ItemStack.EMPTY);
        parameters = new CompoundTag();
    }

    public boolean isEmpty()
    {
        if (getMode() == SlotMode.FLUID) return this.getFluidStack().isEmpty();
        if (getMode() == SlotMode.TAG) return this.getTag() == null;
        if (getMode() == SlotMode.FLUIDTAG) return getFluidTag() == null;
        return getItemStack().isEmpty();
    }

    public CipherWell.STYLE_ENUM getStyle()
    {
        return style;
    }

    public boolean pathMatch(String path)
    {
        return getPath().equals(path) || getPathWithIndex().equals(path);
    }

    public enum SlotMode
    {
        ITEM, TAG, FLUID, FLUIDTAG
    }

    public CipherSlot(CipherWell cipherslot)
    {
        //super(inventory, slotIndex, x, y); // Dummy inventory
        //this.invSlot = slotIndex;
        this.single = cipherslot.getSingle();
        this.style = cipherslot.getStyle();
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
        if (this.cipherobject instanceof CipherWell well && !well.allowItem() && !stack.isEmpty()) return;
        if (this.single) stack.setCount(1);
        // Store the ghost item instead of putting it in an inventory
//        if (stack.isEmpty())
//            Chatter.chat("stackempty");
        this.ghostStack = stack.copy();
        this.ghostFluid = FluidStack.EMPTY;
        this.tagKey = null;
        this.tagCount = 0;
        this.fluidTagKey = null;
        this.fluidTagAmount = 0;
        this.setMode(SlotMode.ITEM);
        //Chatter.chat("setitem: " + stack.toString());
        this.setDirty();
    }

    public void setFluid(FluidStack fluid)
    {
        if (this.cipherobject instanceof CipherWell well && !well.allowFluid() && !fluid.isEmpty()) return;
        this.ghostFluid = fluid.copy();
        this.ghostStack = ItemStack.EMPTY;
        this.tagKey = null;
        this.tagCount = 0;
        this.fluidTagKey = null;
        this.fluidTagAmount = 0;
        this.setMode(SlotMode.FLUID);
        //Chatter.chat("setfluid: " + fluid.toString());
        this.setDirty();
    }

    public void setFluidTagKey(TagKey<Fluid> fluidTag)
    {
        if (this.cipherobject instanceof CipherWell well && !well.allowFluidTag() && fluidTag != null) return;
        this.ghostStack = ItemStack.EMPTY;
        this.ghostFluid = FluidStack.EMPTY;
        this.tagKey = null;
        this.tagCount = 0;
        this.fluidTagKey = fluidTag;
        this.fluidTagAmount = 1000;
        this.setMode(SlotMode.FLUIDTAG);
        //Chatter.chat("setfluid: " + fluid.toString());
        this.setDirty();
    }

    public void setTagKey(TagKey<Item> tagKey)
    {
        if (this.cipherobject instanceof CipherWell well && !well.allowTag() && tagKey != null) return;
        this.ghostStack = ItemStack.EMPTY; // ^ first
        this.ghostFluid = FluidStack.EMPTY;
        this.fluidTagKey = null;
        this.fluidTagAmount = 0;
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

    public FluidStack getFluidStack()
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
            var max = 1000;
            if (this.cipherobject instanceof CipherWell well) max = well.getMaxFluidAmount();
            renderFluidStack(gui, fh, max, ghostFluid);
        }
        if (this.getMode() == SlotMode.FLUIDTAG)
        {
            try
            {
                var fh = new FluidHelper();
                var max = 1000;
                if (this.cipherobject instanceof CipherWell well) max = well.getMaxFluidAmount();
                var fluidLookup = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.FLUID);
                var fluids = fluidLookup.getOrThrow(fluidTagKey) // returns a HolderSet<Fluid>
                        .stream().map(Holder::value).toList();
                var idx = (_ticker / 10) % fluids.size();
                var fluidStack = new FluidStack(fluids.get(idx), this.fluidTagAmount);
                renderFluidStack(gui, fh, max, fluidStack);
            } catch (Exception ignored)
            {
            }
        }
        if (this.getCipherobject() instanceof CipherWell well)
        {
            for (var wp : well.getWellParameters())
            {
                if (wp.getDisplay() != null && !wp.getDisplay().isBlank())
                {
                    String text = getWellParameter(wp);
                    if (wp.getDisplay().contains("percent"))
                    {
                        try
                        {
                            var fl = Float.parseFloat(text);
                            text = String.format("%.0f%%", fl * 100);
                        } catch (Exception ignored)
                        {
                        }
                    }
                    var font = Minecraft.getInstance().font;
                    var w = font.width(text);
                    var h = font.lineHeight;
                    var x = well.width / 2 - w / 2;
                    var y = well.height / 2 - h / 2;
                    gui.pose().pushPose();
                    gui.pose().translate(this.x, this.y, 1000);
                    gui.pose().scale(.5f, .5f, 1f);
                    if (wp.getDisplay().contains("top-"))
                    {
                        y = 1;
                    } else if (wp.getDisplay().contains("bottom-"))
                    {
                        y = well.height - 1;
                    }
                    if (wp.getDisplay().contains("center-"))
                    {
                        y = well.height / 2;
                    }
                    if (wp.getDisplay().contains("-left"))
                    {
                        x = 1;
                    } else if (wp.getDisplay().contains("-right"))
                    {
                        x = well.width - w;
                    } else if (wp.getDisplay().contains("-center"))
                    {
                        x = well.width / 2 - w / 2;
                    }
                    if (text != null)
                    {
                        gui.pose().translate(x, y, 1000);
                        gui.drawString(font, text, 0, 0, 0xffffff);
                    }
                    gui.pose().popPose();
                }
            }
        }
        gui.pose().popPose();
    }

    private void renderFluidStack(GuiGraphics gui, FluidHelper fh, int max, FluidStack pi_ghostFluid)
    {
        fh.drawFluid(gui, 16, 16, pi_ghostFluid, this.x, this.y, max);
        var font = Minecraft.getInstance().font;
        gui.pose().pushPose();
        gui.pose().translate(this.x + 8, this.y + 11, 100);
        gui.pose().scale(.5f, .5f, 1f);
        var w = font.width(pi_ghostFluid.getAmount() + "");
        gui.pose().translate(-w / 2f, 0, 0);
        gui.drawString(font, pi_ghostFluid.getAmount() + "", 0, 0, 0xffffff);
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

    // This handles moving the recipe JSON to the SLOT.
    public void setItem(JsonObject json)
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
            var ppath = cipherobject.getItemPath(); // in case the item is nested
            var cpath = cipherobject.getPathWithIndex(); // for extra members
            var child = JsonHelpers.getNestedObject(json, ppath);
            var childmembers = JsonHelpers.getNestedObject(json, cpath);
            if (child.isNull())
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
                        trySetWellParameters(childmembers.jobject);
                    }
                }
                break;
                case "ingredient":
                {
                    if (child.isJsonObject())
                    {
                        setIngredient(child.jobject);
                        trySetWellParameters(childmembers.jobject);
                    }
                }
                break;
            }
        }
        // in case the count is somewhere else
        if (getCipherobject().hasCountField())
        {
            var t = JsonHelpers.getNestedObject(json, getCipherobject().getCountPath());
            if (t.isNumber())
            {
                if (this._mode == SlotMode.ITEM && this.ghostStack != null) this.ghostStack.setCount((int) t.number);
                if (this._mode == SlotMode.TAG && this.tagKey != null) this.tagCount = (int) t.number;
                if (this._mode == SlotMode.FLUID && this.ghostFluid != null) this.ghostFluid.setAmount((int) t.number);
                if (this._mode == SlotMode.FLUIDTAG && this.fluidTagKey != null) this.fluidTagAmount = (int) t.number;
            }
        }
    }

    // look for additional members such as "chance"
    private void trySetWellParameters(JsonObject jobject)
    {
        if (this.getCipherobject() instanceof CipherWell well)
        {
            for (var p : well.getWellParameters())
            {
                if (jobject.has(p.getPath()))
                {
                    setWellParameter(p.getPath(), jobject.get(p.getPath()).getAsString());
                }
            }
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
        } else if (json.has("fluidTag"))
        {
            var ftag = json.get("fluidTag").getAsString();
            var famount = 1000;
            if (json.has("amount")) famount = json.get("amount").getAsInt();
            setFluidTagKey(TagKey.create(Registries.FLUID, new ResourceLocation(ftag)));
            fluidTagAmount = famount;
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
                    setTagKey(TagKey.create(Registries.ITEM, new ResourceLocation(tagstring.string)));
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

    public TagKey<Fluid> getFluidTag()
    {
        return fluidTagKey;
    }

    public int getTagCount()
    {
        return tagCount;
    }

    public int getFluidTagAmount()
    {
        return fluidTagAmount;
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
                ghostStack.save(itemtag);
                //this.getItem().save(itemtag);
                tag.put("item", itemtag);
                break;
            case FLUID:
                var fluidtag = new CompoundTag();
                ghostFluid.writeToNBT(fluidtag);
                tag.put("fluid", fluidtag);
                break;
            case TAG:
                tag.putString("tag", getTag().location().toString());
                tag.putInt("tagcount", tagCount);
                break;
            case FLUIDTAG:
                tag.putString("fluidtag", getFluidTag().location().toString());
                tag.putInt("fluidtagcount", fluidTagAmount);
                break;
        }
        if (getCipherobject() instanceof CipherWell well)
        {
            tag.put("parameters", parameters);
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
                this.fluidTagKey = null;
                this.fluidTagAmount = 0;
                break;
            case FLUID:
                this.ghostStack = ItemStack.EMPTY;
                ghostFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluid"));
                this.tagKey = null;
                this.tagCount = 0;
                this.fluidTagKey = null;
                this.fluidTagAmount = 0;
                break;
            case TAG:
                this.ghostStack = ItemStack.EMPTY;
                this.ghostFluid = FluidStack.EMPTY;
                this.tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(tag.getString("tag")));
                this.tagCount = tag.getInt("tagcount");
                this.fluidTagKey = null;
                this.fluidTagAmount = 0;
                break;
            case FLUIDTAG:
                this.fluidTagKey = TagKey.create(Registries.FLUID, new ResourceLocation(tag.getString("fluidtag")));
                this.fluidTagAmount = tag.getInt("fluidtagcount");
                this.ghostStack = ItemStack.EMPTY;
                this.ghostFluid = FluidStack.EMPTY;
                this.tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(tag.getString("tag")));
                this.tagCount = tag.getInt("tagcount");
                break;
        }
        if (tag.contains("parameters")) parameters = tag.getCompound("parameters");
    }

//    // Compare if client state is out-of-date
//    public boolean needsSync(CompoundTag lastSynced)
//    {
//        var savetag = new CompoundTag();
//        saveExtra(savetag);
//        return !savetag.equals(lastSynced);
//    }
}

