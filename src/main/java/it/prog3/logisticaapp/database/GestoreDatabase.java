package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce le operazioni CRUD.
 * <p>
 * NOTA SULLE ECCEZIONI:
 * In questa classe usiamo il pattern "Wrap and Rethrow".
 * Catturiamo la SQLException (checked) e la rilanciamo come RuntimeException (unchecked).
 * Questo permette al Controller di gestire l'errore, senza sporcare le interfacce con "throws SQLException".
 * </p>
 */
public class GestoreDatabase {

    public GestoreDatabase() {}

    // =================================================================================
    // SEZIONE COLLI
    // =================================================================================

    public List<ICollo> getColliInPreparazione() {
        List<ICollo> lista = new ArrayList<>();
        String sql = "SELECT codice, stato FROM colli WHERE stato = 'IN_PREPARAZIONE'";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                // Creiamo Proxy leggeri
                lista.add(new ColloProxy(rs.getString("codice"), rs.getString("stato")));
            }

        } catch (SQLException e) {
            // RILANCIA L'ERRORE! Non ingoiarlo.
            throw new RuntimeException("Errore DB in getColliInPreparazione", e);
        }
        return lista;
    }

    public ICollo getCollo(String codice) {
        String sql = "SELECT codice, stato FROM colli WHERE codice = ?";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new ColloProxy(rs.getString("codice"), rs.getString("stato"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB ricerca collo " + codice, e);
        }
        return null; // Qui va bene null se non trovato (non è un errore tecnico)
    }

    /**
     * Metodo usato dal PROXY per il caricamento ritardato.
     */
    public ColloReale getColloRealeCompleto(String codice) {
        String sql = "SELECT * FROM colli WHERE codice = ?";
        ColloReale reale = null;

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    reale = new ColloReale();
                    reale.setCodice(rs.getString("codice"));
                    reale.setStato(rs.getString("stato"));
                    reale.setPeso(rs.getDouble("peso"));
                    reale.setMittente(rs.getString("mittente"));
                    reale.setDestinatario(rs.getString("destinatario"));

                    // Caricamento storico
                    reale.setStorico(getStoricoPerCollo(codice));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore DB caricamento completo collo " + codice, e);
        }
        return reale;
    }

    private List<String> getStoricoPerCollo(String codiceCollo) {
        List<String> storico = new ArrayList<>();
        String sql = "SELECT descrizione, timestamp FROM storico_spostamenti WHERE collo_codice = ? ORDER BY timestamp DESC";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, codiceCollo);
            try (ResultSet rs = st.executeQuery()) {
                while(rs.next()) {
                    storico.add(rs.getString("timestamp") + " - " + rs.getString("descrizione"));
                }
            }
        } catch (SQLException e) {
            // Anche qui, se fallisce lo storico, è un problema
            throw new RuntimeException("Errore DB storico collo " + codiceCollo, e);
        }
        return storico;
    }

    public void salvaCollo(ICollo c) {
        String sql = "UPDATE colli SET stato = ? WHERE codice = ?";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, c.getStato());
            st.setString(2, c.getCodice());
            st.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore salvataggio collo " + c.getCodice(), e);
        }
    }

    // =================================================================================
    // SEZIONE VEICOLI
    // =================================================================================

    public List<IVeicolo> getFlotta(String nomeAzienda) {
        List<IVeicolo> flotta = new ArrayList<>();
        String sql = "SELECT * FROM veicoli WHERE azienda = ?";

        // Factory Method
        AziendaConcreta factory = new AziendaConcreta();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, nomeAzienda);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String codice = rs.getString("codice");
                    int capienza = rs.getInt("capienza");

                    // NOTA: createVeicolo in AziendaConcreta deve essere PUBLIC per funzionare qui
                    IVeicolo v = factory.createVeicolo(tipo, codice);

                    if (v != null) {
                        v.setCapienza(capienza); // Settiamo la capienza letta dal DB
                        flotta.add(v);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore caricamento flotta azienda " + nomeAzienda, e);
        }
        return flotta;
    }
}