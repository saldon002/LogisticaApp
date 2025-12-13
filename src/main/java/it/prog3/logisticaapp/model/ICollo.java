package it.prog3.logisticaapp.model;

import it.prog3.logisticaapp.util.IObservable;

/**
 * Interfaccia Composita.
 * Un "Collo" nel nostro sistema è l'unione di:
 * 1. Un set di Dati (IColloDati)
 * 2. La capacità di essere osservato (IObservable)
 * * Questo approccio soddisfa ISP e SRP separando le definizioni.
 */
public interface ICollo extends IColloDati, IObservable {
    // Non serve aggiungere metodi, eredita tutto dai padri.
}