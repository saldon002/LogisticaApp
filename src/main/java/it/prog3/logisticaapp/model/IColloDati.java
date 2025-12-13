package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia segregata per i Dati (SRP/ISP).
 * Contiene solo i metodi relativi allo stato del collo, senza riferimenti agli Observer.
 */
public interface IColloDati extends Serializable {

    String getCodice();
    void setCodice(String codice);

    String getStato();

    /**
     * @throws SecurityException se l'utente non è autorizzato (nel Proxy).
     */
    void setStato(String stato);

    String getMittente();
    void setMittente(String mittente);

    String getDestinatario();
    void setDestinatario(String destinatario);

    double getPeso();

    /**
     * @throws IllegalArgumentException se il peso è negativo.
     */
    void setPeso(double peso);

    List<String> getStorico();
}