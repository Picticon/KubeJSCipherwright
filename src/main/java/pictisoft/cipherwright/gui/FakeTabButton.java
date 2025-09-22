package pictisoft.cipherwright.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class FakeTabButton extends Button
{
    private boolean _selected;

    public FakeTabButton(int x, int y, int w, int h, Component text, Button.OnPress pOnPress)
    {
        super(x, y, w, h, text, pOnPress, Button.DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.alpha = _selected ? 1 : .5f;
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public void setSelected(boolean b)
    {
        _selected = b;
    }
}
