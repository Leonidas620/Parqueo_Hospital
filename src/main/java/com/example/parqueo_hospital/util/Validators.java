package com.example.parqueo_hospital.util;

import java.util.regex.Pattern;

public final class Validators {
    private static final Pattern PLATE = Pattern.compile("^[PBFW]\\d{6}$");
    private static final Pattern PHONE = Pattern.compile("^[+\\d][\\d\\s()-]{5,19}$");

    private Validators() { }

    public static String plate(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (!PLATE.matcher(normalized).matches()) {
            throw new IllegalArgumentException("La chapa debe comenzar con P, B, F o W y continuar con seis números.");
        }
        return normalized;
    }

    public static String required(String value, String label) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) throw new IllegalArgumentException("El campo «" + label + "» es obligatorio.");
        return clean;
    }

    public static String phone(String value) {
        String clean = required(value, "Teléfono");
        if (!PHONE.matcher(clean).matches()) throw new IllegalArgumentException("Escriba un número de teléfono válido.");
        return clean;
    }

    public static double positiveAmount(String value, String label) {
        try {
            double amount = Double.parseDouble(value.trim().replace(',', '.'));
            if (!Double.isFinite(amount) || amount < 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El importe de «" + label + "» debe ser un número válido mayor o igual que cero.");
        }
    }
}
