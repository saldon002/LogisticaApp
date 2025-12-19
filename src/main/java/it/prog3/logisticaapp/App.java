package it.prog3.logisticaapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Carica la vista di Login
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/manager.fxml"));
        Parent root = fxmlLoader.load();

        scene = new Scene(root, 600, 400);
        stage.setTitle("LogisticaApp - Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Metodo statico per cambiare schermata facilmente da qualsiasi controller.
     * Es: App.setRoot("view/staff");
     */
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());

        // Opzionale: Ridimensiona la finestra se cambi schermata (es. da Login piccolo a Staff grande)
        Stage stage = (Stage) scene.getWindow();
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}