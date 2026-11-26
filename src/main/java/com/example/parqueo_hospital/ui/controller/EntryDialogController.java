package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.VehicleType;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Locale;

public class EntryDialogController extends DialogController {
    @FXML private TextField txtPlate;
    @FXML private ComboBox<VehicleType> cmbType;
    @FXML private TextField txtColor;
    @FXML private TextField txtMake;
    @FXML private TextField txtOwner;
    @FXML private TextField txtPhone;

    @FXML
    private void initialize() {
        cmbType.getItems().setAll(VehicleType.values());
        cmbType.setValue(VehicleType.AUTO);
        // La chapa se escribe siempre en mayúsculas y con un máximo de 7 caracteres.
        txtPlate.setTextFormatter(new TextFormatter<String>(change -> {
            change.setText(change.getText().toUpperCase(Locale.ROOT));
            return change.getControlNewText().length() <= 7 ? change : null;
        }));
        Platform.runLater(txtPlate::requestFocus);
    }

    @FXML
    private void confirm() {
        Dialogs.guard(() -> {
            Session.parking().enter(txtPlate.getText(), cmbType.getValue(), txtColor.getText(),
                    txtMake.getText(), txtOwner.getText(), txtPhone.getText(), Session.shift());
            succeed("Vehículo agregado con éxito.");
        });
    }
}
