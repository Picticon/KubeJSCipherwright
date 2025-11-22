package pictisoft.cipherwright.util;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

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

    public static JsonArray ingredientsWithNBTToJson(List<Ingredient> ingredients)
    {
        var ret = new JsonArray();
        for(var i:ingredients)
        {
            ret.add(ingredientWithNBTToJson(i));
        }
        return ret;
    }

    public static JsonObject ingredientWithNBTToJson(Ingredient ingredient)
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

    public static JsonObject itemStackToJson(ItemStack itemStack)
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

    public static void quoteAndEscapeForJS(StringBuilder stringBuilder, String string)
    {
        int start = stringBuilder.length();
        stringBuilder.append(' ');
        char c = 0;

        for (int i = 0; i < string.length(); ++i) {
            char d = string.charAt(i);
            if (d == '\\') {
                stringBuilder.append('\\');
            } else if (d == '"' || d == '\'') {
                if (c == 0) {
                    c = d == '\'' ? '"' : '\'';
                }

                if (c == d) {
                    stringBuilder.append('\\');
                }
            }

            stringBuilder.append(d);
        }

        if (c == 0) {
            c = '\'';
        }

        stringBuilder.setCharAt(start, c);
        stringBuilder.append(c);
    }

    public static String itemStackToKubeJS(ItemStack itemStack, boolean weaknbt)
    {
        var count = itemStack.getCount();
        var hasNbt = itemStack.hasTag();
        var loc = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();

        var builder = new StringBuilder();
        if (count > 1 && !hasNbt)
        {
            builder.append('\'');
            builder.append(count);
            builder.append("x ");
            builder.append(loc);
            builder.append('\'');
        } else if (hasNbt)
        {
            builder.append("Item.of('");
            builder.append(loc);
            builder.append('\'');
            List<Pair<String, Integer>> enchants = null;

            if (count > 1)
            {
                builder.append(", ");
                builder.append(count);
            }

            var t = itemStack.getTag();

            if (t != null && !t.isEmpty())
            {
                var key = itemStack.getItem() == Items.ENCHANTED_BOOK ? "StoredEnchantments" : "Enchantments";

                if (t.contains(key, 9))
                {
                    var l = t.getList(key, 10);
                    enchants = new ArrayList<>(l.size());

                    for (var i = 0; i < l.size(); i++)
                    {
                        var t1 = l.getCompound(i);
                        enchants.add(Pair.of(t1.getString("id"), t1.getInt("lvl")));
                    }

                    t = t.copy();
                    t.remove(key);

                    if (t.isEmpty())
                    {
                        t = null;
                    }
                }
            }
            if (t != null)
            {
                builder.append(", ");
                ItemAndIngredientHelpers.quoteAndEscapeForJS(builder, t.toString());
            }

            builder.append(')');

            if (enchants != null)
            {
                for (var e : enchants)
                {
                    builder.append(".enchant('");
                    builder.append(e.getKey());
                    builder.append("', ");
                    builder.append(e.getValue());
                    builder.append(')');
                }
            }
            if (weaknbt) builder.append(".weakNBT()");
        } else
        {
            builder.append('\'');
            builder.append(loc);
            builder.append('\'');
        }

        return builder.toString();
    }
}
