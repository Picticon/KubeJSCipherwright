package pictisoft.cipherwright.cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;
import pictisoft.cipherwright.util.RecipeEncoder;

import java.util.List;
import java.util.Map;

public class CipherEncoderJSON extends CipherEncoderBase
{
    public CipherEncoderJSON(CipherTemplate.DataLoad data)
    {
        super(data);
    }

    @Override
    protected String encodeItemStacks(List<CipherSlot> slots)
    {
        var json = new JsonArray();
        for (var slot : slots)
        {
            if (slot.getMode() == CipherSlot.SlotMode.ITEM
                    || slot.getMode() == CipherSlot.SlotMode.TAG) // tag shouldn't happen, but...
            {
                var itemstack = slot.getItemStack();
                if (!itemstack.isEmpty())
                    json.add(ItemAndIngredientHelpers.itemStackToJson(itemstack));
            } else if (slot.getMode() == CipherSlot.SlotMode.FLUID)
            {
                var fluidstack = slot.getFluid();
                if (!fluidstack.isEmpty())
                    json.add(ItemAndIngredientHelpers.fluidStackToJson(fluidstack));
            }
        }
        return json.toString();
    }

    @Override
    protected String encodeItemStack(ItemStack itemStack)
    {
        return ItemAndIngredientHelpers.itemStackToJson(itemStack).toString();
    }


    //    @Override
//    protected String encodeItemStacks(List<ItemStack> itemStacks)
//    {
//        return "";
//    }
//
//    @Override
//    protected String encodeIngredient(Ingredient ingredient)
//    {
//        return ingredient.toJson().toString();
//    }
    @Override
    protected String encodeIngredient(CipherSlot slot)
    {
        var json = new JsonArray();
        encodetojson(slot, json);
        return json.get(0).toString();
    }

    @Override
    protected String encodeIngredients(List<CipherSlot> slots)
    {
        var json = new JsonArray();
        for (var slot : slots)
        {
            encodetojson(slot, json);
        }
        return json.toString();
    }

    private static void encodetojson(CipherSlot slot, JsonArray json)
    {
        if (slot.getMode() == CipherSlot.SlotMode.ITEM)
        {
            if (slot.getItemStack().isEmpty()) return;
            json.add(ItemAndIngredientHelpers.ingredientWithNBTToJson(Ingredient.of(slot.getItemStack())));
        }
        if (slot.getMode() == CipherSlot.SlotMode.TAG)
        {
            json.add(RecipeEncoder.ingredientFromTag(slot.getTag())); // no counts...
        }
        if (slot.getMode() == CipherSlot.SlotMode.FLUID)
        {
            json.add(ItemAndIngredientHelpers.fluidStackToJson(slot.getFluid())); // no counts...
        }
    }

    @Override
    protected String encodePattern(String[] encoded)
    {
        var json = new JsonArray();
        for (var r : encoded)
        {
            json.add(r);
        }
        return json.toString();
    }

    @Override
    protected String encodePatternKey(Map<CWIngredient, String> map)
    {
        var json = new JsonObject();
        for (var r : map.entrySet())
        {
            json.add(r.getValue(), r.getKey().toJson());
        }
        return json.toString();
    }

}
