package com.fintrack.util;

import com.fintrack.config.AppConfig;
import com.fintrack.exception.AppException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

/**
 * Centralized FXML loading utility.
 */
public final class FXMLLoaderUtil {

    private FXMLLoaderUtil() {}

    /**
     * Loads an FXML file and returns the root {@link Parent} node.
     *
     * @param fxmlFileName the FXML file name (e.g. "login.fxml")
     * @return the loaded Parent node
     * @throws AppException if the FXML cannot be loaded
     */
    public static Parent load(String fxmlFileName) {
        try {
            URL resource = FXMLLoaderUtil.class.getResource(AppConfig.FXML_BASE_PATH + fxmlFileName);
            if (resource == null) {
                throw new AppException("FXML file not found: " + fxmlFileName);
            }
            return FXMLLoader.load(resource);
        } catch (IOException e) {
            throw new AppException("Failed to load FXML: " + fxmlFileName, e);
        }
    }

    /**
     * Loads an FXML file and returns the FXMLLoader for controller access.
     *
     * @param fxmlFileName the FXML file name
     * @return the configured FXMLLoader (call {@code getRoot()} and {@code getController()})
     * @throws AppException if the FXML cannot be loaded
     */
    public static FXMLLoader loadWithController(String fxmlFileName) {
        try {
            URL resource = FXMLLoaderUtil.class.getResource(AppConfig.FXML_BASE_PATH + fxmlFileName);
            if (resource == null) {
                throw new AppException("FXML file not found: " + fxmlFileName);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            return loader;
        } catch (IOException e) {
            throw new AppException("Failed to load FXML: " + fxmlFileName, e);
        }
    }
}
