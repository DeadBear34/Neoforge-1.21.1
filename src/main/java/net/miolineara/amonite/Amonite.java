package net.miolineara.amonite;

import com.mojang.logging.LogUtils;
import net.miolineara.amonite.command.ModCommands;
import net.miolineara.amonite.database.BlacklistDAO;
import net.miolineara.amonite.database.DatabaseManager;
import net.miolineara.amonite.database.PlayerDAO;
import net.miolineara.amonite.database.WhitelistDAO;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Amonite.MOD_ID)
public class Amonite {

    public static final String MOD_ID = "amonite";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final PlayerDAO playerDAO = new PlayerDAO();
    public static final BlacklistDAO blacklistDAO = new BlacklistDAO();
    public static final WhitelistDAO whitelistDAO = new WhitelistDAO();

    public Amonite(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Tidak ada perubahan di sini
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server is starting, initializing database tables...");
        DatabaseManager.initializeTables();
    }

    // LOGIKA UTAMA ADA DI SINI
    // Di dalam kelas Amonite.java

    // Di dalam kelas Amonite.java

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        String uuid = event.getEntity().getUUID().toString();
        String username = event.getEntity().getName().getString();

        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 1. Cek Blacklist
        if (blacklistDAO.isBlacklisted(uuid)) {
            ((net.minecraft.server.level.ServerPlayer) event.getEntity()).connection.disconnect(net.minecraft.network.chat.Component.literal("Anda ada di dalam daftar hitam server ini."));
            return;
        }

        // --- LOGIKA WHITELIST YANG DIPERBARUI ---
        // 2. Cek Whitelist HANYA JIKA fiturnya aktif di config
        if (Config.WHITELIST_ENABLED.get() && !whitelistDAO.isWhitelisted(uuid)) {
            // Jika whitelist aktif dan pemain tidak ada di daftar, tendang
            ((net.minecraft.server.level.ServerPlayer) event.getEntity()).connection.disconnect(net.minecraft.network.chat.Component.literal("Server ini hanya untuk pemain yang ada di dalam daftar putih."));
            return;
        }
        // --- AKHIR LOGIKA WHITELIST ---

        // Jika lolos semua pengecekan, lanjutkan
        if (playerDAO.isPlayerRegistered(uuid)) {
            playerDAO.setPlayerOnline(uuid);
            LOGGER.info("Welcome back, {}!", username);
        } else {
            playerDAO.registerNewPlayer(uuid, username);
        }
    }

    // Menambahkan event handler untuk saat pemain logout
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        String uuid = event.getEntity().getUUID().toString();
        // Hanya jalankan di sisi server
        if (!event.getEntity().level().isClientSide()) {
            playerDAO.setPlayerOffline(uuid);
            LOGGER.info("Player {} has logged out.", event.getEntity().getName().getString());
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
        Amonite.LOGGER.info("Perintah Mod Amonite telah didaftarkan.");
    }
}