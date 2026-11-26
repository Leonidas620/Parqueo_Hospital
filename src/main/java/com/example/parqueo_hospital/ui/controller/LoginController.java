package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Account;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Navigator;
import com.example.parqueo_hospital.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;

    @FXML
    private void initialize() {
        Platform.runLater(txtUser::requestFocus);
    }

    @FXML
    private void login() {
        Dialogs.guard(() -> {
            String user = txtUser.getText().trim();
            if (user.isEmpty() || txtPassword.getText().isEmpty()) {
                throw new IllegalArgumentException("Escriba su usuario y su contraseña.");
            }
            Account account = Session.parking().login(user, txtPassword.getText().toCharArray())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos."));
            Session.setAccount(account);
            // El administrador no abre turno; el parqueador abre uno (o retoma el que dejó abierto).
            Session.setShift(Session.isAdmin() ? null : Session.parking().openShift(account));
            Navigator.showShell();
        });
    }
}
