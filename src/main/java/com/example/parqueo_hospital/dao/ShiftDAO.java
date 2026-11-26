package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.Shift;
import com.example.parqueo_hospital.model.ShiftSummary;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShiftDAO {
    public Shift open(long parkerId, String parkerName) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "INSERT INTO turnos(fecha, parqueador_id, hora_inicio) VALUES (?, ?, ?) RETURNING id";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(now.toLocalDate()));
            ps.setLong(2, parkerId);
            ps.setTimestamp(3, Timestamp.valueOf(now));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Shift(rs.getLong("id"), now.toLocalDate(), parkerName, now, null, 0);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new ParkingException("Ya existe un turno abierto para este usuario.");
            throw new ParkingException("No se pudo abrir el turno.", e);
        }
    }

    public Optional<Shift> findOpen(long parkerId) {
        String sql = "SELECT t.id, t.fecha, t.hora_inicio, c.nombre FROM turnos t JOIN cuentas c ON c.id=t.parqueador_id WHERE t.parqueador_id=? AND t.hora_fin IS NULL";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, parkerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Shift(rs.getLong("id"), rs.getDate("fecha").toLocalDate(),
                        rs.getString("nombre"), rs.getTimestamp("hora_inicio").toLocalDateTime(), null, 0));
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo consultar el turno abierto.", e);
        }
    }

    public boolean isOpen(long shiftId) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM turnos WHERE id=? AND hora_fin IS NULL")) {
            ps.setLong(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo comprobar el estado del turno.", e);
        }
    }

    /**
     * Cierra el turno. El total es la suma de los importes de los servicios que se cobraron en él
     * (cada servicio queda asignado al turno de quien registró su salida).
     */
    public ShiftSummary close(long shiftId, LocalDateTime endedAt) {
        String sql = "UPDATE turnos t SET hora_fin=?, total=COALESCE((SELECT SUM(s.importe) FROM servicios s WHERE s.turno_id=t.id), 0) "
                + "WHERE t.id=? AND t.hora_fin IS NULL RETURNING t.fecha, t.hora_inicio, t.hora_fin, t.total, "
                + "(SELECT c.nombre FROM cuentas c WHERE c.id=t.parqueador_id) AS parqueador, "
                + "(SELECT COUNT(*) FROM servicios s WHERE s.turno_id=t.id) AS servicios";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(endedAt));
            ps.setLong(2, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new ParkingException("No existe un turno abierto para cuadrar.");
                Shift shift = new Shift(shiftId, rs.getDate("fecha").toLocalDate(), rs.getString("parqueador"),
                        rs.getTimestamp("hora_inicio").toLocalDateTime(), rs.getTimestamp("hora_fin").toLocalDateTime(),
                        rs.getDouble("total"));
                return new ShiftSummary(shift, rs.getInt("servicios"));
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo cerrar el turno.", e);
        }
    }

    public List<Shift> findAll(String filter) {
        String q = "%" + (filter == null ? "" : filter.trim().toLowerCase()) + "%";
        String sql = "SELECT t.id, t.fecha, t.hora_inicio, t.hora_fin, t.total, c.nombre AS parqueador FROM turnos t "
                + "JOIN cuentas c ON c.id=t.parqueador_id WHERE CAST(t.fecha AS text) LIKE ? OR lower(c.nombre) LIKE ? ORDER BY t.id DESC";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            return readShifts(ps);
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron cargar los turnos.", e);
        }
    }

    public List<Shift> findDay(LocalDate date) {
        String sql = "SELECT t.id, t.fecha, t.hora_inicio, t.hora_fin, t.total, c.nombre AS parqueador FROM turnos t "
                + "JOIN cuentas c ON c.id=t.parqueador_id WHERE t.fecha=? ORDER BY t.hora_inicio";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            return readShifts(ps);
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron consultar los turnos del día.", e);
        }
    }

    /** Servicios cobrados en los turnos cerrados de una fecha. */
    public int serviceCountOfClosedShiftsOn(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM servicios s JOIN turnos t ON t.id=s.turno_id WHERE t.fecha=? AND t.hora_fin IS NOT NULL";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron contar los servicios del día.", e);
        }
    }

    private List<Shift> readShifts(PreparedStatement ps) throws SQLException {
        List<Shift> rows = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp end = rs.getTimestamp("hora_fin");
                rows.add(new Shift(rs.getLong("id"), rs.getDate("fecha").toLocalDate(), rs.getString("parqueador"),
                        rs.getTimestamp("hora_inicio").toLocalDateTime(), end == null ? null : end.toLocalDateTime(),
                        rs.getDouble("total")));
            }
        }
        return rows;
    }
}
