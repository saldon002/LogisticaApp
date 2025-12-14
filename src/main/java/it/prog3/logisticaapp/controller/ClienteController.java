package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.util.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

public class ClienteController implements Observer {

    @FXML private TextField txtTracking;
    @FXML private VBox boxRisultati;
    @FXML private Label lblStato;
    @FXML private Label lblAzienda;
    @FXML private ListView<String> listStorico;

    private LogisticaFacade facade;
    private ICollo colloOsservato; // Il Subject

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();
    }

    @FXML
    public void onCercaClick() {
        String codice = txtTracking.getText().trim();
        if (codice.isEmpty()) return;

        // 1. Cerchiamo il collo
        ICollo c = facade.cercaCollo(codice);

        if (c != null) {
            gestisciNuovoCollo(c);
        } else {
            boxRisultati.setVisible(false);
            mostraAlert("Non trovato", "Nessuna spedizione trovata con codice: " + codice);
        }
    }

    private void gestisciNuovoCollo(ICollo c) {
        // A. Se stavo osservando un altro collo, mi stacco
        if (this.colloOsservato != null) {
            this.colloOsservato.detach(this);
        }

        // B. Mi collego al nuovo collo (Pattern Observer)
        this.colloOsservato = c;
        this.colloOsservato.attach(this);

        // C. Aggiorno la GUI iniziale
        boxRisultati.setVisible(true);
        listStorico.setVisible(false); // Nascondo storico (Lazy)

        lblStato.setText(c.getStato());

        // Logica visualizzazione Azienda (Solo se spedito)
        if (!"IN_PREPARAZIONE".equals(c.getStato())) {
            // Nota: Qui assumiamo "DHL" hardcoded o recuperabile se salvato nel collo
            // Per semplicità ora mostriamo un testo fisso o vuoto se non in preparazione
            lblAzienda.setText("In gestione presso il corriere");
            lblAzienda.setVisible(true);
        } else {
            lblAzienda.setVisible(false);
        }
    }

    @FXML
    public void onVediStoricoClick() {
        if (colloOsservato == null) return;

        // Pattern Virtual Proxy:
        // La chiamata getStorico() scatenerà la query al DB solo ora!
        List<String> storico = colloOsservato.getStorico();

        listStorico.getItems().clear();
        if (storico.isEmpty()) {
            listStorico.getItems().add("Nessun evento registrato.");
        } else {
            // Aggiungiamo elementi senza usare lambda
            for (String evento : storico) {
                listStorico.getItems().add(evento);
            }
        }
        listStorico.setVisible(true);
    }

    // === IMPLEMENTAZIONE OBSERVER (Backend -> GUI) ===
    @Override
    public void update() {
        // Questo metodo viene chiamato dal ColloReale quando cambia stato.
        // Usiamo Platform.runLater per aggiornare la GUI dal thread corretto.

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (colloOsservato != null) {
                    // WOW EFFECT: La label cambia testo da sola!
                    lblStato.setText(colloOsservato.getStato());

                    // Feedback visivo: cambio colore se Consegnato
                    if ("CONSEGNATO".equals(colloOsservato.getStato())) {
                        lblStato.setTextFill(Color.GREEN);
                    } else {
                        lblStato.setTextFill(Color.web("#27ae60")); // Verde scuro default
                    }

                    // Se la lista storico è aperta, ricarichiamola per vedere la nuova tappa
                    if (listStorico.isVisible()) {
                        onVediStoricoClick();
                    }
                }
            }
        });
    }

    private void mostraAlert(String titolo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titolo);
        a.setContentText(msg);
        a.showAndWait();
    }
}