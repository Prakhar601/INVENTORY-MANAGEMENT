package com.fintrack.controller;

import com.fintrack.exception.ValidationException;
import com.fintrack.navigation.SceneNavigator;
import com.fintrack.service.AuthService;
import com.fintrack.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;

    private final AuthService authService;

    public RegisterController() {
        this.authService = new AuthService();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        try {
            authService.register(username, email, password, confirmPassword);
            AlertUtil.showSuccess("Registration successful! Please login.");
            SceneNavigator.navigateTo("login.fxml");
        } catch (ValidationException e) {
            AlertUtil.showWarning(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("An unexpected error occurred during registration.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        SceneNavigator.navigateTo("login.fxml");
    }
}
