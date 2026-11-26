package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.Subscription;

import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SubscriptionDAO {
    public List<Subscription> findCurrent(String filter) {
        String q = "%" + (filter == null ? "" : filter.trim().toLowerCase()) + "%";
        List<Subscription> rows = new ArrayList<>();
        String sql = "SELECT id, chapa, mes, anio, importe FROM abonos " +
                "WHERE make_date(anio, mes, 1) >= date_trunc('month', CURRENT_DATE)::date " +
                "AND (lower(chapa) LIKE ? OR CAST(mes AS text) LIKE ? OR CAST(anio AS text) LIKE ?) ORDER BY anio, mes, chapa";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(new Subscription(rs.getLong("id"), rs.getString("chapa"),
                        rs.getInt("mes"), rs.getInt("anio"), rs.getDouble("importe")));
            }
            return rows;
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron cargar los abonos.", e);
        }
    }

    public void create(String plate, int month, int year, double amount) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO abonos(chapa, mes, anio, importe) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, plate.trim().toUpperCase());
            ps.setInt(2, month);
            ps.setInt(3, year);
            ps.setDouble(4, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new ParkingException("El vehículo ya tiene abono para ese mes.");
            throw new ParkingException("No se pudo registrar el abono.", e);
        }
    }

    public void delete(long id) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM abonos WHERE id=?")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() != 1) throw new ParkingException("No se encontró el abono seleccionado.");
        } catch (SQLException e) {
            throw new ParkingException("No se pudo eliminar el abono.", e);
        }
    }

    public Set<YearMonth> coveredMonths(String plate, YearMonth start, YearMonth end) {
        Set<YearMonth> months = new HashSet<>();
        String sql = "SELECT mes, anio FROM abonos WHERE chapa=? AND make_date(anio, mes, 1) <= ? AND make_date(anio, mes, 1) >= ?";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setDate(2, Date.valueOf(end.atDay(1)));
            ps.setDate(3, Date.valueOf(start.atDay(1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) months.add(YearMonth.of(rs.getInt("anio"), rs.getInt("mes")));
            }
            return months;
        } catch (SQLException e) {
            throw new ParkingException("No se pudo comprobar la vigencia de los abonos.", e);
        }
    }
}
