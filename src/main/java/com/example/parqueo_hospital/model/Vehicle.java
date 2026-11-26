package com.example.parqueo_hospital.model;

public record Vehicle(String plate, VehicleType type, String color, String make,
                      String owner, String phone) {
    public Vehicle {
        plate = plate == null ? "" : plate.trim().toUpperCase();
        if (type == null) throw new IllegalArgumentException("Seleccione el tipo de vehículo.");
        color = clean(color);
        make = clean(make);
        owner = clean(owner);
        phone = clean(phone);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
