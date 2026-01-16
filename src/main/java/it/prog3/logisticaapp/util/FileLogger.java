package it.prog3.logisticaapp.util;

import it.prog3.logisticaapp.model.ICollo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe test Observer.
 */
public class FileLogger implements Observer {

    private static final String FILE_NAME = "observer_log.txt";
    private ICollo colloOsservato;

    public FileLogger(ICollo collo) {
        this.colloOsservato = collo;

        // --- DEBUG: Stampa il percorso del file ---
        java.io.File f = new java.io.File(FILE_NAME);
        System.out.println("[DEBUG LOGGER] Sto scrivendo il file qui: " + f.getAbsolutePath());
    }

    @Override
    public void update() {
        // 1. Prepara il messaggio
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String messaggio = String.format("[%s] UPDATE: Il collo %s è passato allo stato: %s",
                time, colloOsservato.getCodice(), colloOsservato.getStato());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(messaggio);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del log su file: " + e.getMessage());
        }
    }
}