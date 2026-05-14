package com.fintrack.controller;

import com.fintrack.navigation.SceneNavigator;
import com.fintrack.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {

    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnAccounts;
    @FXML private Button btnBudgets;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        // Any initial setup for the sidebar
    }

    /**
     * Sets the active state for the current navigation item.
     * @param activeTab The ID or name of the active tab (e.g., "dashboard")
     */
    public void setActive(String activeTab) {
        // First, clear active class from all buttons
        btnDashboard.getStyleClass().remove("active");
        btnTransactions.getStyleClass().remove("active");
        btnAccounts.getStyleClass().remove("active");
        btnBudgets.getStyleClass().remove("active");
        btnReports.getStyleClass().remove("active");
        btnSettings.getStyleClass().remove("active");

        // Set active class based on the parameter
        switch (activeTab.toLowerCase()) {
            case "dashboard":
                btnDashboard.getStyleClass().add("active");
                break;
            case "transactions":
                btnTransactions.getStyleClass().add("active");
                break;
            case "accounts":
                btnAccounts.getStyleClass().add("active");
                break;
            case "budgets":
                btnBudgets.getStyleClass().add("active");
                break;
            case "reports":
                btnReports.getStyleClass().add("active");
                break;
            case "settings":
                btnSettings.getStyleClass().add("active");
                break;
        }
    }

    @FXML
    private void navDashboard() {
        SceneNavigator.navigateTo("dashboard.fxml");
    }

    @FXML
    private void navTransactions() {
        SceneNavigator.navigateTo("transactions.fxml");
    }

    @FXML
    private void navAccounts() {
        SceneNavigator.navigateTo("accounts.fxml");
    }

    @FXML
    private void navBudgets() {
        SceneNavigator.navigateTo("budgets.fxml");
    }

    @FXML
    private void navReports() {
        SceneNavigator.navigateTo("reports.fxml");
    }

    @FXML
    private void navSettings() {
        SceneNavigator.navigateTo("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo("login.fxml");
    }
}
