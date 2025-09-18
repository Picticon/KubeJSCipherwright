package pictisoft.cipherwright.blocks.kubejstable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.cipher.*;
import pictisoft.cipherwright.network.*;
import pictisoft.cipherwright.util.*;

import java.util.*;

public class KubeJSTableScreen extends AbstractContainerScreen<KubeJSTableContainer> implements SyncParameterTrigger.IHandleSyncParameterTrigger
{
    private final Rect2i workarea;
    private final Rect2i tabarea;
    private CipherSlotProxy slotSelectedForTag = null;
    private ItemScrollPanel<String> scrollTags;
    private ItemScrollPanel<String> scrollNBT;

    private final KubeJSTableContainer container;
    private final Slice9 slice9;
    private final GUIElementRenderer guiRenderer;
    private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(CipherWrightMod.MODID, "textures/gui/9slice_gui.png");
    private static final ResourceLocation GUI_ELEMENTS = new ResourceLocation(CipherWrightMod.MODID, "textures/gui/gui_elements.png");
    private ItemScrollPanel<String> scrollCategory;
    private ItemScrollPanel<Cipher> scrollChildren;
    private final Map<String, EditBox> editBoxes = new LinkedHashMap<>();
    private final Map<String, ComboBox> comboBoxes = new LinkedHashMap<>();
    private Button btnSampleRecipe1;
    private Button btnSampleRecipe2;
    private Button btnSampleRecipe3;
    private ImageButton btnClearScreen;
    private float _atTick;
    private static final ResourceLocation GUI_BITMAP = new ResourceLocation(CipherWrightMod.MODID, "textures/gui/cipherwright_gui.png");
    private static final int GUI_BITMAP_W = 32;
    private static final int GUI_BITMAP_H = 32;
    private boolean _dontFireControlUpdates;
    private final ArrayList<Button> _templateButtons = new ArrayList<>();
    private final ArrayList<CipherSlotProxy> _dragslots = new ArrayList<>();
    private int _selectedTab;
    private Button btnClearOriginalRecipe;
    private final int BUTTON_WIDTH = 110;
    private final int RIGHT_PANEL_WIDTH = 170;
    private final int TAB_BUTTON_WIDTH = RIGHT_PANEL_WIDTH / 3;
    //private int LEFT_PANEL_WIDTH = 0;
    //private boolean show_left_panel = false;
    private FakeTabButton tabItemEdit;
    private FakeTabButton tabRecipes;
    private FakeTabButton tabClipboard;
    private Button btnAddCount;
    private Button btnSubtractCount;
    private CheckboxWithCallback chkIncludeComments;
    private Button btnReloadJSON;
    private Button btnAddCountX10;
    private Button btnSubtractCountX10;
    private Button btnAddCountX100;
    private Button btnSubtractCountX100;

    public KubeJSTableScreen(KubeJSTableContainer container, Inventory inv, Component title)
    {
        super(container, inv, title);
        this.container = container;
        this.slice9 = new Slice9(GUI_BACKGROUND, 16);
        this.guiRenderer = new GUIElementRenderer(GUI_ELEMENTS);

        this.workarea = new Rect2i(0, 0, container.getFullSize().getWidth(), container.getFullSize().getHeight());
        this.tabarea = new Rect2i(this.workarea.getWidth(), 0, RIGHT_PANEL_WIDTH, this.workarea.getHeight());
        this.imageWidth = Math.max(9 * 18 + 16, this.workarea.getWidth()) + this.tabarea.getWidth();
        this.imageHeight = Math.max(this.workarea.getHeight(), this.tabarea.getHeight());
    }

    public KubeJSTableContainer getContainer()
    {
        return container;
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        if (!_dontFireControlUpdates)
        {
            for (var kvp : getContainer().getBlockEntity().getCipherParameters().entrySet())
            {
                for (var eb : editBoxes.entrySet())
                {
                    if (eb.getKey().equals(kvp.getKey()))
                    {
                        // skip if the textbox is focused. This can cause problems in rare cases (recipe changes from a second user while user is typing).. but... should be very rare
                        if (!eb.getValue().isFocused() && !eb.getValue().getValue().equals(kvp.getValue()))
                        {
                            eb.getValue().setValue(kvp.getValue());
                        }
                    }
                }
                for (var eb : comboBoxes.entrySet())
                {
                    if (eb.getKey().equals(kvp.getKey()))
                    {
                        // skip if the textbox is focused. This can cause problems in rare cases (recipe changes from a second user while user is typing).. but... should be very rare
                        if (!eb.getValue().isFocused() && !eb.getValue().getValue().equals(kvp.getValue()))
                        {
                            eb.getValue().setValue(kvp.getValue());
                        }
                    }
                }
            }
        }
        chkIncludeComments.setSelected(container.getBlockEntity().areCommentsIncluded());
    }

    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY); // need this to get tooltips.
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY)
    {
        //super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        pGuiGraphics.drawString(this.font, Component.literal(container.getBlockEntity().getCipher().getName()), this.titleLabelX, this.titleLabelY, 0x404040,
                false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY)
    {
        _atTick += partialTicks;
        //Chatter.chat(String.format("%d %d vs %d %d", this.leftPos, this.topPos, this.stringScrollPanel.getRectangle().left(),this.stringScrollPanel.getRectangle().top() ));

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        slice9.drawNineSlice(gui, relX, relY, this.imageWidth, this.imageHeight);

        // render illustrations
        gui.pose().pushPose();
        gui.pose().translate(this.leftPos + container.getInset().getX(), this.topPos + container.getInset().getY(), 0);
        for (var illustration : container.getBlockEntity().getCipher().getIllustrations())
        {
            illustration.render(gui, guiRenderer, this.container.getBlockEntity().getCipherParameters());
        }
        gui.pose().popPose();
        gui.pose().pushPose();


        if (_selectedTab == 0)
        {
            gui.drawString(this.font, Component.literal("Convert to tag:"), scrollTags.getRectangle().left() + 2, scrollTags.getRectangle().top() - 10,
                    0x404040, false);
            gui.drawString(this.font, Component.literal("Remove NBT:"), scrollNBT.getRectangle().left() + 2, scrollNBT.getRectangle().top() - 10, 0x404040,
                    false);
        }

        // int left = (width - imageWidth) / 2;
        // int top = (height - imageHeight) / 2;

        gui.pose().pushPose();
        gui.pose().translate(this.leftPos, this.topPos, 0);
        for (var slot : container.slots)
        {
            if (!(slot instanceof CipherSlotProxy))
            {
                guiRenderer.drawSlotWell(gui, slot.x, slot.y);
            }
        }
        gui.pose().popPose();

        // Render slot wells
        gui.pose().pushPose();
        gui.pose().translate(this.leftPos + container.getInset().getX(), this.topPos + container.getInset().getY(), 0);
        for (var slot : menu.getBlockEntity().getCipherSlots())
        {
            if (slot.isLarge())
            {
                guiRenderer.drawSlotWellLarge(gui, slot.getX(), slot.getY());
            } else
            {
                guiRenderer.drawSlotWell(gui, slot.getX(), slot.getY());
            }
        }
        gui.pose().popPose();

        // Render slots
        for (var slot : menu.getBlockEntity().getCipherSlots())
        {
//            if (slot instanceof CipherSlot cipherslot)
            {
                gui.pose().pushPose();
                gui.pose().translate(this.leftPos + container.getInset().getX(), this.topPos + container.getInset().getY(), 0);
                if (slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().equals(slot))
                {
                    // render tag editing mode
                    gui.fill(slot.getX() - 1, slot.getY() - 1, slot.getX() + 17, slot.getY() + 17, 100, 0x44ff00ff);
                }
                slot.render(gui, guiRenderer, (int) _atTick);
                if (slot.getMode() == CipherSlot.SlotMode.ITEM && slot.getItemStack() != null && slot.getItemStack().hasTag())
                {
                    // render tag indicator
                    RenderSystem.disableDepthTest();
                    gui.pose().translate(slot.getX(), slot.getY() + 12, 0);
                    gui.blit(GUI_BITMAP, 0, 0, 0, 26, 4, 5, GUI_BITMAP_W, GUI_BITMAP_H);
                    RenderSystem.enableDepthTest();
                }
                if (slot.getMode() == CipherSlot.SlotMode.TAG)
                {
                    // render tag indicator
                    RenderSystem.disableDepthTest();
                    gui.pose().translate(slot.getX() + 12, slot.getY() + 12, 0);
                    gui.blit(GUI_BITMAP, 0, 0, 0, 26, 4, 5, GUI_BITMAP_W, GUI_BITMAP_H);
                    RenderSystem.enableDepthTest();
                }
                gui.pose().popPose();
            }
        }

        // render inputs (if they need to)
        gui.pose().pushPose();
        gui.pose().translate(this.leftPos + container.getInset().getX(), this.topPos + container.getInset().getY(), 0);
        for (var parameter : container.getBlockEntity().getCipher().getParameters())
        {
            parameter.render(gui, guiRenderer);
        }
        gui.pose().popPose();
        gui.pose().pushPose();

        // render original recipe id
        if (btnClearOriginalRecipe.visible && container.getBlockEntity().getOriginalRecipeID() != null)
        {
            var leftedge = btnClearOriginalRecipe.getX() - 4;
            var topedge = btnClearOriginalRecipe.getY() + 4;
            var str = container.getBlockEntity().getOriginalRecipeID().toString();
            var w = Math.min(tabarea.getWidth() + 20, this.font.width(str));
            gui.enableScissor(leftedge - w, topedge, leftedge, topedge + 10);
            gui.pose().translate(leftedge, topedge, 0);
            gui.pose().scale(.75f, .75f, 1);
            gui.pose().translate(-w, 0, 0);
            gui.drawString(this.font, Component.literal(str), 0, 0, 0x404040, false);
            //btnClearOriginalRecipe.visible = true;
            gui.disableScissor();
        } else btnClearOriginalRecipe.visible = false;
        gui.pose().popPose();

//        // Render tag selection overlay if active
//        if (showTagSelection && selectedSlot != null)
//        {
//            renderTagSelectionPanel(gui, mouseX, mouseY);
//        }
    }

    private boolean isHoveringNotPrivate(Slot pSlot, double pMouseX, double pMouseY)
    {
        return this.isHovering(pSlot.x, pSlot.y, 16, 16, pMouseX, pMouseY);
    }

    protected List<Component> getTooltipFromContainerItem(ItemStack pStack)
    {
        if (this.minecraft != null)
        {
            var ret = getTooltipFromItem(this.minecraft, pStack);
            Slot hoveredSlot = getSlotUnderMouse();
            if (hoveredSlot instanceof CipherSlotProxy cipherslotProxy)
            {
                if (cipherslotProxy.getCipherSlot().getMode() == CipherSlot.SlotMode.TAG)
                {
                    var tagstring = cipherslotProxy.getCipherSlot().getTag().toString();
                    ret.add(Component.literal(tagstring));
                }
            }
            return ret;
        }
        return List.of();
    }


    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY)
    {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null)
        {
            var show = false;
            if (this.hoveredSlot instanceof CipherSlotProxy cs)
            {
                if (cs.getCipherSlot().getMode() == CipherSlot.SlotMode.TAG) show = true;
            }
            if (this.hoveredSlot.hasItem()) show = true;
            if (show)
            {
                ItemStack itemstack = this.hoveredSlot.getItem();
                gui.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton)
    {
        //_mousedown = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    // #START MOUSE
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        //Chatter.chat("mouseClicked");
        //_mousedown = true;
        //Chatter.chat(container.getCarried().toString());
        //_mouseheld = container
        // Handle tag selection panel clicks
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy proxy)
        {
            if (button == 0)
            {
                _dragslots.clear();
                _dragslots.add(proxy);
                hideEditPanel();
                slotSelectedForTag = null;
                setSelectedTab(_selectedTab);
            }
            if (button == 2)
            {
                // the clicked slot MUST be:
                // ITEM MODE
                // NOT EMPTY
                // NOT PREVIOUS
                // if any of the above is not true, hide it.

                if (/*proxy.getCipherSlot().canBeTag() &&*/
                        !(slotSelectedForTag != null && proxy.index == slotSelectedForTag.index))
                {
                    slotSelectedForTag = proxy;
                    setSelectedTab(0);
                    showEditPanel();
                } else
                {
                    hideEditPanel();
                    slotSelectedForTag = null;
                    setSelectedTab(_selectedTab);
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
    {
        //Chatter.chat("mouseDragged");
        if (scrollCategory != null && scrollCategory.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (scrollChildren != null && scrollChildren.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy proxy && pButton == 0 && !_dragslots.contains(proxy))
        {
            _dragslots.add(proxy);
        }
        if (container.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
    // #END MOUSE

    private void setTagModeForSelectedSlotFromList(Integer idx)
    {
        if (slotSelectedForTag == null) return;
        if (slotSelectedForTag.getCipherSlot().canBeTag())
        {
            var tags = getTagsForItemStack(slotSelectedForTag.getCipherSlot().getItemStack());
            if (!tags.isEmpty())
            {
                if (tags.stream().toList().size() > idx)
                {
                    var r = tags.stream().toList().get(idx).location().toString();
                    //slotSelectedForTag.getCipherSlot().setTagKey(tags.stream().toList().get(idx));
                    sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_CHANGE_TO_TAG, slotSelectedForTag.getCipherSlot(), r);
                }
            }
            slotSelectedForTag = null;
            showEditPanel();
            setSelectedTab(_selectedTab);
        }
    }

    Collection<TagKey<Item>> getTagsForItemStack(ItemStack itemstack)
    {
        var item = itemstack.getItem();
        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
        if (itemKey == null) return new ArrayList<>();
        return BuiltInRegistries.ITEM.getHolderOrThrow(itemKey).tags().toList();
    }

    private void showEditPanel()
    {
        if (slotSelectedForTag != null)
        {
            var itemstack = slotSelectedForTag.getCipherSlot().getItemStack();
            if (!itemstack.isEmpty())
            {
                var tags = getTagsForItemStack(itemstack);
                var nbt = itemstack.getTag();
                var arr = new ArrayList<String>();
                if (!tags.isEmpty())
                {
                    for (var tag : tags)
                    {
                        arr.add(tag.location().toString());
                    }
                }
                scrollTags.setItems(arr); // will clear if no available tags

                var arr2 = new ArrayList<String>();
                if (nbt != null && !nbt.isEmpty())
                {
                    for (var key : nbt.getAllKeys())
                    {
                        var n = nbt.get(key);
                        if (n != null)
                        {
                            arr2.add(key + "=" + n);
                        }
                    }
                }
                scrollNBT.setItems(arr2);
            }
            scrollTags.setDisabled(!slotSelectedForTag.getCipherSlot().canBeTag());
        } else
        {
            hideEditPanel();
        }

    }

    private void hideEditPanel()
    {
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setSelectedIndex(-1, false);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setSelectedIndex(-1, false);
    }


//    @Override
//    protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY)
//    {
//        Minecraft minecraft = Minecraft.getInstance();
//        if (container.blockEntity.getOriginalRecipe() != null)
//            matrixStack.drawString(minecraft.font, container.blockEntity.getOriginalRecipe().toString(), 30, 90, 0xffffffff);
//        if (container.blockEntity.getCipherType() != null)
//            matrixStack.drawString(minecraft.font, container.blockEntity.getCipherType(), 30, 110, 0xffffffff);
//    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void init()
    {
        //clearWidgets();
        super.init();
        var leftedge = this.width / 2 - this.imageWidth / 2;
        var topedge = this.height / 2 - this.imageHeight / 2;

        addSnippetButtons();

        tabItemEdit = new FakeTabButton(leftedge + this.tabarea.getX(), topedge + this.tabarea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Item"),
                (a) -> {
                    this.setSelectedTab(0);
                });
        this.addRenderableWidget(tabItemEdit);
        tabRecipes = new FakeTabButton(leftedge + this.tabarea.getX() + TAB_BUTTON_WIDTH, topedge + this.tabarea.getY() - 20, TAB_BUTTON_WIDTH, 20,
                Component.literal("Recipe"), (a) -> {
            this.setSelectedTab(1);
        });
        this.addRenderableWidget(tabRecipes);
        tabClipboard = new FakeTabButton(leftedge + this.tabarea.getX() + TAB_BUTTON_WIDTH * 2, topedge + this.tabarea.getY() - 20, TAB_BUTTON_WIDTH, 20,
                Component.literal("Clipboard"), (a) -> {
            this.setSelectedTab(2);
        });
        this.addRenderableWidget(tabClipboard);

        btnClearOriginalRecipe = new ImageButton(leftedge + this.tabarea.getX() + this.tabarea.getWidth() - 20, topedge + this.tabarea.getY() + 6, 13, 13, 13,
                0, 13, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.ORIGINAL_RECIPE_CLEAR_ON_SERVER, 0);
            btnClearOriginalRecipe.setFocused(false);
            this.setFocused(null);
        });
        btnClearOriginalRecipe.visible = false;
        this.addRenderableWidget(btnClearOriginalRecipe);

        // ADD SUBSTRACTS
        btnSubtractCount = Button.builder(Component.literal("-"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-1");
            }
        }).bounds(leftedge + this.tabarea.getX() + 6, topedge + this.tabarea.getY() + 6, 16, 16).build();
        this.addRenderableWidget(btnSubtractCount);

        btnAddCount = Button.builder(Component.literal("+"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "1");
            }
        }).bounds(btnSubtractCount.getX() + btnSubtractCount.getWidth() + 2, topedge + this.tabarea.getY() + 6, 16, 16).build();
        this.addRenderableWidget(btnAddCount);

        btnSubtractCountX10 = Button.builder(Component.literal("-10"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-10");
            }
        }).bounds(btnAddCount.getX() + btnAddCount.getWidth() + 2, topedge + this.tabarea.getY() + 6, 25, 16).build();
        this.addRenderableWidget(btnSubtractCountX10);

        btnAddCountX10 = Button.builder(Component.literal("+10"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "10");
            }
        }).bounds(btnSubtractCountX10.getX() + btnSubtractCountX10.getWidth() + 2, topedge + this.tabarea.getY() + 6, 25, 16).build();
        this.addRenderableWidget(btnAddCountX10);


        btnSubtractCountX100 = Button.builder(Component.literal("-100"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-100");
            }
        }).bounds(btnAddCountX10.getX() + btnAddCountX10.getWidth() + 2, topedge + this.tabarea.getY() + 6, 32, 16).build();
        this.addRenderableWidget(btnSubtractCountX100);

        btnAddCountX100 = Button.builder(Component.literal("+100"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "100");
            }
        }).bounds(btnSubtractCountX100.getX() + btnSubtractCountX100.getWidth() + 2, topedge + this.tabarea.getY() + 6, 32, 16).build();
        this.addRenderableWidget(btnAddCountX100);

        // END ADD SUBTRACTS

        btnReloadJSON = Button.builder(Component.literal("/reload"), (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RUN_COMMAND, KubeJSTableBlockEntity.RUN_COMMAND_RELOAD);
        }).bounds(leftedge + this.tabarea.getX() + tabarea.getWidth() - 72,
                topedge + this.tabarea.getY() + tabarea.getHeight() - 50, 66, 16).build();
        this.addRenderableWidget(btnReloadJSON);

        var remaining = Math.max(32, (this.tabarea.getHeight() - 30) / 2);
        var ystack = topedge + tabarea.getY() + 23;

        scrollTags = new ItemScrollPanel<>(this.tabarea.getWidth() - 10, remaining - 16, ystack + 16, leftedge + this.tabarea.getX() + 5, (a) -> a);
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setOnClick((idx) -> {
            setTagModeForSelectedSlotFromList(idx);
            showEditPanel(); // reload and hide if neccessary
        });
        this.addRenderableWidget(scrollTags);
        ystack += remaining;

        scrollNBT = new ItemScrollPanel<>(this.tabarea.getWidth() - 10, remaining - 16, ystack + 16, scrollTags.getRectangle().left(), (a) -> a);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setOnClick((idx) -> {
            //setTagModeForSelectedSlotFromList(idx);
            deleteNBTForSelectedSlotFromList(scrollNBT.getItems().get(idx));
            showEditPanel();
        });
        this.addRenderableWidget(scrollNBT);

//        btnCloseLeftPanel = new ImageButton(leftedge - 17, topedge + 3, 13, 13, 13, 0, 13, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
//            hideEditPanel();
//            slotSelectedForTag = null;
//            btnCloseLeftPanel.setFocused(false);
//            this.setFocused(null);
//        });
//        btnCloseLeftPanel.visible = false;
//        this.addRenderableWidget(btnCloseLeftPanel);

//        btnShowRecipeTypes = Button.builder(Component.literal("Recipes"), (btn) -> {
//            scrollCategory.visible = !scrollCategory.visible;
//            scrollChildren.visible = scrollCategory.visible;
//        }).bounds(rightedge + 3, topedge, 110, 20).build();
//        this.addRenderableWidget(btnShowRecipeTypes);

        remaining = Math.max(32, (this.tabarea.getHeight() - 10) / 2);
        ystack = topedge + tabarea.getY() + 6;

        scrollCategory = new ItemScrollPanel<>(this.tabarea.getWidth() - 10, remaining - 3, ystack, leftedge + this.tabarea.getX() + 5, (a) -> a);
        scrollCategory.setItems(CipherJsonLoader.getSortedCipherCategoryNames());
        scrollCategory.setOnClick((idx) -> {
            scrollChildren.setItems(CipherJsonLoader.getSortedCipherChildren(CipherJsonLoader.getSortedCipherCategoryNames().get(idx)));
        });
        scrollCategory.visible = false;
        this.addRenderableWidget(scrollCategory);

        ystack += remaining;

        scrollChildren = new ItemScrollPanel<>(this.tabarea.getWidth() - 10, remaining, ystack, leftedge + this.tabarea.getX() + 5, Cipher::getName);
        scrollChildren.setOnClick((idx) -> {
            //Chatter.chat("client sending : " + (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
            sendIntStringMessage(KubeJSTableBlockEntity.RECIPE_TYPE_SCROLLBOX, (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
        });
        scrollChildren.visible = false;
        this.addRenderableWidget(scrollChildren);

        btnClearScreen = new ImageButton(leftedge + this.workarea.getWidth() - 16, topedge + this.workarea.getY() + 6, 13, 13, 13, 0, 13, GUI_BITMAP,
                GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 1);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        this.addRenderableWidget(btnClearScreen);

        chkIncludeComments = new CheckboxWithCallback(leftedge + tabarea.getX() + 6, topedge + tabarea.getY() + tabarea.getHeight() - 32, 16, 20,
                Component.literal("Comments?"), true);
        chkIncludeComments.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_COMMENT_PARAMETER, 0);
        };
        addRenderableWidget(chkIncludeComments);


        btnSampleRecipe1 = new ImageButton(leftedge + tabarea.getX() + this.tabarea.getWidth() - 36 - 16,
                topedge + this.tabarea.getY() + this.tabarea.getHeight() - 26, 13, 13,
                13, 0, 13, GUI_BITMAP,
                GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 91);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        this.addRenderableWidget(btnSampleRecipe1);
        btnSampleRecipe2 = new ImageButton(leftedge + tabarea.getX() + this.tabarea.getWidth() - 36,
                topedge + this.tabarea.getY() + this.tabarea.getHeight() - 26, 13, 13,
                13, 0, 13, GUI_BITMAP,
                GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 92);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        this.addRenderableWidget(btnSampleRecipe2);
        btnSampleRecipe3 = new ImageButton(leftedge + tabarea.getX() + this.tabarea.getWidth() - 20,
                topedge + this.tabarea.getY() + this.tabarea.getHeight() - 26, 13, 13,
                13, 0, 13, GUI_BITMAP,
                GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 93);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        this.addRenderableWidget(btnSampleRecipe3);

        setCurrentCategoryDropdownValues();
        addParameterFields();
        setSelectedTab(0);
    }

    private void setSelectedTab(int i)
    {
        _selectedTab = i;
        tabItemEdit.setSelected(i == 0);
        tabRecipes.setSelected(i == 1);
        tabClipboard.setSelected(i == 2);

        scrollTags.visible = i == 0;
        scrollNBT.visible = i == 0;

        scrollCategory.visible = i == 1;
        scrollChildren.visible = i == 1;

        btnAddCount.visible = i == 0 && slotSelectedForTag != null && (!slotSelectedForTag.getCipherSlot().isSingle() || slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID);
        btnSubtractCount.visible = i == 0 && slotSelectedForTag != null && (!slotSelectedForTag.getCipherSlot().isSingle() || slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID);

        btnAddCountX10.visible = i == 0 && slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
        btnSubtractCountX10.visible = i == 0 && slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
        btnAddCountX100.visible = i == 0 && slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
        btnSubtractCountX100.visible = i == 0 && slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;

        btnClearOriginalRecipe.visible = i == 2;
        for (var b : _templateButtons)
            b.visible = i == 2;

        btnSampleRecipe1.visible = i == 2;
        btnSampleRecipe2.visible = i == 2;
        btnSampleRecipe3.visible = i == 2;
        chkIncludeComments.visible = i == 2;
        btnReloadJSON.visible = i == 2;
    }

    private void deleteNBTForSelectedSlotFromList(String s)
    {
        if (s == null) return;
        if (!s.contains("=")) return;
        var t = s.substring(0, s.indexOf("="));
        if (slotSelectedForTag != null)
        {
            var itemstack = slotSelectedForTag.getCipherSlot().getItemStack();
            if (!itemstack.isEmpty() && itemstack.getTag() != null)
            {
                for (var key : itemstack.getTag().getAllKeys())
                {
                    if (key.equals(t))
                    {
                        //itemstack.getTag().remove(key);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_REMOVE_NBT, slotSelectedForTag.getCipherSlot(), key);
                        slotSelectedForTag = null;
                        showEditPanel();
                        setSelectedTab(_selectedTab);
                        return;
                    }
                }
            }
        }
    }


    private void addSnippetButtons()
    {
        List<CipherTemplate> templates = container.getBlockEntity().getCipher().getTemplates();
        var y = topPos + this.tabarea.getY() + 20 + 6;
        for (CipherTemplate template : templates)
        {
            var b = Button.builder(Component.literal(template.getName()), (btn) -> {
                container.getBlockEntity().clipboardTemplate(template);
            }).bounds(leftPos + this.tabarea.getX() + 3, y, this.tabarea.getWidth() - 9, 20).build();
            y += 21;
            _templateButtons.add(b);
            this.addRenderableWidget(b);
        }
    }

    private int snippetsHeight()
    {
        return container.getBlockEntity().getCipher().getTemplates().size() * 21;
    }

    // adds parameter controls based on the current Cipher
    private void addParameterFields()
    {
        for (var eb : editBoxes.entrySet()) removeWidget(eb.getValue());
        for (var eb : comboBoxes.entrySet()) removeWidget(eb.getValue());
        editBoxes.clear();
        comboBoxes.clear();
        var cipher = container.getBlockEntity().getCipher();
        for (var p : cipher.getParameters())
        {
            if (p.getType().equals("text") || p.getType().equals("number"))
            {
                var box = new EditBox(this.font, leftPos + container.getInset().getX() + p.getPosX(), topPos + container.getInset().getY() + p.getPosY(),
                        p.getWidth(), 10, p.getLabel());
                box.setHint(p.getLabel());
                if (p.getType().equals("number")) box.setMaxLength(8);
                else box.setMaxLength(20);
                editBoxes.put(p.getPath(), box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntStringStringMessage(KubeJSTableBlockEntity.PARAMETER_SYNC, p.getPath(), text); // to server
                    }
                });
            }
            if (p.getType().equals("combo"))
            {
                var box = new ComboBox(leftPos + container.getInset().getX() + p.getPosX(),
                        topPos + container.getInset().getY() + p.getPosY()-3,
                        p.getWidth(), 16, Component.literal("XXX"), (a) -> {
                }, p.getComboOptions()
                );
                comboBoxes.put(p.getPath(), box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntStringStringMessage(KubeJSTableBlockEntity.PARAMETER_SYNC, p.getPath(), text); // to server
                    }
                });

            }
        }
        for (var eb : editBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var eb : comboBoxes.entrySet()) addRenderableWidget(eb.getValue());

        setParameterControlsFromBlock();
    }

    private void setParameterControlsFromBlock()
    {
        //Chatter.chat("setParameterControlsFromBlock");
        var be = this.container.getBlockEntity();
        var pars = be.getCipherParameters();
        _dontFireControlUpdates = true;
        for (var p : editBoxes.entrySet())
        {
            if (pars.containsKey(p.getKey())) p.getValue().setValue(pars.get(p.getKey()));
        }
        for (var p : comboBoxes.entrySet())
        {
            if (pars.containsKey(p.getKey())) p.getValue().setValue(pars.get(p.getKey()));
        }
        _dontFireControlUpdates = false;
    }

    private void setCurrentCategoryDropdownValues()
    {
        var categories = CipherJsonLoader.getSortedCipherCategoryNames();
        for (var i = 0; i < categories.size(); i++)
        {
            if (categories.get(i).equals(container.getBlockEntity().getCipher().getCategory()))
            {
                scrollCategory.setSelectedIndex(i, false);
                scrollChildren.setItems(CipherJsonLoader.getSortedCipherChildren(CipherJsonLoader.getSortedCipherCategoryNames().get(i)));
                break;
            }
        }
        var childCategories = CipherJsonLoader.getSortedCipherChildren(container.getBlockEntity().getCipher().getCategory());
        for (var i = 0; i < childCategories.size(); i++)
        {
            if (childCategories.get(i).getName().equals(container.getBlockEntity().getCipher().getName()))
            {
                scrollChildren.setSelectedIndex(i, false);
                break;
            }
        }
    }

    private void sendIntStringMessage(int control, String value)
    {
        //Chatter.chat("MSG:" + control + ":" + value);
        MessageRegistry.sendToServer(new EntityToServerIntStringMessage(minecraft.player.level(), container.getBlockEntity().getBlockPos(), control, value));
    }

    private void sendIntStringStringMessage(int control, String value1, String value2)
    {
        //Chatter.chat("MSG:" + control + ":" + value1 + ":" + value2);
        MessageRegistry.sendToServer(
                new EntityToServerIntStringStringMessage(minecraft.player.level(), container.getBlockEntity().getBlockPos(), control, value1, value2));
    }

    private void sendIntIntMessage(int control, int value)
    {
        //Chatter.chat("MSG:" + control + ":" + value);
        MessageRegistry.sendToServer(new EntityToServerIntIntMessage(minecraft.player.level(), container.getBlockEntity().getBlockPos(), control, value));
    }

    private void sendIntCipherMessage(int cipherMessage, CipherSlot cipher, String command)
    {
        MessageRegistry.sendToServer(
                new EntityC2SCipherCommand(minecraft.player.level(), container.getBlockEntity().getBlockPos(), cipherMessage, cipher, command));
    }

    // add exclusion zones to JEI
    public void addExclusions(List<Rect2i> exclusions)
    {
        //exclusions.add(torect(btnShowRecipeTypes.getRectangle()));
//        if (this.scrollCategory.visible) exclusions.add(torect(scrollCategory.getRectangle()));
//        if (this.scrollChildren.visible) exclusions.add(torect(scrollChildren.getRectangle()));
//        for (var b : _templateButtons)
//        {
//            exclusions.add(torect(b.getRectangle()));
//        }
        exclusions.add(torect(tabClipboard.getRectangle()));
        exclusions.add(torect(tabRecipes.getRectangle()));
        exclusions.add(torect(tabItemEdit.getRectangle()));
    }

    private Rect2i torect(ScreenRectangle rectangle)
    {
        return new Rect2i(rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height());
    }

    // this is sent from the server when the recipe changes.
    @Override
    public void updateCustomData(BlockPos blockpos, CompoundTag nbt)
    {
        if (container.getBlockEntity().getBlockPos().equals(blockpos))
        {
            _dontFireControlUpdates = true;
            for (var p : editBoxes.entrySet())
            {
                if (nbt.contains(p.getKey())) p.getValue().setValue(nbt.getString(p.getKey()));
            }
            for (var p : comboBoxes.entrySet())
            {
                if (nbt.contains(p.getKey())) p.getValue().setValue(nbt.getString(p.getKey()));
            }
            _dontFireControlUpdates = false;
        }
    }
}
