package pictisoft.cipherwright.cipher;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import pictisoft.cipherwright.util.GUIElementRenderer;

import java.util.ArrayList;

// This class defines a "well", or something that will hold a fake Slot object

public class CipherWell extends CipherPathObject
{
    private String BGItem;

    public void drawBackGround(GuiGraphics gui, GUIElementRenderer guiRenderer)
    {
        if (this.getBGItem() != null)
        {
            var item = BuiltInRegistries.ITEM.get(new ResourceLocation(this.getBGItem()));
            if (!item.equals(Items.AIR))
            {

                gui.pose().pushPose();
                gui.pose().translate(getPosX(), getPosY(), 0);
                //gui.pose().translate(8f, 8f, 0);      // -8 to adjust for the internal translation done in renderitem

                gui.renderItem(new ItemStack(item, 1), 0, 0);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                gui.pose().translate(0, 0, 200);
                gui.fill(0, 0, 16, 16,
                        (int) (.7f * 255) << 24 | 0x888888);
                gui.pose().popPose();
            }
        }
    }

    public String getBGItem()
    {
        return BGItem;
    }

    public void setBGItem(String bgItem)
    {
        this.BGItem = bgItem;
    }

    public enum STYLE_ENUM
    {
        normal,
        large,
        bucket;
    }

    private static final Logger LOGGER = LogManager.getLogger();

    protected STYLE_ENUM style = STYLE_ENUM.normal;
    protected String ingredient;
    protected boolean single;
    protected boolean allowtag = true;
    protected boolean allowfluid = false;
    protected boolean allowfluidtag = false;
    protected boolean allowitem = true;
    protected int maxFluidAmount = 1000;
    private ArrayList<CipherParameter> parameters = new ArrayList<>();

    //private int startIndex = -1;
    public STYLE_ENUM getStyle()
    {
        return style;
    }

    public boolean getSingle()
    {
        return single;
    }

    public int getMaxFluidAmount()
    {
        return maxFluidAmount;
    }

    public boolean allowTag()
    {
        return allowtag;
    }

    public boolean allowFluid()
    {
        return allowfluid;
    }

    public boolean allowItem()
    {
        return allowitem;
    }

    public boolean allowFluidTag()
    {
        return allowfluidtag;
    }


    @Override
    public int getWidth()
    {
        return getStyle() == STYLE_ENUM.large ? large_width : width;
    }

    @Override
    public int getHeight()
    {
        return getStyle() == STYLE_ENUM.large ? large_height : height;
    }

    @Override
    public <T extends CipherGridObject> void readJson(T ret, @NotNull JsonObject input)
    {
        super.readJson(ret, input);
        if (ret instanceof CipherWell cw)
        {
            if (input.has("index")) cw.index = input.get("index").getAsInt();
            if (input.has("ingredient")) cw.ingredient = input.get("ingredient").getAsString();
            if (input.has("single")) cw.single = input.get("single").getAsBoolean();
            if (input.has("maxfluid")) cw.maxFluidAmount = input.get("maxfluid").getAsInt();
            if (input.has("style")) cw.style = convertStyle(input.get("style").getAsString());
            if (input.has("allowtag")) cw.allowtag = input.get("allowtag").getAsBoolean();
            if (input.has("allowfluid")) cw.allowfluid = input.get("allowfluid").getAsBoolean();
            if (input.has("allowfluidtag")) cw.allowfluidtag = input.get("allowfluidtag").getAsBoolean();
            if (input.has("allowitem")) cw.allowitem = input.get("allowitem").getAsBoolean();
            if (input.has("bgitem")) cw.setBGItem(input.get("bgitem").getAsString());
            //if (input.has("bgalpha")) cw.setBGAlpha(input.get("bgalpha").getAsString());
            if (input.has("members") && input.get("members").isJsonArray())
            {
                var parray = input.get("members").getAsJsonArray();
                parameters = new ArrayList<>();
                for (var parameter : parray)
                {
                    try
                    {
                        CipherFactory.LoadInputsFromJson(CipherParameter.class, parameter.getAsJsonObject(), parameters);
                    } catch (Exception e)
                    {
                        LOGGER.debug("Error while loading Ciphers (recipe types): " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private STYLE_ENUM convertStyle(String style)
    {
        try
        {
            return STYLE_ENUM.valueOf(style.toLowerCase());
        } catch (Exception ignored)
        {
        }
        return STYLE_ENUM.normal;
    }

    public ArrayList<CipherParameter> getWellParameters()
    {
        return parameters;
    }
}
