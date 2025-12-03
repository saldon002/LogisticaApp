package it.prog3.logisticaapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnessioneDB {
    // Istanza statica del Sigleton
    private static ConnessioneDB instance;

    // URL del database
    private final String url = "jdbc:sqlite:logistica.db";

    public ConnessioneDB() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Metodo per ottenere l'istanza del Singleton
    public static ConnessioneDB getInstance() {
        if (instance == null) {
            instance = new ConnessioneDB();
        }
        return instance;
    }

    // Metodo che restituisce una nuova connessione
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}