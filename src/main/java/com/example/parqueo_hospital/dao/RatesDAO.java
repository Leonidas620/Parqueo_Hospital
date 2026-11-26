package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.Rates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RatesDAO {
    public Rates get() {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT tarifa_auto, tarifa_moto, tarifa_nocturna FROM tarifas WHERE id=1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new ParkingException("No existen tarifas configuradas en la base de datos.");
            return new Rates(rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron consultar las tarifas.", e);
        }
    }

    public void save(Rates rates) {
        String sql = "INSERT INTO tarifas(id, tarifa_auto, tarifa_moto, tarifa_nocturna) VALUES (1, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET tarifa_auto=excluded.tarifa_auto, tarifa_moto=excluded.tarifa_moto, tarifa_nocturna=excluded.tarifa_nocturna";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, rates.carHourly());
            ps.setDouble(2, rates.motorcycleHourly());
            ps.setDouble(3, rates.nightHourly());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron guardar las tarifas.", e);
        }
    }
}
