package pictisoft.cipherwright.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class AdvancedEditBox extends EditBox
{
    private final boolean rightSide;

    public AdvancedEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean rightSide)
    {
        super(pFont, pX, pY, pWidth, pHeight, null, pMessage);
        this.rightSide = rightSide;
    }


    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        var font = Minecraft.getInstance().font;
        var textwidth = font.width(this.getMessage());
        var xx = getX() - textwidth - 4;
        if (this.rightSide)
        {
            xx = getX() + getWidth() + 3;
        }
        pGuiGraphics.drawString(font, this.getMessage(), xx, getY() + getHeight() / 2 - font.lineHeight/2, 0x404040, false);
    }
}
