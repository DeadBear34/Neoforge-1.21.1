package net.miolineara.amonite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.authlib.GameProfile;
import net.miolineara.amonite.Amonite;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.miolineara.amonite.Config;

import java.util.Collection;
import java.util.UUID;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

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

        // --- PERINTAH BLACKLIST (Dengan Disconnect Otomatis) ---
        dispatcher.register(Commands.literal("blacklist")
                .requires(source -> {
                    try {
                        return Amonite.playerDAO.getPermissionLevel(source.getPlayerOrException().getUUID().toString()) >= 1;
                    } catch (Exception e) {
                        return source.hasPermission(2);
                    }
                })
                // Sub-perintah: /blacklist add <username> [alasan]
                .then(Commands.literal("add")
                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                // Versi tanpa alasan
                                .executes(context -> {
                                    String reason = "Tidak ada alasan";
                                    return executeBlacklist(context.getSource(), GameProfileArgument.getGameProfiles(context, "target"), reason);
                                })
                                // Versi dengan alasan
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String reason = StringArgumentType.getString(context, "reason");
                                            return executeBlacklist(context.getSource(), GameProfileArgument.getGameProfiles(context, "target"), reason);
                                        }))))
                // Sub-perintah: /blacklist remove <username>
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "target");
                                    GameProfile targetProfile = profiles.iterator().next();
                                    String targetUuid = targetProfile.getId().toString();
                                    String targetName = targetProfile.getName();

                                    Amonite.blacklistDAO.removeFromBlacklist(targetUuid);
                                    context.getSource().sendSuccess(() -> Component.literal(targetName + " telah dihapus dari blacklist."), true);
                                    return 1;
                                }))));

        // --- PERINTAH WHITELIST ---
        // (Kode whitelist tidak perlu diubah)
        dispatcher.register(Commands.literal("whitelist")
                .requires(source -> {
                    try {
                        return Amonite.playerDAO.getPermissionLevel(source.getPlayerOrException().getUUID().toString()) >= 1;
                    } catch (Exception e) {
                        return source.hasPermission(2);
                    }
                })
                // Sub-perintah: /whitelist add <username>
                .then(Commands.literal("add")
                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    // ... (logika 'add' tidak berubah)
                                    Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "target");
                                    GameProfile targetProfile = profiles.iterator().next();
                                    String targetUuid = targetProfile.getId().toString();
                                    String targetName = targetProfile.getName();
                                    String adminUuid = context.getSource().getPlayerOrException().getUUID().toString();
                                    Amonite.whitelistDAO.addToWhitelist(targetUuid, adminUuid);
                                    context.getSource().sendSuccess(() -> Component.literal(targetName + " telah ditambahkan ke whitelist."), true);
                                    return 1;
                                })))
                // Sub-perintah: /whitelist remove <username>
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                .executes(context -> {
                                    // ... (logika 'remove' tidak berubah)
                                    Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "target");
                                    GameProfile targetProfile = profiles.iterator().next();
                                    String targetUuid = targetProfile.getId().toString();
                                    String targetName = targetProfile.getName();
                                    Amonite.whitelistDAO.removeFromWhitelist(targetUuid);
                                    context.getSource().sendSuccess(() -> Component.literal(targetName + " telah dihapus dari whitelist."), true);
                                    return 1;
                                })))
                // --- SUB-PERINTAH BARU: /whitelist toggle ---
                .then(Commands.literal("toggle")
                        .executes(context -> {
                            // Ambil nilai saat ini dari config
                            boolean currentValue = Config.WHITELIST_ENABLED.get();
                            // Atur nilainya menjadi kebalikannya
                            Config.WHITELIST_ENABLED.set(!currentValue);
                            // Simpan perubahan ke file .toml
                            Config.SPEC.save();

                            String status = !currentValue ? "diaktifkan" : "dinonaktifkan";
                            context.getSource().sendSuccess(() -> Component.literal("Fitur whitelist telah " + status + "."), true);
                            return 1;
                        }))
                // --- SUB-PERINTAH BARU: /whitelist status ---
                .then(Commands.literal("status")
                        .executes(context -> {
                            boolean currentValue = Config.WHITELIST_ENABLED.get();
                            String status = currentValue ? "AKTIF" : "NONAKTIF";
                            context.getSource().sendSuccess(() -> Component.literal("Status whitelist saat ini: " + status), false);
                            return 1;
                        })));
    }

    /**
     * Method terpusat untuk logika blacklist agar tidak ada duplikasi kode.
     */
    private static int executeBlacklist(CommandSourceStack source, Collection<GameProfile> profiles, String reason) {
        GameProfile targetProfile = profiles.iterator().next();
        UUID targetUuid = targetProfile.getId();
        String targetName = targetProfile.getName();

        try {
            String adminUuid = source.getPlayerOrException().getUUID().toString();
            Amonite.blacklistDAO.addToBlacklist(targetUuid.toString(), reason, adminUuid);
            source.sendSuccess(() -> Component.literal(targetName + " telah ditambahkan ke blacklist."), true);

            // --- LOGIKA DISCONNECT ---
            // Cek apakah pemain yang di-blacklist sedang online
            ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(targetUuid);
            if (targetPlayer != null) {
                // Jika online, tendang dari server
                targetPlayer.connection.disconnect(Component.literal("Anda telah dimasukkan ke daftar hitam.\nAlasan: " + reason));
                Amonite.LOGGER.info("Pemain " + targetName + " ditendang karena dimasukkan ke daftar hitam.");
            }
            // --- AKHIR LOGIKA DISCONNECT ---

        } catch (Exception e) {
            source.sendFailure(Component.literal("Gagal menjalankan perintah: " + e.getMessage()));
            return 0;
        }

        return 1;
    }
}