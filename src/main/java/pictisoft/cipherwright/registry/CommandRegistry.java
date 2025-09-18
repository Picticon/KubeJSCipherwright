package pictisoft.cipherwright.registry;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pictisoft.cipherwright.CipherWrightMod;
import pictisoft.cipherwright.cipher.Cipher;

@Mod.EventBusSubscriber(modid = CipherWrightMod.MODID)
public class CommandRegistry
{
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("cw")
                .then(Commands.literal("reload")
                        .executes(context -> {
                            return reloadCommand(context.getSource());
                        })
                )
        );
    }

    private static int reloadCommand(CommandSourceStack source)
    {
        // Your reload logic here
        source.sendSuccess(() -> Component.literal("This don't work my man!").withStyle(ChatFormatting.GREEN), true);

        //Cipher.TryParse()

        return 1; // return value is command "success" count
    }
}
