module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;
    requires javafx.swing;

    requires java.net.http; // For HttpClient, HttpRequest, etc.
    requires org.json;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires unirest.java;
    // MySQL connector will be handled through automatic modules

    opens com.example.demo.controller to javafx.fxml;
    opens com.example.demo.view to javafx.fxml;
    opens com.example.demo.model to javafx.base;
    opens com.example.demo.database to javafx.base;
    opens com.example.demo.auth to javafx.base;
    opens com.example.demo.utils to javafx.base;

    exports com.example.demo;
    exports com.example.demo.controller;
    exports com.example.demo.model;
    exports com.example.demo.utils;
    exports com.example.demo.database;
    exports com.example.demo.auth;

    opens com.example.demo to javafx.fxml;
}
