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
                    "destinatario TEXT" +
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

            // ==========================================
            // 3. INSERIMENTO DATI DI PROVA (MOCK DATA)
            // ==========================================
            // Nota: Qui usiamo Statement semplice perché i dati sono costanti e sicuri.
            // In un'app reale con input utente useremmo sempre PreparedStatement.

            // A. INSERIMENTO FLOTTA
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V01', 'CAMION',  5, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V02', 'FURGONE', 2, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V03', 'CAMION',  5, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V04', 'FURGONE', 2, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V05', 'CAMION',  5, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V06', 'CAMION',  5, 'BARTOLINI')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V07', 'FURGONE', 2, 'BARTOLINI')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V08', 'CAMION',  5, 'BARTOLINI')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V09', 'FURGONE', 2, 'BARTOLINI')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V10', 'CAMION',  5, 'BARTOLINI')");
            System.out.println("-> Dati veicoli inseriti.");

            // B. INSERIMENTO COLLI
            for (int i = 1; i <= 25; i++) {
                String codice = String.format("C%02d", i); // Genera C_01, C_02...

                String sql = String.format("INSERT INTO colli VALUES ('%s', 1.0, 'IN_PREPARAZIONE', 'X', 'X')", codice);
                stmt.executeUpdate(sql);
            }
            System.out.println("-> Dati colli inseriti.");

            System.out.println("=== SETUP COMPLETATO CON SUCCESSO ===");

        } catch (SQLException e) {
            System.err.println("ERRORE DURANTE IL SETUP DEL DB:");
            e.printStackTrace();
        }
    }
}