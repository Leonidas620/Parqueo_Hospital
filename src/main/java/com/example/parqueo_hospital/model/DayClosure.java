package com.example.parqueo_hospital.model;

import java.nio.file.Path;

/**
 * Resultado de cuadrar el día. El total solo suma turnos ya cerrados;
 * {@code openShifts} indica cuántos turnos del día seguían abiertos.
 */
public record DayClosure(Path file, int shifts, int openShifts, int services, double total) {
}
