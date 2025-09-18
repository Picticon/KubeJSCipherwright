package pictisoft.cipherwright.blocks.kubejstable;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pictisoft.cipherwright.registry.ContainerRegistry;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class KubeJSTableContainer extends AbstractContainerMenu
{
    private static final int INVENTORY_PAD_LEFT = 8; // for inventory only
    private static final int INVENTORY_PAD_TOP = 20; // space between cipher and inventory   (for "player inventory" string)
    private static final int INVENTORY_PAD_RIGHT = 8; // for inventory only
    private static final int INVENTORY_PAD_BOTTOM = 8; // space between cipher and inventory
    private static final int INVENTORY_WIDTH = 9 * 18; // size of inventory
    private static final int INVENTORY_HEIGHT = 58 + 18; // size of inventory
    private static final int CIPHER_PAD_LEFT = 8; // padding for container/screen
    private static final int CIPHER_PAD_TOP = 20; // space for title
    private static final int CIPHER_PAD_RIGHT = 8;
    private static final int CIPHER_PAD_BOTTOM = 8;
    private final KubeJSTableBlockEntity blockEntity;
    private ResourceLocation currentRecipeID;


    public KubeJSTableContainer(int windowId, BlockPos pos, Inventory playerInventory, Player playerEntity)
    {
        super(ContainerRegistry.KUBEJS_TABLE_CONTAINER.get(), windowId);
        blockEntity = (KubeJSTableBlockEntity) playerEntity.getCommandSenderWorld().getBlockEntity(pos);
        //Container dummyContainer = new SimpleContainer(100); // Large enough for most layouts
        setupSlots(playerInventory);
    }

    public Rect2i getInset()
    {
        return new Rect2i(CIPHER_PAD_LEFT, CIPHER_PAD_TOP, blockEntity.getCipher().getWidth(), blockEntity.getCipher().getHeight());
    }

    public Rect2i getFullSize()
    {
        return new Rect2i(0, 0,
                Math.max(CIPHER_PAD_LEFT + blockEntity.getCipher().getWidth() + CIPHER_PAD_RIGHT, INVENTORY_PAD_LEFT + INVENTORY_WIDTH + INVENTORY_PAD_RIGHT),
                CIPHER_PAD_TOP + getBlockEntity().getCipher().getHeight() + CIPHER_PAD_BOTTOM + INVENTORY_PAD_TOP + INVENTORY_HEIGHT + INVENTORY_PAD_BOTTOM);
    }

    public void setupSlots(Inventory playerInventory)
    {
        if (blockEntity == null) return;
        // calculate size of dynamic container well
        var cipher = blockEntity.getCipher();
        if (cipher.getRecipeTypeId() == currentRecipeID) return;
        currentRecipeID = cipher.getRecipeTypeId();

        //var i = 0;
        for (var clotObj : blockEntity.getCipherSlots())
        {
            addSlot(new CipherSlotProxy(clotObj, /*i++*/ 0,
                    getInset().getX() + clotObj.getX(),
                    getInset().getY() + clotObj.getY()));
        }

        addPlayerInventory(playerInventory, getFullSize().getX() + (getFullSize().getWidth() / 2) - (INVENTORY_WIDTH / 2),
                getInventoryY());
    }

    public  int getInventoryY()
    {
        return getFullSize().getY() + getFullSize().getHeight() - INVENTORY_HEIGHT - 6;
    }

    // return just the subset of CipherSlotProxy members
    public List<CipherSlotProxy> getCipherSlotProxies()
    {
        var ret = new ArrayList<CipherSlotProxy>();
        for (var l : this.slots)
        {
            if (l instanceof CipherSlotProxy ll) ret.add(ll);
        }
        return ret;
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer)
    {
        try
        {
            if (pSlotId >= 0 && pSlotId < slots.size() && getSlot(pSlotId) instanceof CipherSlotProxy)
            {
                this.doClickCipherSlot(pSlotId, pButton, pClickType, pPlayer);
                this.blockEntity.updateBlock();
            }
            else super.clicked(pSlotId, pButton, pClickType, pPlayer);
        } catch (Exception exception)
        {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
//            crashreportcategory.setDetail("Menu Type", () -> {
//                return this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>";
//            });
            crashreportcategory.setDetail("Menu Class", () -> {
                return this.getClass().getCanonicalName();
            });
            crashreportcategory.setDetail("Slot Count", this.slots.size());
            crashreportcategory.setDetail("Slot", pSlotId);
            crashreportcategory.setDetail("Button", pButton);
            crashreportcategory.setDetail("Type", pClickType);
            throw new ReportedException(crashreport);
        }
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
    {
        //Chatter.chatDebug(this.blockEntity.getLevel(), " pButton:" + pButton + " pClickType:" + pButton + " pDragX:" + pDragX + " pDragY:" + pDragY);
        return false;
    }


    private void doClickCipherSlot(int pSlotId, int pButton, ClickType pClickType, Player pPlayer)
    {
        //Chatter.chatDebug(this.blockEntity.getLevel(), +pSlotId + " pButton:" + pButton + " pClickType:" + pClickType);
        var cursorStack = this.getCarried();
        var slot = (CipherSlotProxy) slots.get(pSlotId);
        var slotStack = slot.getCipherSlot().getItemStack();

//        if (pClickType == ClickType.CLONE)
//        {
//            if (!slot.getCipherSlot().getItem().isEmpty())
//            {
//                this.menu
//            }
//        }

        if ((pClickType == ClickType.PICKUP || pClickType == ClickType.PICKUP_ALL || pClickType == ClickType.QUICK_CRAFT))
        {
            if (slotStack.isEmpty())
            {
                // just place a copy of the stack
                if (pButton == 0)
                    slot.getCipherSlot().setItem(cursorStack.copy());
                else
                    slot.getCipherSlot().setItem(new ItemStack(cursorStack.getItem(), 1));
            } else
            {
                if (ItemStack.isSameItemSameTags(cursorStack, slotStack))
                {
                    var combostack = cursorStack.copy();
                    combostack.setCount(Math.max(1, Math.min((pButton == 0 ? 1 : -1) + slotStack.getCount(), cursorStack.getMaxStackSize())));
                    slot.getCipherSlot().setItem(combostack);
                } else
                {
                    slot.getCipherSlot().setItem(cursorStack.copy());
                }
            }
        }
    }


    private void addPlayerInventory(Inventory playerInv, int left, int top)
    {
        // Hotbar (9 slots, #0- #8)
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInv, col, left + col * 18, top + 58));
        }
        // Main inventory (27 slots, 3x9 grid, #9-#35)
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, left + col * 18, top + row * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return blockEntity.getLevel() != null && blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5, blockEntity.getBlockPos().getY() + 0.5, blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    public KubeJSTableBlockEntity getBlockEntity()
    {
        return blockEntity;
    }

    public boolean hasRecipeTypeChanged()
    {
        return !currentRecipeID.equals(blockEntity.getCipher().getRecipeTypeId());
    }
}