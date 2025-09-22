package pictisoft.cipherwright.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemScrollPanel<T> extends ScrollPanel
{
    private final List<T> items;
    private final Function<T, String> stringProvider;
    public boolean visible = true;
    private int selectedIndexPrivate = -1;
    private final int textColor;
    private final int selectedColor;
    private final int backgroundColor;
    private Consumer<Integer> onClick;
    protected Font font;
    private boolean _disabled = false;
    private boolean _scrolling;
    private double _barLeft;
    private double _barWidth = 6;

    protected void setSelectedPrivate(int val)
    {
        selectedIndexPrivate = val;
        //Chatter.chat("setSelectedPrivate=" + val);
    }

    protected int getSelectedPrivate()
    {
        return selectedIndexPrivate;
    }


    public ItemScrollPanel(int width, int height, int top, int left, Function<T, String> stringProvider)
    {
        super(Minecraft.getInstance(), width, height, top, left);
        this.items = new ArrayList<>();
        this.textColor = 0xFFFFFFFF;
        this.selectedColor = 0xFF3864BD;
        this.backgroundColor = 0xFF202020;
        this.font = Minecraft.getInstance().font;
        this.stringProvider = stringProvider;
    }

//    public StringScrollPanel(int width, int height, int top, int left,
//            int textColor, int selectedColor, int backgroundColor)
//    {
//        super(Minecraft.getInstance(), width, height, top, left);
//        this.items = new ArrayList<>();
//        this.textColor = textColor;
//        this.selectedColor = selectedColor;
//        this.backgroundColor = backgroundColor;
//        this.font = Minecraft.getInstance().font;
//    }

    private int getMaxScroll()
    {
        return this.getContentHeight() - (this.height - this.border);
    }

    private void applyScrollLimits()
    {
        int max = getMaxScroll();

        if (max < 0)
        {
            max = 0;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > max)
        {
            this.scrollDistance = max;
        }
    }


    @Override
    protected int getScrollAmount()
    {
        return getItemHeight() * 3;
    }

    public @NotNull ScreenRectangle getRectangle()
    {
        return new ScreenRectangle(left, top, width, height);
    }


    @Override
    protected int getContentHeight()
    {
        return items.size() * getItemHeight();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (this.visible) super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY)
    {
        if (!this.visible) return;
        //Chatter.chat(String.format("%d %d %d %d", this.left, this.top, this.width,this.height));

        Minecraft minecraft = Minecraft.getInstance();
        int left = this.left;
        //int contentHeight = getContentHeight();

        var hoveredidx = getItemOver(mouseX, mouseY);

        // Draw each visible item
        for (int i = 0; i < items.size(); i++)
        {
            int itemY = relativeY + (i * getItemHeight());

            // Skip if the item is not visible
            if (itemY + getItemHeight() < 0 || itemY > this.top + this.height)
            {
                continue;
            }

            if (!_disabled)
            {
                // Highlight selected item
                if (i == hoveredidx && !_scrolling)
                {
                    int mix;
                    if (i == getSelectedPrivate()) // over selected item
                        mix = FastColor.ARGB32.lerp(.5f, textColor, selectedColor);
                    else
                    {
                        //Chatter.chat("NO HIGHLIGHT " + this._scrolling);
                        mix = FastColor.ARGB32.lerp(.5f, backgroundColor, selectedColor);
                    }
                    guiGraphics.fill(left, itemY, entryRight, itemY + getItemHeight(), mix);
                } else
                {
                    if (i == getSelectedPrivate())
                    {
                        guiGraphics.fill(left, itemY, entryRight, itemY + getItemHeight(), selectedColor);
                    }
                }
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(left + 4, itemY + (float) (getItemHeight() - 8) / 2, 0);
            var str = stringProvider.apply(items.get(i));
            var fw = minecraft.font.width(str);
            if (fw > width)
            {
                if (fw * .75f > width)
                {
                    guiGraphics.pose().scale(.5f, .5f, 1);
                    guiGraphics.pose().translate(0, 4, 0);
                } else
                {
                    guiGraphics.pose().scale(.75f, .75f, 1);
                    guiGraphics.pose().translate(0, 1, 0);
                }
            }
            // Draw item text
            var tcolor = textColor;
            if (_disabled)
                tcolor = FastColor.ARGB32.lerp(.5f, textColor, 0x111111);
            guiGraphics.drawString(
                    minecraft.font,
                    str,
                    0,
                    0,
                    tcolor
            );
            guiGraphics.pose().popPose();
        }
        guiGraphics.renderOutline(left, top, width, height, 0xff0044ff);
    }

    private int getItemOver(double mouseX, double mouseY)
    {
        if (mouseX < left || mouseX > left + width - 6 /*assume bar width is 6, it is default...but private */) return -1;
        int relativeY = (int) (mouseY - top - border) + (int) scrollDistance;
        int itemIndex = relativeY / getItemHeight();
        if (relativeY < 0) return -1;
        if (itemIndex >= 0 && itemIndex < items.size())
        {
            return itemIndex;
        }
        return -1;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        boolean ret = this._scrolling;
        this._scrolling = false;
        return ret;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!this.visible) return false;
        if (mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return false;

        this._barLeft = this.left + this.width - _barWidth;
        this._scrolling = button == 0 && mouseX >= _barLeft && mouseX < _barLeft + _barWidth;
        if (this._scrolling)
        {
            return true;
        }
//        int mouseListY = ((int) mouseY) - this.top - this.getContentHeight() + (int) this.scrollDistance - border;
//        if (mouseX >= left && mouseX <= right && mouseListY < 0)
//        {
//            return this.clickPanel(mouseX - left, mouseY - this.top + (int) this.scrollDistance - border, button);
//        }
//        return false;
//        if (super.mouseClicked(mouseX, mouseY, button))
//        {
//            return true;
//        }
//
        if (button == 0 && getItemOver(mouseX, mouseY) >= 0)
        {
            setSelectedIndex(getItemOver(mouseX, mouseY), true);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (!visible) return false;
        //Chatter.chat(deltaX + " " + deltaY);
        {
            int barHeight = (height * height) / (getContentHeight() == 0 ? 1 : getContentHeight());

            if (barHeight < 32) barHeight = 32;

            if (barHeight > height - border * 2)
                barHeight = height - border * 2;

            int maxScroll = height - barHeight;
            if (maxScroll != 0)
            {
                double moved = deltaY / maxScroll;
                this.scrollDistance += (float) (getMaxScroll() * moved);
                applyScrollLimits();
            }
            return true;
        }

//        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if (visible && scroll != 0)
        {
            //Chatter.chat("scroll");
            this.scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    protected void drawBackground(GuiGraphics guiGraphics, Tesselator tess, float partialTick)
    {
        if (!this.visible) return;
        guiGraphics.fill(left, top, left + width, top + height, backgroundColor);
    }

    public void setItems(List<T> items)
    {
        this.items.clear();
        this.items.addAll(items);
        this.setSelectedPrivate(-1);
        this.scrollDistance = 0.0F;
    }

    public void addItem(T item)
    {
        this.items.add(item);
    }

    public T getSelectedItem()
    {
        return getSelectedPrivate() >= 0 && getSelectedPrivate() < items.size() ? items.get(getSelectedPrivate()) : null;
    }

    public int getSelectedIndex()
    {
        return getSelectedPrivate();
    }

    public void setOnClick(Consumer<Integer> func)
    {
        onClick = func;
    }

    public void setSelectedIndex(int index, boolean fireEvent)
    {
        if (index >= -1 && index < items.size())
        {
            var alsoFire = this.getSelectedPrivate() != index;
            this.setSelectedPrivate(index);
            if (fireEvent && alsoFire && onClick != null)
                onClick.accept(index);
            // Ensure the selected item is visible
            if (getSelectedPrivate() >= 0)
            {
                int selectedTop = getSelectedPrivate() * getItemHeight();
                int selectedBottom = selectedTop + getItemHeight();

                if (selectedTop < scrollDistance)
                {
                    scrollDistance = selectedTop;
                } else if (selectedBottom > scrollDistance + height)
                {
                    scrollDistance = selectedBottom - height;
                }

                scrollDistance = Math.max(0, Math.min(scrollDistance, getContentHeight() - height));
            }
        }
    }

    public List<T> getItems()
    {
        return new ArrayList<>(items);
    }

    public int getItemHeight()
    {
        return font.lineHeight + 2;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    public void unselect()
    {
        setSelectedPrivate(-1);
    }

    public void setDisabled(boolean b)
    {
        _disabled = b;
    }
}