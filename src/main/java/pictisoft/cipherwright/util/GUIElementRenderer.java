package pictisoft.cipherwright.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class GUIElementRenderer
{
    private static final int WELL_X = 0;
    private static final int WELL_Y = 0;
    private static final int WELL_W = 18;
    private static final int WELL_H = 18;
    private static final int WELL_LARGE_X = 0;
    private static final int WELL_LARGE_Y = 18;
    private static final int WELL_LARGE_W = 26;
    private static final int WELL_LARGE_H = 26;
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final int ARROW_LEFT_X = 18;
    private static final int ARROW_LEFT_Y = 0;
    private static final int ARROW_LEFT_W = 22;
    private static final int ARROW_LEFT_H = 15;
    private static final int ARROW_RIGHT_X = 40;
    private static final int ARROW_RIGHT_Y = 0;
    private static final int ARROW_RIGHT_W = 22;
    private static final int ARROW_RIGHT_H = 15;
    private static final int ARROW_UP_X = 49;
    private static final int ARROW_UP_Y = 37;
    private static final int ARROW_UP_W = 15;
    private static final int ARROW_UP_H = 22;
    private static final int ARROW_DOWN_X = 49;
    private static final int ARROW_DOWN_Y = 15;
    private static final int ARROW_DOWN_W = 15;
    private static final int ARROW_DOWN_H = 22;
    private final ResourceLocation GUI;

    public GUIElementRenderer(ResourceLocation elements)
    {
        GUI = elements;
    }

    public void drawSlotWell(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x - 1, y - 1, WELL_X, WELL_Y, WELL_W, WELL_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawSlotWellLarge(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x - 5, y - 5, WELL_LARGE_X, WELL_LARGE_Y, WELL_LARGE_W, WELL_LARGE_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawLeftArrow(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x, y, ARROW_LEFT_X, ARROW_LEFT_Y, ARROW_LEFT_W, ARROW_LEFT_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawRightArrow(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x, y, ARROW_RIGHT_X, ARROW_RIGHT_Y, ARROW_RIGHT_W, ARROW_RIGHT_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawUpArrow(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x, y, ARROW_UP_X, ARROW_UP_Y, ARROW_UP_W, ARROW_UP_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawDownArrow(GuiGraphics gui, int x, int y)
    {
        gui.blit(GUI, x, y, ARROW_DOWN_X, ARROW_DOWN_Y, ARROW_DOWN_W, ARROW_DOWN_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void drawLeftArrow(GuiGraphics gui, int x, int y, float alpha, float scale)
    {
        preDraw(gui, x, y, ARROW_LEFT_W, ARROW_LEFT_H, alpha, scale);
        gui.blit(GUI, 0, 0, ARROW_LEFT_X, ARROW_LEFT_Y, ARROW_LEFT_W, ARROW_LEFT_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        postDraw(gui);
    }

    public void drawRightArrow(GuiGraphics gui, int x, int y, float alpha, float scale)
    {
        preDraw(gui, x, y, ARROW_RIGHT_W, ARROW_RIGHT_H, alpha, scale);
        gui.blit(GUI, 0, 0, ARROW_RIGHT_X, ARROW_RIGHT_Y, ARROW_RIGHT_W, ARROW_RIGHT_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        postDraw(gui);
    }

    public void drawUpArrow(GuiGraphics gui, int x, int y, float alpha, float scale)
    {
        preDraw(gui, x, y, ARROW_UP_W, ARROW_UP_H, alpha, scale);
        gui.blit(GUI, 0, 0, ARROW_UP_X, ARROW_UP_Y, ARROW_UP_W, ARROW_UP_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        postDraw(gui);
    }

    public void drawDownArrow(GuiGraphics gui, int x, int y, float alpha, float scale)
    {
        preDraw(gui, x, y, ARROW_DOWN_W, ARROW_DOWN_H, alpha, scale);
        gui.blit(GUI, 0, 0, ARROW_DOWN_X, ARROW_DOWN_Y, ARROW_DOWN_W, ARROW_DOWN_H, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        postDraw(gui);
    }

    private void postDraw(GuiGraphics gui)
    {
        //  gui.setColor(1, 1, 1, 1);
        gui.pose().popPose();
    }

    private void preDraw(GuiGraphics gui, int x, int y, int w, int h, float alpha, float scale)
    {
        gui.pose().pushPose();
        //gui.setColor(1, 1, 1, alpha);
        gui.pose().translate(x + (w * .5f), y + (h * .5f), 0);
        gui.pose().scale(scale, scale, 1);
        gui.pose().translate(w * -.5f, h * -.5f, 0);
    }
}
