package net.miolineara.amonite.util;

import net.miolineara.amonite.Amonite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Menghasilkan hash SHA-256 dari password teks biasa.
     * Tidak lagi menggunakan jBCrypt.
     * @param plainPassword Password yang akan di-hash.
     * @return String hash password dalam format hexadecimal.
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Gunakan algoritma SHA-256 yang sudah ada di Java
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            // Konversi byte array ke format hex string
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Ini seharusnya tidak pernah terjadi karena SHA-256 adalah standar Java
            Amonite.LOGGER.error("Algoritma hashing SHA-256 tidak ditemukan!", e);
            throw new RuntimeException("Gagal melakukan hashing password", e);
        }
    }

    /**
     * Memeriksa apakah password teks biasa cocok dengan hash SHA-256 yang ada.
     * @param plainPassword Password yang dimasukkan oleh pengguna.
     * @param hashedPasswordFromDB Hash yang tersimpan di database.
     * @return true jika cocok, false jika tidak.
     */
    public static boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        // Hash password yang dimasukkan dengan metode yang sama
        String newHash = hashPassword(plainPassword);
        // Bandingkan hasilnya
        return newHash.equals(hashedPasswordFromDB);
    }
}