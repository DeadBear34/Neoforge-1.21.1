package net.miolineara.amonite;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Pengaturan Database
    public static final ModConfigSpec.ConfigValue<String> DB_HOST;
    public static final ModConfigSpec.ConfigValue<Integer> DB_PORT;
    public static final ModConfigSpec.ConfigValue<String> DB_NAME;
    public static final ModConfigSpec.ConfigValue<String> DB_USER;
    public static final ModConfigSpec.ConfigValue<String> DB_PASSWORD;

    // --- PENGATURAN BARU ---
    public static final ModConfigSpec.BooleanValue WHITELIST_ENABLED;

    static {
        BUILDER.push("database");
        DB_HOST = BUILDER.comment("Database server host").define("host", "localhost");
        DB_PORT = BUILDER.comment("Database server port").define("port", 5432);
        DB_NAME = BUILDER.comment("Database name").define("databaseName", "minecraft_db");
        DB_USER = BUILDER.comment("Database user").define("user", "postgres");
        DB_PASSWORD = BUILDER.comment("Database password").define("password", "your_password_here");
        BUILDER.pop();

        // --- DEFINISI PENGATURAN BARU ---
        BUILDER.push("features");
        WHITELIST_ENABLED = BUILDER
                .comment("Setel ke true untuk mengaktifkan whitelist, false untuk menonaktifkannya.")
                .define("whitelistEnabled", false); // Default: whitelist mati
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}