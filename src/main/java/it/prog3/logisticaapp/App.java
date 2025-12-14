package it.prog3.logisticaapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto di ingresso (Entry Point) dell'applicazione JavaFX.
 * Questa classe estende Application e avvia il ciclo di vita della GUI.
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Carichiamo il file "main.fxml" che si trova nella cartella delle risorse "view"
        // NOTA: Se non hai ancora il file fxml, questo codice non partirà, ma è corretto.
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/main.fxml"));

        Parent root = fxmlLoader.load();

        // Impostiamo la scena (la finestra)
        scene = new Scene(root, 900, 600); // Larghezza 900, Altezza 600

        stage.setTitle("LogisticaApp - Gestione Spedizioni");
        stage.setScene(scene);
        stage.show();
    }

    // Metodo Main standard che lancia l'applicazione
    public static void main(String[] args) {
        launch();
    }
}