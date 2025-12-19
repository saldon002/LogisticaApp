package it.prog3.logisticaapp.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Classe di utilità per la creazione e il popolamento iniziale del Database.
 */
public class DbSetup {

    public static void main(String[] args) {
        System.out.println("=== SETUP DATABASE ===");

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // ==========================================
            // 1. PULIZIA (DROP TABLES)
            // ==========================================
            stmt.executeUpdate("DROP TABLE IF EXISTS storico_spostamenti");
            stmt.executeUpdate("DROP TABLE IF EXISTS colli");
            stmt.executeUpdate("DROP TABLE IF EXISTS veicoli");
            System.out.println("-> Tabelle vecchie eliminate.");

            // ==========================================
            // 2. CREAZIONE SCHEMA (DDL)
            // ==========================================

            // Tabella VEICOLI
            String sqlVeicoli = "CREATE TABLE veicoli (" +
                    "codice TEXT PRIMARY KEY, " +
                    "tipo TEXT NOT NULL, " +
                    "capienza INTEGER NOT NULL, " +
                    "azienda TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(sqlVeicoli);
            System.out.println("-> Tabella 'veicoli' creata.");

            // Tabella COLLI
            String sqlColli = "CREATE TABLE colli (" +
                    "codice TEXT PRIMARY KEY, " +
                    "peso REAL, " +
                    "stato TEXT, " +
                    "mittente TEXT, " +
                    "destinatario TEXT," +
                    "veicolo_codice TEXT" +
                    ");";
            stmt.executeUpdate(sqlColli);
            System.out.println("-> Tabella 'colli' creata.");

            // Tabella STORICO (Relazione 1-a-N)
            // Usata per il tracciamento. Collegata ai colli tramite Foreign Key.
            String sqlStorico = "CREATE TABLE storico_spostamenti (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "collo_codice TEXT, " +
                    "descrizione TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(collo_codice) REFERENCES colli(codice)" +
                    ");";
            stmt.executeUpdate(sqlStorico);
            System.out.println("-> Tabella 'storico_spostamenti' creata.");

            System.out.println("=== SETUP COMPLETATO CON SUCCESSO ===");

        } catch (SQLException e) {
            System.err.println("ERRORE DURANTE IL SETUP DEL DB:");
            e.printStackTrace();
        }
    }
}