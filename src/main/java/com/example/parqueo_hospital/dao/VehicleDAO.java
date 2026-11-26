package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.Vehicle;
import com.example.parqueo_hospital.model.VehicleType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDAO {
    public Optional<Vehicle> findByPlate(String plate) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM vehiculos WHERE chapa=?")) {
            ps.setString(1, plate.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo buscar el vehículo.", e);
        }
    }

    public List<Vehicle> findAll(String filter) {
        String q = "%" + (filter == null ? "" : filter.trim().toLowerCase()) + "%";
        String sql = "SELECT * FROM vehiculos WHERE lower(chapa) LIKE ? OR lower(tipo) LIKE ? " +
                "OR lower(propietario) LIKE ? OR lower(marca) LIKE ? ORDER BY chapa";
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) vehicles.add(map(rs));
            }
            return vehicles;
        } catch (SQLException e) {
            throw new ParkingException("No se pudo cargar el registro global de vehículos.", e);
        }
    }

    static Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(rs.getString("chapa"), VehicleType.valueOf(rs.getString("tipo")),
                rs.getString("color"), rs.getString("marca"), rs.getString("propietario"), rs.getString("telefono"));
    }
}
