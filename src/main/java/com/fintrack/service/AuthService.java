package com.fintrack.service;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.dao.UserDAO;
import com.fintrack.dao.impl.UserDAOImpl;
import com.fintrack.exception.AuthenticationException;
import com.fintrack.exception.DatabaseException;
import com.fintrack.exception.ValidationException;
import com.fintrack.model.User;
import com.fintrack.util.PasswordUtil;
import com.fintrack.util.ValidationUtil;

import java.util.logging.Logger;

/**
 * Service orchestrating authentication and registration workflows.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public User login(String username, String password) {
        System.out.println("[DEBUG-AUTH] AuthService.login() called for: " + username);
        
        try {
            ValidationUtil.requireNotBlank(username, "Username");
            ValidationUtil.requireNotBlank(password, "Password");
        } catch (ValidationException e) {
            System.out.println("[DEBUG-AUTH] Validation failed.");
            throw e;
        }

        try {
            System.out.println("[DEBUG-AUTH] Calling userDAO.findByUsername()");
            User user = userDAO.findByUsername(username);
            
            if (user == null) {
                System.out.println("[DEBUG-AUTH] User not found in database.");
                throw new AuthenticationException("Invalid username or password.");
            }

            System.out.println("[DEBUG-AUTH] User found! Verifying BCrypt password hash...");
            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                System.out.println("[DEBUG-AUTH] Password hash mismatch.");
                throw new AuthenticationException("Invalid username or password.");
            }

            System.out.println("[DEBUG-AUTH] Login totally successful for: " + username);
            LOGGER.info("User authenticated: " + username);
            return user;
            
        } catch (DatabaseException e) {
            System.err.println("[FATAL-AUTH] DatabaseException thrown during login! See inner trace:");
            e.printStackTrace(System.err);
            throw e; // Bubble up
        } catch (Exception e) {
            System.err.println("[FATAL-AUTH] Unexpected generic Exception in AuthService:");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    public User register(String username, String email, String password, String confirmPassword) {
        // ... (Skipped adding debug to register for brevity, focus is on login flow)
        ValidationUtil.requireNotBlank(username, "Username");
        ValidationUtil.requireNotBlank(email, "Email");
        ValidationUtil.requireNotBlank(password, "Password");
        ValidationUtil.requireValidEmail(email);

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        if (userDAO.existsByUsername(username)) {
            throw new ValidationException("Username is already taken.");
        }
        if (userDAO.existsByEmail(email)) {
            throw new ValidationException("Email is already registered.");
        }

        User user = new User(username, email, PasswordUtil.hash(password));

        if (!userDAO.insert(user)) {
            DatabaseConfig.rollback();
            throw new DatabaseException("Failed to register user. No rows affected.");
        }

        DatabaseConfig.commit();
        LOGGER.info("New user registered: " + username);
        return user;
    }
}
