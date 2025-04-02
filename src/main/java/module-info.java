module com.example.personaltrial {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;
    requires java.management;

    opens com.example.personaltrial to javafx.fxml;
    exports com.example.personaltrial;
}