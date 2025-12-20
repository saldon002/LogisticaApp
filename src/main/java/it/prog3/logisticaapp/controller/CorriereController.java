package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class CorriereController {

    @FXML private Label lblBenvenuto;
    @FXML private ComboBox<IVeicolo> comboVeicoli;
    @FXML private ListView<String> listCarico;
    @FXML private TextField txtLuogo;
    @FXML private Button btnAggiorna;
    @FXML private Label lblStatus;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();

        // Messaggio generico, non serve più il nome azienda
        lblBenvenuto.setText("Pannello Operatore Corriere");

        // 1. Configura il ComboBox (NO LAMBDA)
        comboVeicoli.setConverter(new StringConverter<IVeicolo>() {
            @Override
            public String toString(IVeicolo v) {
                if (v == null) return null;
                // Mostriamo: TIPO - TARGA (Capienza occupata)
                return v.getTipo() + " - " + v.getCodice() + " (Carico: " + v.getCarico().size() + ")";
            }

            @Override
            public IVeicolo fromString(String string) {
                return null;
            }
        });

        // 2. Listener cambio selezione (NO LAMBDA)
        comboVeicoli.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<IVeicolo>() {
            @Override
            public void changed(ObservableValue<? extends IVeicolo> observable, IVeicolo oldValue, IVeicolo newValue) {
                mostraCaricoVeicolo(newValue);
            }
        });

        // 3. Carica TUTTI i veicoli
        caricaFlottaCompleta();
    }

    private void caricaFlottaCompleta() {
        try {
            // Chiama il nuovo metodo generico del Facade
            List<IVeicolo> flotta = facade.getFlotta();

            comboVeicoli.setItems(FXCollections.observableArrayList(flotta));

            if (flotta.isEmpty()) {
                lblStatus.setText("Nessun veicolo presente nel sistema.");
            } else {
                lblStatus.setText("Seleziona il veicolo da aggiornare.");
            }
        } catch (Exception e) {
            lblStatus.setText("Errore caricamento flotta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraCaricoVeicolo(IVeicolo v) {
        if (v == null) {
            listCarico.getItems().clear();
            return;
        }

        listCarico.getItems().clear();

        if (v.getCarico().isEmpty()) {
            listCarico.getItems().add("Il veicolo è vuoto.");
            // Potresti voler disabilitare il bottone aggiorna se è vuoto
            return;
        }

        for (ICollo c : v.getCarico()) {
            String riga = String.format("[%s] -> %s (Stato: %s)",
                    c.getCodice(), c.getDestinatario(), c.getStato());
            listCarico.getItems().add(riga);
        }

        lblStatus.setText("Veicolo " + v.getCodice() + " selezionato.");
    }

    @FXML
    public void onAggiornaPosizione() {
        IVeicolo veicoloSelezionato = comboVeicoli.getSelectionModel().getSelectedItem();
        String luogo = txtLuogo.getText().trim();

        if (veicoloSelezionato == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona prima un veicolo!");
            alert.show();
            return;
        }

        if (veicoloSelezionato.getCarico().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Questo veicolo è vuoto, non c'è nulla da tracciare.");
            alert.show();
            return;
        }

        if (luogo.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Inserisci il luogo corrente.");
            alert.show();
            return;
        }

        try {
            facade.registraTappaVeicolo(veicoloSelezionato, luogo);

            lblStatus.setText("Posizione aggiornata a: " + luogo);
            txtLuogo.clear();

            // Refresh della lista per vedere eventuali cambi (opzionale)
            mostraCaricoVeicolo(veicoloSelezionato);

            Alert info = new Alert(Alert.AlertType.INFORMATION, "Tracking aggiornato con successo!");
            info.show();

        } catch (Exception e) {
            lblStatus.setText("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogout() {
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}