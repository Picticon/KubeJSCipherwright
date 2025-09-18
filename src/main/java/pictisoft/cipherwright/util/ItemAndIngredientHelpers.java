package pictisoft.cipherwright.util;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import pictisoft.cipherwright.cipher.CWIngredient;

public class ItemAndIngredientHelpers
{
    public static Ingredient ingredientWithNBTFromJson(JsonObject json)
    {
        var ig = CraftingHelper.getIngredient(json, true);
        if (json.has("tag"))
        {
            return ig;
        }
        if (json.has("nbt"))
        {
            var tag = CraftingHelper.getNBT(json.get("nbt"));
            ig.getItems()[0].setTag(tag);
        }
        return ig;
    }

    public static JsonElement ingredientWithNBTToJson(Ingredient ingredient)
    {
        JsonObject result = new JsonObject();
        // Get the first matching stack (assuming single item ingredient)
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length > 0)
        {
            ItemStack stack = stacks[0];
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());

            if (stack.hasTag())
            {
                result.add("nbt", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, stack.getTag()));
            }

            if (stack.getCount() > 1)
            {
                result.addProperty("count", stack.getCount());
            }
        }
        return result;
    }

    public static JsonElement itemStackToJson(ItemStack itemStack)
    {
        JsonObject result = new JsonObject();
        var item = itemStack.getItem();
        var loc = BuiltInRegistries.ITEM.getKey(item).toString();
        result.addProperty("item", loc);
        if (itemStack.getCount() > 1)
            result.addProperty("count", itemStack.getCount());
        if (itemStack.hasTag())
        {
            result.add("nbt", compoundTagToJson(itemStack.getTag()));
        }

        return result;
    }

    public static JsonObject compoundTagToJson(CompoundTag tag)
    {
        JsonObject json = new JsonObject();

        for (String key : tag.getAllKeys())
        {
            Tag nbtBase = tag.get(key);
            json.add(key, nbtBaseToJson(nbtBase));
        }

        return json;
    }


//    public Ingredient ingredientFromJsonWithNBT(JsonObject jsonElement)
//    {
//        CraftingHelper.getIngredient()
//        JsonObject obj = GsonHelper.convertToJsonObject(jsonElement, "ingredient");
//        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(obj, "item"));
//        Item item = BuiltInRegistries.ITEM.get(id);
//        ItemStack stack = new ItemStack(item);
//        if (obj.has("nbt"))
//        {
//            CompoundTag tag = (CompoundTag)
//                    NbtOps.INSTANCE.convertTo(NbtOps.INSTANCE, JsonOps.INSTANCE, obj.get("nbt"))
//                            .getOrThrow(false, msg -> {
//            });
//            stack.setTag(tag);
//        }
//        return Ingredient.of(stack);
//    }

    private static JsonElement nbtBaseToJson(Tag tag)
    {
        if (tag instanceof CompoundTag)
        {
            return compoundTagToJson((CompoundTag) tag);
        } else if (tag instanceof ListTag)
        {
            JsonArray jsonArray = new JsonArray();
            ListTag listTag = (ListTag) tag;

            for (int i = 0; i < listTag.size(); i++)
            {
                jsonArray.add(nbtBaseToJson(listTag.get(i)));
            }

            return jsonArray;
        } else if (tag instanceof StringTag)
        {
            return new JsonPrimitive(((StringTag) tag).getAsString());
        } else if (tag instanceof ByteTag)
        {
            return new JsonPrimitive(((ByteTag) tag).getAsByte());
        } else if (tag instanceof ShortTag)
        {
            return new JsonPrimitive(((ShortTag) tag).getAsShort());
        } else if (tag instanceof IntTag)
        {
            return new JsonPrimitive(((IntTag) tag).getAsInt());
        } else if (tag instanceof LongTag)
        {
            return new JsonPrimitive(((LongTag) tag).getAsLong());
        } else if (tag instanceof FloatTag)
        {
            return new JsonPrimitive(((FloatTag) tag).getAsFloat());
        } else if (tag instanceof DoubleTag)
        {
            return new JsonPrimitive(((DoubleTag) tag).getAsDouble());
        } else if (tag instanceof ByteArrayTag)
        {
            JsonArray jsonArray = new JsonArray();
            byte[] byteArray = ((ByteArrayTag) tag).getAsByteArray();

            for (byte b : byteArray)
            {
                jsonArray.add(b);
            }

            return jsonArray;
        } else if (tag instanceof IntArrayTag)
        {
            JsonArray jsonArray = new JsonArray();
            int[] intArray = ((IntArrayTag) tag).getAsIntArray();

            for (int i : intArray)
            {
                jsonArray.add(i);
            }

            return jsonArray;
        } else if (tag instanceof LongArrayTag)
        {
            JsonArray jsonArray = new JsonArray();
            long[] longArray = ((LongArrayTag) tag).getAsLongArray();

            for (long l : longArray)
            {
                jsonArray.add(l);
            }

            return jsonArray;
        } else
        {
            // Handle any other tag types or return null
            return JsonNull.INSTANCE;
        }
    }

    // this is a stupid logic that assumes that the ingredients are just itemstacks...
    public static boolean equalIngredients(Ingredient i1, Ingredient i2)
    {
        if (i1.getItems().length == 0 && i2.getItems().length == 0) return true;
        if (i1.getItems().length == 0 || i2.getItems().length == 0) return false;
        if (i1.isEmpty() && i2.isEmpty()) return true;
        if (i1.isEmpty() || i2.isEmpty()) return false;
        return i1.getItems().length == i2.getItems().length
                && ItemStack.isSameItemSameTags(i1.getItems()[0], i2.getItems()[0]);
    }

    public static boolean equalItemstack(ItemStack itemStack1, ItemStack itemStack2)
    {
        return ItemStack.isSameItemSameTags(itemStack1, itemStack2);
    }


    public static boolean equalTags(TagKey<Item> tag1, TagKey<Item> tag2)
    {
        return (tag1.location().equals(tag2.location()));
    }

    public static Ingredient ingredientWithNBT(ItemStack itemstack)
    {
        var ret = Ingredient.of(itemstack);
        if (itemstack.getTag() != null && !itemstack.getTag().isEmpty())
        {
            ret.getItems()[0].setTag(itemstack.getTag());
        }
        return ret;
    }

    public static JsonObject fluidStackToJson(FluidStack fluidStack)
    {
        JsonObject result = new JsonObject();
        var fluid = fluidStack.getFluid();
        var loc = BuiltInRegistries.FLUID.getKey(fluid).toString();
        result.addProperty("fluid", loc);
        if (fluidStack.getAmount() > 1)
            result.addProperty("amount", fluidStack.getAmount());
        if (fluidStack.hasTag())
        {
            result.add("nbt", compoundTagToJson(fluidStack.getTag()));
        }

        return result;
    }

    public static ItemStack jsonToItemStack(String value2)
    {
        JsonElement jsonRoot = JsonParser.parseString(value2);
        if (jsonRoot.isJsonObject())
        {
            JsonObject json = jsonRoot.getAsJsonObject();

            return CraftingHelper.getItemStack(json, true);
        }
        return ItemStack.EMPTY;
    }

    public static FluidStack jsonToFluidStack(String value2)
    {
        JsonElement jsonRoot = JsonParser.parseString(value2);
        if (jsonRoot.isJsonObject())
        {
            JsonObject json = jsonRoot.getAsJsonObject();
            return jsonToFluidStack(json);
        }
        return FluidStack.EMPTY;
    }

    public static FluidStack jsonToFluidStack(JsonObject json)
    {
        var fluid = json.get("fluid").getAsString();
        var amount = json.get("amount").getAsInt();
//            if (json.has("nbt"))
//            {
//
//            }
        var flu = BuiltInRegistries.FLUID.get(new ResourceLocation(fluid));
        if (flu != Fluids.EMPTY)
        {
            return new FluidStack(flu, amount);
        }
        return FluidStack.EMPTY;
    }
}
