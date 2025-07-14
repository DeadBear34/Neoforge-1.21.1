package net.miolineara.amonite.database;

import net.miolineara.amonite.Amonite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDAO {

    /**
     * Mengecek apakah pemain sudah terdaftar berdasarkan UUID.
     * Ini menjadi method kunci kita.
     */
    public boolean isPlayerRegistered(String uuid) {
        String sql = "SELECT 1 FROM players WHERE uuid = ? LIMIT 1;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true jika ada hasil, false jika tidak
            }
        } catch (SQLException e) {
            Amonite.LOGGER.error("Could not check for player UUID: " + uuid, e);
        }
        return false;
    }

    /**
     * Mendaftarkan pemain baru hanya dengan UUID dan username mereka.
     */
    public void registerNewPlayer(String uuid, String username) {
        String sql = "INSERT INTO players (uuid, username, is_online, last_login, last_seen) VALUES (?, ?, TRUE, NOW(), NOW());";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            Amonite.LOGGER.info("Pemain baru '{}' dengan UUID {} telah otomatis terdaftar.", username, uuid);

        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal mendaftarkan pemain baru: " + username, e);
        }
    }

    /**
     * Mengatur status pemain menjadi online dan memperbarui waktu login.
     */
    public void setPlayerOnline(String uuid) {
        String sql = "UPDATE players SET is_online = TRUE, last_login = NOW(), last_seen = NOW() WHERE uuid = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal update status online untuk UUID: " + uuid, e);
        }
    }

    /**
     * Mengatur status pemain menjadi offline.
     */
    public void setPlayerOffline(String uuid) {
        String sql = "UPDATE players SET is_online = FALSE, last_seen = NOW() WHERE uuid = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal update status offline untuk UUID: " + uuid, e);
        }
    }

    // Method lain seperti getPermissionLevel dan setPermissionLevel bisa tetap ada tanpa perubahan.
    public int getPermissionLevel(String uuid) {
        String sql = "SELECT permission_level FROM players WHERE uuid = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("permission_level");
            }
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal mendapatkan level izin untuk UUID: " + uuid, e);
        }
        return 0;
    }

    public void setPermissionLevel(String uuid, int level) {
        String sql = "UPDATE players SET permission_level = ? WHERE uuid = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, level);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal mengatur level izin untuk UUID: " + uuid, e);
        }
    }
}