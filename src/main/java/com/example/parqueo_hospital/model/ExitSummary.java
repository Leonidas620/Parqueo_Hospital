package com.example.parqueo_hospital.model;

import java.nio.file.Path;

/** Resultado de registrar una salida. {@code receipt} es null si no se pudo guardar el recibo. */
public record ExitSummary(ParkingServiceRecord service, Charge charge, Path receipt) {
}
