package it.prog3.logisticaapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestione Singleton della connessione al Database.
 * <p>
 * L'istanza viene creata al caricamento della classe (Thread-safe).
 * </p>
 */
public class ConnessioneDB {

    // EAGER INITIALIZATION
    private static final ConnessioneDB INSTANCE = new ConnessioneDB();

    // Stringa di connessione JDBC per SQLite
    private static final String URL = "jdbc:sqlite:logistica.db";

    /**
     * Costruttore PRIVATO (Singleton).
     * Carica il driver JDBC. Se fallisce, blocca l'applicazione.
     */
    private ConnessioneDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("[ConnessioneDB] Driver SQLite caricato correttamente.");
        } catch (ClassNotFoundException e) {
            // Lanciamo una RuntimeException per fermare l'avvio del programma.
            throw new RuntimeException("ERRORE: Driver JDBC SQLite non trovato!", e);
        }
    }

    /**
     * Restituisce l'istanza Singleton.
     */
    public static ConnessioneDB getInstance() {
        return INSTANCE;
    }

    /**
     * Crea e restituisce una nuova connessione al DB.
     *
     * @return Connection attiva.
     * @throws SQLException Se il file db non si trova o è bloccato.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}