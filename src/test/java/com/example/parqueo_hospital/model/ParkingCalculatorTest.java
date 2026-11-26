package com.example.parqueo_hospital.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParkingCalculatorTest {
    private final Rates rates = new Rates(10, 5, 3);

    @Test
    void roundsEachDayAndNightCategoryUpToCompleteHours() {
        double amount = ParkingCalculator.calculate(
                LocalDateTime.of(2026, 1, 5, 19, 30),
                LocalDateTime.of(2026, 1, 5, 20, 30),
                rates, VehicleType.AUTO, Set.of());
        assertEquals(13, amount);
    }

    @Test
    void subscriptionCoversTheWholeMatchingCalendarMonth() {
        double amount = ParkingCalculator.calculate(
                LocalDateTime.of(2026, 1, 31, 23, 0),
                LocalDateTime.of(2026, 2, 1, 1, 0),
                rates, VehicleType.AUTO, Set.of(YearMonth.of(2026, 1)));
        assertEquals(3, amount);
    }

    @Test
    void chargesFullHoursForDaytimeRemainder() {
        double amount = ParkingCalculator.calculate(
                LocalDateTime.of(2026, 1, 5, 9, 40),
                LocalDateTime.of(2026, 1, 5, 12, 5),
                rates, VehicleType.AUTO, Set.of());
        assertEquals(30, amount);
    }

    @Test
    void chargeReportsDayAndNightHoursSeparately() {
        // 08:00 a 21:30 del mismo día: 12 h diurnas y 2 h nocturnas (20:00 a 21:30 redondeado).
        Charge charge = ParkingCalculator.charge(
                LocalDateTime.of(2026, 1, 5, 8, 0),
                LocalDateTime.of(2026, 1, 5, 21, 30),
                rates, VehicleType.AUTO, Set.of());
        assertEquals(12, charge.dayHours());
        assertEquals(2, charge.nightHours());
        assertEquals(14, charge.totalHours());
        assertEquals(12 * 10 + 2 * 3, charge.amount());
    }

    @Test
    void motorcyclesUseTheirOwnHourlyRate() {
        double amount = ParkingCalculator.calculate(
                LocalDateTime.of(2026, 1, 5, 9, 0),
                LocalDateTime.of(2026, 1, 5, 11, 0),
                rates, VehicleType.MOTO, Set.of());
        assertEquals(10, amount);
    }

    @Test
    void tricyclesPayTheCarRate() {
        double amount = ParkingCalculator.calculate(
                LocalDateTime.of(2026, 1, 5, 9, 0),
                LocalDateTime.of(2026, 1, 5, 11, 0),
                rates, VehicleType.TRICICLO, Set.of());
        assertEquals(20, amount);
    }

    @Test
    void aSubscribedStayHasNoChargeAtAll() {
        Charge charge = ParkingCalculator.charge(
                LocalDateTime.of(2026, 1, 5, 9, 0),
                LocalDateTime.of(2026, 1, 5, 18, 0),
                rates, VehicleType.AUTO, Set.of(YearMonth.of(2026, 1)));
        assertEquals(0, charge.totalHours());
        assertEquals(0, charge.amount());
    }

    @Test
    void anUnsubscribedFollowingMonthIsChargedFromMidnight() {
        // Abonado en enero: de 00:00 a 01:00 de febrero son 1 h nocturna.
        Charge charge = ParkingCalculator.charge(
                LocalDateTime.of(2026, 1, 31, 23, 0),
                LocalDateTime.of(2026, 2, 1, 1, 0),
                rates, VehicleType.AUTO, Set.of(YearMonth.of(2026, 1)));
        assertEquals(1, charge.nightHours());
        assertEquals(0, charge.dayHours());
    }
}
