package pictisoft.cipherwright.blocks.kubejstable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.cipher.*;
import pictisoft.cipherwright.network.*;
import pictisoft.cipherwright.util.*;

import java.util.*;

public class KubeJSTableScreen extends AbstractContainerScreen<KubeJSTableContainer> implements SyncParameterTrigger.IHandleSyncParameterTrigger
{
    private static final int WELLPARA_TOP_MARGIN = 26;
    private static final int WELLPARA_Y_ADVANCE = 18;
    private static final int WELLPARA_HEIGHT = 12;
    private static final int WELLPARA_WIDTH = 70;
    private static final int PAGE_ITEM_EDIT = 0;
    private static final int PAGE_RECIPES = 1;
    private static final int PAGE_CLIPBOARD = 2;
    private Rect2i workArea;
    private Rect2i tabArea;
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
    private final Map<String, AdvancedEditBox> editBoxes = new LinkedHashMap<>();
    private final Map<String, ComboBox> comboBoxes = new LinkedHashMap<>();
    private final Map<String, CheckboxWithCallback> checkBoxes = new LinkedHashMap<>();
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
    private final int RIGHT_PANEL_WIDTH = 170;
    private final int TAB_BUTTON_WIDTH = RIGHT_PANEL_WIDTH / 3;
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
    private long _disableBtnReloadJSON;
    private ArrayList<AbstractWidget> wellParameterButtons = new ArrayList<>();

    public KubeJSTableScreen(KubeJSTableContainer container, Inventory inv, Component title)
    {
        super(container, inv, title);
        this.container = container;
        this.slice9 = new Slice9(GUI_BACKGROUND, 16);
        this.guiRenderer = new GUIElementRenderer(GUI_ELEMENTS);

        this.workArea = new Rect2i(0, 0, container.getFullSize().getWidth(), container.getFullSize().getHeight());
        this.tabArea = new Rect2i(this.workArea.getWidth(), 0, RIGHT_PANEL_WIDTH, this.workArea.getHeight());
        this.imageWidth = Math.max(9 * 18 + 16, this.workArea.getWidth()) + this.tabArea.getWidth();
        this.imageHeight = Math.max(this.workArea.getHeight(), this.tabArea.getHeight());
    }

    public KubeJSTableContainer getContainer()
    {
        return container;
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        if (Minecraft.getInstance().level != null && btnReloadJSON != null)
        {
            //prevent double clicking the /reload to prevent concurrent access crash
            btnReloadJSON.visible = Minecraft.getInstance().level.getGameTime() > (_disableBtnReloadJSON + 200) && _selectedTab == 2;
        }
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
                for (var eb : checkBoxes.entrySet())
                {
                    if (eb.getKey().equals(kvp.getKey()))
                    {
                        // skip if the textbox is focused. This can cause problems in rare cases (recipe changes from a second user while user is typing).. but... should be very rare
                        var truthValue = kvp.getValue().equals("true");
                        if (!eb.getValue().isFocused() && eb.getValue().selected() != truthValue)
                        {
                            eb.getValue().setSelected(truthValue);
                        }
                    }
                }

                // if selected slot, sync the wellparameters to the text boxes
                if (slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().getCipherobject() instanceof CipherWell well)
                {
                    // the wellparameter config are attached to the Well, as a list of CipherParameters
                    // the wellparameter values are attached to the Slot, as a CompoundTag, indexed by "path"
                    // the widgets are listed in order of the wellparameter config list
                    ArrayList<CipherParameter> wellParameters = well.getWellParameters();
                    for (int i = 0; i < wellParameters.size(); i++)
                    {
                        if (wellParameterButtons.size() > i)
                        {
                            var parameter = wellParameters.get(i);
                            var val = slotSelectedForTag.getCipherSlot().getWellParameter(parameter);
                            var button = wellParameterButtons.get(i);
                            if (button instanceof AdvancedEditBox editbox)
                                if (!editbox.getValue().equals(val))
                                    editbox.setValue(val);
                            if (button instanceof ComboBox bombobox)
                                if (!bombobox.getValue().equals(val))
                                    bombobox.setValue(val);
                        }
                    }
                }
            }
        }
        if (chkIncludeComments != null)
        {
            chkIncludeComments.setSelected(container.getBlockEntity().areCommentsIncluded());
        }
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
        pGuiGraphics.drawString(this.font,
                Component.literal(
                        container.getBlockEntity().getCipher().getCategory() +
                                " : "
                                + container.getBlockEntity().getCipher().getName())
                ,
                this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY)
    {
        this.renderBackground(gui);

        _atTick += partialTicks;
        //Chatter.chat(String.format("%d %d vs %d %d", this.leftPos, this.topPos, this.stringScrollPanel.getRectangle().left(),this.stringScrollPanel.getRectangle().top() ));

        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        slice9.drawNineSlice(gui, relX, relY, this.imageWidth, this.imageHeight);

        // render illustrations
        gui.pose().pushPose();
        gui.pose().translate(workArea.getX(), workArea.getY(), 0);
        for (var illustration : container.getBlockEntity().getCipher().getIllustrations())
        {
            illustration.render(gui, guiRenderer, this.container.getBlockEntity().getCipherParameters());
        }
        gui.pose().popPose();
        gui.pose().pushPose();


        if (_selectedTab == PAGE_ITEM_EDIT && scrollTags != null && scrollNBT != null)
        {
            gui.drawString(this.font, Component.literal("Convert to tag:"), scrollTags.getRectangle().left() + 2, scrollTags.getRectangle().top() - 10,
                    0x404040, false);
            gui.drawString(this.font, Component.literal("Remove NBT:"), scrollNBT.getRectangle().left() + 2, scrollNBT.getRectangle().top() - 10, 0x404040,
                    false);
        }

        // int left = (width - imageWidth) / 2;
        // int top = (height - imageHeight) / 2;

        gui.pose().pushPose();
        gui.pose().translate(leftPos, topPos, 0); // these are window aligned
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
        gui.pose().translate(workArea.getX(), workArea.getY(), 0);
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
                gui.pose().translate(workArea.getX(), workArea.getY(), 0);
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
        gui.pose().translate(workArea.getX(), workArea.getY(), 0);

        for (var parameter : container.getBlockEntity().getCipher().getParameters())
        {
            // draw a label for textboxes/comboboxes
            parameter.render(gui, guiRenderer);
        }
        gui.pose().popPose();
        gui.pose().pushPose();
        //gui.pose().translate(workArea.getX(), workArea.getY(), 0);
        for (var parameter : container.getBlockEntity().getCipherSlots())
        {
            var y = tabArea.getY() + WELLPARA_TOP_MARGIN;
            var x = tabArea.getX();
            if (parameter.getCipherobject() instanceof CipherWell well)
            {
                ArrayList<CipherParameter> wellParameters = well.getWellParameters();
                for (int i = 0; i < wellParameters.size(); i++)
                {
                    var wellpara = wellParameters.get(i);
                    if (wellParameterButtons.size() > i)
                    {
                        var box = wellParameterButtons.get(i);
                        if (box.visible)
                        {
                            wellpara.render(gui, guiRenderer, x, y, WELLPARA_WIDTH);
                        }
                    }
                    y += WELLPARA_Y_ADVANCE;

                }
            }
        }
        gui.pose().popPose();

        // render original recipe id
        if (btnClearOriginalRecipe != null)
        {
            if (btnClearOriginalRecipe.visible && container.getBlockEntity().getOriginalRecipeID() != null)
            {
                gui.pose().pushPose();
                var leftedge = btnClearOriginalRecipe.getX() - 4;
                var topedge = btnClearOriginalRecipe.getY() + 4;
                var str = container.getBlockEntity().getOriginalRecipeID().toString();
                var w = Math.min(tabArea.getWidth() + 20, this.font.width(str));
                gui.enableScissor(leftedge - w, topedge, leftedge, topedge + 10);
                gui.pose().translate(leftedge, topedge, 0);
                gui.pose().scale(.75f, .75f, 1);
                gui.pose().translate(-w, 0, 0);
                gui.drawString(this.font, Component.literal(str), 0, 0, 0x404040, false);
                //btnClearOriginalRecipe.visible = true;
                gui.disableScissor();
                gui.pose().popPose();
            } else
            {
                btnClearOriginalRecipe.visible = false;
            }
        }

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

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY)
    {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null)
        {
            if (this.hoveredSlot instanceof CipherSlotProxy cs)
            {
                if (cs.getCipherSlot().getMode() == CipherSlot.SlotMode.ITEM)
                {
                    var itemstack = cs.getCipherSlot().getItemStack();
                    if (!itemstack.isEmpty())
                        gui.renderTooltip(font, getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, mouseX, mouseY);
                }
                if (cs.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID)
                {
                    var fluidStack = cs.getCipherSlot().getFluid();
                    if (!fluidStack.isEmpty())
                        gui.renderTooltip(font, getTooltipFromContainerFluid(fluidStack), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
                }
                if (cs.getCipherSlot().getMode() == CipherSlot.SlotMode.TAG)
                {
                    var tag = cs.getCipherSlot().getTag();
                    if (tag != null)
                        gui.renderTooltip(font, getTooltipFromContainerTag(tag), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
                }
            }
        }
    }

    private List<Component> getTooltipFromContainerTag(TagKey<Item> tag)
    {
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy cipherslotProxy)
        {
            if (cipherslotProxy.getCipherSlot().getMode() == CipherSlot.SlotMode.TAG)
            {
                var tooltip = new ArrayList<Component>();
                tooltip.add(Component.literal("Tag"));
                tooltip.add(Component.literal("#" + cipherslotProxy.getCipherSlot().getTag().location().toString()));

                return tooltip;
            }
        }
        return List.of();
    }

    protected @NotNull List<Component> getTooltipFromContainerItem(ItemStack pStack)
    {
        if (this.minecraft != null)
        {
            return getTooltipFromItem(this.minecraft, pStack);
        }
        return List.of();
    }

    private List<Component> getTooltipFromContainerFluid(FluidStack fluidStack)
    {
        if (this.minecraft != null)
        {
            var tooltip = new ArrayList<Component>();
            tooltip.add(fluidStack.getDisplayName());
            tooltip.add(Component.literal(fluidStack.getAmount() + " mB"));
            var modID = BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString();
            tooltip.add(Component.literal("§8" + modID));
            return tooltip;
        }
        return List.of();
    }


    // #START MOUSE

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) // MOUSEPIPE
    {
//        if (scrollCategory.mouseClicked(mouseX,mouseY,button))
//        {
//            Chatter.chat("scrollCategory");
//            return true;
//        }
//        if (scrollChildren.mouseClicked(mouseX,mouseY,button))
//        {
//            Chatter.chat("scrollChildren");
//            return true;
//        }
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy proxy)
        {
            if (button == 0)
            {
                _dragslots.clear();
                _dragslots.add(proxy);
                clearSelectedSlot();
                return true;
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
                    setSelectedSlot(proxy);
                    return true;
                } else
                {
                    clearSelectedSlot();
                    return true;
                }
            }
        }
        return (super.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY)
    {
        //Chatter.chat("screen-drag");
        if (container.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        //Chatter.chat("screen-drag-2");
        //Chatter.chat("screen-drag-3");
//        if (scrollCategory != null && scrollCategory.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
//        if (scrollChildren != null && scrollChildren.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy proxy && pButton == 0 && !_dragslots.contains(proxy))
        {
            _dragslots.add(proxy);
        }
        if (this.children().contains(scrollChildren) && scrollChildren.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (this.children().contains(scrollCategory) &&scrollCategory.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (this.children().contains(scrollTags) &&scrollTags.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (this.children().contains(scrollNBT) &&scrollNBT.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) // MOUSEPIPE
    {
        Chatter.chat("mouseReleased");
        //_mousedown = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    // #END MOUSE

    private void setSelectedSlot(CipherSlotProxy slot)
    {
        slotSelectedForTag = slot;
        buildScrollListsForSelectedSlot();
        if (slot.getCipherSlot().getCipherobject() instanceof CipherWell well)
            addWellParameterButtons(well);
        setSelectedTab(0);
    }

    // do not call other functions as it may recurse
    private void clearSelectedSlot()
    {
        slotSelectedForTag = null;
        removeWellParameterButtons();
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setSelectedIndex(-1, false);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setSelectedIndex(-1, false);
        setSelectedTab(_selectedTab);
    }

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
            clearSelectedSlot();
        }
    }

    Collection<TagKey<Item>> getTagsForItemStack(ItemStack itemstack)
    {
        var item = itemstack.getItem();
        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(item).orElse(null);
        if (itemKey == null) return new ArrayList<>();
        return BuiltInRegistries.ITEM.getHolderOrThrow(itemKey).tags().toList();
    }

    private void buildScrollListsForSelectedSlot()
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
            clearSelectedSlot();
        }

    }

    @Override
    protected void rebuildWidgets()
    {
        super.rebuildWidgets();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void init()
    {
        super.init();

        var leftEdge = this.width / 2 - this.imageWidth / 2;
        var topEdge = this.height / 2 - this.imageHeight / 2;
        workArea = new Rect2i(leftEdge + container.getInset().getX(), topEdge + container.getInset().getY(),
                container.getFullSize().getWidth(), container.getFullSize().getHeight());
        tabArea = new Rect2i(leftEdge + workArea.getWidth(),
                topEdge //+ container.getInset().getY(),
                , RIGHT_PANEL_WIDTH,
                this.workArea.getHeight());

        makeTabButtons();
        makeWorkareaButtons();
        makeItemEditButtons();
        makeRecipeButtons();
        makeClipboardButtons();

        setCurrentCategoryDropdownValues();
        addParameterFields();
        addRenderables();
    }

    private void addRenderables()
    {
        this.addRenderableWidget(tabItemEdit);
        this.addRenderableWidget(tabRecipes);
        this.addRenderableWidget(tabClipboard);

        this.addRenderableWidget(btnClearScreen);

        if (_selectedTab == PAGE_ITEM_EDIT)
        {
            addRenderableWidget(scrollTags);
            addRenderableWidget(scrollNBT);
            addRenderableWidget(btnSubtractCount);
            addRenderableWidget(btnAddCount);
            addRenderableWidget(btnSubtractCountX10);
            addRenderableWidget(btnAddCountX10);
            addRenderableWidget(btnSubtractCountX100);
            addRenderableWidget(btnAddCountX100);
        }
        if (_selectedTab == PAGE_RECIPES)
        {
            addRenderableWidget(scrollCategory);
            addRenderableWidget(scrollChildren);
        }
        if (_selectedTab == PAGE_CLIPBOARD)
        {
            addRenderableWidget(btnClearOriginalRecipe);
            addRenderableWidget(chkIncludeComments);
            addRenderableWidget(btnReloadJSON);
            addRenderableWidget(btnSampleRecipe1);
            addRenderableWidget(btnSampleRecipe2);
            addRenderableWidget(btnSampleRecipe3);
            for (var b : _templateButtons)
                addRenderableWidget(b);
        }

        for (var eb : editBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var eb : comboBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var eb : checkBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var wpb : wellParameterButtons)
            addRenderableWidget(wpb);

    }

    private void makeItemEditButtons()
    {
        var temp = getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * WELLPARA_HEIGHT
                + getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * (WELLPARA_Y_ADVANCE - 1);
        temp += (temp > 0 ? 2 : 0); // spacing

        var remaining = Math.max(32, (tabArea.getHeight() - 30 - temp) / 2);
        var ystack = tabArea.getY() + 23 + temp;

        scrollTags = new ItemScrollPanel<>(tabArea.getWidth() - 6, remaining - 16, ystack + 16, tabArea.getX(), (a) -> a);
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setOnClick((idx) -> {
            setTagModeForSelectedSlotFromList(idx);
            buildScrollListsForSelectedSlot(); // reload and hide if neccessary
        });

        ystack += remaining;

        scrollNBT = new ItemScrollPanel<>(tabArea.getWidth() - 6, remaining - 16, ystack + 16, scrollTags.getRectangle().left(), (a) -> a);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setOnClick((idx) -> {
            //setTagModeForSelectedSlotFromList(idx);
            deleteNBTForSelectedSlotFromList(scrollNBT.getItems().get(idx));
            buildScrollListsForSelectedSlot();
        });
        btnSubtractCount = Button.builder(Component.literal("-"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-1");
            }
        }).bounds(tabArea.getX() + 6, tabArea.getY() + 6, 16, 16).build();

        btnAddCount = Button.builder(Component.literal("+"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "1");
            }
        }).bounds(btnSubtractCount.getX() + btnSubtractCount.getWidth() + 2, tabArea.getY() + 6, 16, 16).build();

        btnSubtractCountX10 = Button.builder(Component.literal("-10"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-10");
            }
        }).bounds(btnAddCount.getX() + btnAddCount.getWidth() + 2, tabArea.getY() + 6, 25, 16).build();

        btnAddCountX10 = Button.builder(Component.literal("+10"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "10");
            }
        }).bounds(btnSubtractCountX10.getX() + btnSubtractCountX10.getWidth() + 2, tabArea.getY() + 6, 25, 16).build();


        btnSubtractCountX100 = Button.builder(Component.literal("-100"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "-100");
            }
        }).bounds(btnAddCountX10.getX() + btnAddCountX10.getWidth() + 2, tabArea.getY() + 6, 32, 16).build();

        btnAddCountX100 = Button.builder(Component.literal("+100"), (btn) -> {
            if (slotSelectedForTag != null)
            {
                //slotSelectedForTag.getCipherSlot().adjustCount(1);
                sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(), "100");
            }
        }).bounds(btnSubtractCountX100.getX() + btnSubtractCountX100.getWidth() + 2, tabArea.getY() + 6, 32, 16).build();

    }

    private void makeRecipeButtons()
    {
        var temp = getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * WELLPARA_HEIGHT
                + getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * (WELLPARA_Y_ADVANCE - 1);
        temp += (temp > 0 ? 2 : 0); // spacing


        var remaining = Math.max(32, (tabArea.getHeight() - 10) / 2);
        var ystack = tabArea.getY() + 6;

        scrollCategory = new ItemScrollPanel<>(tabArea.getWidth() - 10, remaining - 3, ystack, tabArea.getX() + 5, (a) -> a);
        scrollCategory.setItems(CipherJsonLoader.getSortedCipherCategoryNames());
        scrollCategory.setOnClick((idx) -> {
            scrollChildren.setItems(CipherJsonLoader.getSortedCipherChildren(CipherJsonLoader.getSortedCipherCategoryNames().get(idx)));
        });
        ystack += remaining;

        scrollChildren = new ItemScrollPanel<>(tabArea.getWidth() - 10, remaining, ystack, tabArea.getX() + 5, Cipher::getName);
        scrollChildren.setOnClick((idx) -> {
            //Chatter.chat("client sending : " + (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
            sendIntStringMessage(KubeJSTableBlockEntity.RECIPE_TYPE_SCROLLBOX, (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
        });

    }

    private void makeClipboardButtons()
    {
        btnClearOriginalRecipe = new ImageButton(tabArea.getX() + tabArea.getWidth() - 20, tabArea.getY() + 6, 13, 13, 13, 0, 13, GUI_BITMAP, GUI_BITMAP_W,
                GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.ORIGINAL_RECIPE_CLEAR_ON_SERVER, 0);
            btnClearOriginalRecipe.setFocused(false);
            this.setFocused(null);
        });

        chkIncludeComments = new CheckboxWithCallback(tabArea.getX() + 6, tabArea.getY() + tabArea.getHeight() - 32, 16, 16, Component.literal("Comments?"),
                true, true);
        chkIncludeComments.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_COMMENT_PARAMETER, 0);
        };

        btnReloadJSON = Button.builder(Component.literal("/reload"), (btn) -> {
            if (Minecraft.getInstance().level != null)
            {
                if (Minecraft.getInstance().level.getGameTime() < _disableBtnReloadJSON + 200) return;
                _disableBtnReloadJSON = Minecraft.getInstance().level.getGameTime();
            }
            sendIntIntMessage(KubeJSTableBlockEntity.RUN_COMMAND, KubeJSTableBlockEntity.RUN_COMMAND_RELOAD);
        }).bounds(tabArea.getX() + tabArea.getWidth() - 72, tabArea.getY() + tabArea.getHeight() - 50, 66, 16).build();

        btnSampleRecipe1 = new ImageButton(tabArea.getX() + tabArea.getWidth() - 36 - 16, tabArea.getY() + tabArea.getHeight() - 26, 13, 13, 13, 0, 13,
                GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 91);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        btnSampleRecipe2 = new ImageButton(tabArea.getX() + this.tabArea.getWidth() - 36, tabArea.getY() + tabArea.getHeight() - 26, 13, 13, 13, 0, 13,
                GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 92);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });
        btnSampleRecipe3 = new ImageButton(tabArea.getX() + tabArea.getWidth() - 20, tabArea.getY() + tabArea.getHeight() - 26, 13, 13, 13, 0, 13, GUI_BITMAP,
                GUI_BITMAP_W, GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 93);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });

        List<CipherTemplate> templates = container.getBlockEntity().getCipher().getTemplates();
        var y = tabArea.getY() + 20 + 6;
        for (CipherTemplate template : templates)
        {
            var b = Button.builder(Component.literal(template.getName()), (btn) -> {
                container.getBlockEntity().clipboardTemplate(template);
            }).bounds(tabArea.getX() + 3, y, this.tabArea.getWidth() - 9, 20).build();
            y += 21;
            _templateButtons.add(b);
        }


    }

    private void makeWorkareaButtons()
    {
        btnClearScreen = new ImageButton(workArea.getX() + workArea.getWidth() - 32, workArea.getY() - 13, 13, 13, 13, 0, 13, GUI_BITMAP, GUI_BITMAP_W,
                GUI_BITMAP_H, (btn) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 1);
            btnClearScreen.setFocused(false);
            this.setFocused(null);
        });

    }

    private void removeWellParameterButtons()
    {
        for (var w : wellParameterButtons)
            removeWidget(w);
        wellParameterButtons.clear();
    }

    private void addWellParameterButtons(CipherWell well)
    {
        removeWellParameterButtons();
        var y = tabArea.getY() + WELLPARA_TOP_MARGIN;
        var x = tabArea.getX();
        for (var p : well.getWellParameters())
        {
            if (p.getType().equals("text") || p.getType().equals("number"))
            {
                var box = new AdvancedEditBox(this.font, x, y, WELLPARA_WIDTH, WELLPARA_HEIGHT, p.getLabel(), p.getFlags().contains("right"));
                if (p.getHint() != null && !p.getHint().getString().isEmpty())
                    box.setHint(p.getHint());
                if (p.getType().equals("number")) box.setMaxLength(8);
                else box.setMaxLength(20);
                if (slotSelectedForTag != null)
                {
                    box.setValue(slotSelectedForTag.getCipherSlot().getWellParameter(p));
                }
                wellParameterButtons.add(box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates && slotSelectedForTag != null)
                    {
                        sendIntCipherMessage(KubeJSTableBlockEntity.WELL_PARAMETER_SYNC, slotSelectedForTag.getCipherSlot(),
                                p.getPath() + ":" + box.getValue()); // to server
                    }
                });
            }
            if (p.getType().equals("combo"))
            {
                var box = new ComboBox(x, y, WELLPARA_WIDTH, WELLPARA_HEIGHT, p.getComboOptions());
                box.setLabel(p.getLabel(), p.getFlags().contains("right"));
                wellParameterButtons.add(box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntCipherMessage(KubeJSTableBlockEntity.WELL_PARAMETER_SYNC, slotSelectedForTag.getCipherSlot(),
                                p.getPath() + ":" + box.getValue()); // to server
                    }
                });
            }
            if (p.getType().equals("boolean"))
            {
                var a = new ArrayList<String>();
                a.add("false");
                a.add("true");
                var box = new ComboBox(x, y, WELLPARA_WIDTH, WELLPARA_HEIGHT, a);
                box.setLabel(p.getLabel(), p.getFlags().contains("right"));
                wellParameterButtons.add(box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntCipherMessage(KubeJSTableBlockEntity.WELL_PARAMETER_SYNC, slotSelectedForTag.getCipherSlot(),
                                p.getPath() + ":" + box.getValue()); // to server
                    }
                });
            }
            y += WELLPARA_Y_ADVANCE;
        }
        setSelectedTab(_selectedTab); // refresh visibility
    }


    private void makeTabButtons()
    {
        tabItemEdit = new FakeTabButton(tabArea.getX(), tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Item"), (a) -> {
            this.setSelectedTab(PAGE_ITEM_EDIT);
        });
        tabItemEdit.setSelected(_selectedTab == PAGE_ITEM_EDIT);

        tabRecipes = new FakeTabButton(tabArea.getX() + TAB_BUTTON_WIDTH, tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Recipe"), (a) -> {
            this.setSelectedTab(PAGE_RECIPES);
        });
        tabRecipes.setSelected(_selectedTab == PAGE_RECIPES);

        tabClipboard = new FakeTabButton(tabArea.getX() + TAB_BUTTON_WIDTH * 2, tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Clipboard"),
                (a) -> {
                    this.setSelectedTab(PAGE_CLIPBOARD);
                });
        tabClipboard.setSelected(_selectedTab == PAGE_CLIPBOARD);
    }

    private void setSelectedTab(int i)
    {
        _selectedTab = i;
        rebuildWidgets();

        if (_selectedTab == PAGE_ITEM_EDIT)
        {
            btnAddCount.visible = slotSelectedForTag != null && (!slotSelectedForTag.getCipherSlot().isSingle() || slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID);
            btnSubtractCount.visible = slotSelectedForTag != null && (!slotSelectedForTag.getCipherSlot().isSingle() || slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID);
            btnAddCountX10.visible = slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
            btnSubtractCountX10.visible = slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
            btnAddCountX100.visible = slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
            btnSubtractCountX100.visible = slotSelectedForTag != null && slotSelectedForTag.getCipherSlot().canBeFluid() && slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID;
        }

        for (var b : _templateButtons)
            b.visible = i == 2;
        for (var b : wellParameterButtons)
            b.visible = i == 0;
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
                        clearSelectedSlot();
                        return;
                    }
                }
            }
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
        for (var eb : checkBoxes.entrySet()) removeWidget(eb.getValue());
        editBoxes.clear();
        comboBoxes.clear();
        checkBoxes.clear();
        var cipher = container.getBlockEntity().getCipher();
        for (var p : cipher.getParameters())
        {
            if (p.getType().equals("text") || p.getType().equals("number"))
            {
                var box = new AdvancedEditBox(this.font, workArea.getX() + p.getPosX(), workArea.getY() + p.getPosY(), p.getWidth(), p.getHeight(),
                        p.getLabel(), p.getFlags().contains("right"));
                if (p.getHint() != null && !p.getHint().getString().isEmpty())
                    box.setHint(p.getHint());
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
                var box = new ComboBox(workArea.getX() + p.getPosX(), workArea.getY() + p.getPosY() - 3, p.getWidth(), p.getHeight(), p.getComboOptions());
                box.setLabel(p.getLabel(), p.getFlags().contains("right"));
                comboBoxes.put(p.getPath(), box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntStringStringMessage(KubeJSTableBlockEntity.PARAMETER_SYNC, p.getPath(), text); // to server
                    }
                });

            }
            if (p.getType().equals("boolean"))
            {
                var box = new CheckboxWithCallback(workArea.getX() + p.getPosX(), workArea.getY() + p.getPosY(), 16, 16,
                        p.getLabel(),
                        false, p.getFlags().contains("right"));
                checkBoxes.put(p.getPath(), box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntStringStringMessage(KubeJSTableBlockEntity.PARAMETER_SYNC, p.getPath(), text ? "true" : "false"); // to server
                    }
                });

            }
        }
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
        for (var p : checkBoxes.entrySet())
        {
            if (pars.containsKey(p.getKey())) p.getValue().setSelected(pars.get(p.getKey()).equals("true"));
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
        MessageRegistry.sendToServer(
                new EntityToServerIntStringMessage(minecraft.player.level(), container.getBlockEntity().getBlockPos(), control, value));
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
        if (tabClipboard != null) exclusions.add(torect(tabClipboard.getRectangle()));
        if (tabRecipes != null) exclusions.add(torect(tabRecipes.getRectangle()));
        if (tabItemEdit != null) exclusions.add(torect(tabItemEdit.getRectangle()));
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
            for (var p : checkBoxes.entrySet())
            {
                if (nbt.contains(p.getKey())) p.getValue().setSelected(nbt.getString(p.getKey()).equals("true"));
            }
            _dontFireControlUpdates = false;
        }
    }
}
