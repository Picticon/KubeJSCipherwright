package pictisoft.cipherwright.cipher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;

public abstract class CWIngredient
{
    public static final CWIngredient EMPTY = CWIngredient.of(ItemStack.EMPTY);
    private Mode mode;
    private ItemStack itemstack;

    public static CWIngredient of(ItemStack copy)
    {
        return new CWIngredientItem(copy);
    }

    public static CWIngredient of(TagKey<Item> copy)
    {
        return new CWIngredientTag(copy);
    }

    public static CWIngredient fromJson(JsonObject json)
    {
        if (json.has("tag"))
            return new CWIngredientTag(json.get("tag").getAsString());
        if (json.has("item"))
            return new CWIngredientItem(json);
        return CWIngredient.EMPTY;
    }

    public abstract JsonElement toJson();

    public abstract boolean isEqual(CWIngredient other);

    public abstract boolean isEmpty();

    public boolean isEqual(ItemStack itemStack)
    {
        if (this.mode != Mode.ItemStack) return false;
        return ItemAndIngredientHelpers.equalItemstack(this.itemstack, itemStack);
    }

    public static class CWIngredientTag extends CWIngredient
    {
        private ResourceLocation taglocation;

        public CWIngredientTag(TagKey<Item> tag)
        {
            super();
            this.taglocation = tag.location();
        }

        public CWIngredientTag(String resourceLocation)
        {
            super();
            this.taglocation = new ResourceLocation(resourceLocation);
        }

        @Override
        public JsonElement toJson()
        {
            var ret = new JsonObject();
            ret.addProperty("tag", taglocation.toString());
            return ret;
        }

        @Override
        public boolean isEqual(CWIngredient other)
        {
            if (other instanceof CWIngredientTag othertag)
                return othertag.taglocation.equals(this.taglocation);
            return false;
        }

        @Override
        public boolean isEmpty()
        {
            return taglocation == null;
        }

        public TagKey<Item> getTagKey()
        {
            return TagKey.create(Registries.ITEM, taglocation);
        }
    }

    public static class CWIngredientItem extends CWIngredient
    {
        private final ItemStack itemStack;

        public CWIngredientItem(ItemStack itemStack)
        {
            super();
            this.itemStack = itemStack;
        }

        public CWIngredientItem(JsonObject json)
        {
            super();
            var nbting = ItemAndIngredientHelpers.ingredientWithNBTFromJson(json);
            if (nbting.getItems().length > 0)
                this.itemStack = nbting.getItems()[0];
            else
                this.itemStack = ItemStack.EMPTY;
        }


        @Override
        public JsonElement toJson()
        {
            return ItemAndIngredientHelpers.itemStackToJson(this.itemStack);
        }

        @Override
        public boolean isEqual(CWIngredient other)
        {
            if (other instanceof CWIngredientItem otheritem)
            {
                return ItemAndIngredientHelpers.equalItemstack(this.itemStack, otheritem.itemStack);
            }
            return false;
        }

        @Override
        public boolean isEmpty()
        {
            return itemStack == null || itemStack.isEmpty();
        }

        public ItemStack getItemStack()
        {
            return itemStack.copy();
        }
    }


    protected enum Mode
    {
        ItemStack, Tag
    }
}
