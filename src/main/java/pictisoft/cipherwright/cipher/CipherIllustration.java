package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.util.GUIElementRenderer;

import java.util.Map;

public class CipherIllustration extends CipherGridObject
{
    protected String text = "";
    protected String item = "";
    private float scale = 1f;
    private float alpha = 1f;
    private String formula;
    private String condition;

    //    public static void load(List<CipherIllustration> illustrations, JsonObject json)
//    {
//        var template = TryParse(json);
//        illustrations.add(template);
//    }
    public CipherIllustration()
    {
        type = "picture";
    }

    @Override
    public <T extends CipherGridObject> void readJson(T ret2, @NotNull JsonObject input)
    {
        super.readJson(ret2, input);
        if (ret2 instanceof CipherIllustration ret)
        {
            if (input.has("text")) ret.text = input.get("text").getAsString();
            if (input.has("formula")) ret.formula = input.get("formula").getAsString();
            if (input.has("item")) ret.item = input.get("item").getAsString();
            if (input.has("scale")) ret.scale = input.get("scale").getAsFloat();
            if (input.has("alpha")) ret.alpha = input.get("alpha").getAsFloat();
            if (input.has("condition")) ret.condition = input.get("condition").getAsString();
        }
    }

    @Override
    boolean isSupportedType(String type)
    {
        if (type == null || type.isEmpty()) return false;
        return type.equals("left_arrow")
                || type.equals("right_arrow")
                || type.equals("up_arrow")
                || type.equals("down_arrow")
                || type.equals("text")
                || type.equals("item")
                || type.equals("highlight")
                ;
    }

    public void render(GuiGraphics gui, GUIElementRenderer guiRenderer, Map<String, String> cipherParameters)
    {
        if (condition != null)
        {
            if (!CipherParameter.testCondition(condition, cipherParameters)) return;
        }
        gui.pose().pushPose();
        if (alpha > 0 && alpha < 1)
        {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        }
        switch (type)
        {
            case "left_arrow":
                guiRenderer.drawLeftArrow(gui, x, y, scale);
                break;
            case "right_arrow":
                guiRenderer.drawRightArrow(gui, x, y, scale);
                break;
            case "up_arrow":
                guiRenderer.drawUpArrow(gui, x, y, scale);
                break;
            case "down_arrow":
                guiRenderer.drawDownArrow(gui, x, y, scale);
                break;
            case "highlight":
                guiRenderer.drawSlotHighlight(gui, x, y);
                break;
            case "text":
                renderText(gui, cipherParameters);
                break;
            case "item":

                renderItem(gui);
                break;
        }
        if (alpha > 0 && alpha < 1)
        {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
        //gui.setColor(1, 1, 1, 1);
        gui.pose().popPose();
    }

    private void renderItem(GuiGraphics gui)
    {
        var item = BuiltInRegistries.ITEM.get(new ResourceLocation(this.item));
        if (item.equals(Items.AIR)) item = Items.CRAFTING_TABLE;
        if (item != Items.AIR)
        {
            gui.pose().translate(getPosX(), getPosY(), 0);
            gui.pose().translate(8f, 8f, 0);      // -8 to adjust for the internal translation done in renderitem
            gui.pose().scale(scale, scale, 1);
            gui.pose().translate(-8f, -8f, 0);      // -8 to adjust for the internal translation done in renderitem
            gui.renderItem(new ItemStack(item, 1), 0, 0);
//            if (alpha > 0 && alpha < 1)
//            {
//                RenderSystem.enableBlend();
//                RenderSystem.defaultBlendFunc();
//                RenderSystem.setShaderColor(1,1,1,1);
//                gui.pose().translate(0,0,1000);
//                gui.fill(0,  0, 16, 16,
//                        (int) ((1.0f - alpha ) * 255) << 24 | 0x888888);
//            }
            //renderItemWithAlpha(gui, new ItemStack(item, 1), getPosX(), getPosY(), alpha);
            //renderItemWithAlpha(gui, new ItemStack(item, 1), getPosX() - 3, getPosY() + 3, alpha);
        }
    }

//    public void renderItemWithAlpha(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float alpha)
//    {
//    THIS CODE MAKES A POORLY LIGHTED BLOCK RENDER
//        PoseStack poseStack = guiGraphics.pose();
//        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
//
//        poseStack.pushPose();
//        poseStack.translate(x, y, 100); // Z-level for rendering
//        poseStack.translate(8, 8, 0);
//        poseStack.scale(16, -16, 16);
//
//        // Set up rendering with alpha
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
//
//        Minecraft.getInstance().getItemRenderer().render(
//                stack,
//                ItemDisplayContext.GUI,
//                false,
//                poseStack,
//                guiGraphics.bufferSource(),
//                15728880, // Full brightness
//                OverlayTexture.NO_OVERLAY,
//                model
//        );
//
//        guiGraphics.flush(); // Important! Flush the buffer
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset
//
//        poseStack.popPose();
//    }

    private void renderText(GuiGraphics gui, Map<String, String> cipherParameters)
    {
        var font = Minecraft.getInstance().font;
        var x = this.getPosX();
        var toprint = this.text;
        if (formula != null)
        {
            if (formula.startsWith("ticks2seconds:"))
            {
                var f = formula.replace("ticks2seconds:", "");
                if (cipherParameters.containsKey(f))
                {
                    try
                    {
                        var tt = Integer.valueOf(cipherParameters.get(f));
                        var ttt = tt / 20;
                        toprint = ttt + "s";
                    } catch (Exception ignored)
                    {
                    }
                }
            }
        }

        var textwidth = font.width(toprint);
        if (this.flags.contains("rightalign"))
        {
            x = this.getPosX() + this.getWidth() - textwidth - 4;
        }
        if (scale > 0 && scale != 0f)
        {
            gui.pose().scale(scale, scale, scale);
        }
        gui.drawString(font, toprint, x, this.getPosY() + 1, 0x404040, false);
    }
}
