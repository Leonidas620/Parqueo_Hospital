package com.example.parqueo_hospital.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Set;

public final class ParkingCalculator {
    private static final LocalTime NIGHT_START = LocalTime.of(20, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(7, 0);

    private ParkingCalculator() { }

    public static double calculate(LocalDateTime start, LocalDateTime end, Rates rates,
                                   VehicleType type, Set<YearMonth> coveredMonths) {
        return charge(start, end, rates, type, coveredMonths).amount();
    }

    /**
     * horas_completas_diurnas * tarifa_vehiculo + horas_completas_nocturnas * tarifa_nocturna.
     * Los meses cubiertos por un abono no se cobran; si la estancia continúa en un mes sin abono,
     * ese mes se cobra desde las 00:00 de su primer día.
     */
    public static Charge charge(LocalDateTime start, LocalDateTime end, Rates rates,
                                VehicleType type, Set<YearMonth> coveredMonths) {
        if (end.isBefore(start)) throw new IllegalArgumentException("La salida no puede ser anterior a la entrada.");
        long daytimeSeconds = 0;
        long nighttimeSeconds = 0;
        LocalDateTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalDateTime nextMonth = YearMonth.from(cursor).plusMonths(1).atDay(1).atStartOfDay();
            LocalDateTime segmentEnd = end.isBefore(nextMonth) ? end : nextMonth;
            if (!coveredMonths.contains(YearMonth.from(cursor))) {
                LocalDateTime point = cursor;
                while (point.isBefore(segmentEnd)) {
                    LocalDateTime nextBoundary = nextBoundary(point, segmentEnd);
                    long seconds = Duration.between(point, nextBoundary).getSeconds();
                    if (isNight(point.toLocalTime())) nighttimeSeconds += seconds;
                    else daytimeSeconds += seconds;
                    point = nextBoundary;
                }
            }
            cursor = segmentEnd;
        }
        long dayHours = (daytimeSeconds + 3599) / 3600;
        long nightHours = (nighttimeSeconds + 3599) / 3600;
        return new Charge(dayHours, nightHours,
                dayHours * rates.hourlyFor(type) + nightHours * rates.nightHourly());
    }

    private static LocalDateTime nextBoundary(LocalDateTime value, LocalDateTime limit) {
        LocalDateTime midnight = value.toLocalDate().plusDays(1).atStartOfDay();
        LocalDateTime boundary = value.toLocalTime().isBefore(NIGHT_END)
                ? value.toLocalDate().atTime(NIGHT_END)
                : value.toLocalTime().isBefore(NIGHT_START)
                    ? value.toLocalDate().atTime(NIGHT_START) : midnight;
        if (!boundary.isAfter(value)) boundary = value.toLocalDate().atTime(NIGHT_START);
        return boundary.isBefore(limit) ? boundary : limit;
    }

    private static boolean isNight(LocalTime time) {
        return !time.isBefore(NIGHT_START) || time.isBefore(NIGHT_END);
    }
}
