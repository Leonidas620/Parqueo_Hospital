package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Account;
import com.example.parqueo_hospital.ui.Columns;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AccountsController {
    @FXML private TableView<Account> table;
    @FXML private TableColumn<Account, String> colName;
    @FXML private TableColumn<Account, String> colUser;

    private final ObservableList<Account> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        Columns.text(colName, Account::parkerName);
        Columns.text(colUser, Account::username);
        table.setPlaceholder(new Label("No hay otras cuentas registradas"));
        table.setItems(data);
        reload();
    }

    @FXML
    private void createAccount() {
        Dialogs.showForm("Crear Cuenta", "AccountDialog.fxml", this::reload);
    }

    @FXML
    private void deleteAccount() {
        Account selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warn("Seleccione en la tabla la cuenta que desea eliminar.");
            return;
        }
        if (!Dialogs.confirm("Eliminar cuenta", "¿Eliminar la cuenta de " + selected.parkerName()
                + " (usuario " + selected.username() + ")?", "Eliminar", "Cancelar")) {
            return;
        }
        Dialogs.guard(() -> {
            Session.parking().deleteAccount(selected);
            reload();
        });
    }

    /** La cuenta con la sesión iniciada no aparece en la tabla, así que no puede eliminarse a sí misma. */
    private void reload() {
        Dialogs.guard(() -> {
            long ownId = Session.account().id();
            data.setAll(Session.parking().accounts().stream().filter(a -> a.id() != ownId).toList());
        });
    }
}
