package pictisoft.cipherwright.client;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.blocks.kubejstable.KubeJSTableBlockEntity;
import pictisoft.cipherwright.registry.BlockRegistry;
import pictisoft.cipherwright.util.Chatter;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CipherWrightMod.MODID, value = Dist.CLIENT)
public class ClientEventSubscriber
{
    @SubscribeEvent
    public static void registerColorHandlers(RegisterColorHandlersEvent.Block event)
    {
//        event.register((state, world, pos, tintIndex) -> {
//            if (world != null && pos != null)
//            {
//                BlockEntity be = world.getBlockEntity(pos);
//                if (be instanceof KubeJSTableBlockEntity kubebe)
//                {
//                    //Chatter.chat("client? " + be.getLevel().isClientSide);
//                    return kubebe.getColor(tintIndex);
//                }
//            }
//            return 0xFFFFFF; // Default white
//        }, BlockRegistry.KUBEJS_TABLE_BLOCK.get());
    }
}
