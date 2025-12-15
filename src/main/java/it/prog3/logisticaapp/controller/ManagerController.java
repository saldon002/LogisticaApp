package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class ManagerController {

    @FXML private Label lblUtente;
    @FXML private ListView<String> listColli;
    @FXML private ListView<String> listVeicoli;
    @FXML private TextArea txtDettagliVeicolo;
    @FXML private Button btnCaricaMerce;

    // Riferimento al Facade (Business Logic)
    private LogisticaFacade facade;

    // Liste di supporto per i dati
    private List<IVeicolo> flottaCorrente;

    @FXML
    public void initialize() {
        // 1. Inizializza il Facade
        this.facade = new LogisticaFacade();

        // 2. Mostra info utente
        lblUtente.setText("Ruolo: " + Sessione.getInstance().getRuoloCorrente());

        // 3. Carica i dati iniziali
        aggiornaViste();

        // 4. Gestione selezione lista veicoli (Mostra dettagli)
        listVeicoli.getSelectionModel().selectedIndexProperty().addListener((observable, oldVal, newVal) -> {
            int index = newVal.intValue();
            if (index >= 0 && index < flottaCorrente.size()) {
                mostraDettagliVeicolo(flottaCorrente.get(index));
            }
        });
    }

    @FXML
    void onCaricaMerce(ActionEvent event) {
        try {
            // Chiama la logica di business
            facade.caricaMerce();

            // Feedback utente
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successo");
            alert.setHeaderText("Algoritmo NextFit Completato");
            alert.setContentText("I colli sono stati distribuiti nei veicoli.");
            alert.showAndWait();

            // Ricarica la grafica per mostrare i cambiamenti
            aggiornaViste();

        } catch (Exception e) {
            e.printStackTrace();
            mostraErrore("Errore durante il carico", e.getMessage());
        }
    }

    @FXML
    void onRefresh(ActionEvent event) {
        aggiornaViste();
    }

    @FXML
    void onLogout(ActionEvent event) throws IOException {
        App.setRoot("view/login");
    }

    /**
     * Metodo privato per rinfrescare le ListView interrogando il Facade.
     */
    private void aggiornaViste() {
        // A. Aggiorna lista Colli (quelli ancora in magazzino o tutti?)
        // Per semplicità mostriamo tutti quelli in preparazione
        List<ICollo> colli = facade.getTuttiIColliInMagazzino();
        listColli.getItems().clear();
        if (colli.isEmpty()) {
            listColli.getItems().add("(Nessun collo IN_PREPARAZIONE trovato)");
        } else {
            for (ICollo c : colli) {
                listColli.getItems().add(c.getCodice() + " | " + c.getDestinatario() + " | " + c.getPeso() + "kg");
            }
        }

        // B. Aggiorna lista Veicoli
        this.flottaCorrente = facade.getFlottaAttuale();
        listVeicoli.getItems().clear();

        for (IVeicolo v : flottaCorrente) {
            // Calcolo riempimento
            int colliCaricati = v.getCarico().size();
            listVeicoli.getItems().add(v.getTipo() + " [" + v.getCodice() + "] - Carico: " + colliCaricati + "/" + v.getCapienza());
        }

        // Pulisci dettagli
        txtDettagliVeicolo.setText("");
    }

    private void mostraDettagliVeicolo(IVeicolo v) {
        StringBuilder sb = new StringBuilder();
        sb.append("VEICOLO: ").append(v.getCodice()).append("\n");
        sb.append("TIPO: ").append(v.getTipo()).append("\n");
        sb.append("CAPIENZA MAX: ").append(v.getCapienza()).append("\n");
        sb.append("----------------------------\n");
        sb.append("CONTENUTO CARICO:\n");

        if (v.getCarico().isEmpty()) {
            sb.append("(Vuoto)");
        } else {
            for (ICollo c : v.getCarico()) {
                sb.append("- ").append(c.getCodice())
                        .append(" (").append(c.getPeso()).append("kg) -> ")
                        .append(c.getDestinatario()).append("\n");
            }
        }
        txtDettagliVeicolo.setText(sb.toString());
    }

    private void mostraErrore(String titolo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(titolo);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}