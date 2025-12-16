package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) che implementa le operazioni CRUD.
 */
public class GestoreDatabase {

    private static final String SELECT_COLLI_PREPARAZIONE = "SELECT codice, stato FROM colli WHERE stato = 'IN_PREPARAZIONE'";
    private static final String SELECT_COLLO_BASE = "SELECT codice, stato FROM colli WHERE codice = ?";
    private static final String SELECT_COLLO_FULL = "SELECT * FROM colli WHERE codice = ?";
    private static final String SELECT_STORICO = "SELECT descrizione, timestamp FROM storico_spostamenti WHERE collo_codice = ? ORDER BY timestamp DESC";
    private static final String SELECT_VEICOLI_AZIENDA = "SELECT * FROM veicoli WHERE azienda = ?";
    private static final String UPDATE_STATO_COLLO = "UPDATE colli SET stato = ? WHERE codice = ?";
    private static final String INSERT_STORICO = "INSERT INTO storico_spostamenti (collo_codice, descrizione) VALUES (?, ?)";

    public GestoreDatabase() {}

    // =================================================================================
    // SEZIONE COLLI
    // =================================================================================

    /**
     * Recupera la lista di tutti i colli in stato 'IN_PREPARAZIONE'.
     * <p>
     * Restituisce oggetti Proxy leggeri per ottimizzare le risorse.
     * </p>
     */
    public List<ICollo> getColliInPreparazione() {
        List<ICollo> lista = new ArrayList<>();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_COLLI_PREPARAZIONE);
             ResultSet rs = st.executeQuery()) { // Slide 9: ResultSet

            while (rs.next()) {
                lista.add(new ColloProxy(rs.getString("codice"), rs.getString("stato")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColliInPreparazione", e);
        }
        return lista;
    }

    /**
     * Cerca un singolo collo tramite il codice.
     *
     * @param codice Il codice del collo da cercare.
     * @return Un oggetto ColloProxy se trovato, altrimenti null.
     */
    public ICollo getColloProxy(String codice) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_COLLO_BASE)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new ColloProxy(rs.getString("codice"), rs.getString("stato"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColloProxy. Ricerca collo " + codice, e);
        }
        return null;
    }

    /**
     * Recupera l'oggetto Reale completo (Dati collo + Storico).
     *
     * @param codice Il codice del collo.
     * @return L'oggetto ColloReale popolato.
     */
    public ColloReale getColloRealeCompleto(String codice) {
        ColloReale reale = null;

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_COLLO_FULL)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    // Mappatura JavaBean (tabella -> oggetto)
                    reale = new ColloReale();
                    reale.setCodice(rs.getString("codice"));
                    reale.setStato(rs.getString("stato"));
                    reale.setPeso(rs.getDouble("peso"));
                    reale.setMittente(rs.getString("mittente"));
                    reale.setDestinatario(rs.getString("destinatario"));
                    // Popola la lista complessa (relazione one-to-many)
                    reale.setStorico(getStoricoPerCollo(codice));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColloRealeCompleto. Caricamento completo collo " + codice, e);
        }
        return reale;
    }

    /**
     * Metodo ausiliario privato per recuperare lo storico spostamenti.
     * Esegue una query sulla tabella correlata 'storico_spostamenti'.
     */
    private List<String> getStoricoPerCollo(String codiceCollo) {
        List<String> storico = new ArrayList<>();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_STORICO)) {

            st.setString(1, codiceCollo);
            try (ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    storico.add(rs.getString("timestamp") + " - " + rs.getString("descrizione"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getStoricoPerCollo. Storico collo " + codiceCollo, e);
        }
        return storico;
    }

    // =================================================================================
    // SEZIONE AGGIORNAMENTI (UPDATE / INSERT)
    // =================================================================================

    /**
     * Aggiorna lo stato di un collo esistente.
     */
    public void salvaCollo(ICollo c) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(UPDATE_STATO_COLLO)) {

            st.setString(1, c.getStato());
            st.setString(2, c.getCodice());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in salvaCollo. Errore salvataggio collo " + c.getCodice(), e);
        }
    }

    /**
     * Inserisce una nuova riga nella tabella storico.
     *
     * @param codiceCollo Il codice del collo a cui aggiungere l'evento.
     * @param descrizione La descrizione dell'evento.
     */
    public void aggiornaTracking(String codiceCollo, String descrizione) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(INSERT_STORICO)) {

            st.setString(1, codiceCollo);
            st.setString(2, descrizione);

            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in aggiornaTracking. Errore inserimento storico per " + codiceCollo, e);
        }
    }

    // =================================================================================
    // SEZIONE VEICOLI
    // =================================================================================

    /**
     * Recupera la lista dei veicoli di una specifica azienda.
     * <p>
     * Utilizza il pattern <b>Factory Method</b> per istanziare
     * dinamicamente l'oggetto corretto (Camion o Furgone) in base al valore della colonna 'tipo'.
     * </p>
     */
    public List<IVeicolo> getFlotta(String nomeAzienda) {
        List<IVeicolo> flotta = new ArrayList<>();
        AziendaConcreta factoryHelper = new AziendaConcreta();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_VEICOLI_AZIENDA)) {

            st.setString(1, nomeAzienda);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String codice = rs.getString("codice");
                    int capienza = rs.getInt("capienza");

                    IVeicolo v = factoryHelper.createVeicolo(tipo, codice);

                    if (v != null) {
                        v.setCapienza(capienza);
                        flotta.add(v);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getFlotta. Errore caricamento flotta azienda " + nomeAzienda, e);
        }
        return flotta;
    }
}