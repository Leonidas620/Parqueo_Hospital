package com.example.parqueo_hospital.model;

/** Desglose de un cobro: horas completas diurnas y nocturnas y el importe resultante. */
public record Charge(long dayHours, long nightHours, double amount) {
    public long totalHours() {
        return dayHours + nightHours;
    }
}
