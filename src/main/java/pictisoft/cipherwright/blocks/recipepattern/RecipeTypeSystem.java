//package pictisoft.cipherwright.blocks.recipepattern;
//
//import net.minecraft.advancements.critereon.ItemPredicate;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.List;
//import java.util.Map;
//
//public class RecipeType {
//    private final ResourceLocation id;
//    private final List<SlotDefinition> inputs;
//    private final List<SlotDefinition> outputs;
//    private final List<NumericField> numericFields;
//    private final List<StringField> stringFields;
//    private final List<Decorator> decorators;
//    private final String scriptTemplate;
//    private final String jsonTemplate;
//
//    // Constructor, getters, etc.
//
//    public static class SlotDefinition {
//        private final int x;
//        private final int y;
//        private final List<ItemPredicate> limitations;
//        private final Map<String, Object> traits;
//
//        // Constructor, getters, etc.
//    }
//
//    public static class NumericField {
//        private final String id;
//        private final String label;
//        private final double defaultValue;
//        private final double minValue;
//        private final double maxValue;
//
//        // Constructor, getters, etc.
//    }
//
//    // Other inner classes for StringField, Decorator, etc.
//}