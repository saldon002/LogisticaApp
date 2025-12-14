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

public class StaffController {

    // === ELEMENTI LOGIN NELLA TOOLBAR ===
    @FXML private ComboBox<Sessione.Ruolo> comboRuolo;
    @FXML private Label lblRuoloCorrente;

    // === BOTTONI AZIONE ===
    @FXML private Button btnCarica;

    // === STATUS BAR ===
    @FXML private Label lblStatus;

    // === TABELLA COLLI ===
    @FXML private TableView<ICollo> tableColli;
    @FXML private TableColumn<ICollo, String> colColloCodice;
    @FXML private TableColumn<ICollo, String> colColloPeso;
    @FXML private TableColumn<ICollo, String> colColloStato;

    // === TABELLA VEICOLI ===
    @FXML private TableView<IVeicolo> tableVeicoli;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCodice;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTipo;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCapienza;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCarico;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        // 1. Inizializza il Facade (Backend)
        try {
            this.facade = new LogisticaFacade();
        } catch (Exception e) {
            mostraErrore("Errore DB", "Impossibile connettersi al database.");
            return;
        }

        // 2. Popola la ComboBox dei ruoli (solo ruoli staff se vuoi, o tutti)
        comboRuolo.setItems(FXCollections.observableArrayList(Sessione.Ruolo.values()));

        // 3. Setup delle colonne delle tabelle (Classi Anonime)
        setupTabelle();

        // 4. Carica i dati iniziali (visualizza stato corrente)
        aggiornaTabelle();
    }

    /**
     * Gestisce il click sul pulsante "Accedi" nella Toolbar.
     */
    @FXML
    public void onLoginClick() {
        Sessione.Ruolo ruoloSelezionato = comboRuolo.getValue();

        if (ruoloSelezionato == null) {
            mostraErrore("Attenzione", "Seleziona un ruolo per accedere.");
            return;
        }

        // Imposta la sessione globale
        Sessione.getInstance().setRuoloCorrente(ruoloSelezionato);

        // Aggiorna la GUI in base al ruolo
        lblRuoloCorrente.setText("Utente: " + ruoloSelezionato);

        if (ruoloSelezionato == Sessione.Ruolo.MANAGER) {
            btnCarica.setDisable(false); // Il Manager può caricare
            lblStatus.setText("Accesso Manager: Controllo completo.");
            lblStatus.setTextFill(Color.BLACK);
        } else if (ruoloSelezionato == Sessione.Ruolo.CORRIERE) {
            btnCarica.setDisable(true);  // Il Corriere può solo vedere
            lblStatus.setText("Accesso Corriere: Modalità visualizzazione.");
            lblStatus.setTextFill(Color.BLACK);
        } else {
            // Se un CLIENTE prova ad accedere qui (non dovrebbe succedere, ma gestiamolo)
            btnCarica.setDisable(true);
            lblStatus.setText("Accesso Cliente limitato.");
        }
    }

/**
 * Esegue l'algoritmo di caricamento (Strategy).
 */