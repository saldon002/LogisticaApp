package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per un'entità di tipo "Collo"
 * <p>
 *     Estende {@link Serializable} per conformità alle specifiche JavaBean/JDBC.
 *     Questa interfaccia è utilizzata sia dall'oggetto reale {@link ColloReale}
 *     che dal proxy {@link ColloProxy}
 * </p>
 */
public interface ICollo extends Serializable {
    /**
     * Restituisce il codice univoco del collo
     * @return Stringa rappresentante il codice
     */
    String getCodice();
    void setCodice(String codice);

    /**
     * Restituisce lo stato attuale del collo
     * @return Stringa rappresentante lo stato
     */
    String getStato();

    /**
     * Imposta il nuovo stato del collo
     * <p>
     *     Nel caso del Proxy, questo metodo implementeà anche controlli di sicurezza
     *     Nel caso del Reale, notificherà gli observers.
     * </p>
     * @param stato Il nuovo stato da assegnare.
     */
    void setStato(String stato);

    String getMittente();
    void setMittente(String mittente);

    String getDestinatario();
    void setDestinatario(String destinatario);

    double getPeso();
    void setPeso(double peso);

    /**
     * Recupera lo storico degli spostamenti del collo.
     * @return Lista di stringhe descrittive dello storico
     */
    List<String> getStorico();
}
