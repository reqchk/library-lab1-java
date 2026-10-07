package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.gui.Controller;
import org.example.gui.View;

public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        View view = new View();
        Controller controller = new Controller(primaryStage, view);

        Scene scene = view.createScene(
                controller::onLoadCsv,
                controller::onSaveCsv,
                controller::onAddBook,
                controller::onEditBook
        );

        primaryStage.setTitle("Электронная библиотека");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}