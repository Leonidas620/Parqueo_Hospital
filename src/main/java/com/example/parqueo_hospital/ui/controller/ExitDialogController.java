package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.ExitSummary;
import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.structures.LinkedVehicleList;
import com.example.parqueo_hospital.structures.VehicleNode;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Formats;
import com.example.parqueo_hospital.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import java.time.Duration;

/** La chapa se elige de la lista de vehículos presentes, por lo que no puede escribirse una inexistente. */
public class ExitDialogController extends DialogController {
    @FXML private ComboBox<String> cmbPlate;

    @FXML
    private void initialize() {
        LinkedVehicleList present = Session.parking().present();
        for (VehicleNode node = present.head(); node != null; node = node.next()) {
            cmbPlate.getItems().add(node.service().vehicle().plate());
        }
    }

    @FXML
    private void confirm() {
        String plate = cmbPlate.getValue();
        if (plate == null) {
            Dialogs.warn("Seleccione la chapa del vehículo que sale.");
            return;
        }
        if (!Dialogs.confirm("Registrar salida", "¿Registrar la salida del vehículo " + plate + "?",
                "Registrar salida", "Cancelar")) {
            return;
        }
        Dialogs.guard(() -> {
            ExitSummary result = Session.parking().exit(plate, Session.shift());
            succeed(describe(result));
        });
    }

    private String describe(ExitSummary result) {
        ParkingServiceRecord service = result.service();
        Duration stay = Duration.between(service.entryAt(), service.exitAt());
        StringBuilder text = new StringBuilder("Servicio " + service.id() + " registrado.\n")
                .append("Tiempo en el parqueo: ").append(stay.toHours()).append(" h ")
                .append(stay.toMinutesPart()).append(" min\n");
        if (result.charge().totalHours() == 0) {
            text.append("Sin cobro: el vehículo tiene el abono vigente.\n");
        } else {
            text.append("Horas cobradas: ").append(result.charge().dayHours()).append(" diurnas y ")
                    .append(result.charge().nightHours()).append(" nocturnas\n");
        }
        text.append("Tarifa por hora: ").append(Formats.money(service.hourlyRate())).append('\n')
                .append("Importe: ").append(Formats.money(service.amount()));
        if (result.receipt() == null) {
            text.append("\n\nNo se pudo guardar el archivo del recibo.");
        }
        return text.toString();
    }
}
