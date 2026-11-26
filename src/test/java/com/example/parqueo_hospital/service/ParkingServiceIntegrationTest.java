package com.example.parqueo_hospital.service;

import com.example.parqueo_hospital.dao.Database;
import com.example.parqueo_hospital.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas con la base de datos real. Solo se ejecutan si existe la variable de entorno
 * PARKING_DB_URL (la misma que usa la aplicación); apunte a una base de datos de pruebas.
 */
@EnabledIfEnvironmentVariable(named = "PARKING_DB_URL", matches = ".+")
class ParkingServiceIntegrationTest {
    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String randomPlate() {
        return "P" + String.format("%06d", (int) (Math.random() * 1_000_000));
    }

    private static void sql(String q, Object... args) throws Exception {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(q)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            ps.executeUpdate();
        }
    }

    private static double scalar(String q, Object... args) throws Exception {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(q)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private Account newParker(ParkingService service, String name) {
        String user = name.toLowerCase() + suffix();
        service.createAccount(name, user, "1234", Role.PARKER);
        return service.login(user, "1234".toCharArray()).orElseThrow();
    }

    @Test
    void theAmountGoesToTheShiftOfWhoChargesNotWhoReceivedTheVehicle() throws Exception {
        ParkingService service = new ParkingService();
        Account receiver = newParker(service, "Recibe");
        Account cashier = newParker(service, "Cobra");
        String plate = randomPlate();

        Shift receiverShift = service.openShift(receiver);
        service.enter(plate, VehicleType.AUTO, "Rojo", "Lada", "Dueño", "55555555", receiverShift);
        sql("UPDATE servicios SET hora_entrada = hora_entrada - interval '3 hours' WHERE chapa=? AND hora_salida IS NULL", plate);
        service = new ParkingService(); // reinicio: la lista se reconstruye con la hora ajustada
        Shift cashierShift = service.openShift(cashier);
        assertEquals(0, service.closeShift(receiverShift).shift().total());

        ExitSummary exit = service.exit(plate, cashierShift);
        assertTrue(exit.service().amount() > 0);
        assertEquals("Cobra", exit.service().parkerName());

        ShiftSummary closed = service.closeShift(cashierShift);
        assertEquals(exit.service().amount(), closed.shift().total());
        assertEquals(1, closed.services());
    }

    @Test
    void aClosedShiftCannotRegisterEntriesOrExits() {
        ParkingService service = new ParkingService();
        Account parker = newParker(service, "Cierra");
        Shift shift = service.openShift(parker);
        service.closeShift(shift);
        assertThrows(ParkingException.class,
                () -> service.enter(randomPlate(), VehicleType.AUTO, "c", "m", "o", "55555555", shift));
        assertThrows(ParkingException.class, () -> service.exit("P000000", shift));
    }

    @Test
    void anOvernightShiftKeepsTheMoneyOfServicesThatLeftBeforeMidnight() throws Exception {
        ParkingService service = new ParkingService();
        Account parker = newParker(service, "Noche");
        String plate = randomPlate();
        sql("INSERT INTO vehiculos VALUES (?, 'AUTO', 'Azul', 'Kia', 'Luis', '55555555') ON CONFLICT DO NOTHING", plate);
        sql("INSERT INTO turnos(fecha, parqueador_id, hora_inicio) VALUES ('2026-03-10', ?, '2026-03-10 20:00')", parker.id());
        long shiftId = (long) scalar("SELECT id FROM turnos WHERE parqueador_id=? AND hora_fin IS NULL", parker.id());
        sql("INSERT INTO servicios(chapa, hora_entrada, hora_salida, tarifa_aplicada, importe, turno_id) "
                + "VALUES (?, '2026-03-10 20:30', '2026-03-10 23:30', 100, 300, ?)", plate, shiftId);

        ShiftSummary closed = new com.example.parqueo_hospital.dao.ShiftDAO()
                .close(shiftId, LocalDateTime.of(2026, 3, 11, 6, 0));
        assertEquals(300, closed.shift().total());
    }

    @Test
    void capacityIsConfigurableAndCannotGoBelowTheVehiclesInside() {
        ParkingService service = new ParkingService();
        Capacity original = service.capacity();
        try {
            int cars = service.currentCarOccupancy();
            int motos = service.currentMotorcycleOccupancy();
            service.updateCapacity(cars + 3, motos + 2);
            assertEquals(new Capacity(cars + 3, motos + 2), service.capacity());
            if (cars > 0) assertThrows(ParkingException.class, () -> service.updateCapacity(cars - 1, motos + 2));
        } finally {
            service.updateCapacity(original.cars(), original.motorcycles());
        }
    }

    @Test
    void anyPlateCanBeSubscribedWithoutPriorRegistrationButNotTwiceInTheSameMonth() {
        ParkingService service = new ParkingService();
        String plate = randomPlate();
        service.createSubscription(plate, 7, 2031, 500);
        assertThrows(ParkingException.class, () -> service.createSubscription(plate, 7, 2031, 500));
        assertThrows(IllegalArgumentException.class, () -> service.createSubscription("X12", 7, 2031, 500));
    }

    @Test
    void aReturningVehicleUpdatesItsDataInTheGlobalRegistry() throws Exception {
        ParkingService service = new ParkingService();
        Account parker = newParker(service, "Datos");
        Shift shift = service.openShift(parker);
        String plate = randomPlate();
        service.enter(plate, VehicleType.AUTO, "Rojo", "Lada", "Dueño Uno", "11111111", shift);
        service.exit(plate, shift);
        service.enter(plate, VehicleType.AUTO, "Verde", "Kia", "Dueño Dos", "22222222", shift);
        try (Connection c = Database.connect(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT color, marca, propietario, telefono FROM vehiculos WHERE chapa='" + plate + "'")) {
            assertTrue(rs.next());
            assertEquals("Verde", rs.getString(1));
            assertEquals("Kia", rs.getString(2));
            assertEquals("Dueño Dos", rs.getString(3));
            assertEquals("22222222", rs.getString(4));
        } finally {
            service.exit(plate, shift);
        }
    }
}
