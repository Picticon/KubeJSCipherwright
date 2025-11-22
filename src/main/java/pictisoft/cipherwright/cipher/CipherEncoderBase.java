package pictisoft.cipherwright.cipher;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

//
// base class for the encoding classes.
// encoders take the recipe make the outputs.
// currently, just JSON and KubeJS.
//
public abstract class CipherEncoderBase
{
    protected final CipherTemplate.DataLoad data;

    public CipherEncoderBase(CipherTemplate.DataLoad data)
    {
        this.data = data;
    }

    // factory to return an encoder
    private static CipherEncoderBase getEncoder(CipherTemplate.FORMAT format, CipherTemplate.DataLoad data)
    {
        if (format== CipherTemplate.FORMAT.KUBEJS) return new CipherEncoderKubeJS(data);
        return new CipherEncoderJSON(data);
    }

    // returns a string representing a list of slot ingredients
    public static String slotsToIngredients(CipherTemplate.FORMAT format, List<CipherSlot> slots, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeIngredients(slots);
    }

    protected abstract String encodeIngredients(List<CipherSlot> slots);
    //

    // returns a string representing an itemstack when given an item stack
    public static String slotToItemStack(CipherSlot slot, CipherTemplate.FORMAT format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStack(r.getItemStack(slot));
    }

    protected abstract String encodeItemStack(ItemStack itemStack);
    //

    // returns a string when given a fluid stack
    public static String slotToFluidStack(CipherSlot slot, CipherTemplate.FORMAT format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeFluidStack(r.getFluidStack(slot));
    }

    protected abstract String encodeFluidStack(FluidStack fluidStack);
    //

    // returns a string representing a COUNT when given a single slot
    public static String slotToItemStackCount(CipherSlot slot, CipherTemplate.FORMAT format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        if (slot.getMode() == CipherSlot.SlotMode.ITEM) return r.encodeItemStackCount(r.getItemStack(slot));
        if (slot.getMode() == CipherSlot.SlotMode.FLUID) return r.encodeFluidStackAmount(r.getFluidStack(slot));
        if (slot.getMode() == CipherSlot.SlotMode.FLUIDTAG) return r.encodeFluidTagAmount(slot.getFluidTagAmount());
        if (slot.getMode() == CipherSlot.SlotMode.TAG) return r.encodeTagItemCount(slot.getTagCount());
        return "??";
    }

    // returns a string representing the ID of a slot's itemstack
    public static String slotToItemStackId(CipherSlot slot, CipherTemplate.FORMAT format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStackId(r.getItemStack(slot));
    }

    // returns a string representing the pattern of a shaped craft. This can be 3x3 up to 9x9 or even higher
    public static String mapToPatternKey(CipherTemplate.FORMAT format, Map<String, CWIngredient> map, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodePatternKey(map);
    }

    protected abstract String encodePatternKey(Map<String, CWIngredient> map);
    //

    // returns a string representing the map of a pattern's inputs for a shaped/shapeless craft
    public static String mapToPattern(CipherTemplate.FORMAT format, String[] encoded, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodePattern(encoded);
    }

    protected abstract String encodePattern(String[] encoded);
    //

    // returns a string for an ingredient slot
    public static String slotToIngredient(CipherTemplate.FORMAT format, CipherSlot cipherSlot, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeIngredient(cipherSlot);
    }

    protected abstract String encodeIngredient(CipherSlot cipherSlot);
    //

    // returns a string representing itemstacks in an array of slots
    public static String slotToItemStacks(CipherTemplate.FORMAT format, List<CipherSlot> slots, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStacks(slots);
    }

    protected abstract String encodeItemStacks(List<CipherSlot> slots);
    //

    // returns a string representing a parameter
    public static String encodeParameter(CipherTemplate.FORMAT format, String path, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeParameter(path, data);
    }

    protected String encodeParameter(String path, CipherTemplate.DataLoad data)
    {
        var val = data.parameters.get(path);
        var cipher = data.cipher;
        var p = cipher.getParameter(path);
        var replacement = "??";
        if (p != null)
        {
            if (p.getType().equals("boolean"))
            {
                replacement = val.equals("true") ? "true" : "false";
            }
            if (p.getType().equals("number") || p.getType().equals("combo-number"))
            {
                try
                {
                    var value = Double.parseDouble(val);
                    replacement = String.valueOf(value);
                } catch (NumberFormatException e)
                {
                    replacement = "\"" + val + "\"";
                }
            }
            if (p.getType().equals("text") || p.getType().equals("combo"))
            {
                replacement = "\"" + val + "\"";
            }
        }
        return replacement;
    }

//    protected abstract String encodeItemStacks(List<ItemStack> itemStacks);
//    protected abstract String encodeIngredient(Ingredient ingredient);

    protected String encodeItemStackCount(ItemStack itemStack)
    {
        return String.valueOf(itemStack.getCount());
    }

    protected String encodeItemStackId(ItemStack itemStack)
    {
        var loc = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        return "\"" + loc + "\"";
    }

    protected String encodeFluidStackAmount(FluidStack fluidStack)
    {
        return String.valueOf(fluidStack.getAmount());
    }

    protected String encodeTagItemCount(int amount)
    {
        return String.valueOf(amount);
    }

    protected String encodeFluidTagAmount(int amount)
    {
        return String.valueOf(amount);
    }

    protected ItemStack getItemStack(CipherSlot slot)
    {
        if (slot != null)
        {
            if (slot.getMode() == CipherSlot.SlotMode.ITEM)
            {
                if (slot.getCipherobject() instanceof CipherWell)
                {
                    return slot.getItemStack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    protected FluidStack getFluidStack(CipherSlot slot)
    {
        if (slot != null)
        {
            if (slot.getMode() == CipherSlot.SlotMode.FLUID)
            {
                if (slot.getCipherobject() instanceof CipherWell)
                {
                    return slot.getFluidStack();
                }
            }
        }
        return FluidStack.EMPTY;
    }

//    protected Ingredient getIngredient(CipherSlot slot)
//    {
//        if (slot != null)
//        {
//            if (slot.getMode() == CipherSlot.SlotMode.ITEM)
//            {
//                if (slot.getCipherobject() instanceof CipherWell well)
//                {
//                    return getIngredient(slot, well);
//                }
//            }
//            if (slot.getMode() == CipherSlot.SlotMode.TAG)
//            {
//                if (slot.getCipherobject() instanceof CipherWell well)
//                {
//                    return getIngredient(slot, well);
//                }
//            }
//        }
//        return Ingredient.EMPTY;
//    }

//    protected Ingredient getIngredient(CipherSlot slot, CipherWell well)
//    {
//        if (slot.getMode() == CipherSlot.SlotMode.ITEM)
//        {
//            return Ingredient.of(slot.getItemStack());
//        }
//        if (slot.getMode() == CipherSlot.SlotMode.TAG)
//        {
//            return Ingredient.of(slot.getTag());
//        }
//        return Ingredient.EMPTY;
//    }
}
