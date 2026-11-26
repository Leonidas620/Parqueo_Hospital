package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.model.DayClosure;
import com.example.parqueo_hospital.model.Shift;
import com.example.parqueo_hospital.ui.Columns;
import com.example.parqueo_hospital.ui.Dialogs;
import com.example.parqueo_hospital.ui.Formats;
import com.example.parqueo_hospital.ui.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Locale;

public class ShiftsController {
    @FXML private DatePicker dateFilter;
    @FXML private TextField txtParker;
    @FXML private TableView<Shift> table;
    @FXML private TableColumn<Shift, String> colId;
    @FXML private TableColumn<Shift, String> colDate;
    @FXML private TableColumn<Shift, String> colParker;
    @FXML private TableColumn<Shift, String> colStart;
    @FXML private TableColumn<Shift, String> colEnd;
    @FXML private TableColumn<Shift, String> colTotal;

    private final ObservableList<Shift> data = FXCollections.observableArrayList();
    private final FilteredList<Shift> filtered = new FilteredList<>(data, row -> true);

    @FXML
    private void initialize() {
        Columns.text(colId, s -> String.valueOf(s.id()));
        Columns.text(colDate, s -> Formats.date(s.date()));
        Columns.text(colParker, Shift::parkerName);
        Columns.text(colStart, s -> Formats.time(s.startedAt()));
        Columns.text(colEnd, s -> s.endedAt() == null ? "En curso" : Formats.time(s.endedAt()));
        Columns.text(colTotal, s -> s.endedAt() == null ? "" : Formats.money(s.total()));
        table.setPlaceholder(new Label("No hay turnos registrados"));
        table.setItems(filtered);
        dateFilter.valueProperty().addListener((obs, old, value) -> applyFilter());
        txtParker.textProperty().addListener((obs, old, text) -> applyFilter());
        reload();
    }

    /** Botón de la lupa: vuelve a leer los turnos de la base de datos y aplica los filtros. */
    @FXML
    private void search() {
        reload();
    }

    @FXML
    private void closeDay() {
        Dialogs.guard(() -> {
            DayClosure closure = Session.parking().closeDay();
            StringBuilder text = new StringBuilder("Turnos del día: " + closure.shifts())
                    .append("\nServicios cobrados: ").append(closure.services())
                    .append("\nImporte total del día: ").append(Formats.money(closure.total()))
                    .append("\n\nArchivo guardado en:\n").append(closure.file());
            if (closure.openShifts() > 0) {
                text.append("\n\nAtención: ").append(closure.openShifts())
                        .append(closure.openShifts() == 1 ? " turno sigue abierto y no está incluido en el total."
                                : " turnos siguen abiertos y no están incluidos en el total.");
            }
            Dialogs.info(text.toString());
            reload();
        });
    }

    private void reload() {
        Dialogs.guard(() -> data.setAll(Session.parking().shiftRecords("")));
    }

    private void applyFilter() {
        LocalDate date = dateFilter.getValue();
        String parker = txtParker.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(s -> (date == null || s.date().equals(date))
                && s.parkerName().toLowerCase(Locale.ROOT).contains(parker));
    }
}
