package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Un servicio queda sin turno mientras el vehículo está dentro. Al registrar la salida se le asigna
 * el turno de quien cobra, de modo que el importe suma al turno correcto.
 */
public class ServiceDAO {
    private static final String RECORD_SELECT = "SELECT s.id, s.chapa, s.hora_entrada, s.hora_salida, s.tarifa_aplicada, s.importe, "
            + "COALESCE(s.turno_id, 0) AS turno_id, COALESCE(c.nombre, '') AS parqueador, "
            + "v.tipo, v.color, v.marca, v.propietario, v.telefono "
            + "FROM servicios s JOIN vehiculos v ON v.chapa=s.chapa "
            + "LEFT JOIN turnos t ON t.id=s.turno_id LEFT JOIN cuentas c ON c.id=t.parqueador_id ";

    public List<ParkingServiceRecord> active() {
        List<ParkingServiceRecord> rows = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(RECORD_SELECT + "WHERE s.hora_salida IS NULL ORDER BY s.id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(map(rs));
            return rows;
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron reconstruir los vehículos presentes.", e);
        }
    }

    public Optional<ParkingServiceRecord> activeByPlate(String plate) {
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(
                RECORD_SELECT + "WHERE s.hora_salida IS NULL AND s.chapa=?")) {
            ps.setString(1, plate.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo consultar el servicio activo.", e);
        }
    }

    public ParkingServiceRecord registerEntry(Vehicle vehicle, LocalDateTime at) {
        String upsertVehicle = "INSERT INTO vehiculos(chapa, tipo, color, marca, propietario, telefono) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT(chapa) DO UPDATE SET tipo=EXCLUDED.tipo, color=EXCLUDED.color, marca=EXCLUDED.marca, "
                + "propietario=EXCLUDED.propietario, telefono=EXCLUDED.telefono";
        String insertService = "INSERT INTO servicios(chapa, hora_entrada, tarifa_aplicada, importe) VALUES (?, ?, ?, 0) RETURNING id";
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                int carCapacity;
                int motoCapacity;
                // Bloquear la fila de capacidad serializa las entradas simultáneas.
                try (PreparedStatement lock = c.prepareStatement(
                        "SELECT capacidad_autos, capacidad_motos FROM capacidad WHERE id=1 FOR UPDATE");
                     ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) throw new ParkingException("No existe una capacidad configurada en la base de datos.");
                    carCapacity = rs.getInt(1);
                    motoCapacity = rs.getInt(2);
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM servicios WHERE chapa=? AND hora_salida IS NULL")) {
                    ps.setString(1, vehicle.plate());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new ParkingException("Ese vehículo ya está dentro del parqueo.");
                    }
                }
                if (vehicle.type().usesCarSpace() && activeCount(c, true) >= carCapacity) {
                    throw new ParkingException("No quedan espacios disponibles para autos o triciclos.");
                }
                if (!vehicle.type().usesCarSpace() && activeCount(c, false) >= motoCapacity) {
                    throw new ParkingException("No quedan espacios disponibles para motos.");
                }
                // Si el vehículo ya figuraba en el registro global, sus datos se actualizan.
                try (PreparedStatement ps = c.prepareStatement(upsertVehicle)) {
                    ps.setString(1, vehicle.plate());
                    ps.setString(2, vehicle.type().name());
                    ps.setString(3, vehicle.color());
                    ps.setString(4, vehicle.make());
                    ps.setString(5, vehicle.owner());
                    ps.setString(6, vehicle.phone());
                    ps.executeUpdate();
                }
                double hourly = vehicle.type() == VehicleType.MOTO
                        ? scalarDouble(c, "SELECT tarifa_moto FROM tarifas WHERE id=1")
                        : scalarDouble(c, "SELECT tarifa_auto FROM tarifas WHERE id=1");
                long id;
                try (PreparedStatement ps = c.prepareStatement(insertService)) {
                    ps.setString(1, vehicle.plate());
                    ps.setTimestamp(2, Timestamp.valueOf(at));
                    ps.setDouble(3, hourly);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                c.commit();
                return new ParkingServiceRecord(id, vehicle, at, null, hourly, 0, "", 0);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (e instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                    throw new ParkingException("Ese vehículo ya está dentro del parqueo.");
                }
                if (e instanceof SQLException sql) throw new ParkingException("No se pudo registrar la entrada.", sql);
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo acceder a la base de datos al registrar la entrada.", e);
        }
    }

    /** Cierra el servicio activo de la chapa y lo asigna al turno de quien cobra. */
    public ParkingServiceRecord registerExit(String plate, LocalDateTime at, double hourlyRate,
                                             double amount, long chargingShiftId) {
        String sql = "UPDATE servicios SET hora_salida=?, tarifa_aplicada=?, importe=?, turno_id=? "
                + "WHERE chapa=? AND hora_salida IS NULL RETURNING id";
        try (Connection c = Database.connect()) {
            long id;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(at));
                ps.setDouble(2, hourlyRate);
                ps.setDouble(3, amount);
                ps.setLong(4, chargingShiftId);
                ps.setString(5, plate.trim().toUpperCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new ParkingException("No se encontró un vehículo presente con esa chapa.");
                    id = rs.getLong(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(RECORD_SELECT + "WHERE s.id=?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new ParkingException("No hay un turno abierto válido para registrar la salida.");
            }
            throw new ParkingException("No se pudo registrar la salida.", e);
        }
    }

    public List<ParkingServiceRecord> findAll(String filter) {
        String q = "%" + (filter == null ? "" : filter.trim().toLowerCase()) + "%";
        List<ParkingServiceRecord> rows = new ArrayList<>();
        String sql = RECORD_SELECT + "WHERE lower(s.chapa) LIKE ? OR lower(c.nombre) LIKE ? ORDER BY s.id DESC";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(map(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron cargar los servicios.", e);
        }
    }

    private int activeCount(Connection c, boolean carSpace) throws SQLException {
        String sql = "SELECT COUNT(*) FROM servicios s JOIN vehiculos v ON v.chapa=s.chapa WHERE s.hora_salida IS NULL AND "
                + (carSpace ? "v.tipo IN ('AUTO','TRICICLO')" : "v.tipo='MOTO'");
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private double scalarDouble(Connection c, String sql) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new SQLException("No existen tarifas configuradas.");
            return rs.getDouble(1);
        }
    }

    private ParkingServiceRecord map(ResultSet rs) throws SQLException {
        Vehicle vehicle = VehicleDAO.map(rs);
        Timestamp exit = rs.getTimestamp("hora_salida");
        return new ParkingServiceRecord(rs.getLong("id"), vehicle, rs.getTimestamp("hora_entrada").toLocalDateTime(),
                exit == null ? null : exit.toLocalDateTime(), rs.getDouble("tarifa_aplicada"),
                rs.getDouble("importe"), rs.getString("parqueador"), rs.getLong("turno_id"));
    }
}
