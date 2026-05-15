package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) che implementa le operazioni CRUD.
 * Gestisce la persistenza di Veicoli, Colli e Storico.
 */
public class GestoreDatabase implements IDataLoader {

    // =================================================================================
    // QUERY SQL
    // =================================================================================
    private static final String SELECT_COLLI_PREPARAZIONE = "SELECT codice, stato FROM colli WHERE stato = 'IN_PREPARAZIONE'";
    private static final String SELECT_COLLO_BASE = "SELECT codice, stato FROM colli WHERE codice = ?";
    private static final String SELECT_COLLO_FULL = "SELECT * FROM colli WHERE codice = ?";
    private static final String SELECT_STORICO = "SELECT descrizione, timestamp FROM storico_spostamenti WHERE collo_codice = ? ORDER BY timestamp DESC";
    private static final String INSERT_COLLO = "INSERT INTO colli (codice, stato, peso, mittente, destinatario) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_VEICOLI_ALL = "SELECT * FROM veicoli ORDER BY azienda, codice";
    private static final String INSERT_VEICOLO = "INSERT INTO veicoli (codice, tipo, capienza, azienda) VALUES (?, ?, ?, ?)";

    private static final String INSERT_STORICO = "INSERT INTO storico_spostamenti (collo_codice, descrizione) VALUES (?, ?)";

    private static final String UPDATE_COLLO_CARICATO = "UPDATE colli SET stato = ?, veicolo_codice = ? WHERE codice = ?";
    private static final String SELECT_COLLI_PER_VEICOLO = "SELECT * FROM colli WHERE veicolo_codice = ?";

    // COSTRUTTORE
    public GestoreDatabase() {}

    // =================================================================================
    // SEZIONE 1: MANAGER & FLOTTA
    // =================================================================================

    /**
     * Metodo per il MANAGER: Recupera l'intera flotta dal DB raggruppata per Azienda.
     * @return Lista di oggetti Azienda, ciascuno con la propria flotta popolata.
     */
    public List<Azienda> getFlottaAll() {
        Map<String, Azienda> mappaAziende = new HashMap<>();
        Map<String, VeicoloFactory> mappaFactory = new HashMap<>();
        mappaFactory.put("CAMION", new CamionFactory());
        mappaFactory.put("FURGONE", new FurgoneFactory());

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_VEICOLI_ALL)) {

            while (rs.next()) {
                String nomeAzienda = rs.getString("azienda");
                String codiceVeicolo = rs.getString("codice");
                String tipo = rs.getString("tipo").toUpperCase();

                // 1. Recupera o crea l'azienda
                Azienda aziendaCorrente = mappaAziende.get(nomeAzienda);
                if (aziendaCorrente == null) {
                    aziendaCorrente = new Azienda(nomeAzienda);
                    mappaAziende.put(nomeAzienda, aziendaCorrente);
                }

                // 2. Creazione Veicolo tramite la Factory specifica
                VeicoloFactory factory = mappaFactory.get(tipo);

                if (factory != null) {
                    IVeicolo v = factory.createVeicolo(codiceVeicolo);

                    // Carichiamo i colli associati al veicolo
                    List<ICollo> colliCaricati = getColliPerVeicolo(codiceVeicolo);
                    for (ICollo c : colliCaricati) {
                        v.caricaCollo(c);
                    }
                    aziendaCorrente.aggiungiVeicolo(v);
                } else {
                    System.err.println("Tipo veicolo non supportato: " + tipo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento flotta", e);
        }

        return new ArrayList<>(mappaAziende.values());
    }

    /**
     * Recupera i colli associati a un veicolo specifico.
     */
    public List<ICollo> getColliPerVeicolo(String codiceVeicolo) {
        List<ICollo> lista = new ArrayList<>();
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COLLI_PER_VEICOLO)) {

            ps.setString(1, codiceVeicolo);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    // Creiamo il Proxy passando 'this' come IDataLoader
                    lista.add(new ColloProxy(
                            rs.getString("codice"),
                            rs.getString("stato"),
                            this
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    // =================================================================================
    // SEZIONE 2: SETUP (Scrittura Dati Iniziali)
    // =================================================================================

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

    public void associaColloVeicolo(ICollo c, String codiceVeicolo) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_COLLO_CARICATO)) {

            ps.setString(1, c.getStato());
            ps.setString(2, codiceVeicolo);
            ps.setString(3, c.getCodice());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore associazione collo-veicolo: " + e.getMessage(), e);
        }
    }

    public void inserisciAzienda(Azienda azienda) {
        if (azienda == null) return;
        for (IVeicolo v : azienda.getFlotta()) {
            inserisciVeicolo(v, azienda.getNome());
        }
    }

    public void inserisciVeicolo(IVeicolo v, String nomeAzienda) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_VEICOLO)) {

            ps.setString(1, v.getCodice());
            ps.setString(2, v.getTipo());
            ps.setInt(3, v.getCapienza());
            ps.setString(4, nomeAzienda);

            ps.executeUpdate();
            System.out.println("[DB] Inserito veicolo: " + v.getCodice() + " per " + nomeAzienda);

        } catch (SQLException e) {
            System.err.println("Errore inserimento veicolo " + v.getCodice() + ": " + e.getMessage());
        }
    }

    // =================================================================================
    // SEZIONE 3: COLLI
    // =================================================================================

    /**
     * Recupera la lista di tutti i colli in stato 'IN_PREPARAZIONE'.
     */
    public List<ICollo> getColliInPreparazione() {
        List<ICollo> lista = new ArrayList<>();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_COLLI_PREPARAZIONE)) {

            while (rs.next()) {
                // Creiamo il Proxy passando 'this' come IDataLoader
                lista.add(new ColloProxy(
                        rs.getString("codice"),
                        rs.getString("stato"),
                        this
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColliInPreparazione", e);
        }
        return lista;
    }

    public ICollo getColloProxy(String codice) {
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_COLLO_BASE)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    // Creiamo il Proxy passando 'this' come IDataLoader
                    return new ColloProxy(
                            rs.getString("codice"),
                            rs.getString("stato"),
                            this
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColloProxy. Ricerca collo " + codice, e);
        }
        return null;
    }

    /**
     * Implementazione di IDataLoader.
     * Recupera l'oggetto Reale completo (Dati collo + Storico).
     * Chiamato dal Proxy quando serve caricare i dettagli.
     */
    @Override
    public ColloReale getColloRealeCompleto(String codice) {
        ColloReale reale = null;

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(SELECT_COLLO_FULL)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    reale = new ColloReale();
                    reale.setCodice(rs.getString("codice"));
                    reale.setStato(rs.getString("stato"));
                    reale.setPeso(rs.getDouble("peso"));
                    reale.setMittente(rs.getString("mittente"));
                    reale.setDestinatario(rs.getString("destinatario"));

                    reale.setStorico(getStoricoPerCollo(codice));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB in getColloRealeCompleto per " + codice, e);
        }
        return reale;
    }

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
    // SEZIONE 4: AGGIORNAMENTI
    // =================================================================================

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
}