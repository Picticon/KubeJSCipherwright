package pictisoft.cipherwright.util;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemStackHelpers
{
//    public static ItemStack mergeStacks(ItemStack target, ItemStack incoming)
//    {
//        if (!ItemStack.isSameItemSameTags(target, incoming))
//        {
//            return incoming; // not mergeable
//        }
//
//        int max = target.getMaxStackSize();
//        int newCount = target.getCount() + incoming.getCount();
//
//        if (newCount <= max)
//        {
//            target.grow(incoming.getCount());
//            return ItemStack.EMPTY;
//        } else
//        {
//            int leftover = newCount - max;
//            target.setCount(max);
//            ItemStack result = incoming.copy();
//            result.setCount(leftover);
//            return result;
//        }
//    }

//    public static JsonObject toJson(ItemStack itemStack)
//    {
//        if (itemStack == null || itemStack.isEmpty())
//        {
//            return new JsonObject();
//        }
//
//        JsonObject json = new JsonObject();
//
//        // Item registry name
//        ResourceLocation itemLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
//        json.addProperty("item", itemLocation.toString());
//
//        // Count (only if not 1)
//        if (itemStack.getCount() != 1)
//        {
//            json.addProperty("count", itemStack.getCount());
//        }
//
//        // NBT data (if present)
//        if (itemStack.hasTag())
//        {
//            CompoundTag nbt = itemStack.getTag();
//            if (nbt != null)
//            {
//                json.addProperty("nbt", nbt.toString());
//            }
//        }
//
//        return json;
//    }
}
