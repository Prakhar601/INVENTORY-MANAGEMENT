package com.fintrack;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.model.User;
import com.fintrack.service.AuthService;

public class TestFlow {
    public static void main(String[] args) {
        try {
            System.out.println("Initializing DB...");
            DatabaseConfig.initialize();
            System.out.println("DB initialized successfully.");

            AuthService authService = new AuthService();
            System.out.println("Registering user...");
            User u = authService.register("testuser", "test@test.com", "password123", "password123");
            System.out.println("Registered: " + u);
            
            System.out.println("Logging in...");
            User loggedIn = authService.login("testuser", "password123");
            System.out.println("Logged in: " + loggedIn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
