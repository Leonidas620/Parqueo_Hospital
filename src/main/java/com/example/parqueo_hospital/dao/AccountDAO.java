package com.example.parqueo_hospital.dao;

import com.example.parqueo_hospital.model.Account;
import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO {
    public Optional<Account> findByUsername(String username) {
        String sql = "SELECT id, nombre, usuario, password_hash, rol FROM cuentas WHERE lower(usuario) = ?";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase(java.util.Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ParkingException("No se pudo consultar la cuenta.", e);
        }
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT id, nombre, usuario, password_hash, rol FROM cuentas ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) accounts.add(map(rs));
            return accounts;
        } catch (SQLException e) {
            throw new ParkingException("No se pudieron cargar las cuentas.", e);
        }
    }

    public void create(String name, String username, String passwordHash, Role role) {
        String sql = "INSERT INTO cuentas(nombre, usuario, password_hash, rol) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, username.trim().toLowerCase(java.util.Locale.ROOT));
            ps.setString(3, passwordHash);
            ps.setString(4, role.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new ParkingException("Ese nombre de usuario ya existe.");
            throw new ParkingException("No se pudo crear la cuenta.", e);
        }
    }

    public void changePassword(long id, String hash) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE cuentas SET password_hash=? WHERE id=?")) {
            ps.setString(1, hash);
            ps.setLong(2, id);
            if (ps.executeUpdate() != 1) throw new ParkingException("La cuenta ya no existe.");
        } catch (SQLException e) {
            throw new ParkingException("No se pudo cambiar la contraseña.", e);
        }
    }

    public void delete(long id) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cuentas WHERE id=?")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) throw new ParkingException("La cuenta seleccionada ya no existe.");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new ParkingException("No se puede eliminar la cuenta porque tiene turnos registrados.");
            }
            throw new ParkingException("No se pudo eliminar la cuenta.", e);
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        return new Account(rs.getLong("id"), rs.getString("nombre"), rs.getString("usuario"),
                rs.getString("password_hash"), Role.valueOf(rs.getString("rol")));
    }
}
