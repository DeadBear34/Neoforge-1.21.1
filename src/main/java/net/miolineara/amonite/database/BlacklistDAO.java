package net.miolineara.amonite.database;

import net.miolineara.amonite.Amonite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlacklistDAO {

    /**
     * Menambahkan pemain ke dalam blacklist.
     * @param uuid UUID pemain yang akan di-blacklist.
     * @param reason Alasan pemblokiran.
     * @param adminUuid UUID admin yang melakukan pemblokiran.
     */
    public void addToBlacklist(String uuid, String reason, String adminUuid) {
        String sql = "INSERT INTO blacklist (uuid, reason, banned_by_uuid) VALUES (?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, reason);
            pstmt.setString(3, adminUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal menambahkan UUID {} ke blacklist.", uuid, e);
        }
    }

    /**
     * Menghapus pemain dari blacklist.
     * @param uuid UUID pemain yang akan dihapus dari blacklist.
     */
    public void removeFromBlacklist(String uuid) {
        String sql = "DELETE FROM blacklist WHERE uuid = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal menghapus UUID {} dari blacklist.", uuid, e);
        }
    }

    /**
     * Mengecek apakah seorang pemain ada di dalam blacklist.
     * @param uuid UUID pemain yang akan dicek.
     * @return true jika pemain ada di blacklist, false jika tidak.
     */
    public boolean isBlacklisted(String uuid) {
        String sql = "SELECT 1 FROM blacklist WHERE uuid = ? LIMIT 1;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal mengecek status blacklist untuk UUID: {}", uuid, e);
        }
        return false;
    }
}