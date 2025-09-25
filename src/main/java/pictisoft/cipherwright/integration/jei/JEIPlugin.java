package pictisoft.cipherwright.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableScreen;

import java.util.Collections;
import java.util.Optional;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(CipherWrightMod.MODID, "jei_plugin");
    private static IJeiRuntime jeiRuntime;
    private static IJeiKeyMappings keyMappings;

    @Override
    public @NotNull ResourceLocation getPluginUid()
    {
        return PLUGIN_ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime)
    {
        jeiRuntime = runtime;
        keyMappings = jeiRuntime.getKeyMappings();
    }

    public static IJeiKeyMapping getShowUses()
    {
        return keyMappings.getShowUses();
    }

    public static IJeiKeyMapping getShowRecipe()
    {
        return keyMappings.getShowRecipe();
    }


    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
    {
        registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler(registration.getTransferHelper()));
    }

    //    @Override
//    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//        registration.addGhostIngredientHandler(KubeJSTableScreen.class, new CipherIngredientHandler());
//    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGhostIngredientHandler(KubeJSTableScreen.class, new CipherIngredientHandler());
        registration.addGuiContainerHandler(KubeJSTableScreen.class, new CipherExclusionHandler());
    }

    public static void showRecipes(ItemStack stack, boolean asOutput)
    {
        if (jeiRuntime == null) return;
        IRecipesGui recipesGui = jeiRuntime.getRecipesGui();
        IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

        Optional<ITypedIngredient<ItemStack>> typed = ingredientManager.createTypedIngredient(stack);
        if (typed.isEmpty()) return;

        RecipeIngredientRole role = asOutput ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        IFocus<ItemStack> focus = focusFactory.createFocus(role, typed.get());

        recipesGui.show(Collections.singletonList(focus));
    }

    public static void showRecipes(FluidStack stack, boolean asOutput)
    {
        if (jeiRuntime == null) return;
        IRecipesGui recipesGui = jeiRuntime.getRecipesGui();
        IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();

        Optional<ITypedIngredient<FluidStack>> typed = ingredientManager.createTypedIngredient(stack); // shrug
        if (typed.isEmpty()) return;

        RecipeIngredientRole role = asOutput ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        IFocus<FluidStack> focus = focusFactory.createFocus(role, typed.get());

        recipesGui.show(Collections.singletonList(focus));
    }


}
