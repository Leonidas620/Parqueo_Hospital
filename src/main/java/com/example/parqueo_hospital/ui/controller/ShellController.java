package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Navigator;
import com.example.parqueo_hospital.ui.Screen;
import com.example.parqueo_hospital.ui.Session;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

/** Ventana principal: menú de navegación según el rol y área central intercambiable. */
public class ShellController {
    @FXML private BorderPane root;
    @FXML private Label lblTitle;
    @FXML private Label lblUser;
    @FXML private MenuItem miPresent;
    @FXML private MenuItem miServices;
    @FXML private MenuItem miShifts;
    @FXML private MenuItem miVehicles;
    @FXML private MenuItem miSubscriptions;
    @FXML private MenuItem miRates;
    @FXML private MenuItem miCapacity;
    @FXML private MenuItem miAccounts;

    @FXML
    private void initialize() {
        Navigator.registerShell(this);
        boolean admin = Session.isAdmin();
        // Cada rol ve solo lo que puede usar.
        miPresent.setVisible(!admin);
        miServices.setVisible(admin);
        miShifts.setVisible(admin);
        miVehicles.setVisible(admin);
        miSubscriptions.setVisible(admin);
        miRates.setVisible(admin);
        miCapacity.setVisible(admin);
        miAccounts.setVisible(admin);
        lblUser.setText(Session.account().parkerName() + (admin ? " · Administrador" : " · Parqueador"));
    }

    public void setContent(Node content, Screen screen) {
        root.setCenter(content);
        lblTitle.setText(screen.title());
    }

    @FXML private void showPresent() { Dialogs.guard(() -> Navigator.show(Screen.PRESENT)); }
    @FXML private void showServices() { Dialogs.guard(() -> Navigator.show(Screen.SERVICES)); }
    @FXML private void showShifts() { Dialogs.guard(() -> Navigator.show(Screen.SHIFTS)); }
    @FXML private void showVehicles() { Dialogs.guard(() -> Navigator.show(Screen.VEHICLES)); }
    @FXML private void showSubscriptions() { Dialogs.guard(() -> Navigator.show(Screen.SUBSCRIPTIONS)); }
    @FXML private void showRates() { Dialogs.guard(() -> Navigator.show(Screen.RATES)); }
    @FXML private void showCapacity() { Dialogs.guard(() -> Navigator.show(Screen.CAPACITY)); }
    @FXML private void showAccounts() { Dialogs.guard(() -> Navigator.show(Screen.ACCOUNTS)); }
    @FXML private void showMyAccount() { Dialogs.guard(() -> Navigator.show(Screen.MY_ACCOUNT)); }

    /**
     * El parqueador solo puede cerrar sesión cuadrando su turno; el administrador recibe una
     * advertencia si todavía no cuadró el día.
     */
    @FXML
    private void logout() {
        if (Session.isAdmin()) {
            boolean dayClosed;
            try {
                dayClosed = Session.parking().isTodayClosed();
            } catch (ParkingException e) {
                Dialogs.warn(e.getMessage());
                return;
            }
            if (!dayClosed && !Dialogs.confirm("Cerrar sesión",
                    "¿Está seguro de que quiere cerrar su sesión sin cuadrar el día?", "Cerrar sesión", "Cancelar")) {
                return;
            }
        } else if (Session.shiftOpen()) {
            Dialogs.warn("Para cerrar sesión debe primero cuadrar su turno.");
            return;
        }
        Navigator.logout();
    }
}
