package com.fintrack.controller;

import com.fintrack.exception.AuthenticationException;
import com.fintrack.exception.ValidationException;
import com.fintrack.model.User;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AuthService;
import com.fintrack.session.SessionManager;
import com.fintrack.util.AlertUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        System.out.println("[DEBUG-LOGIN] LoginController: initialize() TRIGGERED");
        Platform.runLater(() -> {
            if (txtUsername == null || txtPassword == null) {
                System.err.println("[FATAL-LOGIN] FXML Injection Failed! Fields are NULL. Check login.fxml fx:id mappings.");
            } else {
                txtPassword.setOnKeyPressed(this::handleKeyPressed);
                txtUsername.setOnKeyPressed(this::handleKeyPressed);
                System.out.println("[DEBUG-LOGIN] FXML fields bound successfully.");
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println("\n[DEBUG-LOGIN] handleLogin() execution started.");
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        User user = null;

        // PHASE 1: Authentication
        try {
            System.out.println("[DEBUG-LOGIN] PHASE 1: Calling authService.login()...");
            user = authService.login(username, password);
            System.out.println("[DEBUG-LOGIN] authService returned successfully. User ID: " + user.getId());
            
            SessionManager.getInstance().login(user);
            System.out.println("[DEBUG-LOGIN] SessionManager updated successfully.");

        } catch (AuthenticationException | ValidationException e) {
            System.out.println("[DEBUG-LOGIN] Expected Auth/Validation failure: " + e.getMessage());
            AlertUtil.showWarning(e.getMessage());
            return; // Stop execution here cleanly
        } catch (Exception e) {
            System.err.println("[FATAL-LOGIN] Unexpected Database/System error during AUTHENTICATION.");
            e.printStackTrace();
            com.fintrack.util.ErrorDialogUtil.showFriendlyError(e);
            return; // Stop execution here cleanly
        }

        // PHASE 2: Navigation (Completely separated to prevent exception masking)
        try {
            System.out.println("[DEBUG-LOGIN] PHASE 2: Requesting UI Navigation to Dashboard...");
            SceneNavigator.navigateTo("dashboard.fxml");
            System.out.println("[DEBUG-LOGIN] Navigation executed completely.");
        } catch (Exception e) {
            System.err.println("[FATAL-LOGIN] Navigation Phase Crashed! (The login actually succeeded!)");
            e.printStackTrace();
            com.fintrack.util.ErrorDialogUtil.showFriendlyError(e);
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        System.out.println("[DEBUG-LOGIN] Navigating to Register...");
        SceneNavigator.navigateTo("register.fxml");
    }
}
