package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Rates;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class RatesController {
    @FXML private Spinner<Double> spnCar;
    @FXML private Spinner<Double> spnMoto;
    @FXML private Spinner<Double> spnNight;

    @FXML
    private void initialize() {
        Dialogs.guard(() -> {
            Rates rates = Session.parking().rates();
            configure(spnCar, rates.carHourly());
            configure(spnMoto, rates.motorcycleHourly());
            configure(spnNight, rates.nightHourly());
        });
    }

    @FXML
    private void save() {
        Dialogs.guard(() -> {
            spnCar.commitValue();
            spnMoto.commitValue();
            spnNight.commitValue();
            Session.parking().updateRates(spnCar.getValue(), spnMoto.getValue(), spnNight.getValue());
            Dialogs.info("Las tarifas se actualizaron correctamente.");
        });
    }

    private void configure(Spinner<Double> spinner, double value) {
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100000, value, 5));
    }
}
