package pictisoft.cipherwright.registry;

import com.mojang.datafixers.types.Type;
import io.netty.util.Attribute;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;

public class BlockEntityRegistry
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,
            CipherWrightMod.MODID);

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<BlockEntityType<KubeJSTableBlockEntity>> KUBEJS_TABLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("kubejs_table_block_entity",
            () -> BlockEntityType.Builder.of(KubeJSTableBlockEntity::new, BlockRegistry.KUBEJS_TABLE_BLOCK.get()).build((Type) null));

}
