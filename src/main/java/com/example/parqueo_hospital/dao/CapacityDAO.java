package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.Capacity;
import com.example.parqueo_hospital.model.ParkingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CapacityDAO {
    public Capacity get() {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT capacidad_autos, capacidad_motos FROM capacidad WHERE id=1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new ParkingException("No existe una capacidad configurada en la base de datos.");
            return new Capacity(rs.getInt(1), rs.getInt(2));
        } catch (SQLException e) {
            throw new ParkingException("No se pudo consultar la capacidad. Verifique que la base de datos esté actualizada.", e);
        }
    }

    public void save(Capacity capacity) {
        String sql = "INSERT INTO capacidad(id, capacidad_autos, capacidad_motos) VALUES (1, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET capacidad_autos=EXCLUDED.capacidad_autos, capacidad_motos=EXCLUDED.capacidad_motos";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, capacity.cars());
            ps.setInt(2, capacity.motorcycles());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ParkingException("No se pudo guardar la capacidad.", e);
        }
    }
}
