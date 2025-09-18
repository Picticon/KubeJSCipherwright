//package pictisoft.cipherwright.cipher;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//import net.minecraft.client.renderer.GameRenderer;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.item.Item;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.tags.ItemTags;
//import net.minecraft.world.inventory.Slot;
//import net.minecraftforge.registries.ForgeRegistries;
//import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableContainer;
//
//import java.util.List;
//import java.util.ArrayList;
//
//public class BlueprintScreen extends AbstractContainerScreen<BlueprintContainer>
//{
//    private static final ResourceLocation TEXTURE = new ResourceLocation("yourmod", "textures/gui/blueprint.png");
//    private static final int TAG_BUTTON_WIDTH = 12;
//    private static final int TAG_BUTTON_HEIGHT = 12;
//
//    // Currently selected slot for tag selection
//    private BlueprintContainer.BlueprintSlot selectedSlot;
//    private boolean showTagSelection = false;
//    private List<TagKey<Item>> availableTags = new ArrayList<>();
//
//    public BlueprintScreen(BlueprintContainer container, Inventory playerInventory, Component title)
//    {
//        super(container, playerInventory, title);
//        this.imageWidth = 176;
//        this.imageHeight = 166;
//    }
//
//    @Override
//    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY)
//    {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//
//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;
//        gui.blit(x, y, 0, 0, imageWidth, imageHeight);
//
//
//        // Render slot mode indicators
//        for (Slot slot : menu.slots)
//        {
//            if (slot instanceof BlueprintContainer.BlueprintSlot blueprintSlot)
//            {
//                int slotX = x + slot.x;
//                int slotY = y + slot.y;
//
//                // Draw mode indicator (tag vs item)
//                if (blueprintSlot.getMode() == KubeJSTableContainer.SlotMode.TAG)
//                {
//                    // Draw tag indicator
//                    //RenderSystem.setShaderTexture(0, new ResourceLocation("yourmod", "textures/gui/tag_icon.png"));
//                    //gui.blit(slotX + 10, slotY - 8, 0, 0, TAG_BUTTON_WIDTH, TAG_BUTTON_HEIGHT);
//                }
//
//                // Draw mode toggle button
//                //RenderSystem.setShaderTexture(0, new ResourceLocation("yourmod", "textures/gui/toggle_button.png"));
//                ///gui.blit(slotX + 10, slotY + 10, 0, 0, TAG_BUTTON_WIDTH, TAG_BUTTON_HEIGHT);
//            }
//        }
//
//        // Render tag selection overlay if active
//        if (showTagSelection && selectedSlot != null)
//        {
//            renderTagSelectionPanel(gui, mouseX, mouseY);
//        }
//    }
//
//    @Override
//    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY)
//    {
//        super.renderTooltip(gui, mouseX, mouseY);
//
//        // Add custom tooltips for blueprint slots
//        Slot hoveredSlot = getSlotUnderMouse();
//        if (hoveredSlot instanceof BlueprintContainer.BlueprintSlot blueprintSlot)
//        {
//            List<Component> tooltip = new ArrayList<>();
//
//            if (blueprintSlot.getMode() == BlueprintContainer.SlotMode.ITEM)
//            {
//                tooltip.add(Component.translatable("tooltip.yourmod.blueprint.item_mode"));
//                ItemStack ingredient = blueprintSlot.getIngredient();
//                if (!ingredient.isEmpty())
//                {
//                    tooltip.add(ingredient.getHoverName());
//                }
//            } else
//            {
//                tooltip.add(Component.translatable("tooltip.yourmod.blueprint.tag_mode"));
//                if (blueprintSlot.getTagKey() != null)
//                {
//                    tooltip.add(Component.literal(blueprintSlot.getTagKey().location().toString()));
//                }
//            }
//
//            // Add instruction to change mode
//            tooltip.add(Component.translatable("tooltip.yourmod.blueprint.click_to_toggle"));
//            //renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
//            gui.renderTooltip(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
//        }
//    }
//
//
//    private void renderTagSelectionPanel(GuiGraphics gui, int mouseX, int mouseY)
//    {
//        int panelWidth = 120;
//        int panelHeight = Math.min(availableTags.size() * 15 + 10, 150);
//        int x = (width - panelWidth) / 2;
//        int y = (height - panelHeight) / 2;
//
//        // Draw background
//        gui.fill(x, y, x + panelWidth, y + panelHeight, 0xF0101010);
//        gui.fill(x + 1, y + 1, x + panelWidth - 1, y + panelHeight - 1, 0xF0303030);
//
//        // Draw title
//        gui.drawString(font, Component.translatable("gui.yourmod.blueprint.select_tag"),
//                x + 5, y + 5, 0xFFFFFF);
//
//        // Draw tags
//        int tagY = y + 20;
//        for (TagKey<Item> tag : availableTags)
//        {
//            boolean hover = mouseX >= x + 5 && mouseX <= x + panelWidth - 5 &&
//                    mouseY >= tagY && mouseY <= tagY + 12;
//
//            // Highlight on hover
//            if (hover)
//            {
//                gui.fill(x + 2, tagY - 2, x + panelWidth - 2, tagY + 12, 0x80FFFFFF);
//            }
//
//            // Draw tag name
//            var tagloc = Component.literal(tag.location().toString());
//            gui.drawString(font, tagloc, x + 5, tagY, 0xFFFFFF);
//
//            tagY += 15;
//            if (tagY > y + panelHeight - 10) break;
//        }
//    }
//
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int button)
//    {
//        // Handle tag selection panel clicks
//        if (showTagSelection)
//        {
//            int panelWidth = 120;
//            int panelHeight = Math.min(availableTags.size() * 15 + 10, 150);
//            int x = (width - panelWidth) / 2;
//            int y = (height - panelHeight) / 2;
//
//            // Check if click is within panel
//            if (mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + panelHeight)
//            {
//                // Calculate which tag was clicked
//                if (mouseY >= y + 20)
//                {
//                    int index = (int) ((mouseY - (y + 20)) / 15);
//                    if (index >= 0 && index < availableTags.size())
//                    {
//                        // Set selected tag
//                        TagKey<Item> selectedTag = availableTags.get(index);
//                        menu.setSlotTag(selectedSlot.getSlotIndex(), selectedTag);
//                    }
//                }
//
//                // Close panel
//                showTagSelection = false;
//                return true;
//            } else
//            {
//                // Click outside panel closes it
//                showTagSelection = false;
//                return true;
//            }
//        }
//
//        // Check for clicks on blueprint slot toggle buttons
//        for (Slot slot : menu.slots)
//        {
//            if (slot instanceof BlueprintContainer.BlueprintSlot blueprintSlot)
//            {
//                int slotX = leftPos + slot.x;
//                int slotY = topPos + slot.y;
//
//                // Check if toggle button was clicked
//                if (mouseX >= slotX + 10 && mouseX <= slotX + 10 + TAG_BUTTON_WIDTH &&
//                        mouseY >= slotY + 10 && mouseY <= slotY + 10 + TAG_BUTTON_HEIGHT)
//                {
//
//                    // Toggle mode
//                    menu.toggleSlotMode(blueprintSlot.getSlotIndex());
//
//                    // If switching to TAG mode, show tag selection
//                    if (blueprintSlot.getMode() == BlueprintContainer.SlotMode.ITEM)
//                    {  // Will be toggled by the menu
//                        selectedSlot = blueprintSlot;
//                        openTagSelection(blueprintSlot);
//                    }
//
//                    return true;
//                }
//            }
//        }
//
//        return super.mouseClicked(mouseX, mouseY, button);
//    }
//
//    private void openTagSelection(BlueprintContainer.BlueprintSlot slot)
//    {
//        ItemStack ingredient = slot.getIngredient();
//
//        // Get available tags for this item
//        availableTags.clear();
//        if (!ingredient.isEmpty())
//        {
//            Item item = ingredient.getItem();
//
//            // Get all item tags that this item belongs to
//            for (TagKey<Item> tag : ForgeRegistries.ITEMS.tags().getTagNames().toList())
//            {
//                if (ForgeRegistries.ITEMS.tags().getTag(tag).contains(item))
//                {
//                    availableTags.add(tag);
//                }
//            }
//        } else
//        {
//            // If no item selected, show common tags
//            availableTags.add(ItemTags.WOOL);
//            availableTags.add(ItemTags.PLANKS);
//            availableTags.add(ItemTags.LOGS);
//            // Add more common tags as needed
//        }
//
//        if (!availableTags.isEmpty())
//        {
//            showTagSelection = true;
//        }
//    }
//}
