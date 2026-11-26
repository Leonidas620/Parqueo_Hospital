package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Capacity;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class CapacityController {
    @FXML private Spinner<Integer> spnCars;
    @FXML private Spinner<Integer> spnMotos;

    @FXML
    private void initialize() {
        Capacity capacity = Session.parking().capacity();
        configure(spnCars, capacity.cars());
        configure(spnMotos, capacity.motorcycles());
    }

    @FXML
    private void save() {
        Dialogs.guard(() -> {
            spnCars.commitValue();
            spnMotos.commitValue();
            Session.parking().updateCapacity(spnCars.getValue(), spnMotos.getValue());
            Dialogs.info("La capacidad se actualizó correctamente.");
        });
    }

    private void configure(Spinner<Integer> spinner, int value) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, value, 1));
    }
}
