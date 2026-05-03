package it.prog3.logisticaapp.model;

import java.util.List;

/**
 * Estende IColloDati aggiungendo i metodi setter.
 */
public interface ICollo extends IColloDati {

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