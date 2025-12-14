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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.List;

public class MainController {

    // === ELEMENTI CLIENTE ===
    @FXML private TextField txtTracking;
    @FXML private VBox boxRisultatiCliente;
    @FXML private Label lblRisultatoStato;
    @FXML private ListView<String> listStoricoCliente;

    // === ELEMENTI OPERATORE (Gia presenti) ===
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

    private LogisticaFacade facade;

    // Variabile temporanea per ricordare quale collo sta guardando il cliente
    private String codiceColloCercato = null;

    @FXML
    public void initialize() {
        try {
            this.facade = new LogisticaFacade();
        } catch (Exception e) {
            mostraErrore("Errore DB", e.getMessage());
            return;
        }

        setupTabelle(); // Metodo helper per pulire il codice

        // Setup Combo Ruoli
        comboRuolo.setItems(FXCollections.observableArrayList(Sessione.Ruolo.values()));
        comboRuolo.getSelectionModel().select(Sessione.Ruolo.CLIENTE);

        aggiornaTabelle();
    }

    // =========================================================
    // LOGICA CLIENTE (Ricerca Spedizione)
    // =========================================================

    @FXML
    public void onCercaSpedizioneClick() {
        String codice = txtTracking.getText().trim();
        if (codice.isEmpty()) {
            mostraErrore("Dati mancanti", "Inserisci un codice per cercare.");
            return;
        }

        // 1. Cerchiamo il collo tramite Facade
        ICollo c = facade.cercaCollo(codice);

        if (c != null) {
            // TROVATO!
            codiceColloCercato = codice;
            boxRisultatiCliente.setVisible(true); // Mostra il pannello risultati
            lblRisultatoStato.setText(c.getStato()); // Mostra lo stato

            // Colora lo stato in base al valore
            if ("CONSEGNATO".equals(c.getStato())) lblRisultatoStato.setTextFill(Color.GREEN);
            else lblRisultatoStato.setTextFill(Color.BLUE);

            // Nascondiamo lo storico finché non clicca il bottone
            listStoricoCliente.setVisible(false);

        } else {
            // NON TROVATO
            boxRisultatiCliente.setVisible(false);
            mostraErrore("Non trovato", "Nessuna spedizione trovata con codice: " + codice);
        }
    }

    @FXML
    public void onVediStoricoClick() {
        if (codiceColloCercato == null) return;

        // Recupera lo storico dal Facade
        List<String> storico = facade.getStoricoSpedizione(codiceColloCercato);

        if (storico.isEmpty()) {
            listStoricoCliente.setItems(FXCollections.observableArrayList("Nessun movimento registrato."));
        } else {
            listStoricoCliente.setItems(FXCollections.observableArrayList(storico));
        }

        // Rende visibile la lista
        listStoricoCliente.setVisible(true);
    }

    // =========================================================
    // LOGICA OPERATORE (Login e Carico)
    // =========================================================

    @FXML
    public void onLoginClick() {
        Sessione.Ruolo ruolo = comboRuolo.getValue();
        if (ruolo != null) {
            Sessione.getInstance().setRuoloCorrente(ruolo);
            boolean isAbilitato = (ruolo == Sessione.Ruolo.MANAGER || ruolo == Sessione.Ruolo.CORRIERE);
            btnCarica.setDisable(!isAbilitato);
            lblStatus.setText("Ruolo attivo: " + ruolo);
        }
    }

    @FXML
    public void onCaricaClick() {
        try {
            facade.caricaMerce();
            aggiornaTabelle();
            lblStatus.setText("Algoritmo di carico eseguito.");
        } catch (SecurityException e) {
            mostraErrore("Accesso Negato", "Non hai i permessi.");
        } catch (Exception e) {
            mostraErrore("Errore", e.getMessage());
        }
    }

    @FXML
    public void onResetClick() {
        aggiornaTabelle();
        lblStatus.setText("Dati aggiornati.");
    }

    // =========================================================
    // UTILITIES
    // =========================================================

    private void setupTabelle() {
        // Setup Colli
        colColloCodice.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodice()));
        colColloPeso.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPeso() + " kg"));
        colColloStato.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getStato()));

        // Setup Veicoli
        colVeicoloCodice.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodice()));
        colVeicoloTipo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTipo()));
        colVeicoloCapienza.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getCapienza())));
        colVeicoloCarico.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCarico().size() + " colli"));
    }

    private void aggiornaTabelle() {
        tableVeicoli.setItems(FXCollections.observableArrayList(facade.getFlottaAttuale()));
        tableVeicoli.refresh();
        try {
            tableColli.setItems(FXCollections.observableArrayList(facade.getTuttiIColliInMagazzino()));
        } catch (Exception e) {}
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}