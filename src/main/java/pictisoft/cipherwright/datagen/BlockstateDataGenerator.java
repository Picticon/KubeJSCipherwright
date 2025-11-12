package pictisoft.cipherwright.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.registry.BlockRegistry;
import pictisoft.cipherwright.registry.FluidRegistry;

import java.util.function.Function;

public class BlockstateDataGenerator extends BlockStateProvider
{
    public BlockstateDataGenerator(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, CipherWrightMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        // ENTITIES
        // blockTopBottomSideWithItem(BlockRegistry.KUBEJS_TABLE_BLOCK);
        //getVariantBuilder(BlockRegistry.KUBEJS_TABLE_BLOCK.get());
        simpleCubeWithTint(BlockRegistry.KUBEJS_TABLE_BLOCK, "kubejs_table_block");
        onlyItem(BlockRegistry.KUBEJS_TABLE_BLOCK);

        registerFluid(FluidRegistry.DEAD_WATER);
        registerFluid(FluidRegistry.DEADER_WATER);
        registerFluid(FluidRegistry.DEADEST_WATER);

    }

    private void simpleCubeWithTint(RegistryObject<Block> block, String name)
    {
        ModelFile model = models().withExistingParent(name, "minecraft:block/block")
                .texture("side", modLoc("block/" + name + "_side"))
                .texture("top", modLoc("block/" + name + "_top"))
                .texture("bottom", modLoc("block/" + name + "_bottom"))
                .texture("particle", modLoc("block/" + name + "_side"))
                .element()
                .from(0, 0, 0)
                .to(16, 16, 16)

                .face(Direction.DOWN)
                .uvs(0, 0, 16, 16)
                .texture("#bottom")
                .tintindex(0)
                .cullface(Direction.DOWN)
                .end()

                .face(Direction.UP)
                .uvs(0, 0, 16, 16)
                .texture("#top")
                .tintindex(0)
                .cullface(Direction.UP)
                .end()

                .face(Direction.NORTH)
                .uvs(0, 0, 16, 16)
                .texture("#side")
                .tintindex(0)
                .cullface(Direction.NORTH)
                .end()

                .face(Direction.SOUTH)
                .uvs(0, 0, 16, 16)
                .texture("#side")
                .tintindex(0)
                .cullface(Direction.SOUTH)
                .end()

                .face(Direction.EAST)
                .uvs(0, 0, 16, 16)
                .texture("#side")
                .tintindex(0)
                .cullface(Direction.EAST)
                .end()

                .face(Direction.WEST)
                .uvs(0, 0, 16, 16)
                .texture("#side")
                .tintindex(0)
                .cullface(Direction.WEST)
                .end()

                .end();
        simpleBlock(block.get(), model);
    }

    private void registerFluid(RegistryObject<LiquidBlock> regob)
    {
        simpleBlock(regob.get(), models().getExistingFile(new ResourceLocation("minecraft:block/water")));
    }

    // This returns the portion of the block after the namespace "iconicgadgets:block/ball" = "block/ball"
    private String blockname(Block block)
    {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }

    // This appends a suffix to the resourcelocation "block/ball" -> "block/ball_end".
    private ResourceLocation extend(ResourceLocation rl, String suffix)
    {
        return new ResourceLocation(rl.getNamespace(), rl.getPath() + suffix);
    }

    private ResourceLocation extend(RegistryObject<Block> regBlock, String suffix)
    {
        return extend(regBlock.getId(), suffix);
    }

    // makes a simple block along with the matching item
    private void blockWithItem(RegistryObject<Block> regBlock)
    {
        simpleBlockWithItem(regBlock.get(), cubeAll(regBlock.get()));
    }

    // Makes a cube with _side, _bottom, _top, along with the item.
    private void blockTopBottomSideWithItem(RegistryObject<Block> regBlock)
    {
        var name = this.blockname(regBlock.get());
        simpleBlockWithItem(regBlock.get(),
                this.models().cubeBottomTop(name, blockregloc(regBlock, "_side"), blockregloc(regBlock, "_bottom"), blockregloc(regBlock, "_top")));
        onlyItem(regBlock);
    }

    private ResourceLocation texloc(String name)
    {
        return new ResourceLocation(CipherWrightMod.MODID + ":block/" + name);
    }

    private ResourceLocation blockregloc(RegistryObject<Block> regBlock, String side)
    {
        return new ResourceLocation(blockloc(regBlock, side));
    }

    private String blockloc(RegistryObject<Block> regBlock)
    {
        return CipherWrightMod.MODID + ":block/" + ForgeRegistries.BLOCKS.getKey(regBlock.get()).getPath();
    }

    private String blockloc(RegistryObject<Block> regBlock, String suffix)
    {
        return blockloc(regBlock) + suffix;
    }


    private void saplingBlock(RegistryObject<Block> regBlock)
    {
        simpleBlock(regBlock.get(), models().cross(ForgeRegistries.BLOCKS.getKey(regBlock.get()).getPath(), blockTexture(regBlock.get())).renderType("cutout"));
    }

    // use this for inventory only item blocks
    private void onlyItem(RegistryObject<Block> regBlock)
    {
        simpleBlockItem(regBlock.get(), new ModelFile.UncheckedModelFile(blockloc(regBlock)));
    }

    private <T extends Block> void makeFacingBlock(RegistryObject<Block> regBlock)
    {
        this.horizontalBlock(regBlock.get(), blockregloc(regBlock, "_side"), blockregloc(regBlock, "_front"), blockregloc(regBlock, "_top"));
    }

    public void directionalBlockFromNorth(Block block, ModelFile model)
    {
        this.directionalBlockFromNorth(block, ($) -> {
            return model;
        });
    }

    public void directionalBlockFromNorth(Block block, Function<BlockState, ModelFile> modelFunc)
    {
        this.getVariantBuilder(block).forAllStates((state) -> {
            Direction dir = (Direction) state.getValue(BlockStateProperties.FACING);
            var ret = ConfiguredModel.builder().modelFile((ModelFile) modelFunc.apply(state));
            switch (dir)
            {
                case SOUTH:
                    ret.rotationY(180);
                    break;
                case WEST:
                    ret.rotationY(270);
                    break;
                case EAST:
                    ret.rotationY(90);
                    break;
                case UP:
                    ret.rotationX(-90);
                    ret.rotationY(90);
                    break;
                case DOWN:
                    ret.rotationX(90);
                    ret.rotationY(90);
                    break;
            }
            return ret.build();
        });
    }
}