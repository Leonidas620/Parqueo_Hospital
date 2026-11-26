package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Capacity;
import com.example.parqueo_hospital.model.Shift;
import com.example.parqueo_hospital.model.ShiftSummary;
import com.example.parqueo_hospital.service.ParkingService;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Formats;
import com.example.parqueo_hospital.ui.Navigator;
import com.example.parqueo_hospital.ui.Screen;
import com.example.parqueo_hospital.ui.Session;
import com.example.parqueo_hospital.ui.component.ParkingDiagram;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

/** Pantalla principal del parqueador: lista enlazada dibujada, ocupación y acciones del turno. */
public class PresentController {
    @FXML private ProgressBar barCars;
    @FXML private ProgressBar barMotos;
    @FXML private Label lblCars;
    @FXML private Label lblMotos;
    @FXML private TextField txtSearch;
    @FXML private Label lblConsult;
    @FXML private ParkingDiagram diagram;
    @FXML private Button btnEntry;
    @FXML private Button btnExit;
    @FXML private Button btnOver24;
    @FXML private Button btnCloseShift;

    private final ParkingService parking = Session.parking();

    @FXML
    private void initialize() {
        diagram.setSource(parking::present);
        diagram.setOnRedraw(this::refreshOccupancy);
        txtSearch.textProperty().addListener((obs, old, text) -> diagram.setFilter(text));

        if (!Session.shiftOpen()) {
            // Turno ya cuadrado: solo se pueden consultar los vehículos con más de 24 horas.
            btnEntry.setDisable(true);
            btnExit.setDisable(true);
            btnCloseShift.setDisable(true);
            btnOver24.setDisable(true);
            lblConsult.setVisible(true);
            lblConsult.setManaged(true);
            diagram.setShowOver24(true);
        }
        Platform.runLater(diagram::redraw);
    }

    @FXML
    private void registerEntry() {
        Dialogs.showForm("Registrar Entrada", "EntryDialog.fxml", diagram::redraw);
    }

    @FXML
    private void registerExit() {
        Dialogs.showForm("Registrar Salida", "ExitDialog.fxml", diagram::redraw);
    }

    @FXML
    private void toggleOver24() {
        boolean show = !diagram.isShowOver24();
        diagram.setShowOver24(show);
        btnOver24.setText(show ? "Mostrar Todos los Vehículos" : "Mostrar Vehículos +24h");
    }

    @FXML
    private void closeShift() {
        if (!Dialogs.confirm("Cuadrar turno",
                "Se cerrará su turno y ya no podrá registrar entradas ni salidas.", "Cuadrar turno", "Cancelar")) {
            return;
        }
        Dialogs.guard(() -> {
            ShiftSummary summary = parking.closeShift(Session.shift());
            Shift shift = summary.shift();
            Session.setShift(shift);
            String text = "Servicios realizados: " + summary.services()
                    + "\nHorario trabajado: " + Formats.time(shift.startedAt()) + " a " + Formats.time(shift.endedAt())
                    + "\nTotal recaudado: " + Formats.money(shift.total());
            boolean leave = Dialogs.confirm("Turno cuadrado", text + "\n\n¿Desea cerrar sesión ahora?",
                    "Cerrar sesión", "Seguir en la aplicación");
            if (leave) {
                Navigator.logout();
            } else {
                Navigator.show(Screen.PRESENT);
            }
        });
    }

    private void refreshOccupancy() {
        Capacity capacity = parking.capacity();
        update(barCars, lblCars, "Autos", parking.currentCarOccupancy(), capacity.cars());
        update(barMotos, lblMotos, "Motos", parking.currentMotorcycleOccupancy(), capacity.motorcycles());
    }

    private void update(ProgressBar bar, Label label, String name, int used, int capacity) {
        bar.setProgress(capacity == 0 ? 1 : Math.min(1.0, (double) used / capacity));
        label.setText(name + ": " + used + " de " + capacity + " ocupados");
    }
}
