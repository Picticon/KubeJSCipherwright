package pictisoft.cipherwright.integration.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableScreen;
import pictisoft.cipherwright.cipher.CipherSlot;
import pictisoft.cipherwright.network.EntityToServerIntStringStringMessage;
import pictisoft.cipherwright.network.MessageRegistry;
import pictisoft.cipherwright.util.ItemAndIngredientHelpers;
import pictisoft.cipherwright.util.JsonHelpers;

import java.awt.*;
import java.util.ArrayList;

public class CipherIngredientHandler implements IGhostIngredientHandler<KubeJSTableScreen>
{
    @Override
    public <I> java.util.List<Target<I>> getTargetsTyped(KubeJSTableScreen screen, ITypedIngredient<I> ingredient, boolean doStart)
    {
        java.util.List<Target<I>> targets = new ArrayList<>();


        for (CipherSlot slot : screen.getMenu().getBlockEntity().getCipherSlots())
        {
            if (ingredient.getIngredient() instanceof FluidStack && !slot.canBeFluid()) continue;
            if (ingredient.getIngredient() instanceof ItemStack && !slot.canBeItem()) continue;

            targets.add(new Target<>()
            {
                @Override
                public Rect2i getArea()
                {
                    return new Rect2i(
                            screen.getGuiLeft() + screen.getContainer().getInset().getX() + slot.getX(),
                            screen.getGuiTop() + screen.getContainer().getInset().getY() + slot.getY(),
                            16, 16);
                }

                @Override
                public void accept(I ingredient)
                {
                    var be = screen.getMenu().getBlockEntity();
                    if (ingredient instanceof ItemStack itemStack)
                    {
                        MessageRegistry.sendToServer(
                                new EntityToServerIntStringStringMessage(be.getLevel(), be.getBlockPos(), KubeJSTableBlockEntity.SET_ITEM_FROM_JEI_DROP,
                                        slot.getSerialKey(),
                                        ItemAndIngredientHelpers.itemStackToJson(itemStack).toString()));
                        //slot.setItem(itemStack);
                    } else if (ingredient instanceof FluidStack fluidStack)
                    {
                        MessageRegistry.sendToServer(
                                new EntityToServerIntStringStringMessage(be.getLevel(), be.getBlockPos(), KubeJSTableBlockEntity.SET_FLUID_FROM_JEI_DROP,
                                        slot.getSerialKey(),
                                        ItemAndIngredientHelpers.fluidStackToJson(fluidStack).toString()));
                        //slot.setFluid(fluidStack);
                    }
                }
            });

        }
        return targets;
    }

    @Override
    public void onComplete()
    {
        // Optional: Handle logic when dragging is finished
    }
}
