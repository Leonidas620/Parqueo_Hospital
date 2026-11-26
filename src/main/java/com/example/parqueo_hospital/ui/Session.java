package com.example.parqueo_hospital.ui;

import com.example.parqueo_hospital.model.Account;
import com.example.parqueo_hospital.model.Role;
import com.example.parqueo_hospital.model.Shift;
import com.example.parqueo_hospital.service.ParkingService;

/** Estado de la aplicación mientras hay un usuario autenticado. */
public final class Session {
    private static ParkingService parking;
    private static Account account;
    private static Shift shift;

    private Session() { }

    public static void init(ParkingService service) {
        parking = service;
    }

    public static ParkingService parking() {
        return parking;
    }

    public static Account account() {
        return account;
    }

    public static void setAccount(Account value) {
        account = value;
    }

    /** Turno del parqueador; null para el administrador. Puede estar cerrado (modo consulta). */
    public static Shift shift() {
        return shift;
    }

    public static void setShift(Shift value) {
        shift = value;
    }

    public static boolean isAdmin() {
        return account != null && account.role() == Role.ADMIN;
    }

    public static boolean shiftOpen() {
        return shift != null && shift.endedAt() == null;
    }

    public static void clear() {
        account = null;
        shift = null;
    }
}
