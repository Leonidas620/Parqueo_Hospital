package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Account;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Navigator;
import com.example.parqueo_hospital.ui.Screen;
import com.example.parqueo_hospital.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class MyAccountController {
    @FXML private Label lblUser;
    @FXML private PasswordField txtCurrent;
    @FXML private PasswordField txtNew;
    @FXML private PasswordField txtConfirm;

    @FXML
    private void initialize() {
        lblUser.setText("Usuario: " + Session.account().username());
    }

    @FXML
    private void changePassword() {
        Dialogs.guard(() -> {
            if (!txtNew.getText().equals(txtConfirm.getText())) {
                throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña.");
            }
            Account updated = Session.parking().changePassword(Session.account(), txtCurrent.getText(), txtNew.getText());
            Session.setAccount(updated);
            Dialogs.info("La contraseña se cambió correctamente.");
            Navigator.show(Screen.home());
        });
    }
}
