package pictisoft.cipherwright.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Chatter
{
    public static MinecraftServer staticServer;

    public static void sendToAllPlayers(String string)
    {
        if (staticServer == null)
            return;
        var msg = Component.literal(string);
        for (var player : staticServer.getPlayerList().getPlayers())
        {
            player.displayClientMessage(msg, false);
        }
    }

    public static void chat(String message)
    {
        try
        {
            var mc = Minecraft.getInstance();
            mc.player.displayClientMessage(Component.literal(message), false);
        } catch (Exception ex)
        {
            sendToAllPlayers("Server: " + message);
        }
    }

    public static void sendToPlayer(Player player, String string)
    {
        player.displayClientMessage(Component.literal(string), false);
    }

    public static void chatDebug(Level level, String message)
    {
        if (level == null)
        {
            var mc = Minecraft.getInstance();
            if (mc.player != null)
                mc.player.displayClientMessage(Component.literal("Null: " + message).withStyle(ChatFormatting.BLUE), false);
            return;
        }
        if (level.isClientSide())
        {
            var mc = Minecraft.getInstance();
            if (mc.player != null)
                mc.player.displayClientMessage(Component.literal("Client: " + message).withStyle(ChatFormatting.GREEN), false);
        } else
        {
            var mc = Minecraft.getInstance();
            if (mc.player != null)
                mc.player.displayClientMessage(Component.literal("Server: " + message).withStyle(ChatFormatting.RED), false);
            //Chatter.chat(message);
        }
    }
}
