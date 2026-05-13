package com.fintrack.navigation;

import com.fintrack.config.AppConfig;
import com.fintrack.exception.AppException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized scene navigation manager with deep execution tracing.
 */
public final class SceneNavigator {

    private static final Logger LOGGER = Logger.getLogger(SceneNavigator.class.getName());
    private static Stage primaryStage;
    private static final Map<String, Scene> sceneCache = new HashMap<>();

    private SceneNavigator() {}

    public static void init(Stage stage) {
        System.out.println("[DEBUG-NAV] SceneNavigator.init() called");
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlFileName) {
        System.out.println("\n------------------------------------------------");
        System.out.println("[DEBUG-NAV] SceneNavigator: Requesting navigation to " + fxmlFileName);
        
        try {
            Scene scene = sceneCache.computeIfAbsent(fxmlFileName, key -> {
                System.out.println("[DEBUG-NAV] Cache miss. Attempting fresh load for " + key);
                try {
                    String path = AppConfig.FXML_BASE_PATH + key;
                    System.out.println("[DEBUG-NAV] Resolving FXML URL path: " + path);
                    URL resource = SceneNavigator.class.getResource(path);
                    
                    if (resource == null) {
                        System.err.println("[FATAL-NAV] Resource NOT FOUND! Ensure the file exists in target/classes" + path);
                        throw new AppException("View not found: " + path);
                    }
                    System.out.println("[DEBUG-NAV] FXML Resource found: " + resource.toExternalForm());
                    
                    System.out.println("[DEBUG-NAV] Initializing FXMLLoader...");
                    FXMLLoader loader = new FXMLLoader(resource);
                    
                    System.out.println("[DEBUG-NAV] WARNING: Calling FXMLLoader.load(). Controller initialize() executes NOW!");
                    Parent root = loader.load(); // Controller initializes here
                    System.out.println("[DEBUG-NAV] FXMLLoader.load() succeeded flawlessly. Node tree built.");
                    
                    Scene newScene = new Scene(root);

                    System.out.println("[DEBUG-NAV] Applying global CSS...");
                    URL globalCss = SceneNavigator.class.getResource(AppConfig.CSS_BASE_PATH + "global.css");
                    if (globalCss != null) {
                        newScene.getStylesheets().add(globalCss.toExternalForm());
                        System.out.println("[DEBUG-NAV] CSS Applied.");
                    } else {
                        System.err.println("[WARN-NAV] global.css not found!");
                    }

                    return newScene;

                } catch (IOException e) {
                    System.err.println("[FATAL-NAV] FXMLLoader threw IOException!");
                    e.printStackTrace(System.err);
                    throw new AppException("Failed to load view (IOException): " + key, e);
                } catch (RuntimeException e) {
                    System.err.println("[FATAL-NAV] FXMLLoader threw RuntimeException (Often a Controller/Database crash!)");
                    System.err.println("Root Cause extraction:");
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        System.err.println("  -> Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                        cause = cause.getCause();
                    }
                    e.printStackTrace(System.err);
                    throw new AppException("Controller initialization crashed loading: " + key, e);
                }
            });

            System.out.println("[DEBUG-NAV] Switching Stage Scene...");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("[DEBUG-NAV] Navigation complete.");
            System.out.println("------------------------------------------------\n");

        } catch (Exception e) {
            System.err.println("[FATAL-NAV] SceneNavigator caught an exception during navigation execution!");
            e.printStackTrace(System.err);
            throw new RuntimeException("CRITICAL: Navigation totally failed for " + fxmlFileName, e);
        }
    }

    public static void navigateToFresh(String fxmlFileName) {
        sceneCache.remove(fxmlFileName);
        navigateTo(fxmlFileName);
    }

    public static void clearCache() {
        sceneCache.clear();
        System.out.println("[DEBUG-NAV] Scene cache cleared.");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
