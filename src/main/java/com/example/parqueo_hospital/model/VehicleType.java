package com.example.parqueo_hospital.model;

public enum VehicleType {
    AUTO("Auto", true),
    TRICICLO("Triciclo", true),
    MOTO("Moto", false);

    private final String label;
    private final boolean carSpace;

    VehicleType(String label, boolean carSpace) {
        this.label = label;
        this.carSpace = carSpace;
    }

    public boolean usesCarSpace() {
        return carSpace;
    }

    @Override
    public String toString() {
        return label;
    }
}
