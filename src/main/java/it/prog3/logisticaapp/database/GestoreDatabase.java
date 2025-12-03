package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce tutte le operazioni CRUD sul database SQLite.
 * <p>
 * Questa classe agisce come punto unico di accesso ai dati.
 * Rispetta le specifiche JDBC:
 * 1. Usa {@link PreparedStatement} per sicurezza e performance.
 * 2. Apre e chiude la connessione {@link ConnessioneDB} per ogni singola operazione.
 * </p>
 */
public class GestoreDatabase {
    public GestoreDatabase() {}

    // =================================================================================
    // SEZIONE COLLI
    // =================================================================================

    /**
     * Recupera tutti i colli che sono attualmente "IN_PREPARAZIONE".
     *
     * @return Una lista di {@link ICollo}
     */
    public List<ICollo> getColliInPreparazione() {
        List<ICollo> lista = new ArrayList<>();
        String sql = "SELECT codice, stato FROM colli WHERE stato = 'IN_PREPARAZIONE'";

        // Try-with-resources: chiude automaticamente Connection, Statement e ResultSet [cite: 115]
        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                String codice = rs.getString("codice");
                String stato = rs.getString("stato");

                // Restituiamo un Proxy! È leggero e veloce da creare.
                // Caricherà il peso e il resto solo se l'algoritmo lo chiederà.
                lista.add(new ColloProxy(codice, stato));
            }

        } catch (SQLException e) {
            System.err.println("Errore nel recupero colli: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Cerca un collo specifico tramite codice.
     * Utilizzato dalla funzione "Ricerca" della GUI.
     *
     * @param codice Il codice del collo da cercare.
     * @return Un oggetto {@link ColloProxy} se trovato, altrimenti null.
     */
    public ICollo getCollo(String codice) {
        String sql = "SELECT codice, stato FROM colli WHERE codice = ?";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, codice); // Setta il parametro '?' [cite: 86]

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    // Trovato! Restituisco il Proxy
                    return new ColloProxy(rs.getString("codice"), rs.getString("stato"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Non trovato
    }

    /**
     * Metodo "pesante" chiamato SOLO dal ColloProxy per il Lazy Loading.
     * Recupera TUTTI i dati del collo (peso, mittente, storico, ecc.) e restituisce l'oggetto Reale.
     *
     * @param codice Il codice del collo.
     * @return Un'istanza completa di {@link ColloReale}.
     */
    public ColloReale getColloRealeCompleto(String codice) {
        String sql = "SELECT * FROM colli WHERE codice = ?";
        ColloReale reale = null;

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, codice);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    // Creiamo l'oggetto reale (JavaBean)
                    reale = new ColloReale();
                    reale.setCodice(rs.getString("codice"));
                    reale.setStato(rs.getString("stato"));
                    reale.setPeso(rs.getDouble("peso"));
                    reale.setMittente(rs.getString("mittente"));
                    reale.setDestinatario(rs.getString("destinatario"));

                    // Carichiamo anche lo storico (query separata o join)
                    reale.setStorico(getStoricoPerCollo(codice));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reale;
    }

    /**
     * Helper privato per caricare lo storico spostamenti.
     */
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
            e.printStackTrace();
        }
        return storico;
    }

    /**
     * Salva o aggiorna un collo nel database.
     * Usato per aggiornare lo stato dopo il caricamento.
     *
     * @param c Il collo da salvare (estrae i dati tramite interfaccia).
     */
    public void salvaCollo(ICollo c) {
        // Query di UPDATE (assumiamo che il collo esista già)
        String sql = "UPDATE colli SET stato = ? WHERE codice = ?";

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, c.getStato());
            st.setString(2, c.getCodice());

            int righeAggiornate = st.executeUpdate(); // Esegue l'update [cite: 83]
            if (righeAggiornate > 0) {
                System.out.println("Collo " + c.getCodice() + " aggiornato nel DB.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =================================================================================
    // SEZIONE VEICOLI (FACTORY)
    // =================================================================================

    /**
     * Recupera la flotta di veicoli per una specifica azienda.
     * Utilizza il Factory Method {@link AziendaLogistica} per creare gli oggetti corretti.
     *
     * @param nomeAzienda Il nome dell'azienda (es. "DHL").
     * @return Lista di veicoli.
     */
    public List<IVeicolo> getFlotta(String nomeAzienda) {
        List<IVeicolo> flotta = new ArrayList<>();
        // Query filtrata per azienda [cite: 124]
        String sql = "SELECT * FROM veicoli WHERE azienda = ?";

        // Istanziamo la Factory Concreta per creare i veicoli
        AziendaLogistica factory = new AziendaLogistica();

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, nomeAzienda);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");     // Es. "CAMION" o "FURGONE"
                    String codice = rs.getString("codice");
                    // Nota: la capienza la sa la classe concreta, ma se volessimo leggerla dal DB potremmo settarla

                    // USIAMO IL FACTORY METHOD!
                    // Passiamo il tipo (stringa) e la factory ci restituisce l'oggetto giusto (Camion o Furgone)
                    IVeicolo v = factory.createVeicolo(tipo, codice, nomeAzienda);

                    if (v != null) {
                        flotta.add(v);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flotta;
    }
}