package com.example.parqueo_hospital.model;

public enum Role {
    ADMIN, PARKER;

    @Override
    public String toString() {
        return this == ADMIN ? "Administrador" : "Parqueador";
    }
}
