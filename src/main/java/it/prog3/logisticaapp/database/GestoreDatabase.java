package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) che implementa le operazioni CRUD.
 * Gestisce la persistenza di Veicoli, Colli e Storico.
 */
public class GestoreDatabase {

    // Query SQL
    private static final String SELECT_COLLI_PREPARAZIONE = "SELECT codice, stato FROM colli WHERE stato = 'IN_PREPARAZIONE'";
    private static final String SELECT_COLLO_BASE = "SELECT codice, stato FROM colli WHERE codice = ?";
    private static final String SELECT_COLLO_FULL = "SELECT * FROM colli WHERE codice = ?";
    private static final String SELECT_STORICO = "SELECT descrizione, timestamp FROM storico_spostamenti WHERE collo_codice = ? ORDER BY timestamp DESC";
    private static final String INSERT_COLLO = "INSERT INTO colli (codice, stato, peso, mittente, destinatario) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_VEICOLI_AZIENDA = "SELECT * FROM veicoli WHERE azienda = ?";
    private static final String SELECT_VEICOLI_ALL = "SELECT * FROM veicoli ORDER BY azienda, codice";
    private static final String INSERT_VEICOLO = "INSERT INTO veicoli (codice, tipo, capienza, azienda, stato) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_STATO_COLLO = "UPDATE colli SET stato = ? WHERE codice = ?";
    private static final String INSERT_STORICO = "INSERT INTO storico_spostamenti (collo_codice, descrizione) VALUES (?, ?)";

    public GestoreDatabase() {}

    // =================================================================================
    // SEZIONE 1: MANAGER & FLOTTA (Lettura)
    // =================================================================================

    /**
     * Metodo per il MANAGER: Recupera l'intera flotta dal DB raggruppata per Azienda.
     * Serve per popolare la GUI che mostra l'albero Azienda -> Veicoli.
     * * @return Lista di oggetti Azienda, ciascuno con la propria flotta popolata.
     */
    public List<Azienda> getFlottaAll() {
        List<Azienda> listaAziende = new ArrayList<>();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_VEICOLI_ALL)) {

            while (rs.next()) {
                String nomeAzienda = rs.getString("azienda");
                String codice = rs.getString("codice");
                String tipo = rs.getString("tipo");

                // 1. Cerchiamo se abbiamo già creato l'oggetto Azienda nella lista
                Azienda aziendaCorrente = null;
                for (Azienda a : listaAziende) {
                    if (a.getNome().equalsIgnoreCase(nomeAzienda)) {
                        aziendaCorrente = a;
                        break;
                    }
                }

                // Se non c'è, la creiamo e la aggiungiamo
                if (aziendaCorrente == null) {
                    aziendaCorrente = new AziendaConcreta(nomeAzienda);
                    listaAziende.add(aziendaCorrente);
                }

                // 2. Creazione Veicolo tramite Factory dell'Azienda
                try {
                    // La Factory crea l'oggetto corretto (es. new Camion()) impostando la capienza default
                    IVeicolo v = aziendaCorrente.createVeicolo(tipo, codice);

                    if (v != null) {
                        // Aggiungiamo il veicolo alla lista interna dell'azienda
                        aziendaCorrente.aggiungiVeicoloEsistente(v);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Skip veicolo non valido nel DB: " + codice + " (" + tipo + ")");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel caricamento della flotta manager", e);
        }

        return listaAziende;
    }

    /**
     * Recupera la lista dei veicoli di una specifica azienda.
     */
    public List<IVeicolo> getFlottaAzienda(String nomeAzienda) {
        List<IVeicolo> flotta = new ArrayList<>();
        // Usiamo un'istanza helper solo per accedere al factory method
        AziendaConcreta factoryHelper = new AziendaConcreta(nomeAzienda);

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_VEICOLI_AZIENDA)) {

            st.setString(1, nomeAzienda);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String codice = rs.getString("codice");

                    IVeicolo v = factoryHelper.createVeicolo(tipo, codice);

                    if (v != null) {
                        flotta.add(v);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getFlottaAzienda per " + nomeAzienda, e);
        }
        return flotta;
    }

    // =================================================================================
    // SEZIONE 2: SETUP & TESTER (Scrittura Dati Iniziali)
    // =================================================================================

    /**
     * Inserisce un nuovo collo nel DB.
     * Utile per il Setup/Tester iniziale.
     */
    public void inserisciCollo(ICollo c) {

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_COLLO)) {

            ps.setString(1, c.getCodice());
            ps.setString(2, c.getStato());
            ps.setDouble(3, c.getPeso());
            ps.setString(4, c.getMittente());
            ps.setString(5, c.getDestinatario());

            ps.executeUpdate();
            System.out.println("[DB] Inserito collo: " + c.getCodice());

        } catch (SQLException e) {
            System.err.println("Errore inserimento collo " + c.getCodice() + ": " + e.getMessage());
        }
    }

    /**
     * Inserisce un'intera azienda e tutta la sua flotta nel DB.
     * Utile per il Setup/Tester iniziale (popola il DB partendo dagli oggetti Java).
     */
    public void inserisciAzienda(Azienda azienda) {
        if (azienda == null) return;

        // Salviamo ogni veicolo della flotta
        for (IVeicolo v : azienda.getFlotta()) {
            inserisciVeicolo(v, azienda.getNome());
        }
    }

    /**
     * Inserisce un singolo veicolo nel DB.
     * Prende i dati dall'oggetto Java.
     */
    public void inserisciVeicolo(IVeicolo v, String nomeAzienda) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_VEICOLO)) {

            ps.setString(1, v.getCodice());
            ps.setString(2, v.getTipo());
            ps.setInt(3, v.getCapienza());
            ps.setString(4, nomeAzienda);
            //ps.setString(5, "DISPONIBILE");

            ps.executeUpdate();
            System.out.println("[DB] Inserito veicolo: " + v.getCodice() + " per " + nomeAzienda);

        } catch (SQLException e) {
            System.err.println("Errore inserimento veicolo " + v.getCodice() + ": " + e.getMessage());
        }
    }

    // =================================================================================
    // SEZIONE 3: COLLI (Lettura Proxy & Real)
    // =================================================================================

    /**
     * Recupera la lista di tutti i colli in stato 'IN_PREPARAZIONE'.
     * Restituisce oggetti Proxy leggeri.
     */
    public List<ICollo> getColliInPreparazione() {
        List<ICollo> lista = new ArrayList<>();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_COLLI_PREPARAZIONE)) {

            while (rs.next()) {
                lista.add(new ColloProxy(rs.getString("codice"), rs.getString("stato")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColliInPreparazione", e);
        }
        return lista;
    }

    /**
     * Cerca un singolo collo tramite il codice (Proxy).
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
     * Chiamato dal Proxy quando serve caricare i dettagli.
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
            throw new RuntimeException("Errore DB in getStoricoPerCollo per " + codiceCollo, e);
        }
        return storico;
    }

    // =================================================================================
    // SEZIONE 4: AGGIORNAMENTI (Update)
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
            throw new RuntimeException("Errore DB in salvaCollo " + c.getCodice(), e);
        }
    }

    /**
     * Inserisce una nuova riga nella tabella storico.
     */
    public void aggiornaTracking(String codiceCollo, String descrizione) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(INSERT_STORICO)) {

            st.setString(1, codiceCollo);
            st.setString(2, descrizione);

            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in aggiornaTracking per " + codiceCollo, e);
        }
    }

    /**
     * Utility per pulire le tabelle (utile nei test).
     */
    public void resetTabelle() {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM veicoli");
            // stmt.executeUpdate("DELETE FROM colli"); // Decommentare se necessario
            System.out.println("[DB] Tabelle veicoli resettata.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}