package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.ui.Columns;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Formats;
import com.example.parqueo_hospital.ui.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Locale;

public class ServicesController {
    @FXML private TextField txtPlate;
    @FXML private TextField txtParker;
    @FXML private TableView<ParkingServiceRecord> table;
    @FXML private TableColumn<ParkingServiceRecord, String> colId;
    @FXML private TableColumn<ParkingServiceRecord, String> colPlate;
    @FXML private TableColumn<ParkingServiceRecord, String> colEntryDate;
    @FXML private TableColumn<ParkingServiceRecord, String> colEntryTime;
    @FXML private TableColumn<ParkingServiceRecord, String> colExitDate;
    @FXML private TableColumn<ParkingServiceRecord, String> colExitTime;
    @FXML private TableColumn<ParkingServiceRecord, String> colRate;
    @FXML private TableColumn<ParkingServiceRecord, String> colAmount;
    @FXML private TableColumn<ParkingServiceRecord, String> colParker;

    private final ObservableList<ParkingServiceRecord> data = FXCollections.observableArrayList();
    private final FilteredList<ParkingServiceRecord> filtered = new FilteredList<>(data, row -> true);

    @FXML
    private void initialize() {
        Columns.text(colId, s -> String.valueOf(s.id()));
        Columns.text(colPlate, s -> s.vehicle().plate());
        Columns.text(colEntryDate, s -> Formats.date(s.entryAt()));
        Columns.text(colEntryTime, s -> Formats.time(s.entryAt()));
        Columns.text(colExitDate, s -> s.exitAt() == null ? "En el parqueo" : Formats.date(s.exitAt()));
        Columns.text(colExitTime, s -> Formats.time(s.exitAt()));
        Columns.text(colRate, s -> Formats.money(s.hourlyRate()));
        Columns.text(colAmount, s -> s.exitAt() == null ? "" : Formats.money(s.amount()));
        Columns.text(colParker, ParkingServiceRecord::parkerName);
        table.setPlaceholder(new Label("No hay servicios registrados"));
        table.setItems(filtered);
        txtPlate.textProperty().addListener((obs, old, text) -> applyFilter());
        txtParker.textProperty().addListener((obs, old, text) -> applyFilter());
        Dialogs.guard(() -> data.setAll(Session.parking().serviceRecords("")));
    }

    private void applyFilter() {
        String plate = txtPlate.getText().trim().toLowerCase(Locale.ROOT);
        String parker = txtParker.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(s -> s.vehicle().plate().toLowerCase(Locale.ROOT).contains(plate)
                && s.parkerName().toLowerCase(Locale.ROOT).contains(parker));
    }
}
