package it.prog3.logisticaapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Interfaccia Segregata (ISP) - Sola Lettura.
 * Definisce il contratto per l'accesso ai dati del collo senza permettere modifiche.
 * Estende Serializable per compatibilità JDBC.
 */
public interface IColloDati extends Serializable {
    String getCodice();
    String getStato();
    double getPeso();
    String getMittente();
    String getDestinatario();
    List<String> getStorico();
}