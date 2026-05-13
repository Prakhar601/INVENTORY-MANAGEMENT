module com.fintrack {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.fintrack to javafx.fxml;
    opens com.fintrack.controller to javafx.fxml;
    opens com.fintrack.model to javafx.base;

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
