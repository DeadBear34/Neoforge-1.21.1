package net.miolineara.amonite;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Database Configuration Section
    public static final ModConfigSpec.ConfigValue<String> DB_HOST;
    public static final ModConfigSpec.IntValue DB_PORT;
    public static final ModConfigSpec.ConfigValue<String> DB_NAME;
    public static final ModConfigSpec.ConfigValue<String> DB_USER;
    public static final ModConfigSpec.ConfigValue<String> DB_PASSWORD;


    static {
        BUILDER.push("database"); // Mengelompokkan semua pengaturan database

        DB_HOST = BUILDER
                .comment("Database server host address.")
                .define("host", "localhost");

        DB_PORT = BUILDER
                .comment("Database server port.")
                .defineInRange("port", 5432, 1, 65535);

        DB_NAME = BUILDER
                .comment("The name of the database to connect to.")
                .define("databaseName", "minecraft_db");

        DB_USER = BUILDER
                .comment("The username for the database connection.")
                .define("user", "postgres");

        DB_PASSWORD = BUILDER
                .comment("The password for the database connection.")
                .define("password", "123123", s -> s instanceof String); // Gunakan validasi string sederhana

        BUILDER.pop();
    }


    static final ModConfigSpec SPEC = BUILDER.build();
}