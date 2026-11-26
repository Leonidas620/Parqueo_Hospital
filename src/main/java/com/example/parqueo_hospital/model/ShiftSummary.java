package com.example.parqueo_hospital.model;

/** Turno ya cerrado junto con la cantidad de servicios que cobró. */
public record ShiftSummary(Shift shift, int services) {
}
