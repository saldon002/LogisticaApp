package it.prog3.logisticaapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestione Singleton della connessione al Database (Versione Eager Initialization).
 * <p>
 * L'istanza viene creata immediatamente al caricamento della classe, garantendo
 * la thread-safety senza bisogno di sincronizzazione esplicita.
 * </p>
 */
public class ConnessioneDB {

    // --- EAGER INITIALIZATION ---
    // L'istanza viene creata SUBITO (static final), non appena la classe viene caricata in memoria.
    // La JVM garantisce che questa riga venga eseguita una volta sola e in modo sicuro.
    private static final ConnessioneDB INSTANCE = new ConnessioneDB();

    // Stringa di connessione (assicurati che il file logistica.db sia nella cartella del progetto)
    private final String url = "jdbc:sqlite:logistica.db";

    /**
     * Costruttore PRIVATO.
     * Impedisce a chiunque altro di scrivere 'new ConnessioneDB()'.
     * Viene eseguito una sola volta quando viene inizializzata la variabile INSTANCE.
     */
    private ConnessioneDB() {
        try {
            // Carichiamo il driver JDBC per SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("[DB] Errore critico: Driver JDBC SQLite non trovato!");
        }
    }

    /**
     * Metodo di accesso globale.
     * Restituisce l'istanza già pronta. Molto veloce perché non fa controlli if.
     *
     * @return L'unica istanza di ConnessioneDB.
     */
    public static ConnessioneDB getInstance() {
        return INSTANCE;
    }

    /**
     * Fornisce una connessione attiva al database.
     *
     * @return Un oggetto Connection nuovo (da chiudere dopo l'uso).
     * @throws SQLException In caso di errori di collegamento.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}