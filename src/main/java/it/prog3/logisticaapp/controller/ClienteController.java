package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.model.ICollo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;

public class ClienteController {

    @FXML private TextField txtTracking;
    @FXML private VBox boxRisultati;
    @FXML private Label lblStato;
    @FXML private ListView<String> listStorico;

    private LogisticaFacade facade;
    private String codiceCorrente;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();
    }

    @FXML
    public void onCercaClick() {
        String codice = txtTracking.getText().trim();
        if (codice.isEmpty()) return;

        ICollo c = facade.cercaCollo(codice);
        if (c != null) {
            codiceCorrente = codice;
            boxRisultati.setVisible(true);
            lblStato.setText(c.getStato());
            listStorico.setVisible(false); // Nascondi storico finché non richiesto

            if ("CONSEGNATO".equals(c.getStato())) lblStato.setTextFill(Color.GREEN);
            else lblStato.setTextFill(Color.BLUE);
        } else {
            boxRisultati.setVisible(false);
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Spedizione non trovata.");
            a.show();
        }
    }

    @FXML
    public void onVediStoricoClick() {
        List<String> storico = facade.getStoricoSpedizione(codiceCorrente);
        listStorico.setItems(FXCollections.observableArrayList(storico));
        listStorico.setVisible(true);
    }

    @FXML
    public void onLogoutClick() throws IOException {
        // Torna al Login
        App.setRoot("view/login");
    }
}