package it.prog3.logisticaapp.model;

import java.util.List;

/**
 * Interfaccia Completa (Lettura + Scrittura).
 * Estende IColloDati aggiungendo i metodi mutatori (Setter).
 */
public interface ICollo extends IColloDati {

    // Codice: Di solito non si modifica la PK, ma lo lasciamo se serve per il caricamento
    void setCodice(String codice);

    /**
     * @throws SecurityException se l'utente non è autorizzato (gestito dal Proxy).
     */
    void setStato(String stato);

    /**
     * @throws IllegalArgumentException se il peso è negativo.
     */
    void setPeso(double peso);

    void setMittente(String mittente);
    void setDestinatario(String destinatario);

    // Gestione Storico
    void setStorico(List<String> storico);
    void aggiungiEventoStorico(String evento);
}