package pictisoft.cipherwright.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class ShiftButton extends Button
{
    private final Component message2;

    public ShiftButton(int pX, int pY, int pWidth, int pHeight,
            Component pMessage,
            Component pMessage2,
            OnPress pOnPress)
    {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
        this.message2 = pMessage2;
        setTooltip(Tooltip.create(Component.literal("Hold [shift] to adjust by 10x")));
    }

    @Override
    public @NotNull Component getMessage()
    {
        if (Screen.hasShiftDown()) return this.message2;
        return super.getMessage();
    }
}
