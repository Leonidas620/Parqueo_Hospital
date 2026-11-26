module com.example.parqueo_hospital {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    exports com.example.parqueo_hospital.model;
    exports com.example.parqueo_hospital.structures;
    // javafx.graphics necesita acceso a la clase Application para lanzarla.
    exports com.example.parqueo_hospital.ui;

    // El FXMLLoader necesita acceso reflexivo para inyectar los campos @FXML y crear los componentes.
    opens com.example.parqueo_hospital.ui.controller to javafx.fxml;
    opens com.example.parqueo_hospital.ui.component to javafx.fxml;
}
