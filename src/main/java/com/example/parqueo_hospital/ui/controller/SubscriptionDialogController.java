package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.time.LocalDate;
import java.util.Locale;

/** Cualquier chapa válida puede abonarse: no necesita haber entrado antes al parqueo. */
public class SubscriptionDialogController extends DialogController {
    @FXML private TextField txtPlate;
    @FXML private ComboBox<String> cmbMonth;
    @FXML private Spinner<Integer> spnYear;
    @FXML private TextField txtAmount;

    @FXML
    private void initialize() {
        LocalDate today = LocalDate.now();
        for (int month = 1; month <= 12; month++) cmbMonth.getItems().add(SubscriptionsController.monthName(month));
        cmbMonth.getSelectionModel().select(today.getMonthValue() - 1);
        spnYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                today.getYear(), today.getYear() + 5, today.getYear()));
        txtPlate.setTextFormatter(new TextFormatter<String>(change -> {
            change.setText(change.getText().toUpperCase(Locale.ROOT));
            return change.getControlNewText().length() <= 7 ? change : null;
        }));
        Platform.runLater(txtPlate::requestFocus);
    }

    @FXML
    private void confirm() {
        Dialogs.guard(() -> {
            spnYear.commitValue();
            double amount;
            try {
                amount = Double.parseDouble(txtAmount.getText().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El importe debe ser un número (por ejemplo 500 o 500.50).");
            }
            if (amount < 0) throw new IllegalArgumentException("El importe no puede ser negativo.");
            Session.parking().createSubscription(txtPlate.getText(), cmbMonth.getSelectionModel().getSelectedIndex() + 1,
                    spnYear.getValue(), amount);
            succeed("Abono registrado con éxito.");
        });
    }
}
