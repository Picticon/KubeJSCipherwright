package pictisoft.cipherwright.cipher;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import pictisoft.cipherwright.util.Chatter;

import java.util.*;

public abstract class CipherEncoderBase
{
    private final CipherTemplate.DataLoad data;

    public CipherEncoderBase(CipherTemplate.DataLoad data)
    {
        this.data = data;
    }

    private static CipherEncoderBase getEncoder(String format, CipherTemplate.DataLoad data)
    {
        if (Objects.equals(format, "kubejs")) return new CipherEncoderKubeJS(data);
        return new CipherEncoderJSON(data);
    }

    public static String slotsToIngredients(String format, List<CipherSlot> slots, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeIngredients(slots);
    }


    public static String slotToItemStack(CipherSlot slot, String format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStack(r.getItemstack(slot));
    }

    public static String slotToItemStackCount(CipherSlot slot, String format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStackCount(r.getItemstack(slot));
    }

    public static String slotToItemStackId(CipherSlot slot, String format, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStackId(r.getItemstack(slot));
    }

    public static String mapToPatternKey(String format, Map<String, CWIngredient> map, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodePatternKey(map);
    }


    public static String mapToPattern(String format, String[] encoded, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodePattern(encoded);
    }

    public static String slotToIngredient(String format, CipherSlot cipherSlot, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeIngredient(cipherSlot);
    }

    public static String slotToItemStacks(String format, List<CipherSlot> slots, CipherTemplate.DataLoad data)
    {
        CipherEncoderBase r = getEncoder(format, data);
        return r.encodeItemStacks(slots);
    }

    public static String encodeParameter(String format, String path, CipherTemplate.DataLoad data)
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

    protected abstract String encodeItemStacks(List<CipherSlot> slots);

//    protected abstract String encodeItemStacks(List<ItemStack> itemStacks);
//    protected abstract String encodeIngredient(Ingredient ingredient);

    protected abstract String encodeIngredient(CipherSlot cipherSlot);

    protected abstract String encodeItemStack(ItemStack itemStack);

    protected String encodeItemStackCount(ItemStack itemStack)
    {
        return String.valueOf(itemStack.getCount());
    }

    protected String encodeItemStackId(ItemStack itemStack)
    {
        var loc = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        return "\"" + loc + "\"";
    }

    protected abstract String encodeIngredients(List<CipherSlot> slots);

    protected abstract String encodePattern(String[] encoded);

    protected abstract String encodePatternKey(Map<String, CWIngredient> map);

    protected ItemStack getItemstack(CipherSlot slot)
    {
        if (slot != null)
        {
            if (slot.getMode() == CipherSlot.SlotMode.ITEM)
            {
                if (slot.getCipherobject() instanceof CipherWell well)
                {
                    return slot.getItemStack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    protected Ingredient getIngredient(CipherSlot slot)
    {
        if (slot != null)
        {
            if (slot.getMode() == CipherSlot.SlotMode.ITEM)
            {
                if (slot.getCipherobject() instanceof CipherWell well)
                {
                    return getIngredient(slot, well);
                }
            }
            if (slot.getMode() == CipherSlot.SlotMode.TAG)
            {
                if (slot.getCipherobject() instanceof CipherWell well)
                {
                    return getIngredient(slot, well);
                }
            }
        }
        return Ingredient.EMPTY;
    }

    protected Ingredient getIngredient(CipherSlot slot, CipherWell well)
    {
        if (slot.getMode() == CipherSlot.SlotMode.ITEM)
        {
            return Ingredient.of(slot.getItemStack());
        }
        if (slot.getMode() == CipherSlot.SlotMode.TAG)
        {
            return Ingredient.of(slot.getTag());
        }
        return Ingredient.EMPTY;
    }
}
