package pictisoft.cipherwright.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollableDropdownWidget extends AbstractWidget implements Renderable
{
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final int MAX_VISIBLE_ITEMS = 8;

    private final List<String> items;
    private final Consumer<Integer> onSelectionChanged;
    private boolean isExpanded = false;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private boolean isDraggingScrollbar = false;
    private int lastMouseY = 0;

    public ScrollableDropdownWidget(int x, int y, int width, int height, Consumer<Integer> onSelectionChanged)
    {
        super(x, y, width, height, Component.literal(""));
        this.items = new ArrayList<>();
        this.onSelectionChanged = onSelectionChanged;
    }

    public void addItem(String item)
    {
        items.add(item);
    }

    public void setItems(List<String> items)
    {
        this.items.clear();
        this.items.addAll(items);
        this.scrollOffset = 0;

        if (this.selectedIndex >= items.size())
        {
            this.selectedIndex = items.isEmpty() ? -1 : 0;
        }
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    public String getSelectedItem()
    {
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : "";
    }

    public void setSelectedIndex(int index)
    {
        if (index >= -1 && index < items.size())
        {
            this.selectedIndex = index;
            if (onSelectionChanged != null)
            {
                onSelectionChanged.accept(selectedIndex);
            }
        }
    }

    public boolean isExpanded()
    {
        return isExpanded;
    }

    public void setExpanded(boolean expanded)
    {
        isExpanded = expanded;
    }

    // This gets called only for the main button area
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        if (!this.active) return;

        // Toggle dropdown on main button click
        this.isExpanded = !this.isExpanded;
    }

    // Override to handle all click events manually
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        //Chatter.chat("mouseClicked");
        if (!this.active || button != 0) return false;

        // If clicked on the main button
        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height)
        {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            //Chatter.chat("mouseClicked: main button");
            return true;
        }

        // If dropdown is expanded and clicking in the expanded area
        if (isExpanded && isPointInDropdown((int) mouseX, (int) mouseY))
        {
            // Check if click is on scrollbar
            if (shouldShowScrollbar() && isMouseOverScrollbar((int) mouseX, (int) mouseY))
            {
                //Chatter.chat("mouseClicked: scrollbar");
                isDraggingScrollbar = true;
                lastMouseY = (int) mouseY;
                return true;
            }

            // Check if click is on dropdown item
            int dropdownY = this.getY() + this.height;
            int relativeY = (int) mouseY - dropdownY;
            int itemIndex = relativeY / 20 + scrollOffset;

            if (itemIndex >= 0 && itemIndex < items.size() && relativeY >= 0 && relativeY < Math.min(items.size(), MAX_VISIBLE_ITEMS) * 20)
            {
                this.selectedIndex = itemIndex;
                if (onSelectionChanged != null)
                {
                    onSelectionChanged.accept(selectedIndex);
                }
                this.isExpanded = false;
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }

            return true; // Consume click even if not on an item
        } else if (isExpanded)
        {
            // Clicking outside the dropdown should close it
            this.isExpanded = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (isDraggingScrollbar)
        {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        //Chatter.chat("mouseDragged");
        if (isDraggingScrollbar && button == 0)
        {
            int currentMouseY = (int) mouseY;
            int deltaY = currentMouseY - lastMouseY;
            lastMouseY = currentMouseY;

            if (deltaY != 0)
            {
                int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
                int maxOffset = Math.max(0, items.size() - visibleItems);

                if (maxOffset > 0)
                {
                    float scrollRange = items.size() - visibleItems;
                    float trackHeight = visibleItems * 20;

                    float scrollAmount = deltaY / trackHeight * scrollRange;
                    scrollOffset = Mth.clamp(scrollOffset + Math.round(scrollAmount), 0, maxOffset);
                }

                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount)
    {
        if (isExpanded && shouldShowScrollbar() && isPointInDropdown((int) mouseX, (int) mouseY))
        {
            int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
            int maxScrollOffset = Math.max(0, items.size() - visibleItems);
            scrollOffset = Mth.clamp(scrollOffset - (int) scrollAmount, 0, maxScrollOffset);
            return true;
        }
        return false;
    }

    private boolean isPointInDropdown(int mouseX, int mouseY)
    {
        int dropdownHeight = Math.min(items.size(), MAX_VISIBLE_ITEMS) * 20;
        return mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + this.height + dropdownHeight;
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY)
    {
        if (!shouldShowScrollbar()) return false;

        int scrollbarX = this.getX() + this.width - 10;
        int scrollbarY = this.getY() + this.height;
        int scrollbarHeight = Math.min(items.size(), MAX_VISIBLE_ITEMS) * 20;

        return mouseX >= scrollbarX && mouseX <= scrollbarX + 10 && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }

    private boolean shouldShowScrollbar()
    {
        return items.size() > MAX_VISIBLE_ITEMS;
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput)
    {

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();

        // Render the main dropdown button
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46, this.width / 2, this.height);
        guiGraphics.blit(WIDGETS_LOCATION, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46, this.width / 2, this.height);

        // Draw dropdown arrow
        guiGraphics.blit(WIDGETS_LOCATION, this.getX() + this.width - 15, this.getY() + (this.height - 5) / 2, isExpanded ? 15 : 0, 84, 10, 5);

        // Draw selected item text
        String displayText = selectedIndex >= 0 ? items.get(selectedIndex) : "Select an item...";
        guiGraphics.drawCenteredString(minecraft.font, displayText, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);

        // Draw dropdown list if expanded
        if (isExpanded && !items.isEmpty())
        {
            int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
            int dropdownHeight = visibleItems * 20;
            int dropdownY = this.getY() + this.height;

            // Draw dropdown background
            guiGraphics.fill(this.getX(), dropdownY, this.getX() + this.width, dropdownY + dropdownHeight, 0xFF000000);

            // Draw visible items
            for (int i = 0; i < visibleItems; i++)
            {
                int itemIndex = i + scrollOffset;
                if (itemIndex >= items.size()) break;

                int itemY = dropdownY + i * 20;

                // Highlight selected item or item under mouse
                if (itemIndex == selectedIndex || (mouseX >= this.getX() && mouseX <= this.getX() + this.width - (shouldShowScrollbar() ? 10 : 0) && mouseY >= itemY && mouseY < itemY + 20))
                {
                    guiGraphics.fill(this.getX(), itemY, this.getX() + this.width - (shouldShowScrollbar() ? 10 : 0), itemY + 20, 0xFF3C3C3C);
                }

                // Draw item text
                guiGraphics.drawString(minecraft.font, items.get(itemIndex), this.getX() + 5, itemY + 6, 0xFFFFFF);
            }

            // Draw scrollbar if needed
            if (shouldShowScrollbar())
            {
                int scrollbarX = this.getX() + this.width - 10;

                // Draw scrollbar background
                guiGraphics.fill(scrollbarX, dropdownY, scrollbarX + 10, dropdownY + dropdownHeight, 0xFF202020);

                // Calculate scrollbar thumb position and size
                float thumbPercentage = (float) visibleItems / items.size();
                int thumbHeight = Math.max(20, Math.round(dropdownHeight * thumbPercentage));

                float scrollPercentage = items.size() <= visibleItems ? 0 : (float) scrollOffset / (items.size() - visibleItems);
                int thumbY = dropdownY + Math.round((dropdownHeight - thumbHeight) * scrollPercentage);

                // Draw scrollbar thumb
                guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 10, thumbY + thumbHeight, 0xFF606060);

                if (isDraggingScrollbar || isMouseOverScrollbar(mouseX, mouseY))
                {
                    // Highlight scrollbar thumb
                    guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 10, thumbY + thumbHeight, 0xFF808080);
                }
            }
        }
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY)
    {
        if (this.isExpanded)
        {
            //Chatter.chat("mouseOver");
            int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
            return this.active && this.visible && pMouseX >= (double) this.getX()
                    && pMouseY >= (double) this.getY()
                    && pMouseX < (double) (this.getX() + this.width)
                    && pMouseY < (double) (this.getY() + this.height + visibleItems * 20);
        }

        return this.active && this.visible && pMouseX >= (double) this.getX()
                && pMouseY >= (double) this.getY()
                && pMouseX < (double) (this.getX() + this.width)
                && pMouseY < (double) (this.getY() + this.height);
    }

    @Override
    public boolean isHoveredOrFocused()
    {
        // Override to make sure the widget still renders as hovered when hovering over the dropdown area
        return super.isHoveredOrFocused() || (isExpanded && isPointInDropdown((int) Minecraft.getInstance().mouseHandler.xpos(),
                (int) Minecraft.getInstance().mouseHandler.ypos()));
    }
}