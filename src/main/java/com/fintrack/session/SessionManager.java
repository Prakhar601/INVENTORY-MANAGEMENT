package com.fintrack.session;

import com.fintrack.model.User;
import com.fintrack.navigation.SceneNavigator;

import java.util.logging.Logger;

/**
 * Thread-safe singleton managing the authenticated user session.
 * <p>
 * Holds the current {@link User} for the application lifecycle.
 * Provides login, logout, and session state query methods.
 * </p>
 */
public final class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the authenticated user and starts the session.
     */
    public void login(User user) {
        this.currentUser = user;
        LOGGER.info("User logged in: " + user.getUsername());
    }

    /**
     * Clears the session and navigates back to the login screen.
     */
    public void logout() {
        LOGGER.info("User logged out: " + (currentUser != null ? currentUser.getUsername() : "null"));
        this.currentUser = null;
        SceneNavigator.clearCache();
        SceneNavigator.navigateTo("login.fxml");
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the current {@link User}, or {@code null} if no session
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @return user ID, or -1 if no session
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    /**
     * Checks whether a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
