package pictisoft.cipherwright.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class ComboBox extends Button
{
    private final List<String> _options;
    private int _selectedIndex = 0;
    private Consumer<String> responder;

    public ComboBox(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, List<String> options)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
        _options = options;
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


    public void setResponder(Consumer<String> pResponder)
    {
        this.responder = pResponder;
    }

    public String getValue()
    {
        if (_selectedIndex < _options.size())
            return _options.get(_selectedIndex);
        return "";
    }

}
