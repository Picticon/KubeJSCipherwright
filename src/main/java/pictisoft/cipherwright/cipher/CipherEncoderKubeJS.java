package pictisoft.cipherwright.cipher;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CipherEncoderKubeJS extends CipherEncoderBase
{
    public CipherEncoderKubeJS(CipherTemplate.DataLoad data)
    {
        super(data);
    }

    @Override
    protected String encodeItemStacks(List<CipherSlot> slots)
    {
        var arr = new ArrayList<String>();
        for (CipherSlot slot : slots)
        {
            var itemstack = slot.getItemStack();
            if (!itemstack.isEmpty())
            {
                arr.add(jsify(encode(itemstack)));
            }
        }
        return "[" + String.join(",", arr) + "]";

    }

    @Override
    protected String encodeItemStack(ItemStack itemStack)
    {
        return encode(itemStack);
    }

    @Override
    protected String encodeFluidStack(FluidStack fluidStack)
    {
        return encode(fluidStack);
    }

    private String encode(ItemStack itemStack)
    {
        if (itemStack.hasTag())
        {

            var ret = "Item.of(";
            ret += "\"" + BuiltInRegistries.ITEM.getKey(itemStack.getItem()) + "\"";
            ret += ", " + itemStack.getCount();
            ret += ", " + ItemAndIngredientHelpers.compoundTagToJson(itemStack.getTag());
            ret += ")";
            return ret;
        } else
        {
            var ret = "\"";
            if (itemStack.getCount() > 1)
                ret += itemStack.getCount() + "x ";
            ret += BuiltInRegistries.ITEM.getKey(itemStack.getItem());
            ret += "\"";
            return ret;
        }
    }

    private String encode(FluidStack fluidStack)
    {
        if (fluidStack.hasTag() || fluidStack.getAmount() != 1000)
        {

            var ret = "Fluid.of(";
            ret += "\"" + BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()) + "\"";
            ret += ", " + fluidStack.getAmount();
            ret += ", " + ItemAndIngredientHelpers.compoundTagToJson(fluidStack.getTag());
            ret += ")";
            return ret;
        } else
        {
            var ret = "\"";
            ret += BuiltInRegistries.FLUID.getKey(fluidStack.getFluid());
            ret += "\"";
            return ret;
        }
    }

//    @Override
//    protected String encodeItemStacks(List<ItemStack> itemStacks)
//    {
//        var arr = new ArrayList<String>();
//        for (var is : itemStacks)
//        {
//            arr.add(encodeItemStack(is));
//        }
//        return "[" + String.join(",", arr) + "]";
//    }
//
//    @Override
//    protected String encodeIngredient(Ingredient ingredient)
//    {
//        return "";
//    }

    @Override
    protected String encodeIngredient(CipherSlot slot)
    {
        return encode(slot);
    }


    @Override
    protected String encodeIngredients(List<CipherSlot> slots)
    {
        var arr = new ArrayList<String>();
        for (CipherSlot slot : slots)
        {
            var ingredient = encode(slot);
            if (ingredient != null) arr.add(jsify(ingredient));
        }
        return "[" + String.join(",", arr) + "]";
    }

    private String jsify(String ingredient)
    {
        return ingredient.replace("\"", "\\\"");
    }

    private String encode(CipherSlot slot)
    {
        if (slot.getMode() == CipherSlot.SlotMode.TAG)
            return "\"#" + slot.getTag().location() + "\"";
        if (slot.getMode() == CipherSlot.SlotMode.ITEM)
        {
            if (slot.getItemStack().isEmpty()) return null;
            return encode(slot.getItemStack());
        }
        if (slot.getMode() == CipherSlot.SlotMode.FLUID)
        {
            return "Fluid.of('??')";

        }
        if (slot.getMode() == CipherSlot.SlotMode.FLUIDTAG)
        {
            return "Fluid.of('??')";
        }
        return "??";
    }

    // turns an array of spaces and letters into the output format.
    @Override
    protected String encodePattern(String[] encoded)
    {
        var arr = new String[encoded.length];
        for (int i = 0; i < encoded.length; i++)
        {
            var r = encoded[i];
            arr[i] = "\"" + r + "\"";
        }
        return "[" + String.join(",", arr) + "]";
    }

    @Override
    protected String encodePatternKey(Map<String, CWIngredient> map)
    {
        var arr = new ArrayList<String>();
        for (var kvp : map.entrySet())
        {
            arr.add("{\"" + kvp.getKey() + "\":" + jsify(kvp.getValue().toJson().toString()) + "}");
        }
        return "[" + String.join(",", arr) + "]";
    }

//    public String getAsIngredient(CipherSlot slot)
//    {
//        var ret = "";
//        switch (slot.getMode())
//        {
//            case ITEM:
//            {
//                var ing = slot.getItemStack();
//                var is = Ingredient.of(new ItemStack(ing.getItem(), 1));
//                ret = "\"";
//                if (ing.getItem().getCount() > 1)
//                {
//                    ret += ing.getItem().getCount() + "x ";
//                }
//                ret += ing.getItem().getItem().toString();
//                ret += "\"";
//            }
//            case TAG:
//            {
//                ret = "\"";
//                if (ing.getItem().getCount() > 1)
//                {
//                    ret += ing.getItem().getCount() + "x ";
//                }
//                ret += "#";
//                ret += ing.getTag().location().toString();
//                ret += "\"";
//            }
//        }
//        return ret;
//    }

}
