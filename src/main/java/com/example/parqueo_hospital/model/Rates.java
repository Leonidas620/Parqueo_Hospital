package com.example.parqueo_hospital.model;

public record Rates(double carHourly, double motorcycleHourly, double nightHourly) {
    public double hourlyFor(VehicleType type) {
        return type == VehicleType.MOTO ? motorcycleHourly : carHourly;
    }
}
