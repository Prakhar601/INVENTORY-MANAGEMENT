package com.fintrack.controller;

import com.fintrack.navigation.SceneNavigator;
import com.fintrack.session.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ReportController {

    // Navigation Methods
    @FXML private void navDashboard(ActionEvent event) { SceneNavigator.navigateTo("dashboard.fxml"); }
    @FXML private void navTransactions(ActionEvent event) { SceneNavigator.navigateTo("transactions.fxml"); }
    @FXML private void navAccounts(ActionEvent event) { SceneNavigator.navigateTo("accounts.fxml"); }
    @FXML private void navBudgets(ActionEvent event) { SceneNavigator.navigateTo("budgets.fxml"); }
    @FXML private void navReports(ActionEvent event) { SceneNavigator.navigateTo("reports.fxml"); }
    @FXML private void navSettings(ActionEvent event) { SceneNavigator.navigateTo("settings.fxml"); }
    
    @FXML 
    private void handleLogout(ActionEvent event) { 
        SessionManager.getInstance().logout(); 
    }
}
