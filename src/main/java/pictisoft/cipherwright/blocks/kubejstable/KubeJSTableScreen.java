package pictisoft.cipherwright.blocks.kubejstable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.cipher.*;
import pictisoft.cipherwright.gui.*;
import pictisoft.cipherwright.integration.jei.JEIPlugin;
import pictisoft.cipherwright.network.*;
import pictisoft.cipherwright.util.*;

import java.util.*;

public class KubeJSTableScreen extends AbstractContainerScreen<KubeJSTableContainer> implements SyncParameterTrigger.IHandleSyncParameterTrigger
{
    private static final int WELLPARA_TOP_MARGIN = 22;
    private static final int WELLPARA_Y_ADVANCE = 18;
    private static final int WELLPARA_HEIGHT = 12;
    private static final int WELLPARA_WIDTH = 70;
    private static final int PAGE_ITEM_EDIT = 0;
    private static final int PAGE_RECIPES = 1;
    private static final int PAGE_CLIPBOARD = 2;
    private static final int PAGE_SETTINGS = 3;
    private static final int GUI_BUTTON_W = 13;
    private static final int GUI_BUTTON_H = 13;
    private static final int GUI_CLEAR_X = 13;
    private static final int GUI_CLEAR_Y = 0;
    private static final int GUI_COPY_X = 26;
    private static final int GUI_COPY_Y = 0;
    private static final int GUI_PASTE_X = 39;
    private static final int GUI_PASTE_Y = 0;
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
    private static final int GUI_BITMAP_W = 64;
    private static final int GUI_BITMAP_H = 64;
    private boolean _dontFireControlUpdates;
    private final ArrayList<Button> _templateButtons = new ArrayList<>();
    private final ArrayList<CipherSlotProxy> _dragslots = new ArrayList<>();
    private int _selectedTab;
    private Button btnClearOriginalRecipe;
    private Button btnCopyOriginalRecipe;
    private Button btnPasteOriginalRecipe;
    private final int RIGHT_PANEL_WIDTH = 170;
    private final int TAB_SETTINGS_WIDTH = 20;
    private final int TAB_BUTTON_WIDTH = (RIGHT_PANEL_WIDTH - TAB_SETTINGS_WIDTH + 40) / 3;
    private FakeTabButton tabItemEdit;
    private FakeTabButton tabRecipes;
    private FakeTabButton tabClipboard;
    private FakeTabButton tabSettings;
    private Button btnAddCount;
    private Button btnSubtractCount;
    private CheckboxWithCallback chkIncludeComments;
    private CheckboxWithCallback chkIncludeWeakNBT;
    private CheckboxWithCallback chkFormatCode;
    private CheckboxWithCallback chkNeverRemove;
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
        if (chkNeverRemove != null)
        {
            chkNeverRemove.setSelected(container.getBlockEntity().areRemoveRecipe());
        }
        if (chkIncludeWeakNBT != null)
        {
            chkIncludeWeakNBT.setSelected(container.getBlockEntity().areWeakNBTIncluded());
        }
        if (chkFormatCode != null)
        {
            chkFormatCode.setSelected(container.getBlockEntity().areCodeFormatted());
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
        pGuiGraphics.enableScissor(leftPos, topPos, leftPos + workArea.getWidth() - 30, topPos + 30);
        pGuiGraphics.drawString(this.font,
                Component.literal(
                        container.getBlockEntity().getCipher().getCategory() +
                                " : "
                                + container.getBlockEntity().getCipher().getName())
                ,
                this.titleLabelX, this.titleLabelY, 0x404040, false);
        pGuiGraphics.disableScissor();
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
            // draw non-slots
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
            if (slot.getStyle() == CipherWell.STYLE_ENUM.large)
            {
                guiRenderer.drawSlotWellLarge(gui, slot.getX(), slot.getY());
            } else if (slot.getStyle() == CipherWell.STYLE_ENUM.bucket)
            {
                guiRenderer.drawSlotWellBucket(gui, slot.getX(), slot.getY());

            } else
            {
                guiRenderer.drawSlotWell(gui, slot.getX(), slot.getY());
            }
            if (slot.getCipherobject() instanceof CipherWell well && slot.isEmpty())
            {
                well.drawBackGround(gui, guiRenderer);
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
                    gui.blit(GUI_BITMAP, 0, 0, 8, 52, 4, 5, GUI_BITMAP_W, GUI_BITMAP_H);
                    RenderSystem.enableDepthTest();
                }
                if (slot.getMode() == CipherSlot.SlotMode.TAG)
                {
                    // render tag indicator
                    RenderSystem.disableDepthTest();
                    gui.pose().translate(slot.getX() + 12, slot.getY() + 12, 0);
                    gui.blit(GUI_BITMAP, 0, 0, 0, 52, 4, 4, GUI_BITMAP_W, GUI_BITMAP_H);
                    RenderSystem.enableDepthTest();
                }
                if (slot.getMode() == CipherSlot.SlotMode.FLUIDTAG)
                {
                    // render tag indicator
                    RenderSystem.disableDepthTest();
                    gui.pose().translate(slot.getX() + 12, slot.getY() + 12, 0);
                    gui.blit(GUI_BITMAP, 0, 0, 4, 52, 4, 4, GUI_BITMAP_W, GUI_BITMAP_H);
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
        if (btnClearOriginalRecipe != null && children().contains(btnClearOriginalRecipe))
        {
            if (container.getBlockEntity().getOriginalRecipeID() != null)
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
                btnClearOriginalRecipe.visible = true;
                btnCopyOriginalRecipe.active = true;
            } else
            {
                btnClearOriginalRecipe.visible = false;
                btnCopyOriginalRecipe.active = false;
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
                    var fluidStack = cs.getCipherSlot().getFluidStack();
                    if (!fluidStack.isEmpty())
                        gui.renderTooltip(font, getTooltipFromContainerFluid(fluidStack), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
                }
                if (cs.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUIDTAG)
                {
                    var tag = cs.getCipherSlot().getFluidTag();
                    if (tag != null)
                        gui.renderTooltip(font, getTooltipFromContainerFluidTag(tag), Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
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

    private List<Component> getTooltipFromContainerFluidTag(TagKey<Fluid> tag)
    {
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy cipherslotProxy)
        {
            if (cipherslotProxy.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUIDTAG)
            {
                var tooltip = new ArrayList<Component>();
                tooltip.add(Component.literal("Fluid Tag"));
                tooltip.add(Component.literal("#" + cipherslotProxy.getCipherSlot().getFluidTag().location().toString()));

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
        Slot hoveredSlot = getSlotUnderMouse();
        if (hoveredSlot instanceof CipherSlotProxy proxy)
        {
            if (button == 0)
            {
                _dragslots.clear();
                _dragslots.add(proxy);
                clearSelectedSlot();
                return (super.mouseClicked(mouseX, mouseY, button));
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
        if (this.children().contains(scrollCategory) && scrollCategory.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (this.children().contains(scrollTags) && scrollTags.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (this.children().contains(scrollNBT) && scrollNBT.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) // MOUSEPIPE
    {
        //Chatter.chat("mouseReleased");
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
        setSelectedTab(0); // calls setButtonVisibilities
    }

    // do not call other functions as it may recurse
    // clears out the Selected Slot indicator and related things
    private void clearSelectedSlot()
    {
        slotSelectedForTag = null;
        removeWellParameterButtons();
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setSelectedIndex(-1, false);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setSelectedIndex(-1, false);
        setSelectedTab(_selectedTab); // calls setButtonVisibilities
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
            return;
        }
        if (slotSelectedForTag.getCipherSlot().canBeFluidTag())
        {
            var tags = getTagsForFluidStack(slotSelectedForTag.getCipherSlot().getFluidStack());
            if (!tags.isEmpty())
            {
                if (tags.stream().toList().size() > idx)
                {
                    var r = tags.stream().toList().get(idx).location().toString();
                    //slotSelectedForTag.getCipherSlot().setTagKey(tags.stream().toList().get(idx));
                    sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_CHANGE_TO_FLUID_TAG, slotSelectedForTag.getCipherSlot(), r);
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
        scrollTags.setDisabled(true);
        if (slotSelectedForTag != null)
        {
            var itemstack = slotSelectedForTag.getCipherSlot().getItemStack();
            var fluidstack = slotSelectedForTag.getCipherSlot().getFluidStack();
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
                scrollTags.setDisabled(!slotSelectedForTag.getCipherSlot().canBeTag());
            }
            if (!fluidstack.isEmpty())
            {
                var tags = getTagsForFluidStack(fluidstack);
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
                scrollTags.setDisabled(!slotSelectedForTag.getCipherSlot().canBeFluidTag());
            }
        } else
        {
            scrollTags.setItems(new ArrayList<>());
            scrollNBT.setItems(new ArrayList<>());
        }

    }

    Collection<TagKey<Fluid>> getTagsForFluidStack(FluidStack fluidStack)
    {
        var fluid = fluidStack.getFluid();
        ResourceKey<Fluid> fluidKey = BuiltInRegistries.FLUID.getResourceKey(fluid).orElse(null);
        if (fluidKey == null) return new ArrayList<>();
        return BuiltInRegistries.FLUID.getHolderOrThrow(fluidKey).tags().toList();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Let super handle text boxes etc.
        if (super.keyPressed(keyCode, scanCode, modifiers))
        {
            return true;
        }
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        Minecraft mc = Minecraft.getInstance();

//        // Mouse position in *window* coordinates
//        double mouseX = mc.mouseHandler.xpos();
//        double mouseY = mc.mouseHandler.ypos();
//
//        // Convert to scaled GUI coordinates
//        double guiMouseX = mouseX * (double) this.width / (double) mc.getWindow().getScreenWidth();
//        double guiMouseY = mouseY * (double) this.height / (double) mc.getWindow().getScreenHeight();

        var hovered = getSlotUnderMouse();
        // Check JEI’s registered key mappings
        if (hovered != null && hovered instanceof CipherSlotProxy proxy)
        {
            if (proxy.getCipherSlot().getMode() == CipherSlot.SlotMode.ITEM)
            {
                if (JEIPlugin.getShowUses().isActiveAndMatches(mouseKey))    // U. show inputs
                {
                    JEIPlugin.showRecipes(proxy.getCipherSlot().getItemStack(), false);
                    return true; // consumed
                }
                if (JEIPlugin.getShowRecipe().isActiveAndMatches(mouseKey))  // R. show outputs
                {
                    JEIPlugin.showRecipes(proxy.getCipherSlot().getItemStack(), true);
                    return true; // consumed
                }
            }
            if (proxy.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID)
            {
                if (JEIPlugin.getShowUses().isActiveAndMatches(mouseKey))    // U. show inputs
                {
                    JEIPlugin.showRecipes(proxy.getCipherSlot().getFluidStack(), false);
                    return true; // consumed
                }
                if (JEIPlugin.getShowRecipe().isActiveAndMatches(mouseKey))  // R. show outputs
                {
                    JEIPlugin.showRecipes(proxy.getCipherSlot().getFluidStack(), true);
                    return true; // consumed
                }
            }
        }
        return false;
    }

    @Override
    protected void rebuildWidgets()
    {
        super.rebuildWidgets();
        setButtonVisibilities();
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

        buildScrollListsForSelectedSlot();
        setCurrentCategoryDropdownValues();
        addParameterFields();
        setButtonVisibilities();
        addRenderables();
    }

    private void addRenderables()
    {
        this.addRenderableWidget(tabItemEdit);
        this.addRenderableWidget(tabRecipes);
        this.addRenderableWidget(tabClipboard);
        this.addRenderableWidget(tabSettings);

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
            addRenderableWidget(btnCopyOriginalRecipe);
            addRenderableWidget(btnPasteOriginalRecipe);
            addRenderableWidget(btnReloadJSON);
            addRenderableWidget(btnSampleRecipe1);
            addRenderableWidget(btnSampleRecipe2);
            addRenderableWidget(btnSampleRecipe3);
            for (var b : _templateButtons)
                addRenderableWidget(b);
        }

        if (_selectedTab == PAGE_SETTINGS)
        {
            addRenderableWidget(chkIncludeComments);
            addRenderableWidget(chkFormatCode);
            addRenderableWidget(chkIncludeWeakNBT);
            addRenderableWidget(chkNeverRemove);
        }

        for (var eb : editBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var eb : comboBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var eb : checkBoxes.entrySet()) addRenderableWidget(eb.getValue());
        for (var wpb : wellParameterButtons)
            addRenderableWidget(wpb);

    }

    private void makeItemEditButtons()
    {
        var temp = getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * (WELLPARA_Y_ADVANCE);
        temp += WELLPARA_TOP_MARGIN;// area for buttons
        //temp += (temp == 0 ? WELLPARA_TOP_MARGIN : 0); // top of scroll bars

        var remaining = Math.max(32, (tabArea.getHeight() - temp - 6) / 2);
        var ystack = tabArea.getY() + temp;

        scrollTags = new ItemScrollPanel<>(tabArea.getWidth() - 8, remaining - 18,
                ystack + 17, tabArea.getX() + 1, (a) -> a);
        scrollTags.setItems(new ArrayList<>());
        scrollTags.setOnClick((idx) -> {
            setTagModeForSelectedSlotFromList(idx);
            buildScrollListsForSelectedSlot(); // reload and hide if neccessary
        });

        ystack += remaining;

        scrollNBT = new ItemScrollPanel<>(tabArea.getWidth() - 8, remaining - 18,
                ystack + 17, scrollTags.getRectangle().left(),
                (a) -> a);
        scrollNBT.setItems(new ArrayList<>());
        scrollNBT.setOnClick((idx) -> {
            //setTagModeForSelectedSlotFromList(idx);
            deleteNBTForSelectedSlotFromList(scrollNBT.getItems().get(idx));
            buildScrollListsForSelectedSlot();
        });
        btnSubtractCount = new ShiftButton(
                tabArea.getX(), tabArea.getY() + 6, 16, 16,
                Component.literal("-"),
                Component.literal("-10"),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? -10 : -1));
                    }
                });

        btnAddCount = new ShiftButton(
                btnSubtractCount.getX() + btnSubtractCount.getWidth() + 2, tabArea.getY() + 6, 16, 16,
                Component.literal("+"),
                Component.literal("+10"),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? 10 : 1));
                    }
                });

        btnSubtractCountX10 = new ShiftButton(
                btnAddCount.getX() + btnAddCount.getWidth() + 2, tabArea.getY() + 6, 25, 16,
                Component.literal("-10"),
                Component.literal("-100"),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? -100 : -10));
                    }
                });

        btnAddCountX10 = new ShiftButton(
                btnSubtractCountX10.getX() + btnSubtractCountX10.getWidth() + 2, tabArea.getY() + 6, 25, 16,
                Component.literal("+10"),
                Component.literal("+100").withStyle(style -> style.withFont(new ResourceLocation("alt"))),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? 100 : 10));
                    }
                });

        btnSubtractCountX100 = new ShiftButton(
                btnAddCountX10.getX() + btnAddCountX10.getWidth() + 2, tabArea.getY() + 6, 32, 16,
                Component.literal("-100"),
                Component.literal("-1000").withStyle(style -> style.withFont(new ResourceLocation("alt"))),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(-1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? -1000 : -100));
                    }
                });

        btnAddCountX100 = new ShiftButton(
                btnSubtractCountX100.getX() + btnSubtractCountX100.getWidth() + 2, tabArea.getY() + 6, 32, 16,
                Component.literal("+100"),
                Component.literal("+1000").withStyle(style -> style.withFont(new ResourceLocation("alt"))),
                (btn) -> {
                    if (slotSelectedForTag != null)
                    {
                        //slotSelectedForTag.getCipherSlot().adjustCount(1);
                        sendIntCipherMessage(KubeJSTableBlockEntity.CIPHER_ADJUSTMENT, slotSelectedForTag.getCipherSlot(),
                                "" + (Screen.hasShiftDown() ? 1000 : 100));
                    }
                });

    }

    private void makeRecipeButtons()
    {
        var temp = getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * WELLPARA_HEIGHT
                + getContainer().getBlockEntity().getCipher().getWellParameterMaximumCount() * (WELLPARA_Y_ADVANCE - 1);
        temp += (temp > 0 ? 2 : 0); // spacing


        var remaining = Math.max(32, (tabArea.getHeight() - 10) / 2);
        var ystack = tabArea.getY() + 6;

        scrollCategory = new ItemScrollPanel<>(tabArea.getWidth() - 12, remaining - 5, ystack + 1, tabArea.getX() + 6, (a) -> a);
        scrollCategory.setItems(CipherJsonLoader.getSortedCipherCategoryNames());
        scrollCategory.setOnClick((idx) -> {
            scrollChildren.setItems(CipherJsonLoader.getSortedCipherChildren(CipherJsonLoader.getSortedCipherCategoryNames().get(idx)));
        });
        ystack += remaining;

        scrollChildren = new ItemScrollPanel<>(tabArea.getWidth() - 12, remaining - 3, ystack + 1, tabArea.getX() + 6, Cipher::getName);
        scrollChildren.setOnClick((idx) -> {
            //Chatter.chat("client sending : " + (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
            sendIntStringMessage(KubeJSTableBlockEntity.RECIPE_TYPE_SCROLLBOX, (scrollChildren.getItems()).get(idx).getRecipeTypeId().toString());
        });

    }

    private void makeClipboardButtons()
    {
        btnClearOriginalRecipe = new NoFocusImageButton(tabArea.getX() + tabArea.getWidth() - 16, tabArea.getY() + 6,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_CLEAR_X, GUI_CLEAR_Y, GUI_BUTTON_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    sendIntIntMessage(KubeJSTableBlockEntity.ORIGINAL_RECIPE_CLEAR_ON_SERVER, 0);
                    btnClearOriginalRecipe.setFocused(false);
                    this.setFocused(null);
                });
        btnClearOriginalRecipe.setTooltip(Tooltip.create(Component.literal("Clear original recipe ID so it is not exported as a remove code fragment.")));

        var py = tabArea.getY() + tabArea.getHeight() - 26;
        var px = tabArea.getX() + tabArea.getWidth() - 20;

        btnCopyOriginalRecipe = new NoFocusImageButton(px, py,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_COPY_X, GUI_COPY_Y, GUI_BUTTON_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    if (container.getBlockEntity().getOriginalRecipeID() != null)
                    {
                        var json = RecipeJsonFetcher.getRecipeJson(Minecraft.getInstance().getSingleplayerServer(),
                                container.getBlockEntity().getOriginalRecipeID());
                        Minecraft.getInstance().keyboardHandler.setClipboard(json.toString());
                        Chatter.chat(container.getBlockEntity().getOriginalRecipeID().toString() + " copied to clipboard.");
                    } else
                        btnCopyOriginalRecipe.active = false; // somehow we are visible?
                });
        btnCopyOriginalRecipe.setTooltip(Tooltip.create(Component.literal("Copy the original recipe JSON from resources/data to the clipboard.")));
        px -= 15;

        btnPasteOriginalRecipe = new NoFocusImageButton(px, py,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_PASTE_X, GUI_PASTE_Y, GUI_BUTTON_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    var json = Minecraft.getInstance().keyboardHandler.getClipboard();
                    sendIntStringMessage(KubeJSTableBlockEntity.HANDLE_JSON_AS_RECIPE, json);

                    this.container.getBlockEntity().handleRecipe(json, null);
                });
        btnPasteOriginalRecipe.setTooltip(Tooltip.create(Component.literal("Paste JSON recipe from clipboard.")));
        px -= 15;


        btnReloadJSON = Button.builder(Component.literal("/reload"), (btn) -> {
            if (Minecraft.getInstance().level != null)
            {
                if (Minecraft.getInstance().level.getGameTime() < _disableBtnReloadJSON + 200) return;
                _disableBtnReloadJSON = Minecraft.getInstance().level.getGameTime();
            }
            sendIntIntMessage(KubeJSTableBlockEntity.RUN_COMMAND, KubeJSTableBlockEntity.RUN_COMMAND_RELOAD);
        }).bounds(tabArea.getX() + tabArea.getWidth() - 72, tabArea.getY() + tabArea.getHeight() - 50, 66, 16).build();


        btnSampleRecipe3 = new NoFocusImageButton(px, py,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_CLEAR_X, GUI_CLEAR_Y, GUI_BITMAP_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 93);
                    btnSampleRecipe3.setFocused(false);
                    this.setFocused(null);
                });
        btnSampleRecipe3.setTooltip(Tooltip.create(Component.literal("Sample recipe 3")));
        px -= 15;


        btnSampleRecipe2 = new NoFocusImageButton(px, py,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_CLEAR_X, GUI_CLEAR_Y, GUI_BITMAP_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 92);
                    btnSampleRecipe2.setFocused(false);
                    this.setFocused(null);
                });
        btnSampleRecipe2.setTooltip(Tooltip.create(Component.literal("Sample recipe 2")));
        px -= 15;


        btnSampleRecipe1 = new NoFocusImageButton(px, py,
                GUI_BUTTON_W, GUI_BUTTON_H, GUI_CLEAR_X, GUI_CLEAR_Y, GUI_BITMAP_H, GUI_BITMAP, GUI_BITMAP_W, GUI_BITMAP_H,
                (btn) -> {
                    sendIntIntMessage(KubeJSTableBlockEntity.RECIPE_CLEAR, 91);
                    btnSampleRecipe1.setFocused(false);
                    this.setFocused(null);
                });
        btnSampleRecipe1.setTooltip(Tooltip.create(Component.literal("Sample recipe 1")));


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

        // Settings
        chkIncludeComments = new CheckboxWithCallback(tabArea.getX() + 6, tabArea.getY() + 16, 16, 16, Component.literal("Comments?"),
                true, true);
        chkIncludeComments.setTooltip(Tooltip.create(Component.literal("Inserts comments into code.")));
        chkIncludeComments.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_COMMENT_PARAMETER, 0);
        };
        chkIncludeWeakNBT = new CheckboxWithCallback(tabArea.getX() + 6, tabArea.getY() + 36, 16, 16, Component.literal("Weak NBT?"),
                true, true);
        chkIncludeWeakNBT.setTooltip(Tooltip.create(Component.literal("Appends .weakNBT() for items with NBT data.")));
        chkIncludeWeakNBT.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_WEAKNBT_PARAMETER, 0);
        };
        chkFormatCode = new CheckboxWithCallback(tabArea.getX() + 6, tabArea.getY() + 56, 16, 16, Component.literal("Format Code?"),
                true, true);
        chkFormatCode.setTooltip(Tooltip.create(Component.literal("Formats code when possible. Includes CRLF and spacing. Avoids single lines.")));
        chkFormatCode.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_FORMATTING_PARAMETER, 0);
        };
        chkNeverRemove = new CheckboxWithCallback(tabArea.getX() + 6, tabArea.getY() + 76, 16, 16, Component.literal("Add Remove Code?"),
                true, true);
        chkNeverRemove.setTooltip(Tooltip.create(Component.literal("Add code to remove an existing recipe when possible.")));
        chkNeverRemove.callback = (a) -> {
            sendIntIntMessage(KubeJSTableBlockEntity.CHANGE_REMOVERECIPE_PARAMETER, 0);
        };
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
            if (p.getType().equals("combo") || p.getType().equals("combo-number"))
            {
                var box = new ComboBox(x, y, WELLPARA_WIDTH, WELLPARA_HEIGHT,
                        p.getComboOptions());
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
                var a = new ArrayList<Pair<String, String>>();
                a.add(Pair.of("false", "False"));
                a.add(Pair.of("true", "True"));
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
        tabItemEdit = new FakeTabButton(tabArea.getX()-40, tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Item"), (a) -> {
            this.setSelectedTab(PAGE_ITEM_EDIT);
        });
        tabItemEdit.setSelected(_selectedTab == PAGE_ITEM_EDIT);

        tabRecipes = new FakeTabButton(tabArea.getX()-40 + TAB_BUTTON_WIDTH, tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Recipe"), (a) -> {
            this.setSelectedTab(PAGE_RECIPES);
        });
        tabRecipes.setSelected(_selectedTab == PAGE_RECIPES);

        tabClipboard = new FakeTabButton(tabArea.getX()-40 + TAB_BUTTON_WIDTH * 2, tabArea.getY() - 20, TAB_BUTTON_WIDTH, 20, Component.literal("Clipboard"),
                (a) -> {
                    this.setSelectedTab(PAGE_CLIPBOARD);
                });
        tabClipboard.setSelected(_selectedTab == PAGE_CLIPBOARD);

        tabSettings = new FakeTabButton(tabArea.getX()-40 + TAB_BUTTON_WIDTH * 3, tabArea.getY() - 20, TAB_SETTINGS_WIDTH, 20, Component.literal("?"),
                (a) -> {
                    this.setSelectedTab(PAGE_SETTINGS);
                });
        tabSettings.setSelected(_selectedTab == PAGE_SETTINGS);
    }

    private void setSelectedTab(int i)
    {
        _selectedTab = i;
        rebuildWidgets();
        setButtonVisibilities();
    }

    private void setButtonVisibilities()
    {
        if (_selectedTab == PAGE_ITEM_EDIT)
        {
            if (slotSelectedForTag == null)
            {
                btnAddCount.visible = false;
                btnSubtractCount.visible = false;
                btnAddCountX10.visible = false;
                btnSubtractCountX10.visible = false;
                btnAddCountX100.visible = false;
                btnSubtractCountX100.visible = false;
            } else
            {
                var fluidmode = slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUID
                        || slotSelectedForTag.getCipherSlot().getMode() == CipherSlot.SlotMode.FLUIDTAG;
                var multimode = !slotSelectedForTag.getCipherSlot().isSingle();
                var by1 = (multimode || fluidmode);
                var by10 = (multimode || fluidmode);
                var by100 = fluidmode;

                btnAddCount.visible = by1;
                btnSubtractCount.visible = by1;
                btnAddCountX10.visible = by10;
                btnSubtractCountX10.visible = by10;
                btnAddCountX100.visible = by100;
                btnSubtractCountX100.visible = by100;
            }
        }

        for (var b : _templateButtons)
            b.visible = _selectedTab == 2;
        for (var b : wellParameterButtons)
            b.visible = _selectedTab == 0;
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
                if (p.getTooltip() != null && !p.getTooltip().isEmpty())
                    box.setTooltip(Tooltip.create(Component.translatableWithFallback(p.getTooltip(), p.getTooltip())));
                editBoxes.put(p.getPath(), box);
                box.setResponder(text -> {
                    if (!_dontFireControlUpdates)
                    {
                        sendIntStringStringMessage(KubeJSTableBlockEntity.PARAMETER_SYNC, p.getPath(), text); // to server
                    }
                });
            }
            if (p.getType().equals("combo") || p.getType().equals("combo-number"))
            {
                var box = new ComboBox(workArea.getX() + p.getPosX(), workArea.getY() + p.getPosY() - 3, p.getWidth(), p.getHeight(), p.getComboOptions());
                box.setLabel(p.getLabel(), p.getFlags().contains("right"));
                if (p.getTooltip() != null && !p.getTooltip().isEmpty())
                    box.setTooltip(Tooltip.create(Component.translatableWithFallback(p.getTooltip(), p.getTooltip())));
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
                if (p.getTooltip() != null && !p.getTooltip().isEmpty())
                    box.setTooltip(Tooltip.create(Component.translatableWithFallback(p.getTooltip(), p.getTooltip())));
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
