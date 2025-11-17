package pictisoft.cipherwright.blocks.kubejstable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.cipher.CipherSlot;

public class CipherSlotProxy extends Slot
{
    private final CipherSlot parent;
    private Container cont = new SimpleContainer(1);

    public CipherSlotProxy(CipherSlot parent, int pSlot, int pX, int pY)
    {
        super(new SimpleContainer(1), pSlot, pX, pY); // 150, dgaf
        cont = this.container;
        this.parent = parent;
    }

    public CipherSlot getCipherSlot()
    {
        return parent;
    }

    @Override
    public boolean mayPickup(Player pPlayer)
    {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack pStack)
    {
        return false;
    }

    @Override
    public @NotNull ItemStack getItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack pStack) {
        this.setChanged();
        //Chatter.chat("Should not be called!");
    }

    public void draw(GuiGraphics gui)
    {

    }
}
