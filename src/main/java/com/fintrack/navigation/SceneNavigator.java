package com.fintrack.navigation;

import com.fintrack.config.AppConfig;
import com.fintrack.exception.AppException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

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

                    // Centralized CSS loading — each view gets global.css + its specific sheets
                    String[] cssFiles = getCssForView(key);
                    for (String cssFile : cssFiles) {
                        URL css = SceneNavigator.class.getResource(AppConfig.CSS_BASE_PATH + cssFile);
                        if (css != null) {
                            newScene.getStylesheets().add(css.toExternalForm());
                        } else {
                            System.err.println("[WARN-NAV] CSS not found: " + cssFile);
                        }
                    }
                    System.out.println("[DEBUG-NAV] Applied " + cssFiles.length + " stylesheets.");
                    
                    // Apply user's selected theme (Dark/Light)
                    com.fintrack.util.ThemeManager.applyTheme(newScene);

                    return newScene;

                } catch (IOException e) {
                    System.err.println("[FATAL-NAV] FXMLLoader threw IOException!");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (RuntimeException e) {
                    System.err.println("[FATAL-NAV] FXMLLoader threw RuntimeException!");
                    e.printStackTrace();
                    throw e;
                }
            });

            System.out.println("[DEBUG-NAV] Switching Stage Scene...");
            
            // Apply Fade Transition
            Parent root = scene.getRoot();
            root.setOpacity(0);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            System.out.println("[DEBUG-NAV] Navigation complete.");
            System.out.println("------------------------------------------------\n");

        } catch (Throwable t) {
            System.err.println("[FATAL-NAV] SceneNavigator caught an exception during navigation execution!");
            t.printStackTrace();
            throw new RuntimeException(t);
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

    /**
     * Maps each FXML view to its required CSS stylesheets.
     * global.css is always included first.
     */
    private static String[] getCssForView(String fxmlFileName) {
        return switch (fxmlFileName) {
            case "login.fxml", "register.fxml" -> new String[]{"global.css", "login.css"};
            case "dashboard.fxml"              -> new String[]{"global.css", "sidebar.css", "dashboard.css"};
            case "transactions.fxml"           -> new String[]{"global.css", "sidebar.css", "dashboard.css"};
            case "accounts.fxml"               -> new String[]{"global.css", "sidebar.css", "dashboard.css"};
            case "budgets.fxml"                -> new String[]{"global.css", "sidebar.css", "dashboard.css", "analytics.css"};
            case "reports.fxml"                -> new String[]{"global.css", "sidebar.css", "dashboard.css", "analytics.css"};
            case "settings.fxml"               -> new String[]{"global.css", "sidebar.css", "dashboard.css"};
            default                            -> new String[]{"global.css"};
        };
    }
}
