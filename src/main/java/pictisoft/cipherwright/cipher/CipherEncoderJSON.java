package pictisoft.cipherwright.cipher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;
import pictisoft.cipherwright.util.RecipeEncoder;
import pictisoft.cipherwright.util.RecipeHelper;
import pictisoft.cipherwright.util.StringConversions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                {
                    json.add(appendMembers(slot, ItemAndIngredientHelpers.itemStackToJson(itemstack)));
                }
            } else if (slot.getMode() == CipherSlot.SlotMode.FLUID)
            {
                var fluidstack = slot.getFluidStack();
                if (!fluidstack.isEmpty())
                {
                    json.add(appendMembers(slot, ItemAndIngredientHelpers.fluidStackToJson(fluidstack)));
                }
            }
        }
        return json.toString();
    }

    // adds members to the json object, such as "chance"
    private JsonObject appendMembers(CipherSlot slot, JsonObject jchild)
    {
        if (slot.getCipherobject() instanceof CipherWell well)
        {
            for (var para : well.getWellParameters())
            {
                var val = slot.getWellParameter(para);
                if (val == null || val.isBlank()) continue;
                if (Objects.equals(para.getType(), "text") || Objects.equals(para.getType(), "combo"))
                    jchild.addProperty(slot.getWellMember(para), val);
                if (Objects.equals(para.getType(), "number") || Objects.equals(para.getType(), "combo-number"))
                    jchild.addProperty(slot.getWellMember(para), StringConversions.getAsFloat(val));
                if (Objects.equals(para.getType(), "boolean"))
                    jchild.addProperty(slot.getWellMember(para), val.equals("true"));
            }
        }
        return jchild;
    }

    @Override
    protected String encodeItemStack(ItemStack itemStack)
    {
        return ItemAndIngredientHelpers.itemStackToJson(itemStack).toString();
    }

    @Override
    protected String encodeFluidStack(FluidStack fluidStack)
    {
        return ItemAndIngredientHelpers.fluidStackToJson(fluidStack).toString();
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
        encodeToJSON(slot, json);
        return json.get(0).toString();
    }

    @Override
    protected String encodeIngredients(List<CipherSlot> slots)
    {
        var json = new JsonArray();
        for (var slot : slots)
        {
            encodeToJSON(slot, json);
        }
        return json.toString();
    }

    private void encodeToJSON(CipherSlot sslot, JsonArray json)
    {
        JsonObject jchild = null;
        if (sslot.getMode() == CipherSlot.SlotMode.ITEM)
        {
            if (sslot.getItemStack().isEmpty()) return;
            jchild = ItemAndIngredientHelpers.ingredientWithNBTToJson(Ingredient.of(sslot.getItemStack()));
//            if (sslot.getCipherobject() instanceof CipherWell well)
//            {
//                if (well.hasCountField())
//                {
//                }
//            }
        }
        if (sslot.getMode() == CipherSlot.SlotMode.TAG)
        {
            jchild = RecipeEncoder.ingredientFromTag(sslot.getTag(), sslot.getTagCount());
        }
        if (sslot.getMode() == CipherSlot.SlotMode.FLUIDTAG)
        {
            jchild = RecipeEncoder.ingredientFromFluidTag(sslot.getFluidTag(), sslot.getFluidTagAmount());
        }
        if (sslot.getMode() == CipherSlot.SlotMode.FLUID)
        {
            jchild = ItemAndIngredientHelpers.fluidStackToJson(sslot.getFluidStack());
        }
        if (jchild != null)
        {
            jchild = appendMembers(sslot, jchild);
            json.add(jchild);
        }
    }

    @Override
    protected String encodePattern(String[] encoded)
    {
        return RecipeHelper.encodePattern(encoded);
    }

    @Override
    protected String encodePatternKey(Map<String, CWIngredient> map)
    {
        return RecipeHelper.encodePatternKey(map);
    }

}
