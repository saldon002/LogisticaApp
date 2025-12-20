package it.prog3.logisticaapp.util;

import it.prog3.logisticaapp.model.ICollo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Observer {

    private static final String FILE_NAME = "audit_log.txt"; // Il file verrà creato nella cartella del progetto
    private ICollo colloOsservato;

    public FileLogger(ICollo collo) {
        this.colloOsservato = collo;

        // --- DEBUG: Stampa il percorso assoluto ---
        java.io.File f = new java.io.File(FILE_NAME);
        System.out.println("[DEBUG LOGGER] Sto scrivendo il file qui: " + f.getAbsolutePath());
    }

    @Override
    public void update() {
        // 1. Prepara il messaggio
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String messaggio = String.format("[%s] UPDATE: Il collo %s è passato allo stato: %s",
                time, colloOsservato.getCodice(), colloOsservato.getStato());

        // 2. Scrivi su file (Try-with-resources chiude automaticamente il file)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(messaggio);
            writer.newLine(); // Vai a capo
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del log su file: " + e.getMessage());
        }
    }
}