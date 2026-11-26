package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Role;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AccountDialogController extends DialogController {
    private static final String DEFAULT_PASSWORD = "1234";

    @FXML private TextField txtName;
    @FXML private TextField txtUser;
    @FXML private ComboBox<Role> cmbRole;

    @FXML
    private void initialize() {
        cmbRole.getItems().setAll(Role.PARKER, Role.ADMIN);
        cmbRole.setValue(Role.PARKER);
        Platform.runLater(txtName::requestFocus);
    }

    @FXML
    private void confirm() {
        Dialogs.guard(() -> {
            Session.parking().createAccount(txtName.getText(), txtUser.getText(), DEFAULT_PASSWORD, cmbRole.getValue());
            succeed("Cuenta creada. La contraseña inicial es " + DEFAULT_PASSWORD + ".");
        });
    }
}
