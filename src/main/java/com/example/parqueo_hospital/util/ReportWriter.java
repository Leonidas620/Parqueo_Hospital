package com.example.parqueo_hospital.util;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.model.Shift;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ReportWriter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private ReportWriter() { }

    public static Path service(ParkingServiceRecord service) {
        Path folder = Path.of("reportes", "servicios");
        String text = "Servicios de Parking del Hospital Provincial\n\n"
                + "No Servicio: " + service.id() + "\n"
                + "Chapa: " + service.vehicle().plate() + "\n"
                + "Fecha Entrada: " + service.entryAt().format(DATE) + "\n"
                + "Hora Entrada: " + service.entryAt().format(TIME) + "\n"
                + "Fecha Salida: " + service.exitAt().format(DATE) + "\n"
                + "Hora Salida: " + service.exitAt().format(TIME) + "\n\n"
                + String.format(Locale.US, "Tarifa: $%.2f\n", service.hourlyRate())
                + String.format(Locale.US, "Importe: $%.2f\n", service.amount())
                + "Usted fue atendido por: " + service.parkerName() + "\n\n"
                + "Gracias por contar con nuestros servicios\n";
        return write(folder.resolve(service.id() + ".txt"), text);
    }

    /** Cuadre del día: solo los turnos cerrados suman al total; los abiertos se listan como "En curso". */
    public static Path daily(LocalDate date, List<Shift> shifts, double total, int serviceCount) {
        StringBuilder text = new StringBuilder("Cuadre del Dia ").append(date.format(DATE)).append("\n\n")
                .append("Cantidad de turnos trabajados: ").append(shifts.size()).append("\n")
                .append("Parqueadores que trabajaron ese dia:\n");
        for (Shift shift : shifts) {
            text.append(shift.parkerName()).append(" desde ")
                    .append(shift.startedAt().format(TIME)).append(" hasta ")
                    .append(shift.endedAt() == null ? "En curso (no incluido en el total)" : shift.endedAt().format(TIME))
                    .append('\n');
        }
        text.append("\nServicios cobrados: ").append(serviceCount).append("\n")
                .append(String.format(Locale.US, "Importe Total del Dia: $%.2f\n", total));
        return write(Path.of("reportes", "cuadres_dias",
                "Cuadre del Dia " + date.format(DATE).replace('/', '-') + ".txt"), text.toString());
    }

    public static Path rates(double car, double moto, double night) {
        String text = String.format(Locale.US,
                "Tarifa autos por hora: $%.2f\nTarifa motos por hora: $%.2f\nTarifa nocturna por hora: $%.2f\n",
                car, moto, night);
        return write(Path.of("tarifas", "tarifas.txt"), text);
    }

    private static Path write(Path file, String text) {
        try {
            Files.createDirectories(file.getParent());
            return Files.writeString(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ParkingException("No se pudo guardar el archivo " + file + ".", e);
        }
    }
}
