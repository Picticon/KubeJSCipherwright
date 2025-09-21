package pictisoft.cipherwright.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class Slice9
{
    private final ResourceLocation texture;
    private final int gridsize;

    ///  Assumes an equal 3x3 grid of corner/top/corner/left/middle/right/corner/bottom/corner
    public Slice9(ResourceLocation texture, int gridSize)
    {
        this.texture = texture;
        this.gridsize = gridSize;
    }

    public void drawNineSlice(GuiGraphics guiGraphics, int x, int y, int w, int h)
    {
        var ts = gridsize * 3;


        // Corners (fixed)

        // top-left
        guiGraphics.blit(texture, x, y, 0, 0, gridsize, gridsize, ts, ts); // Top-left

        // top-right
        guiGraphics.blit(texture, x + w - gridsize, y, gridsize * 2, 0, gridsize, gridsize, ts, ts); // Top-right

        // bottom left
        guiGraphics.blit(texture, x, y + h - gridsize, 0, gridsize * 2, gridsize, gridsize, ts, ts); // Bottom-left

        // bottom right
        guiGraphics.blit(texture, x + w - gridsize, y + h - gridsize, gridsize * 2, gridsize * 2, gridsize, gridsize, ts, ts); // Bottom-right


        // Borders (stretchable)

        // Top edge
        if (w > 2 * gridsize)
            guiGraphics.blit(texture,
                    x + gridsize,
                    y,
                    w - 2 * gridsize,
                    gridsize,
                    gridsize,
                    0,
                    gridsize,
                    gridsize, ts,
                    ts);

        // Bottom
        if (w > 2 * gridsize)
            guiGraphics.blit(texture,     //
                    x + gridsize,
                    y + h - gridsize, // GXY
                    w - 2 * gridsize,
                    gridsize,             // GWH
                    gridsize,
                    gridsize * 2, // TXY
                    gridsize,
                    gridsize,             // TWH
                    ts, ts);

        // Left
        if (h > 2 * gridsize)
            guiGraphics.blit(texture,//
                    x,
                    y + gridsize,//
                    gridsize,
                    h - 2 * gridsize,//
                    0,
                    gridsize, //
                    gridsize,
                    gridsize,//
                    ts, ts);

        // Right
        if (h > 2 * gridsize)
            guiGraphics.blit(texture,//
                    x + w - gridsize,
                    y + gridsize,//
                    gridsize,
                    h - 2 * gridsize,//
                    gridsize * 2,
                    gridsize,//
                    gridsize,
                    gridsize, //
                    ts, ts);

        // Center (stretchable)
        if (w > 2 * gridsize && h > 2 * gridsize)
            guiGraphics.blit(texture,//
                    x + gridsize, y + gridsize,//
                    w - 2 * gridsize, h - 2 * gridsize,//
                    gridsize, gridsize,//
                    gridsize, gridsize,//
                    ts, ts); // center
    }
}
