module com.fintrack {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Java Platform
    requires java.sql;
    requires java.logging;
    requires java.prefs;

    // Database
    requires org.xerial.sqlitejdbc;

    // Security (automatic module — name derived from JAR filename)
    requires jbcrypt;

    // Icons
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    // Export Libraries (automatic modules)
    requires itextpdf;
    requires org.apache.commons.csv;

    // Open packages to JavaFX for FXML reflection
    opens com.fintrack to javafx.fxml;
    opens com.fintrack.controller to javafx.fxml;
    opens com.fintrack.model to javafx.base;

    // Export public API packages
    exports com.fintrack;
    exports com.fintrack.model;
    exports com.fintrack.controller;
    exports com.fintrack.service;
    exports com.fintrack.dao;
    exports com.fintrack.config;
    exports com.fintrack.navigation;
    exports com.fintrack.session;
    exports com.fintrack.util;
    exports com.fintrack.exception;
}
