package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Subscription;
import com.example.parqueo_hospital.ui.Columns;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Formats;
import com.example.parqueo_hospital.ui.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Locale;

public class SubscriptionsController {
    private static final String ALL_MONTHS = "Todos los meses";

    @FXML private TextField txtPlate;
    @FXML private ComboBox<String> cmbMonth;
    @FXML private TextField txtYear;
    @FXML private TableView<Subscription> table;
    @FXML private TableColumn<Subscription, String> colId;
    @FXML private TableColumn<Subscription, String> colPlate;
    @FXML private TableColumn<Subscription, String> colMonth;
    @FXML private TableColumn<Subscription, String> colYear;
    @FXML private TableColumn<Subscription, String> colAmount;

    private final ObservableList<Subscription> data = FXCollections.observableArrayList();
    private final FilteredList<Subscription> filtered = new FilteredList<>(data, row -> true);

    @FXML
    private void initialize() {
        Columns.text(colId, s -> String.valueOf(s.id()));
        Columns.text(colPlate, Subscription::plate);
        Columns.text(colMonth, Subscription::monthName);
        Columns.text(colYear, s -> String.valueOf(s.year()));
        Columns.text(colAmount, s -> Formats.money(s.amount()));
        cmbMonth.getItems().add(ALL_MONTHS);
        for (int month = 1; month <= 12; month++) cmbMonth.getItems().add(monthName(month));
        cmbMonth.setValue(ALL_MONTHS);
        table.setPlaceholder(new Label("No hay abonos vigentes"));
        table.setItems(filtered);
        txtPlate.textProperty().addListener((obs, old, text) -> applyFilter());
        txtYear.textProperty().addListener((obs, old, text) -> applyFilter());
        cmbMonth.valueProperty().addListener((obs, old, value) -> applyFilter());
        reload();
    }

    @FXML
    private void addSubscription() {
        Dialogs.showForm("Agregar Abono", "SubscriptionDialog.fxml", this::reload);
    }

    @FXML
    private void deleteSubscription() {
        Subscription selected = table.getSelectionModel().getSelectedItem();
        Dialogs.showForm("Eliminar Abono", "DeleteSubscriptionDialog.fxml",
                (DeleteSubscriptionDialogController controller) -> {
                    if (selected != null) controller.setCode(selected.id());
                }, this::reload);
    }

    private void reload() {
        Dialogs.guard(() -> data.setAll(Session.parking().subscriptions("")));
    }

    private void applyFilter() {
        String plate = txtPlate.getText().trim().toLowerCase(Locale.ROOT);
        String year = txtYear.getText().trim();
        String month = cmbMonth.getValue();
        filtered.setPredicate(s -> s.plate().toLowerCase(Locale.ROOT).contains(plate)
                && String.valueOf(s.year()).contains(year)
                && (month == null || ALL_MONTHS.equals(month) || s.monthName().equalsIgnoreCase(month)));
    }

    static String monthName(int month) {
        String name = java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.FULL,
                Locale.forLanguageTag("es"));
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }
}
