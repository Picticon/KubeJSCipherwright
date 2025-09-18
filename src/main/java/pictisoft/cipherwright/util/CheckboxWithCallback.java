package pictisoft.cipherwright.util;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public class CheckboxWithCallback extends Checkbox
{
    public Consumer<Boolean> callback;

    public CheckboxWithCallback(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pSelected);
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
}
