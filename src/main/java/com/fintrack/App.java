package com.fintrack;

import com.fintrack.config.DatabaseConfig;
import com.fintrack.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX Application entry point.
 * <p>
 * Responsible for initializing the database, configuring the primary stage,
 * and handing control to the {@link SceneNavigator} for view management.
 * </p>
 */
public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize database and run schema migrations
            DatabaseConfig.initialize();

            // 2. Configure primary stage
            primaryStage.setTitle("FinTrack — Personal Finance Manager");
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);

            // 3. Initialize scene navigator with the primary stage
            SceneNavigator.init(primaryStage);

            // 4. Load the login view as the starting scene
            SceneNavigator.navigateTo("login.fxml");

            // 5. Handle application close — cleanup resources
            primaryStage.setOnCloseRequest(event -> {
                LOGGER.info("Application shutting down...");
                DatabaseConfig.shutdown();
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        DatabaseConfig.shutdown();
        LOGGER.info("Application stopped.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
