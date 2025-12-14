module it.prog3.logisticaapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    exports it.prog3.logisticaapp;

    opens it.prog3.logisticaapp.controller to javafx.fxml;
    //opens it.prog3.logisticaapp.model to javafx.base;

}