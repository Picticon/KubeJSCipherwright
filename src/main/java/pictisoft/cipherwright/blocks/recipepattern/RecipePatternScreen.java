//package pictisoft.cipherwright.blocks.recipepattern;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Recipe;
//
//import java.awt.*;
//import java.util.ArrayList;
//
//public class RecipePatternScreen extends AbstractContainerScreen<RecipePatternMenu>
//{
//    private static final ResourceLocation TEXTURE = new ResourceLocation("modid", "textures/gui/recipe_pattern.png");
//    private Button generateButton;
//    private Button typeSelectButton;
//    //private final List<Decorator> activeDecorators = new ArrayList<>();
//
//    public RecipePatternScreen(RecipePatternMenu menu, Inventory inventory, Component title)
//    {
//        super(menu, inventory, title);
//        this.imageWidth = 256;
//        this.imageHeight = 222;
//    }
//
//    @Override
//    protected void init()
//    {
//        super.init();
//
////        // Add buttons for generation and type selection
////        this.generateButton = addRenderableWidget(Button.builder(Component.translatable("button.generate"),
////                        button -> generateRecipe())
////                .pos(leftPos + 180, topPos + 100)
////                .size(60, 20)
////                .build());
////
////        this.typeSelectButton = addRenderableWidget(Button.builder(Component.translatable("button.change_type"),
////                        button -> showTypeSelection())
////                .pos(leftPos + 10, topPos + 10)
////                .size(80, 20)
////                .build());
////
//        setupDecorators();
//    }
//
//    private void setupDecorators()
//    {
////        activeDecorators.clear();
//        var type = menu.getBlockEntity().getCipher();
//        if (type == null) return;
//
//        // Set up decorators based on recipe type
////        for (Decorator decorator : type.getDecorators()) {
////            activeDecorators.add(decorator);
////        }
//    }
//
//    @Override
//    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick)
//    {
//        super.render(gui, mouseX, mouseY, partialTick);
//
//        // Render ghost slots
//        for (GhostSlot slot : menu.getGhostSlots())
//        {
//            renderGhostSlot(gui, slot);
//        }
//
//        // Render decorators
////        for (Decorator decorator : activeDecorators) {
////            renderDecorator(poseStack, decorator);
////        }
//
//        renderTooltip(gui, mouseX, mouseY);
//    }
//
//    @Override
//    protected void renderBg(GuiGraphics gui, float pPartialTick, int pMouseX, int pMouseY)
//    {
//        renderBackground(gui);
//    }
//
//    private void renderGhostSlot(GuiGraphics gui, GhostSlot slot)
//    {
//        // Render the slot background
//        //gui.blit( leftPos + slot.x, topPos + slot.y, 0, 0, 18, 18);
//
//        // Render the item if there is one
//        ItemStack stack = slot.getStack();
//        if (!stack.isEmpty())
//        {
//            //itemRenderer.renderAndDecorateItem(stack, leftPos + slot.x + 1, topPos + slot.y + 1);
//        }
//
//        // Render tag indicator if it has a tag
//        if (slot.hasTag())
//        {
//            //gui.blit(leftPos + slot.x + 10, topPos + slot.y, 240, 0, 16, 16);
//        }
//    }
//
////    private void renderDecorator(GuiGraphics gui)
////    {
////        // Render decorator based on type (arrow, progress, etc.)
////        if (decorator.getType() == DecoratorType.ARROW)
////        {
////            blit(poseStack, leftPos + decorator.getX(), topPos + decorator.getY(), 176, 0, 22, 15);
////        } else if (decorator.getType() == DecoratorType.PROGRESS)
////        {
////            // Render progress bar with current progress
////            int progress = getMenu().getProgressValue();
////            int maxProgress = getMenu().getMaxProgressValue();
////            int width = (int) (decorator.getWidth() * (progress / (float) maxProgress));
////
////            blit(poseStack, leftPos + decorator.getX(), topPos + decorator.getY(),
////                    176, 15, width, decorator.getHeight());
////        }
////    }
//
//    /*private void generateRecipe()
//    {
//        // Generate script or JSON using templates and slot data
//        var type = menu.getBlockEntity().getCipher();
//        if (type == null) return;
//
//        String template = type.getScriptTemplate(); // or getJsonTemplate()
//
//        // Replace placeholders in template with actual values
//        for (GhostSlot slot : menu.getGhostSlots())
//        {
//            ItemStack stack = slot.getStack();
//            ResourceLocation tagId = slot.getTagId();
//
//            String placeholder = slot.isInput() ?
//                    String.format("{{input_%d}}", slot.getId()) :
//                    String.format("{{output_%d}}", slot.getId());
//
//            String replacement;
//            if (tagId != null)
//            {
//                replacement = "#" + tagId.toString();
//            } else if (!stack.isEmpty())
//            {
//                replacement = stack.getItem().getRegistryName().toString();
//            } else
//            {
//                replacement = "minecraft:air";
//            }
//
//            template = template.replace(placeholder, replacement);
//        }
//
//        // Replace numeric and string placeholders
//        for (NumericField field : type.getNumericFields())
//        {
//            String placeholder = "{{" + field.getId() + "}}";
//            double value = menu.getBlockEntity().getNumericValue(field.getId());
//            template = template.replace(placeholder, String.valueOf(value));
//        }
//
//        // Similar for string fields
//
//        // Copy to clipboard
//        Minecraft.getInstance().keyboardHandler.setClipboard(template);
//    } */
//
//
////    private void showTypeSelection()
////    {
////        // Show a list of available recipe types to select from
////        RecipeTypeSelectionScreen screen = new RecipeTypeSelectionScreen(this);
////        this.minecraft.setScreen(screen);
////    }
////
////    // JEI integration method
////    public void setRecipeFromJEI(Recipe<?> recipe)
////    {
////        // Determine recipe type from the recipe
////        RecipeType matchingType = findMatchingType(recipe);
////        if (matchingType == null) return;
////
////        // Set the recipe type
////        menu.getBlockEntity().setRecipeType(matchingType);
////
////        // Fill slots based on recipe
////        fillSlotsFromRecipe(recipe);
////
////        // Update UI
////        setupGhostSlots();
////        setupDecorators();
////    }
////
////    private RecipeType findMatchingType(Recipe<?> recipe)
////    {
////        // Find the matching recipe type based on the recipe class or other attributes
////        // ...
////    }
////
////    private void fillSlotsFromRecipe(Recipe<?> recipe)
////    {
////        // Map recipe ingredients to slots
////        // ...
////    }
//
//}