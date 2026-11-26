package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Registra qué días ya fueron cuadrados por el administrador. */
public class DailyCloseDAO {
    public void save(LocalDate date, double total) {
        String sql = "INSERT INTO cuadres_dia(fecha, total, creado_en) VALUES (?, ?, ?) "
                + "ON CONFLICT(fecha) DO UPDATE SET total=EXCLUDED.total, creado_en=EXCLUDED.creado_en";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setDouble(2, total);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ParkingException("No se pudo registrar el cuadre del día.", e);
        }
    }

    public boolean isClosed(LocalDate date) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM cuadres_dia WHERE fecha=?")) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo comprobar si el día ya fue cuadrado.", e);
        }
    }
}
