package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Service responsible for backing up and restoring the SQLite database.
 */
public class DatabaseBackupService {

    /**
     * Backs up the active database to the specified destination.
     */
    public void backupDatabase(String destinationPath) throws IOException {
        File dbFile = new File("fintrack.db");
        if (!dbFile.exists()) {
            throw new IOException("Source database file not found at " + dbFile.getAbsolutePath());
        }

        File destFile = new File(destinationPath);
        Files.copy(dbFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Restores the database from a backup file.
     * WARNING: This replaces the current database completely.
     */
    public void restoreDatabase(String sourcePath) throws IOException {
        File backupFile = new File(sourcePath);
        if (!backupFile.exists()) {
            throw new IOException("Backup file not found at " + backupFile.getAbsolutePath());
        }

        File currentDbFile = new File("fintrack.db");
        Files.copy(backupFile.toPath(), currentDbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
