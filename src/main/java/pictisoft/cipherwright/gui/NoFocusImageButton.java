package pictisoft.cipherwright.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

// This button has a separate focus state, so you can click the button once without staying "hovered"
// accepts a texture with 4 images stacked vertically, normal, hovered, focused, not-active (disabled).
public class NoFocusImageButton extends ImageButton
{
    private final int ydiff;

    public NoFocusImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation,
            int guiBitmapW, int guiBitmapH, OnPress pOnPress)
    {
        super(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, guiBitmapW, guiBitmapH, pOnPress);
        ydiff = pYDiffTex;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.renderTexture(pGuiGraphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width,
                this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public void renderTexture(GuiGraphics pGuiGraphics, ResourceLocation pTexture, int pX, int pY, int pUOffset, int pVOffset, int pTextureDifference,
            int pWidth, int pHeight, int pTextureWidth, int pTextureHeight)
    {
        {
            int i = pVOffset;
            if (!this.isActive())
            {
                i = pVOffset + pTextureDifference * 3;
            } else if (this.isHovered())
            {
                i = pVOffset + pTextureDifference;

            } else if (this.isFocused())
            {
                i = pVOffset + pTextureDifference * 2;
            }
            RenderSystem.enableDepthTest();
            pGuiGraphics.blit(pTexture, pX, pY, (float) pUOffset, (float) i, pWidth, pHeight, pTextureWidth, pTextureHeight);
        }
    }
}
