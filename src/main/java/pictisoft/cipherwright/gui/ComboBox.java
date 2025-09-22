package pictisoft.cipherwright.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class ComboBox extends Button
{
    private final List<String> _options;
    private int _selectedIndex = 0;
    private Consumer<String> responder;
    private Component label;
    private boolean rightSide = false;

    public ComboBox(int pX, int pY, int pWidth, int pHeight, List<String> options)
    {
        super(pX, pY, pWidth, pHeight, Component.literal(""), (btn) -> {
        }, Button.DEFAULT_NARRATION);
        _options = options;
        _selectedIndex = 0;
        setText();
    }

    public void setLabel(Component label)
    {
        this.label = label;
    }

    public void setLabel(Component label, boolean rightSide)
    {
        this.label = label;
        this.rightSide = rightSide;
    }

    public void onPress()
    {
        this.onPress.onPress(this);
        _selectedIndex++;
        if (_selectedIndex >= _options.size()) _selectedIndex = 0;
        setText();
    }

    public void setValue(String value)
    {
        if (_options.contains(value))
        {
            _selectedIndex = _options.indexOf(value);
            setText();
        }
    }

    private void setText()
    {
        this.setMessage(Component.literal(_options.get(_selectedIndex)));
        if (responder != null) responder.accept(_options.get(_selectedIndex));
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.label != null)
        {
            var font = Minecraft.getInstance().font;
            var textwidth = font.width(this.label);
            var xx = getX() - textwidth - 4;
            if (this.rightSide)
            {
                xx = getX() + getWidth() + 3;
            }
            pGuiGraphics.drawString(font, this.label, xx, getY() + getHeight() / 2-font.lineHeight/2, 0x404040, false);
        }

    }

    public void setResponder(Consumer<String> pResponder)
    {
        this.responder = pResponder;
    }

    public String getValue()
    {
        if (_selectedIndex < _options.size()) return _options.get(_selectedIndex);
        return "";
    }

}
