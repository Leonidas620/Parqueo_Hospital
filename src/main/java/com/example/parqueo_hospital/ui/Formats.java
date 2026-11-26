package com.example.parqueo_hospital.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de fecha, hora (24 h) y moneda usados en toda la interfaz. */
public final class Formats {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Formats() { }

    public static String date(java.time.LocalDate value) {
        return value == null ? "" : value.format(DATE);
    }

    public static String date(LocalDateTime value) {
        return value == null ? "" : value.format(DATE);
    }

    public static String time(LocalDateTime value) {
        return value == null ? "" : value.format(TIME);
    }

    public static String money(double value) {
        return String.format(Locale.US, "$%.2f", value);
    }
}
