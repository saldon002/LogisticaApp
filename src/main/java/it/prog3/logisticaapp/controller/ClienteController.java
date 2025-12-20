package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.util.Observer;
import it.prog3.logisticaapp.util.Subject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ClienteController implements Observer {

    @FXML private TextField txtCodice;
    @FXML private Button btnCerca;

    @FXML private Label lblRisultato;
    @FXML private Label lblStato;

    @FXML private VBox boxStorico; // Contenitore per nascondere/mostrare lo storico
    @FXML private ListView<String> listStorico;

    private LogisticaFacade facade;

    // Manteniamo un riferimento al soggetto osservato (Subject)
    private Subject colloOsservato;
    // Manteniamo anche il riferimento all'interfaccia ICollo per leggere i dati comodamente
    private ICollo colloCorrente;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();
        this.boxStorico.setVisible(false); // Inizialmente nascosto

        // Focus sul campo testo
        Platform.runLater(new Runnable() {
            @Override public void run() { txtCodice.requestFocus(); }
        });
    }

    @FXML
    public void onCercaSpedizione() {
        String codice = txtCodice.getText().trim().toUpperCase();

        if (codice.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Inserisci un codice spedizione.");
            alert.show();
            return;
        }

        // Reset vista
        listStorico.getItems().clear();
        lblStato.setText("");
        lblRisultato.setText("Ricerca in corso...");
        boxStorico.setVisible(false);

        // Rimuovi vecchio observer se presente (detach)
        if (colloOsservato != null) {
            colloOsservato.detach(this);
            colloOsservato = null;
            colloCorrente = null;
        }

        try {
            // 1. Cerca il collo
            ICollo collo = facade.cercaCollo(codice);

            if (collo == null) {
                lblRisultato.setText("Spedizione non trovata.");
                lblStato.setText("Verifica il codice inserito.");
                return;
            }

            this.colloCorrente = collo;

            // 2. Aggiorna GUI in base allo stato
            lblRisultato.setText("Spedizione: " + collo.getCodice());
            aggiornaVistaDettaglio();

            // 3. OBSERVER PATTERN: Attacchiamo l'observer se è un Subject
            if (collo instanceof Subject) {
                this.colloOsservato = (Subject) collo;
                this.colloOsservato.attach(this);
            }

        } catch (Exception e) {
            lblRisultato.setText("Errore di sistema.");
            e.printStackTrace();
        }
    }

    /**
     * Metodo helper per popolare la vista usando 'colloCorrente'
     */
    private void aggiornaVistaDettaglio() {
        if (colloCorrente == null) return;

        String stato = colloCorrente.getStato();

        if ("IN_PREPARAZIONE".equals(stato)) {
            // Caso 1: Non ancora partito
            lblStato.setText("Stato: NON ANCORA SPEDITO");
            lblStato.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;"); // Arancione

            // Nascondiamo lo storico ma mostriamo un messaggio di cortesia
            listStorico.getItems().clear();
            listStorico.getItems().add("Il pacco è in preparazione presso i nostri magazzini.");
            boxStorico.setVisible(true);

        } else if ("IN_TRANSITO".equals(stato)) {
            // Caso 2: In viaggio
            lblStato.setText("Stato: IN TRANSITO");
            lblStato.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
            boxStorico.setVisible(true);

            // Recupera storico completo
            // Nota: Se colloCorrente è ColloReale, ha già lo storico dentro.
            // Se è Proxy, facade.getStoricoCollo forza il caricamento.
            List<String> storico = facade.getStoricoCollo(colloCorrente.getCodice());

            listStorico.getItems().clear();
            if (storico.isEmpty()) {
                listStorico.getItems().add("Nessun aggiornamento disponibile.");
            } else {
                for (String evento : storico) {
                    listStorico.getItems().add(evento);
                }
            }
        } else {
            // Altri stati
            lblStato.setText("Stato: " + stato);
            lblStato.setStyle("-fx-text-fill: #333333;");
            boxStorico.setVisible(true);
        }
    }

    /**
     * Metodo dell'interfaccia Observer.
     * Chiamato quando il Subject notifica un cambiamento.
     * NON ha parametri, quindi usiamo 'colloCorrente' che abbiamo salvato.
     */
    @Override
    public void update() {
        // Platform.runLater assicura che l'aggiornamento GUI avvenga nel thread JavaFX
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("[ClienteGUI] Ricevuta notifica update()! Ricarico vista...");
                aggiornaVistaDettaglio();
            }
        });
    }

    @FXML
    public void onLogout() {
        // Pulizia Observer
        if (this.colloOsservato != null) {
            this.colloOsservato.detach(this);
        }

        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}