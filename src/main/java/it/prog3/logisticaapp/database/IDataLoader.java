package it.prog3.logisticaapp.database;

import it.prog3.logisticaapp.model.ColloReale;

public interface IDataLoader {
    ColloReale getColloRealeCompleto(String codice);
}
