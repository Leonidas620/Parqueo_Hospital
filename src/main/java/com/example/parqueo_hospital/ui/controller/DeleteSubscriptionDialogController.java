package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DeleteSubscriptionDialogController extends DialogController {
    @FXML private TextField txtCode;

    @FXML
    private void initialize() {
        Platform.runLater(txtCode::requestFocus);
    }

    /** Si había un abono seleccionado en la tabla, su código llega ya escrito. */
    public void setCode(long code) {
        txtCode.setText(String.valueOf(code));
    }

    @FXML
    private void confirm() {
        Dialogs.guard(() -> {
            long code;
            try {
                code = Long.parseLong(txtCode.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Escriba el código del abono (solo números).");
            }
            Session.parking().deleteSubscription(code);
            succeed("Abono eliminado.");
        });
    }
}
