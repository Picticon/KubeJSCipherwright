package pictisoft.cipherwright.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableScreen;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(CipherWrightMod.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid()
    {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
    {
        //registration.addRecipeTransferHandler(new KubeJSTableTransferHandler(), RecipeTypes.CRAFTING);
        registration.addUniversalRecipeTransferHandler(new EncodePatternTransferHandler());
//        registration.addRecipeTransferHandler(EssenceDistillerBlockContainer.class, ContainerRegistry.ESSENCE_DISTILLER_BLOCK_CONTAINER.get(),
//                ESSENCE_DISTILLER_CRAFT, EssenceDistillerBlockContainer.getRecipeStart(), EssenceDistillerBlockContainer.getRecipeCount(),
//                EssenceDistillerBlockContainer.getInventoryStart(), EssenceDistillerBlockContainer.getInventoryCount());
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


}
