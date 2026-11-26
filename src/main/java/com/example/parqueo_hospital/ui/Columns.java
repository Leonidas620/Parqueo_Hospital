package com.example.parqueo_hospital.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

/** Atajo para definir columnas de texto de una tabla. */
public final class Columns {
    private Columns() { }

    public static <T> void text(TableColumn<T, String> column, Function<T, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}
