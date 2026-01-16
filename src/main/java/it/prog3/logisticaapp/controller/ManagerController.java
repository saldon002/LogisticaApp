package it.prog3.logisticaapp.controller;

import it.prog3.logisticaapp.App;
import it.prog3.logisticaapp.business.LogisticaFacade;
import it.prog3.logisticaapp.business.NextFitStrategy;
import it.prog3.logisticaapp.model.Azienda;
import it.prog3.logisticaapp.model.ICollo;
import it.prog3.logisticaapp.model.IVeicolo;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class ManagerController {

    // --- Componenti FXML: Tab 1 (Carico) ---
    @FXML private TableView<ICollo> tableColliInAttesa;
    @FXML private TableColumn<ICollo, String> colCodice;
    @FXML private TableColumn<ICollo, Double> colPeso;
    @FXML private TableColumn<ICollo, String> colDestinazione;

    @FXML private TreeView<String> treeVeicoliDisponibili;
    @FXML private ComboBox<String> comboStrategia;
    @FXML private Label lblStatus;

    // --- Componenti FXML: Tab 2 (Monitoraggio) ---
    @FXML private TreeView<String> treeFlottaAttiva;

    private LogisticaFacade facade;

    @FXML
    public void initialize() {
        this.facade = new LogisticaFacade();

        // 1. Configurazione Colonne Tabella

        // Colonna Codice
        colCodice.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ICollo, String> cell) {
                return new SimpleStringProperty(cell.getValue().getCodice());
            }
        });

        // Colonna Peso
        colPeso.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, Double>, ObservableValue<Double>>() {
            @Override
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<ICollo, Double> cell) {
                // .asObject() serve per convertire il double primitivo in oggetto Double
                return new SimpleDoubleProperty(cell.getValue().getPeso()).asObject();
            }
        });

        // Colonna Destinazione
        colDestinazione.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ICollo, String> cell) {
                return new SimpleStringProperty(cell.getValue().getDestinatario());
            }
        });

        // 2. Configurazione Strategie
        comboStrategia.setItems(FXCollections.observableArrayList("NextFit"));
        comboStrategia.getSelectionModel().selectFirst();

        // 3. Caricamento Dati
        aggiornaDati();
    }

    /**
     * Ricarica tutti i dati dal DB e aggiorna le viste.
     */
    private void aggiornaDati() {
        try {
            // A. Aggiorna tabella colli
            List<ICollo> colli = facade.getColliInAttesa();
            tableColliInAttesa.setItems(FXCollections.observableArrayList(colli));

            String msg = "Colli in attesa: " + colli.size();
            if (lblStatus != null) lblStatus.setText(msg);

            // B. Recupera struttura Aziende/Veicoli
            List<Azienda> aziende = facade.getAziendeAll();

            // C. Costruisci Albero Disponibilità (Tab 1)
            costruisciAlberoDisponibili(aziende);

            // D. Costruisci Albero Monitoraggio (Tab 2)
            costruisciAlberoMonitoraggio(aziende);

        } catch (Exception e) {
            System.err.println("Errore aggiornamento dati: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * TAB 1: Mostra SOLO i veicoli che hanno ancora spazio disponibile.
     * Se un veicolo è pieno (carico == capienza), non viene mostrato.
     */
    private void costruisciAlberoDisponibili(List<Azienda> aziende) {
        TreeItem<String> root = new TreeItem<>("Flotta Disponibile (Capienza residua > 0)");
        root.setExpanded(true);

        for (Azienda az : aziende) {
            TreeItem<String> nodoAzienda = new TreeItem<>(az.getNome());
            boolean aziendaHaVeicoliDisponibili = false;

            for (IVeicolo v : az.getFlotta()) {
                // CALCOLO SPAZIO RIMANENTE
                int spazioOccupato = v.getCarico().size();
                int spazioTotale = v.getCapienza();
                int spazioRimanente = spazioTotale - spazioOccupato;

                // Mostriamo il veicolo solo se ha spazio (> 0)
                if (spazioRimanente > 0) {
                    aziendaHaVeicoliDisponibili = true;

                    // Mostriamo info utili
                    String infoVeicolo = String.format("%s [%s] - Libero: %d/%d",
                            v.getTipo(), v.getCodice(), spazioRimanente, spazioTotale);

                    TreeItem<String> nodoVeicolo = new TreeItem<>(infoVeicolo);
                    nodoAzienda.getChildren().add(nodoVeicolo);
                }
            }

            // Aggiungiamo l'azienda all'albero solo se ha almeno un veicolo disponibile
            if (aziendaHaVeicoliDisponibili) {
                root.getChildren().add(nodoAzienda);
            }
        }
        treeVeicoliDisponibili.setRoot(root);
    }

    /**
     * TAB 2: Mostra SOLO i veicoli che trasportano qualcosa.
     * I colli figli mostrano SOLO il codice.
     */
    private void costruisciAlberoMonitoraggio(List<Azienda> aziende) {
        TreeItem<String> root = new TreeItem<>("Monitoraggio Spedizioni (Veicoli Carichi)");
        root.setExpanded(true);

        for (Azienda az : aziende) {
            TreeItem<String> nodoAzienda = new TreeItem<>(az.getNome());
            boolean aziendaAttiva = false;

            for (IVeicolo v : az.getFlotta()) {
                // Mostriamo il veicolo solo se pieno (è in viaggio)
                if (v.getCarico().size() == v.getCapienza()) {
                    aziendaAttiva = true;

                    String infoVeicolo = v.getTipo() + " [" + v.getCodice() + "] - Carico: " + v.getCarico().size();
                    TreeItem<String> nodoVeicolo = new TreeItem<>(infoVeicolo);

                    // Espandiamo per vedere subito il contenuto
                    nodoVeicolo.setExpanded(true);

                    // Aggiungiamo i figli mostrando SOLO IL CODICE
                    for (ICollo c : v.getCarico()) {
                        TreeItem<String> nodoCollo = new TreeItem<>(c.getCodice());
                        nodoVeicolo.getChildren().add(nodoCollo);
                    }

                    nodoAzienda.getChildren().add(nodoVeicolo);
                }
            }

            // Aggiungiamo l'azienda solo se ha spedizioni in corso
            if (aziendaAttiva) {
                root.getChildren().add(nodoAzienda);
            }
        }
        treeFlottaAttiva.setRoot(root);
    }

    @FXML
    public void onEseguiCarico() {
        try {
            // 1. Imposta strategia
            String selezione = comboStrategia.getValue();
            if ("NextFit".equals(selezione)) {
                facade.setStrategy(new NextFitStrategy());
            }

            // 2. Esegui
            facade.eseguiCarico();

            // 3. Feedback
            lblStatus.setText("Carico completato con successo!");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successo");
            alert.setHeaderText(null);
            alert.setContentText("Procedura di carico terminata.\nControlla il tab 'Stato Flotta'.");
            alert.showAndWait();

            // 4. Refresh
            aggiornaDati();

        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setContentText(e.getMessage());
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore critico: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    @FXML
    public void onRefresh() {
        aggiornaDati();
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