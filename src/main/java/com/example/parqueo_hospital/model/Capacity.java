package com.example.parqueo_hospital.model;

/** Cantidad de espacios del parqueo. Los triciclos usan espacios de autos. */
public record Capacity(int cars, int motorcycles) {
    public Capacity {
        if (cars < 0 || motorcycles < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa.");
        }
    }

    public int forType(VehicleType type) {
        return type.usesCarSpace() ? cars : motorcycles;
    }
}
