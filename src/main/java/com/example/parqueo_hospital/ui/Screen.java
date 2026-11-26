package com.example.parqueo_hospital.ui;

/** Pantallas que se cargan en el centro de la ventana principal. */
public enum Screen {
    PRESENT("Registro de Vehículos Presentes", "Present.fxml"),
    SERVICES("Registro de Servicios", "Services.fxml"),
    SHIFTS("Registro de Turnos", "Shifts.fxml"),
    VEHICLES("Registro de Vehículos Globales", "Vehicles.fxml"),
    SUBSCRIPTIONS("Gestión de Abonos", "Subscriptions.fxml"),
    RATES("Gestión de Tarifas", "Rates.fxml"),
    CAPACITY("Gestión de Capacidad", "Capacity.fxml"),
    ACCOUNTS("Gestión de Cuentas", "Accounts.fxml"),
    MY_ACCOUNT("Mi Cuenta", "MyAccount.fxml");

    private final String title;
    private final String fxml;

    Screen(String title, String fxml) {
        this.title = title;
        this.fxml = fxml;
    }

    public String title() {
        return title;
    }

    public String fxml() {
        return fxml;
    }

    /** Primera pantalla después de iniciar sesión. */
    public static Screen home() {
        return Session.isAdmin() ? SHIFTS : PRESENT;
    }
}
