module it.prog3.logisticaapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens it.prog3.logisticaapp to javafx.fxml;
    exports it.prog3.logisticaapp;
}