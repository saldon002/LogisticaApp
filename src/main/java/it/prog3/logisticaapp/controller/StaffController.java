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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class StaffController {

    // Login e UI generale
    @FXML private ComboBox<Sessione.Ruolo> comboRuolo;
    @FXML private Label lblUtente;
    @FXML private Label lblStatus;

    // Pannelli specifici
    @FXML private HBox boxManager;   // Contiene bottoni Manager
    @FXML private HBox boxCorriere;  // Contiene bottoni Corriere

    // Controlli Manager
    @FXML private ComboBox<String> comboStrategia;

    // Controlli Corriere
    @FXML private TextField txtLuogoTappa;

    // Tabelle
    @FXML private TableView<ICollo> tableColli;
    @FXML private TableColumn<ICollo, String> colColloCodice;
    @FXML private TableColumn<ICollo, String> colColloPeso;
    @FXML private TableColumn<ICollo, String> colColloDest;

    @FXML private TableView<IVeicolo> tableVeicoli;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTarga;
    @FXML private TableColumn<IVeicolo, String> colVeicoloTipo;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCapienza;
    @FXML private TableColumn<IVeicolo, String> colVeicoloCarico;

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

        // Popola la combo strategie (Mockup, in realtà è gestito dal Facade)
        comboStrategia.getItems().add("Next Fit Strategy");
        comboStrategia.getSelectionModel().selectFirst();

        setupTabelle();
        aggiornaDati();
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
        // Logica MANAGER
        try {
            facade.caricaMerce(); // Algoritmo NextFit
            aggiornaDati();
            lblStatus.setText("Caricamento completato con successo!");
            mostraAlert("Successo", "Algoritmo eseguito. I colli sono stati spostati nei veicoli.");
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
            // Chiamata al metodo Bulk Update nel Facade
            facade.registraTappaVeicolo(veicoloSelezionato.getCodice(), luogo);

            aggiornaDati(); // Ricarica le tabelle per vedere i cambiamenti
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
            // Aggiorna tabella Colli (Quelli ancora a terra)
            tableColli.setItems(FXCollections.observableArrayList(facade.getTuttiIColliInMagazzino()));

            // Aggiorna tabella Veicoli
            tableVeicoli.setItems(FXCollections.observableArrayList(facade.getFlottaAttuale()));
            tableVeicoli.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTabelle() {
        // Setup Colonne COLLI (Senza Lambda)
        colColloCodice.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<ICollo, String> p) {
                return new SimpleStringProperty(p.getValue().getCodice());
            }
        });
        colColloPeso.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<ICollo, String> p) {
                return new SimpleStringProperty(p.getValue().getPeso() + " kg");
            }
        });
        colColloDest.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ICollo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<ICollo, String> p) {
                return new SimpleStringProperty(p.getValue().getDestinatario());
            }
        });

        // Setup Colonne VEICOLI
        colVeicoloTarga.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<IVeicolo, String> p) {
                return new SimpleStringProperty(p.getValue().getCodice());
            }
        });
        colVeicoloTipo.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<IVeicolo, String> p) {
                return new SimpleStringProperty(p.getValue().getTipo());
            }
        });
        colVeicoloCapienza.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<IVeicolo, String> p) {
                return new SimpleStringProperty(String.valueOf(p.getValue().getCapienza()));
            }
        });
        // Colonna Calcolata: Quanti colli ci sono dentro?
        colVeicoloCarico.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<IVeicolo, String>, ObservableValue<String>>() {
            @Override public ObservableValue<String> call(TableColumn.CellDataFeatures<IVeicolo, String> p) {
                int n = (p.getValue().getCarico() == null) ? 0 : p.getValue().getCarico().size();
                return new SimpleStringProperty(n + " colli");
            }
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