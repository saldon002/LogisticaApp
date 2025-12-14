package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.business.Sessione;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class StaffController {

    // Login e UI generale
    @FXML private ComboBox<Sessione.Ruolo> comboRuolo;
    @FXML private Label lblUtente;
    @FXML private Label lblStatus;

    // Pannelli specifici per i ruoli
    @FXML private HBox boxManager;   // Contiene bottoni Manager
    @FXML private HBox boxCorriere;  // Contiene bottoni Corriere

    // Controlli Manager
    @FXML private ComboBox<String> comboStrategia;

    // Controlli Corriere
    @FXML private TextField txtLuogoTappa;

    // --- TABELLE (Tab 1) ---
    @FXML private TableView<ICollo> tableColli;
    @FXML private TableColumn<ICollo, String> colColloCodice;
    @FXML private TableColumn<ICollo, String> colColloPeso;
    @FXML private TableColumn<ICollo, String> colColloDest;

    @FXML private TableView<IVeicolo> tableVeicoli;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTarga;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTipo;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCapienza;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCarico;

    // --- GRAFICA (Tab 2) ---
    // Questo è il contenitore orizzontale dove "disegneremo" i camion
    @FXML private HBox containerGrafico;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        try {
            this.facade = new LogisticaFacade();
        } catch (Exception e) {
            lblStatus.setText("Errore Critico: Impossibile connettere DB");
            return;
        }

        // Popola la combo ruoli
        comboRuolo.setItems(FXCollections.observableArrayList(Sessione.Ruolo.values()));

        // Popola la combo strategie
        comboStrategia.getItems().add("Next Fit Strategy");
        comboStrategia.getSelectionModel().selectFirst();

        setupTabelle();
        aggiornaDati(); // Carica dati iniziali
    }

    @FXML
    public void onLoginClick() {
        Sessione.Ruolo ruolo = comboRuolo.getValue();
        if (ruolo == null) {
            mostraAlert("Errore", "Seleziona un ruolo!");
            return;
        }

        // 1. Imposta Sessione
        Sessione.getInstance().setRuoloCorrente(ruolo);
        lblUtente.setText("Loggato come: " + ruolo);
        lblUtente.setTextFill(Color.GREEN);

        // 2. Gestione Visibilità Pannelli (Interfaccia Adattiva)
        if (ruolo == Sessione.Ruolo.MANAGER) {
            boxManager.setVisible(true);
            boxManager.setManaged(true);
            boxCorriere.setVisible(false);
            boxCorriere.setManaged(false);
            lblStatus.setText("Modalità Manager: Puoi eseguire il caricamento.");
        }
        else if (ruolo == Sessione.Ruolo.CORRIERE) {
            boxManager.setVisible(false);
            boxManager.setManaged(false);
            boxCorriere.setVisible(true);
            boxCorriere.setManaged(true);
            lblStatus.setText("Modalità Corriere: Seleziona un veicolo per registrare la tappa.");
        }
        else {
            // Cliente o altro
            boxManager.setVisible(false);
            boxCorriere.setVisible(false);
        }
    }

    @FXML
    public void onCaricaClick() {
        // Logica MANAGER: Esegue l'algoritmo
        try {
            facade.caricaMerce(); // Algoritmo NextFit
            aggiornaDati();       // Aggiorna sia tabelle che grafica

            lblStatus.setText("Caricamento completato con successo!");
            mostraAlert("Successo", "Algoritmo eseguito. Controlla il Tab 'Visualizzazione Carico'!");
        } catch (Exception e) {
            mostraAlert("Errore Carico", e.getMessage());
        }
    }

    @FXML
    public void onRegistraTappaClick() {
        // Logica CORRIERE
        IVeicolo veicoloSelezionato = tableVeicoli.getSelectionModel().getSelectedItem();
        String luogo = txtLuogoTappa.getText().trim();

        if (veicoloSelezionato == null) {
            mostraAlert("Attenzione", "Devi selezionare un veicolo dalla tabella 'Flotta'!");
            return;
        }
        if (luogo.isEmpty()) {
            mostraAlert("Attenzione", "Inserisci il nome del luogo (es. Hub Milano).");
            return;
        }

        try {
            facade.registraTappaVeicolo(veicoloSelezionato.getCodice(), luogo);

            aggiornaDati();
            lblStatus.setText("Tappa registrata per il veicolo " + veicoloSelezionato.getCodice());
            txtLuogoTappa.clear();

        } catch (SecurityException se) {
            mostraAlert("Accesso Negato", "Non hai i permessi per questa operazione!");
        } catch (Exception e) {
            mostraAlert("Errore", e.getMessage());
        }
    }

    // --- Metodi di Supporto ---

    private void aggiornaDati() {
        try {
            // 1. Aggiorna Tabelle
            tableColli.setItems(FXCollections.observableArrayList(facade.getTuttiIColliInMagazzino()));
            tableVeicoli.setItems(FXCollections.observableArrayList(facade.getFlottaAttuale()));
            tableVeicoli.refresh();

            // 2. Aggiorna Grafica (Disegna i rettangoli)
            disegnaFlottaGrafica();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo core per la visualizzazione dell'algoritmo Bin Packing.
     * Crea dinamicamente nodi JavaFX (Rettangoli) in base ai dati del modello.
     */
    private void disegnaFlottaGrafica() {
        if (containerGrafico == null) return;
        containerGrafico.getChildren().clear(); // Pulisce il disegno precedente

        for (IVeicolo v : facade.getFlottaAttuale()) {

            // --- A. COSTRUZIONE DEL CONTENITORE VEICOLO ---
            VBox boxVeicolo = new VBox();
            boxVeicolo.setSpacing(2);
            boxVeicolo.setAlignment(Pos.BOTTOM_CENTER); // I pacchi cadono sul fondo
            boxVeicolo.setPrefWidth(120);
            boxVeicolo.setMinWidth(120);

            // Calcolo altezza visuale proporzionale alla capienza (es. scala 1:3)
            double altezzaTotale = v.getCapienza() * 3.0;
            // Limiti min/max estetici per evitare veicoli enormi o invisibili
            if (altezzaTotale < 150) altezzaTotale = 150;
            if (altezzaTotale > 400) altezzaTotale = 400;

            boxVeicolo.setPrefHeight(altezzaTotale);
            // Stile CSS inline per bordi e colore di sfondo
            boxVeicolo.setStyle("-fx-border-color: #34495e; -fx-border-width: 3; -fx-background-color: #ecf0f1; -fx-padding: 5;");

            // --- B. RIEMPIMENTO PACCHI ---
            if (v.getCarico() != null) {
                for (ICollo c : v.getCarico()) {
                    // Creiamo una "scatola" per ogni collo
                    Label lblPacco = new Label(c.getCodice() + "\n" + c.getPeso() + "kg");
                    lblPacco.setAlignment(Pos.CENTER);
                    lblPacco.setMaxWidth(Double.MAX_VALUE); // Si espande in larghezza

                    // Altezza pacco proporzionale al suo peso rispetto alla capienza del veicolo
                    double percentualeOccupata = c.getPeso() / (double)v.getCapienza();
                    double altezzaPacco = percentualeOccupata * altezzaTotale;

                    // Assicuriamo un'altezza minima per leggibilità
                    if (altezzaPacco < 25) altezzaPacco = 25;

                    lblPacco.setPrefHeight(altezzaPacco);

                    // Colore arancione stile "pacco Amazon"
                    lblPacco.setStyle("-fx-background-color: #e67e22; -fx-border-color: white; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");

                    // Aggiungiamo il pacco "in cima" alla pila (index 0 in VBox = in alto)
                    // Nota: Poiché VBox è Pos.BOTTOM_CENTER, add(0) spinge gli altri giù se non c'è spazio,
                    // ma qui vogliamo impilarli. Per simulare la gravità visiva usiamo add(0) e l'ordine inverso o add() normale.
                    // Usando add(0) li mettiamo in alto. Usando add() li mettiamo in basso.
                    // Bin Packing reale: riempiamo dal fondo. Quindi usiamo add(0) per mettere l'ultimo arrivato SOPRA.
                    boxVeicolo.getChildren().add(0, lblPacco);
                }
            }

            // --- C. ETICHETTA DESCRITTIVA SOTTO IL VEICOLO ---
            String infoTesto = String.format("%s\n%s\n(%d/%d)", v.getTipo(), v.getCodice(), v.getCarico().size(), v.getCapienza());
            Label info = new Label(infoTesto);
            info.setStyle("-fx-font-weight: bold; -fx-text-alignment: center; -fx-text-fill: #2c3e50;");

            // Wrapper verticale per unire [Veicolo] + [Etichetta]
            VBox wrapper = new VBox(5, boxVeicolo, info);
            wrapper.setAlignment(Pos.BOTTOM_CENTER);

            // Aggiungiamo il tutto alla scena orizzontale
            containerGrafico.getChildren().add(wrapper);
        }
    }

    private void setupTabelle() {
        // Setup Colonne COLLI
        colColloCodice.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodice()));
        colColloPeso.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPeso() + " kg"));
        colColloDest.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDestinatario()));

        // Setup Colonne VEICOLI
        colVeicoloTarga.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getCodice()));
        colVeicoloTipo.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTipo()));
        colVeicoloCapienza.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getCapienza())));
        colVeicoloCarico.setCellValueFactory(p -> {
            int n = (p.getValue().getCarico() == null) ? 0 : p.getValue().getCarico().size();
            return new SimpleStringProperty(n + " colli");
        });
    }

    private void mostraAlert(String titolo, String testo) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titolo);
        a.setHeaderText(null);
        a.setContentText(testo);
        a.showAndWait();
    }
}