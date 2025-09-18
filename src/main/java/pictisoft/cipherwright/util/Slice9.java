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

    public void drawNineSlice(GuiGraphics guiGraphics, int leftPos, int topPos, int guiWidth, int guiHeight)
    {
        var ts = gridsize*3;
        guiGraphics.blit(texture, leftPos, topPos, 0, 0, gridsize, gridsize, ts, ts); // Top-left
        guiGraphics.blit(texture, leftPos + guiWidth - gridsize, topPos, gridsize * 2, 0, gridsize, gridsize, ts, ts); // Top-right
        guiGraphics.blit(texture, leftPos, topPos + guiHeight - gridsize, 0, gridsize * 2, gridsize, gridsize, ts, ts); // Bottom-left
        guiGraphics.blit(texture, leftPos + guiWidth - gridsize, topPos + guiHeight - gridsize, gridsize * 2, gridsize * 2, gridsize, gridsize, ts, ts); // Bottom-right

        // Borders (stretchable)
        guiGraphics.blit(texture, leftPos + gridsize, topPos, guiWidth - 2 * gridsize, gridsize, gridsize, 0, gridsize, gridsize, ts,
                ts); // Top
        guiGraphics.blit(texture, //
                leftPos + gridsize, topPos + guiHeight - gridsize, // GXY
                guiWidth - 2 * gridsize, gridsize,                        // GWH
                gridsize, gridsize * 2,                            // TXY
                gridsize, gridsize,                                      // TWH
                ts, ts); // Bottom
        guiGraphics.blit(texture,//
                leftPos, topPos + gridsize,//
                gridsize, guiHeight - 2 * gridsize,//
                0, gridsize, //
                gridsize, gridsize,//
                ts, ts); // Left
        guiGraphics.blit(texture,//
                leftPos + guiWidth - gridsize, topPos + gridsize,//
                gridsize, guiHeight - 2 * gridsize,//
                gridsize * 2, gridsize,//
                gridsize, gridsize, //
                ts, ts); // Right

        // Center (stretchable)
        guiGraphics.blit(texture,//
                leftPos + gridsize, topPos + gridsize,//
                guiWidth - 2 * gridsize, guiHeight - 2 * gridsize,//
                gridsize, gridsize,//
                gridsize, gridsize,//
                ts, ts); // center
    }
}
