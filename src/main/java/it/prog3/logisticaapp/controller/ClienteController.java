package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.util.Observer;
import it.prog3.logisticaapp.util.Subject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class ClienteController implements Observer {

    @FXML private TextField txtCodice;
    @FXML private Button btnCerca;

    @FXML private Label lblRisultato;
    @FXML private Label lblStato;

    // Nuovo bottone per attivare il Proxy/Caricamento
    @FXML private Button btnVediStorico;

    @FXML private VBox boxStorico;
    @FXML private ListView<String> listStorico;

    private LogisticaFacade facade;

    private Subject colloOsservato;
    private ICollo colloCorrente;

    // Flag per sapere se l'utente ha già chiesto di vedere i dettagli
    private boolean dettagliCaricati = false;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();
        resetVista();

        Platform.runLater(() -> txtCodice.requestFocus());
    }

    private void resetVista() {
        lblRisultato.setText("Inserisci un codice per iniziare");
        lblStato.setText("");
        boxStorico.setVisible(false);
        btnVediStorico.setVisible(false);
        listStorico.getItems().clear();
        dettagliCaricati = false;
    }

    @FXML
    public void onCercaSpedizione() {
        String codice = txtCodice.getText().trim().toUpperCase();

        if (codice.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Inserisci un codice spedizione.").show();
            return;
        }

        resetVista();

        // Stacca observer precedente
        if (colloOsservato != null) {
            colloOsservato.detach(this);
            colloOsservato = null;
            colloCorrente = null;
        }

        try {
            // 1. Otteniamo il Proxy (leggero, solo codice e stato)
            ICollo collo = facade.cercaCollo(codice);

            if (collo == null) {
                lblRisultato.setText("Spedizione non trovata.");
                return;
            }

            this.colloCorrente = collo;
            lblRisultato.setText("Spedizione: " + collo.getCodice());

            // 2. Aggiorniamo la vista base (senza caricare storico)
            aggiornaStatoUI();

            // 3. Observer
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
     * Aggiorna solo l'intestazione dello stato.
     * NON carica lo storico dal DB.
     */
    private void aggiornaStatoUI() {
        if (colloCorrente == null) return;

        String stato = colloCorrente.getStato();

        // Gestione stati "Non Spedito"
        if ("IN_PREPARAZIONE".equals(stato) || "CARICATO".equals(stato)) {
            lblStato.setText("Stato: NON ANCORA SPEDITO");
            lblStato.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");

            // Nascondiamo tutto ciò che riguarda lo storico
            btnVediStorico.setVisible(false);
            boxStorico.setVisible(false);

        } else if ("IN_TRANSITO".equals(stato)) {
            lblStato.setText("Stato: IN TRANSITO / SPEDITO");
            lblStato.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // Qui sta la differenza: Mostriamo il bottone, MA NON CARICHIAMO I DATI
            if (!dettagliCaricati) {
                btnVediStorico.setVisible(true);
                boxStorico.setVisible(false);
            } else {
                // Se l'utente aveva già cliccato, ricarichiamo la lista (caso aggiornamento live)
                caricaStorico();
            }
        }
    }

    /**
     * Azione del bottone "Visualizza Aggiornamenti".
     * Questo è il momento in cui il Proxy carica il RealSubject (o fa la query pesante).
     */
    @FXML
    public void onVediStorico() {
        if (colloCorrente == null) return;

        System.out.println("[ClienteController] Utente richiede storico -> Attivazione Proxy/DB...");

        dettagliCaricati = true; // Ricordiamo che l'utente vuole vedere i dettagli
        btnVediStorico.setVisible(false); // Nascondiamo il bottone
        boxStorico.setVisible(true); // Mostriamo la lista

        caricaStorico();
    }

    private void caricaStorico() {
        try {
            // Questa chiamata triggera il caricamento completo
            List<String> storico = facade.getStoricoCollo(colloCorrente.getCodice());

            listStorico.getItems().clear();
            if (storico.isEmpty()) {
                listStorico.getItems().add("Nessun dettaglio disponibile al momento.");
            } else {
                for (String evento : storico) {
                    listStorico.getItems().add(evento);
                }
            }
        } catch (Exception e) {
            listStorico.getItems().add("Errore nel recupero dati.");
        }
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            System.out.println("[ClienteGUI] Update ricevuto.");
            // Aggiorniamo lo stato (es. se passa da PREPARAZIONE a TRANSITO)
            aggiornaStatoUI();
        });
    }

    @FXML
    public void onLogout() {
        if (colloOsservato != null) colloOsservato.detach(this);
        try {
            it.prog3.logisticaapp.App.setRoot("login");
        } catch (Exception e) { e.printStackTrace(); }
    }
}