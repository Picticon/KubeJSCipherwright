//package pictisoft.cipherwright.blocks.recipepattern;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.client.gui.widget.ScrollPanel;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TagSelectionScreen extends Screen
//{
//    private final GhostSlot slot;
//    private final List<ResourceLocation> availableTags = new ArrayList<>();
//    private ScrollPanel scrollPanel;
//
//    public TagSelectionScreen(GhostSlot slot) {
//        super(Component.translatable("screen.tag_selection"));
//        this.slot = slot;
//
//        // Populate available tags for the item
//        ItemStack stack = slot.getStack();
//        if (!stack.isEmpty()) {
//            populateAvailableTags(stack.getItem());
//        }
//    }
//
//    private void populateAvailableTags(Item item) {
//        // Find all tags that contain this item
//        ForgeRegistries.ITEMS.tags().stream()
//                .filter(tag -> tag.contains(item))
//                .map(tag -> tag.getName())
//                .forEach(availableTags::add);
//    }
//
//    @Override
//    protected void init() {
//        // Create scroll panel with tags
//        this.scrollPanel = new ScrollPanel(minecraft, width, height, 50, height - 50, 20);
//
//        for (int i = 0; i < availableTags.size(); i++) {
//            final int index = i;
//            ResourceLocation tagId = availableTags.get(i);
//
//            Button button = Button.builder(Component.literal(tagId.toString()),
//                            btn -> selectTag(tagId))
//                    .pos(width / 2 - 100, 50 + i * 25)
//                    .size(200, 20)
//                    .build();
//
//            scrollPanel.addButton(button);
//        }
//
//        // Add back button
//        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"),
//                        btn -> minecraft.setScreen(null))
//                .pos(width / 2 - 100, height - 30)
//                .size(200, 20)
//                .build());
//    }
//
//    private void selectTag(ResourceLocation tagId) {
//        // Set the tag for the slot
//        slot.setTagId(tagId);
//        minecraft.setScreen(null);
//    }
//
//    @Override
//    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
//        renderBackground(poseStack);
//        scrollPanel.render(poseStack, mouseX, mouseY, partialTick);
//
//        drawCenteredString(poseStack, font, title, width / 2, 20, 0xFFFFFF);
//
//        super.render(poseStack, mouseX, mouseY, partialTick);
//    }
//}