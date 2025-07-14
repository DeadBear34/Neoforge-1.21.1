package net.miolineara.amonite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.miolineara.amonite.Amonite;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Perintah /register dan /login sudah dihapus

        // Perintah Admin: /setlevel <pemain> <level>
        dispatcher.register(Commands.literal("setlevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 99))
                                .executes(context -> {
                                    ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
                                    int level = IntegerArgumentType.getInteger(context, "level");

                                    int sourceLevel = Amonite.playerDAO.getPermissionLevel(sourcePlayer.getUUID().toString());
                                    if (sourceLevel < 2) {
                                        context.getSource().sendFailure(Component.literal("Anda tidak punya izin untuk melakukan ini."));
                                        return 0;
                                    }

                                    Amonite.playerDAO.setPermissionLevel(targetPlayer.getUUID().toString(), level);
                                    context.getSource().sendSuccess(() -> Component.literal("Level izin untuk " + targetPlayer.getName().getString() + " telah diatur ke " + level), true);
                                    targetPlayer.sendSystemMessage(Component.literal("Level izin Anda telah diubah menjadi " + level));
                                    return 1;
                                }))));

        // Perintah lain seperti /ban bisa tetap ada jika Anda sudah membuatnya
    }
}