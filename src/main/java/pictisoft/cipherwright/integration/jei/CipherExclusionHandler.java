package pictisoft.cipherwright.integration.jei;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableScreen;

import java.util.ArrayList;
import java.util.List;

// This gives the excluded rectangles on the screen.
public class CipherExclusionHandler implements IGuiContainerHandler<KubeJSTableScreen>
{
    @Override
    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull KubeJSTableScreen containerScreen)
    {
        List<Rect2i> exclusions = new ArrayList<>();
        containerScreen.addExclusions(exclusions);
        return exclusions;
    }
}
