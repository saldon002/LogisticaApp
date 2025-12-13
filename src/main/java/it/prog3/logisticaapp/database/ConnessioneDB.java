package it.prog3.logisticaapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestione Singleton della connessione al Database (Versione Eager Initialization).
 * <p>
 * L'istanza viene creata al caricamento della classe.
 * Garantisce thread-safety delegando la gestione alla JVM.
 * </p>
 */
public class ConnessioneDB {

    // EAGER INITIALIZATION: Istanza creata subito. Thread-safe.
    private static final ConnessioneDB INSTANCE = new ConnessioneDB();

    // Stringa di connessione JDBC per SQLite
    private final String url = "jdbc:sqlite:logistica.db";

    /**
     * Costruttore PRIVATO.
     * Carica il driver JDBC. Se fallisce, blocca l'applicazione.
     */
    private ConnessioneDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("[ConnessioneDB] Driver SQLite caricato correttamente.");
        } catch (ClassNotFoundException e) {
            // "Fail Fast": Se manca il driver, è inutile proseguire.
            // Lanciamo una RuntimeException per fermare l'avvio del programma.
            throw new RuntimeException("ERRORE FATALE: Driver JDBC SQLite non trovato! Controlla le librerie.", e);
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
     * Chi chiama questo metodo è responsabile di chiudere la connessione con .close().
     *
     * @return Connection attiva.
     * @throws SQLException Se il file db non si trova o è bloccato.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}