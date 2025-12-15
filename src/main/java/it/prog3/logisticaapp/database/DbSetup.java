package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.database.ConnessioneDB;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DbSetup {

    public static void main(String[] args) {
        System.out.println("=== SETUP DATABASE INIZIALE ===");

        try (Connection conn = ConnessioneDB.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. PULIZIA (Opzionale: cancella tutto se vuoi ripartire da zero)
            stmt.executeUpdate("DROP TABLE IF EXISTS storico_spostamenti");
            stmt.executeUpdate("DROP TABLE IF EXISTS colli");
            stmt.executeUpdate("DROP TABLE IF EXISTS veicoli");
            System.out.println("-> Tabelle vecchie eliminate.");

            // 2. CREAZIONE TABELLA VEICOLI
            // Nota: Le colonne devono chiamarsi ESATTAMENTE come nel GestoreDatabase
            String sqlVeicoli = "CREATE TABLE veicoli (" +
                    "codice TEXT PRIMARY KEY, " +
                    "tipo TEXT NOT NULL, " +
                    "capienza INTEGER NOT NULL, " +
                    "azienda TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(sqlVeicoli);
            System.out.println("-> Tabella 'veicoli' creata.");

            // 3. CREAZIONE TABELLA COLLI
            String sqlColli = "CREATE TABLE colli (" +
                    "codice TEXT PRIMARY KEY, " +
                    "peso REAL, " +
                    "stato TEXT, " +
                    "mittente TEXT, " +
                    "destinatario TEXT" +
                    ");";
            stmt.executeUpdate(sqlColli);
            System.out.println("-> Tabella 'colli' creata.");

            // 4. CREAZIONE TABELLA STORICO (Per il Lazy Loading)
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
            // INSERIMENTO DATI DI PROVA (MOCK DATA)
            // ==========================================

            // A. INSERIAMO LA FLOTTA (AZIENDA: DHL)
            // Un Camion grande (capienza 100) e un Furgone piccolo (capienza 20)
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V01', 'CAMION', 100, 'DHL')");
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V02', 'FURGONE', 20, 'DHL')");

            // Aggiungiamo un veicolo di un'altra azienda per verificare che NON venga caricato
            stmt.executeUpdate("INSERT INTO veicoli VALUES ('V03', 'CAMION', 100, 'BARTOLINI')");
            System.out.println("-> Dati veicoli inseriti.");

            // B. INSERIAMO I COLLI (STATO: IN_PREPARAZIONE)
            // Inseriamo 5 colli. Alcuni piccoli, alcuni grandi.
            stmt.executeUpdate("INSERT INTO colli VALUES ('C01', 5.5, 'IN_PREPARAZIONE', 'Mario Rossi', 'Luigi Verdi')");
            stmt.executeUpdate("INSERT INTO colli VALUES ('C02', 12.0, 'IN_PREPARAZIONE', 'Amazon', 'Cliente A')");
            stmt.executeUpdate("INSERT INTO colli VALUES ('C03', 2.0, 'IN_PREPARAZIONE', 'Ebay', 'Cliente B')");
            // Questo ha uno stato diverso, non dovrebbe essere caricato dall'algoritmo
            stmt.executeUpdate("INSERT INTO colli VALUES ('C_OLD', 1.0, 'CONSEGNATO', 'Vecchio', 'Cliente C')");

            // C. INSERIAMO STORICO FINTO
            stmt.executeUpdate("INSERT INTO storico_spostamenti (collo_codice, descrizione) VALUES ('C01', 'Creazione etichetta')");
            stmt.executeUpdate("INSERT INTO storico_spostamenti (collo_codice, descrizione) VALUES ('C01', 'Arrivo in magazzino centrale')");
            System.out.println("-> Dati colli inseriti.");

            System.out.println("=== SETUP COMPLETATO CON SUCCESSO ===");

        } catch (SQLException e) {
            System.err.println("ERRORE DURANTE IL SETUP DEL DB:");
            e.printStackTrace();
        }
    }
}
