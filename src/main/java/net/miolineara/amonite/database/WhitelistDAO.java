package net.miolineara.amonite.database;

import net.miolineara.amonite.Amonite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WhitelistDAO {

    /**
     * Menambahkan pemain ke dalam whitelist.
     * @param uuid UUID pemain yang akan ditambahkan.
     * @param adminUuid UUID admin yang menambahkan.
     */
    public void addToWhitelist(String uuid, String adminUuid) {
        String sql = "INSERT INTO whitelist (uuid, added_by_uuid) VALUES (?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, adminUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal menambahkan UUID {} ke whitelist.", uuid, e);
        }
    }

    /**
     * Menghapus pemain dari whitelist.
     * @param uuid UUID pemain yang akan dihapus.
     */
    public void removeFromWhitelist(String uuid) {
        String sql = "DELETE FROM whitelist WHERE uuid = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal menghapus UUID {} dari whitelist.", uuid, e);
        }
    }

    /**
     * Mengecek apakah seorang pemain ada di dalam whitelist.
     * @param uuid UUID pemain yang akan dicek.
     * @return true jika pemain ada di whitelist, false jika tidak.
     */
    public boolean isWhitelisted(String uuid) {
        String sql = "SELECT 1 FROM whitelist WHERE uuid = ? LIMIT 1;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Amonite.LOGGER.error("Gagal mengecek status whitelist untuk UUID: {}", uuid, e);
        }
        return false;
    }
}