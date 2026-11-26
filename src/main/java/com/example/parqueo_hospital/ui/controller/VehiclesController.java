package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.Vehicle;
import com.example.parqueo_hospital.model.VehicleType;
import com.example.parqueo_hospital.ui.Columns;
import com.example.parqueo_hospital.ui.Dialogs;
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

public class VehiclesController {
    private static final String ALL_TYPES = "Todos los tipos";

    @FXML private TextField txtPlate;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtOwner;
    @FXML private TextField txtMake;
    @FXML private TableView<Vehicle> table;
    @FXML private TableColumn<Vehicle, String> colPlate;
    @FXML private TableColumn<Vehicle, String> colType;
    @FXML private TableColumn<Vehicle, String> colColor;
    @FXML private TableColumn<Vehicle, String> colMake;
    @FXML private TableColumn<Vehicle, String> colOwner;
    @FXML private TableColumn<Vehicle, String> colPhone;

    private final ObservableList<Vehicle> data = FXCollections.observableArrayList();
    private final FilteredList<Vehicle> filtered = new FilteredList<>(data, row -> true);

    @FXML
    private void initialize() {
        Columns.text(colPlate, Vehicle::plate);
        Columns.text(colType, v -> v.type().toString());
        Columns.text(colColor, Vehicle::color);
        Columns.text(colMake, Vehicle::make);
        Columns.text(colOwner, Vehicle::owner);
        Columns.text(colPhone, Vehicle::phone);
        cmbType.getItems().add(ALL_TYPES);
        for (VehicleType type : VehicleType.values()) cmbType.getItems().add(type.toString());
        cmbType.setValue(ALL_TYPES);
        table.setPlaceholder(new Label("No hay vehículos registrados"));
        table.setItems(filtered);
        txtPlate.textProperty().addListener((obs, old, text) -> applyFilter());
        txtOwner.textProperty().addListener((obs, old, text) -> applyFilter());
        txtMake.textProperty().addListener((obs, old, text) -> applyFilter());
        cmbType.valueProperty().addListener((obs, old, value) -> applyFilter());
        Dialogs.guard(() -> data.setAll(Session.parking().vehicles("")));
    }

    private void applyFilter() {
        String plate = txtPlate.getText().trim().toLowerCase(Locale.ROOT);
        String owner = txtOwner.getText().trim().toLowerCase(Locale.ROOT);
        String make = txtMake.getText().trim().toLowerCase(Locale.ROOT);
        String type = cmbType.getValue();
        filtered.setPredicate(v -> v.plate().toLowerCase(Locale.ROOT).contains(plate)
                && v.owner().toLowerCase(Locale.ROOT).contains(owner)
                && v.make().toLowerCase(Locale.ROOT).contains(make)
                && (type == null || ALL_TYPES.equals(type) || v.type().toString().equals(type)));
    }
}
