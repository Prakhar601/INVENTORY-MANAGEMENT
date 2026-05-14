package com.fintrack.controller;

import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.DatabaseBackupService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import com.fintrack.util.AsyncUtil;
import com.fintrack.util.PreferenceManager;
import com.fintrack.util.ThemeManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

import java.io.File;

public class SettingsController {

    @FXML private SidebarController sidebarController;
    @FXML private ComboBox<String> cmbTheme;
    @FXML private ComboBox<String> cmbCurrency;
    @FXML private CheckBox chkNotifications;

    private DatabaseBackupService backupService;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().getCurrentUser() == null) {
            Platform.runLater(() -> SceneNavigator.navigateTo("login.fxml"));
            return;
        }

        if (sidebarController != null) {
            sidebarController.setActive("settings");
        }

        backupService = new DatabaseBackupService();

        // Initialize Dropdowns
        cmbTheme.setItems(FXCollections.observableArrayList("Light", "Dark"));
        cmbCurrency.setItems(FXCollections.observableArrayList("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD"));

        // Load Preferences
        cmbTheme.setValue(PreferenceManager.getTheme());
        cmbCurrency.setValue(PreferenceManager.getCurrency());
        chkNotifications.setSelected(PreferenceManager.isNotificationsEnabled());

        // Listeners for auto-save
        cmbTheme.setOnAction(e -> {
            String selected = cmbTheme.getValue();
            PreferenceManager.setTheme(selected);
            ThemeManager.refreshAllThemes();
        });

        cmbCurrency.setOnAction(e -> {
            PreferenceManager.setCurrency(cmbCurrency.getValue());
        });

        chkNotifications.setOnAction(e -> {
            PreferenceManager.setNotificationsEnabled(chkNotifications.isSelected());
        });
    }

    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Database Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));
        fileChooser.setInitialFileName("fintrack_backup_" + System.currentTimeMillis() + ".db");
        
        File file = fileChooser.showSaveDialog(cmbTheme.getScene().getWindow());
        if (file != null) {
            AsyncUtil.runAsync(() -> {
                try {
                    backupService.backupDatabase(file.getAbsolutePath());
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, success -> {
                AlertUtil.showSuccess("Database backed up successfully to " + file.getName());
            }, error -> {
                AlertUtil.showError("Failed to backup database: " + error.getCause().getMessage());
            });
        }
    }

    @FXML
    private void handleRestore() {
        boolean confirm = AlertUtil.showConfirm("Restore Database", "Are you sure you want to restore the database? This will overwrite ALL current data and log you out. This action cannot be undone.");
        if (!confirm) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database Backup File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite Database", "*.db"));

        File file = fileChooser.showOpenDialog(cmbTheme.getScene().getWindow());
        if (file != null) {
            AsyncUtil.runAsync(() -> {
                try {
                    backupService.restoreDatabase(file.getAbsolutePath());
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, success -> {
                // Force logout and clear cache to reload DB state cleanly
                SessionManager.getInstance().logout();
                SceneNavigator.clearCache();
                SceneNavigator.navigateTo("login.fxml");
                // Don't use ToastUtil here since we just swapped scenes, but AlertUtil works fine as blocking before scene swap if we wanted, but we did async.
                System.out.println("Database restored. Returning to login.");
            }, error -> {
                AlertUtil.showError("Failed to restore database: " + error.getCause().getMessage());
            });
        }
    }

    @FXML
    private void handleLogout() {
        boolean confirm = AlertUtil.showConfirm("Logout", "Are you sure you want to log out?");
        if (confirm) {
            SessionManager.getInstance().logout();
            SceneNavigator.clearCache();
            SceneNavigator.navigateTo("login.fxml");
        }
    }
}
