package pictisoft.cipherwright.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.util.Slice9;

import java.util.function.Consumer;

public class CheckboxWithCallback extends Checkbox
{
    private final Slice9 slice9;
    private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(CipherWrightMod.MODID, "textures/gui/9slice_checkbox.png");
    private static final ResourceLocation GUI_BACKGROUND_SELECTED = new ResourceLocation(CipherWrightMod.MODID, "textures/gui/9slice_checkbox_selected.png");
    private final boolean rightSide;
    public Consumer<Boolean> callback;

    public CheckboxWithCallback(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected, boolean rightSide)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pSelected, false);
        this.slice9 = new Slice9(GUI_BACKGROUND, 8);
        this.rightSide = rightSide;
    }

    public void setSelected(boolean value)
    {
        if (this.selected() != value) super.onPress(); // WHY is selected PRIVATE?
    }

    @Override
    public void onPress()
    {
        super.onPress();
        if (this.callback != null) this.callback.accept(this.selected());
    }

    public void setResponder(Consumer<Boolean> callback)
    {
        this.callback = callback;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        //super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        RenderSystem.enableBlend();
        slice9.drawNineSlice(pGuiGraphics, getX(), getY(), getWidth(), getHeight());
        if (this.selected())
            pGuiGraphics.blit(GUI_BACKGROUND_SELECTED, getX() +getWidth() / 2 - 4, getY() + getHeight()/ 2 - 4, 0, 0, 8, 8, 8, 8);
        var font = Minecraft.getInstance().font;
        var textwidth = font.width(getMessage());
        var xx = getX() - textwidth - 4;
        if (this.rightSide)
        {
            xx = getX() + getWidth() + 3;
        }
        pGuiGraphics.drawString(font, getMessage(), xx, getY() + getHeight() / 2 - font.lineHeight / 2, 0x404040, false);

    }

}
