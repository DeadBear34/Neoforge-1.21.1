package net.miolineara.amonite.database;

import net.miolineara.amonite.Amonite;
import net.miolineara.amonite.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    /**
     * Membuat koneksi BARU ke database setiap kali dipanggil.
     */
    public static Connection getConnection() throws SQLException {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                Config.DB_HOST.get(),
                Config.DB_PORT.get(),
                Config.DB_NAME.get()
        );
        return DriverManager.getConnection(jdbcUrl, Config.DB_USER.get(), Config.DB_PASSWORD.get());
    }

    /**
     * Method untuk membuat semua tabel yang dibutuhkan jika belum ada.
     * Dipanggil sekali saat server pertama kali dijalankan.
     */
    public static void initializeTables() {
        Amonite.LOGGER.info("Memeriksa dan menyiapkan tabel database...");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tabel Pemain (inti)
            stmt.execute("CREATE TABLE IF NOT EXISTS players (id SERIAL PRIMARY KEY, uuid VARCHAR(36) NOT NULL UNIQUE, username VARCHAR(16) NOT NULL UNIQUE, hashed_password VARCHAR(60) NOT NULL, permission_level INT NOT NULL DEFAULT 0, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), last_login TIMESTAMP WITH TIME ZONE);");

            // Tabel Statistik
            stmt.execute("CREATE TABLE IF NOT EXISTS player_stats (player_id INT PRIMARY KEY, kills INT NOT NULL DEFAULT 0, deaths INT NOT NULL DEFAULT 0, play_time_seconds BIGINT NOT NULL DEFAULT 0, last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), CONSTRAINT fk_player FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE);");

            // Tabel Hukuman (Ban, Mute, dll.)
            stmt.execute("CREATE TABLE IF NOT EXISTS punishments (id SERIAL PRIMARY KEY, player_id INT NOT NULL, admin_id INT NOT NULL, punishment_type VARCHAR(10) NOT NULL, reason TEXT, issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), expires_at TIMESTAMP WITH TIME ZONE, is_active BOOLEAN NOT NULL DEFAULT TRUE, CONSTRAINT fk_player FOREIGN KEY(player_id) REFERENCES players(id) ON DELETE CASCADE, CONSTRAINT fk_admin FOREIGN KEY(admin_id) REFERENCES players(id) ON DELETE CASCADE);");

            // Tabel Log Aksi Admin
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_log (id SERIAL PRIMARY KEY, actor_id INT NOT NULL, action VARCHAR(255) NOT NULL, target_uuid VARCHAR(36), details TEXT, log_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), CONSTRAINT fk_actor FOREIGN KEY(actor_id) REFERENCES players(id) ON DELETE SET NULL);");

            Amonite.LOGGER.info("Pemeriksaan tabel database selesai.");
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal menginisialisasi tabel database!", e);
        }
    }
}