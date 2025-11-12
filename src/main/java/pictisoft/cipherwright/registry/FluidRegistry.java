package pictisoft.cipherwright.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;

import java.util.function.Consumer;

public class FluidRegistry
{
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, CipherWrightMod.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CipherWrightMod.MODID);
    public static void register(IEventBus eventBus)
    {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }
    
    public static final RegistryObject<FluidType> DEAD_WATER_FLUID_TYPE = FLUID_TYPES.register("dead_water_fluid_type", () -> new FluidType(
            FluidType.Properties.create().descriptionId("dead_water_fluid_type").fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(
                    true).supportsBoating(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY,
                    SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).canHydrate(false).viscosity(1000))
    {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
        {
            consumer.accept(new IClientFluidTypeExtensions()
            {
                public static final ResourceLocation FLUID_SOURCE_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/dead_water_source");
                public static final ResourceLocation FLUID_FLOWING_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/dead_water_flowing");

                @Override
                public ResourceLocation getStillTexture()
                {
                    return FLUID_SOURCE_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture()
                {
                    return FLUID_FLOWING_TEXTURE;
                }
            });
        }
    });

    private static ForgeFlowingFluid.Properties makeDeadWaterProperties()
    {
        return new ForgeFlowingFluid.Properties(DEAD_WATER_FLUID_TYPE, DEAD_WATER_SOURCE, DEAD_WATER_FLOWING).block(DEAD_WATER).bucket(
                ItemRegistry.DEAD_WATER_BUCKET);
    }

    public static final RegistryObject<Fluid> DEAD_WATER_SOURCE = FLUIDS.register("dead_water_source",
            () -> new ForgeFlowingFluid.Source(makeDeadWaterProperties()));
    public static final RegistryObject<FlowingFluid> DEAD_WATER_FLOWING = FLUIDS.register("dead_water_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeDeadWaterProperties()));
    public static final RegistryObject<LiquidBlock> DEAD_WATER = BlockRegistry.BLOCKS.register("dead_water",
            () -> new LiquidBlock(DEAD_WATER_FLOWING, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    // ------------------ //

    public static final RegistryObject<FluidType> DEADER_WATER_FLUID_TYPE = FLUID_TYPES.register("deader_water_fluid_type", () -> new FluidType(
            FluidType.Properties.create().descriptionId("deader_water_fluid_type").fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(
                    true).supportsBoating(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY,
                    SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).canHydrate(false).viscosity(1000))
    {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
        {
            consumer.accept(new IClientFluidTypeExtensions()
            {
                public static final ResourceLocation FLUID_SOURCE_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/deader_water_source");
                public static final ResourceLocation FLUID_FLOWING_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/deader_water_flowing");

                @Override
                public ResourceLocation getStillTexture()
                {
                    return FLUID_SOURCE_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture()
                {
                    return FLUID_FLOWING_TEXTURE;
                }
            });
        }
    });

    private static ForgeFlowingFluid.Properties makeDeaderWaterProperties()
    {
        return new ForgeFlowingFluid.Properties(DEADER_WATER_FLUID_TYPE, DEADER_WATER_SOURCE, DEADER_WATER_FLOWING).block(DEADER_WATER).bucket(
                ItemRegistry.DEADER_WATER_BUCKET);
    }

    public static final RegistryObject<Fluid> DEADER_WATER_SOURCE = FLUIDS.register("deader_water_source",
            () -> new ForgeFlowingFluid.Source(makeDeaderWaterProperties()));
    public static final RegistryObject<FlowingFluid> DEADER_WATER_FLOWING = FLUIDS.register("deader_water_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeDeaderWaterProperties()));
    public static final RegistryObject<LiquidBlock> DEADER_WATER = BlockRegistry.BLOCKS.register("deader_water",
            () -> new LiquidBlock(DEADER_WATER_FLOWING, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    // ------------------ //

    public static final RegistryObject<FluidType> DEADEST_WATER_FLUID_TYPE = FLUID_TYPES.register("deadest_water_fluid_type", () -> new FluidType(
            FluidType.Properties.create().descriptionId("deadest_water_fluid_type").fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(
                    true).supportsBoating(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY,
                    SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).canHydrate(false).viscosity(1000))
    {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
        {
            consumer.accept(new IClientFluidTypeExtensions()
            {
                public static final ResourceLocation FLUID_SOURCE_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/deadest_water_source");
                public static final ResourceLocation FLUID_FLOWING_TEXTURE = new ResourceLocation(CipherWrightMod.MODID + ":block/deadest_water_flowing");

                @Override
                public ResourceLocation getStillTexture()
                {
                    return FLUID_SOURCE_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture()
                {
                    return FLUID_FLOWING_TEXTURE;
                }
            });
        }
    });

    private static ForgeFlowingFluid.Properties makeDeadestWaterProperties()
    {
        return new ForgeFlowingFluid.Properties(DEADEST_WATER_FLUID_TYPE, DEADEST_WATER_SOURCE, DEADEST_WATER_FLOWING).block(DEADEST_WATER).bucket(
                ItemRegistry.DEADEST_WATER_BUCKET);
    }

    public static final RegistryObject<Fluid> DEADEST_WATER_SOURCE = FLUIDS.register("deadest_water_source",
            () -> new ForgeFlowingFluid.Source(makeDeadestWaterProperties()));
    public static final RegistryObject<FlowingFluid> DEADEST_WATER_FLOWING = FLUIDS.register("deadest_water_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeDeadestWaterProperties()));
    public static final RegistryObject<LiquidBlock> DEADEST_WATER = BlockRegistry.BLOCKS.register("deadest_water",
            () -> new LiquidBlock(DEADEST_WATER_FLOWING, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

}
