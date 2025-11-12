package pictisoft.cipherwright.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.registry.FluidRegistry;

import java.util.concurrent.CompletableFuture;

public class FluidTagGen extends FluidTagsProvider
{
    public FluidTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper)
    {
        super(output, lookup, CipherWrightMod.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        tag(TagKey.create(Registries.FLUID, new ResourceLocation(CipherWrightMod.MODID, "dead"))).add(FluidRegistry.DEAD_WATER_SOURCE.get())
                .add(FluidRegistry.DEADER_WATER_SOURCE.get())
                .add(FluidRegistry.DEADEST_WATER_SOURCE.get());

        tag(TagKey.create(Registries.FLUID, new ResourceLocation(CipherWrightMod.MODID, "deader"))).add(FluidRegistry.DEADER_WATER_SOURCE.get())
                .add(FluidRegistry.DEADEST_WATER_SOURCE.get());

        tag(TagKey.create(Registries.FLUID, new ResourceLocation("forge", "lava")))
                .add(Fluids.LAVA);
        tag(TagKey.create(Registries.FLUID, new ResourceLocation("forge", "water")))
                .add(Fluids.WATER);
    }
}