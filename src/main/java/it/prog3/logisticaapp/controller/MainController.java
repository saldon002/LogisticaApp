package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.List;

/**
 * Controller che gestisce l'interazione tra la GUI (main.fxml) e il Backend (LogisticaFacade).
 * Implementa la logica di visualizzazione senza usare Lambda Expressions.
 */
public class MainController {

    // --- COLLEGAMENTI CON IL FILE FXML ---
    @FXML private TableView<ICollo> tableColli;
    @FXML private TableColumn<ICollo, String> colColloCodice;
    @FXML private TableColumn<ICollo, String> colColloPeso;
    @FXML private TableColumn<ICollo, String> colColloStato;

    @FXML private TableView<IVeicolo> tableVeicoli;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCodice;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTipo;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCapienza;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCarico;

    @FXML private ComboBox<Sessione.Ruolo> comboRuolo;
    @FXML private Button btnCarica;
    @FXML private Button btnLogin;
    @FXML private Label lblStatus;

    // Riferimento al Service Layer (Backend)
    private LogisticaFacade facade;

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     */
    @FXML
    public void initialize() {
        // 1. Inizializziamo il Facade
        try {
            this.facade = new LogisticaFacade();
        } catch (Exception e) {
            mostraErrore("Errore Database", "Impossibile connettersi al DB: " + e.getMessage());
            return;
        }

        // ============================================================
        // 2. SETUP COLONNE TABELLA COLLI (Uso di Classi Anonime)
        // ============================================================

        // Colonna Codice
        colColloCodice.setCellValueFactory(new Callback<CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ICollo, String> param) {
                return new SimpleStringProperty(param.getValue().getCodice());
            }
        });

        // Colonna Peso (Aggiungiamo " kg" per formattazione)
        colColloPeso.setCellValueFactory(new Callback<CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ICollo, String> param) {
                return new SimpleStringProperty(param.getValue().getPeso() + " kg");
            }
        });

        // Colonna Stato
        colColloStato.setCellValueFactory(new Callback<CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<ICollo, String> param) {
                return new SimpleStringProperty(param.getValue().getStato());
            }
        });

        // ============================================================
        // 3. SETUP COLONNE TABELLA VEICOLI
        // ============================================================

        colVeicoloCodice.setCellValueFactory(new Callback<CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<IVeicolo, String> param) {
                return new SimpleStringProperty(param.getValue().getCodice());
            }
        });

        colVeicoloTipo.setCellValueFactory(new Callback<CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<IVeicolo, String> param) {
                return new SimpleStringProperty(param.getValue().getTipo());
            }
        });

        colVeicoloCapienza.setCellValueFactory(new Callback<CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<IVeicolo, String> param) {
                return new SimpleStringProperty(String.valueOf(param.getValue().getCapienza()));
            }
        });

        // Colonna Carico: Mostra il numero di colli caricati (es. "3 colli")
        colVeicoloCarico.setCellValueFactory(new Callback<CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<IVeicolo, String> param) {
                int n = param.getValue().getCarico().size();
                return new SimpleStringProperty(n + " colli");
            }
        });

        // 4. Popoliamo il menu dei ruoli
        comboRuolo.setItems(FXCollections.observableArrayList(Sessione.Ruolo.values()));
        comboRuolo.getSelectionModel().select(Sessione.Ruolo.CLIENTE); // Default

        // 5. Caricamento dati iniziale
        aggiornaTabelle();
    }

    /**
     * Gestisce il click sul pulsante LOGIN.
     */
    @FXML
    public void onLoginClick() {
        Sessione.Ruolo ruolo = comboRuolo.getValue();
        if (ruolo != null) {
            Sessione.getInstance().setRuoloCorrente(ruolo);

            // Abilita il tasto carica solo se MANAGER o CORRIERE
            boolean isAbilitato = (ruolo == Sessione.Ruolo.MANAGER || ruolo == Sessione.Ruolo.CORRIERE);
            btnCarica.setDisable(!isAbilitato);

            lblStatus.setText("Login effettuato come: " + ruolo);
            lblStatus.setTextFill(Color.BLACK);
        }
    }

    /**
     * Gestisce il click sul pulsante AVVIA CARICO.
     */
    @FXML
    public void onCaricaClick() {
        try {
            lblStatus.setText("Elaborazione algoritmo in corso...");

            // Chiamata al backend
            facade.caricaMerce();

            // Aggiorna la vista
            aggiornaTabelle();
            lblStatus.setText("Carico completato con successo!");
            lblStatus.setTextFill(Color.GREEN);

        } catch (SecurityException e) {
            mostraErrore("Accesso Negato", "Non hai i permessi: " + e.getMessage());
            lblStatus.setText("Errore: Accesso Negato");
            lblStatus.setTextFill(Color.RED);
        } catch (IllegalArgumentException e) {
            mostraErrore("Attenzione", e.getMessage());
            lblStatus.setText("Attenzione: " + e.getMessage());
            lblStatus.setTextFill(Color.ORANGE);
        } catch (Exception e) {
            e.printStackTrace();
            mostraErrore("Errore di Sistema", e.getMessage());
            lblStatus.setText("Errore Critico");
            lblStatus.setTextFill(Color.RED);
        }
    }

    @FXML
    public void onResetClick() {
        // Ricarica la vista simulando un reset
        initialize();
        lblStatus.setText("Stato: Reset effettuato.");
    }

    private void aggiornaTabelle() {
        // Aggiorna tabella veicoli
        tableVeicoli.setItems(FXCollections.observableArrayList(facade.getFlottaAttuale()));
        tableVeicoli.refresh();

        // Aggiorna tabella colli (richiede il metodo getTuttiIColliInMagazzino nel Facade)
        try {
            tableColli.setItems(FXCollections.observableArrayList(facade.getTuttiIColliInMagazzino()));
        } catch (Exception e) {
            // Se non hai aggiunto il metodo al Facade, non caricherà i colli ma non crasherà
            System.err.println("Warning: Impossibile caricare colli. Manca metodo nel Facade?");
        }
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}