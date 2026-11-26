package com.example.parqueo_hospital.service;

import com.example.parqueo_hospital.dao.*;
import com.example.parqueo_hospital.model.*;
import com.example.parqueo_hospital.structures.LinkedVehicleList;
import com.example.parqueo_hospital.util.PasswordHasher;
import com.example.parqueo_hospital.util.ReportWriter;
import com.example.parqueo_hospital.util.Validators;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ParkingService {
    private final AccountDAO accounts = new AccountDAO();
    private final VehicleDAO vehicles = new VehicleDAO();
    private final ServiceDAO services = new ServiceDAO();
    private final ShiftDAO shifts = new ShiftDAO();
    private final RatesDAO rates = new RatesDAO();
    private final CapacityDAO capacities = new CapacityDAO();
    private final SubscriptionDAO subscriptions = new SubscriptionDAO();
    private final DailyCloseDAO dailyCloses = new DailyCloseDAO();
    private final LinkedVehicleList present = new LinkedVehicleList();
    private Capacity capacity;

    public ParkingService() {
        rebuildPresentList();
        capacity = capacities.get();
    }

    public Optional<Account> login(String username, char[] password) {
        Optional<Account> account = accounts.findByUsername(username);
        Optional<Account> authenticated = account.filter(value -> PasswordHasher.verify(password, value.passwordHash()));
        if (authenticated.isPresent() && authenticated.get().passwordHash().startsWith("sha256$")) {
            Account legacy = authenticated.get();
            String upgradedHash = PasswordHasher.hash(password);
            accounts.changePassword(legacy.id(), upgradedHash);
            return Optional.of(new Account(legacy.id(), legacy.parkerName(), legacy.username(), upgradedHash, legacy.role()));
        }
        return authenticated;
    }

    /** Reconstruye la lista enlazada desde la base de datos: el más reciente queda en la cabeza. */
    public void rebuildPresentList() {
        for (ParkingServiceRecord record : services.active()) present.addFirst(record);
    }

    public LinkedVehicleList present() {
        return present;
    }

    public List<Vehicle> vehicles(String filter) {
        return vehicles.findAll(filter);
    }

    public List<ParkingServiceRecord> serviceRecords(String filter) {
        return services.findAll(filter);
    }

    public List<Shift> shiftRecords(String filter) {
        return shifts.findAll(filter);
    }

    public List<Account> accounts() {
        return accounts.findAll();
    }

    public List<Subscription> subscriptions(String filter) {
        return subscriptions.findCurrent(filter);
    }

    public Rates rates() {
        return rates.get();
    }

    public Capacity capacity() {
        return capacity;
    }

    // ------------------------------------------------------------------
    // Turnos, entradas y salidas
    // ------------------------------------------------------------------

    public Shift openShift(Account account) {
        return shifts.findOpen(account.id()).orElseGet(() -> shifts.open(account.id(), account.parkerName()));
    }

    public ParkingServiceRecord enter(String plate, VehicleType type, String color, String make,
                                      String owner, String phone, Shift shift) {
        requireOpenShift(shift);
        Vehicle vehicle = new Vehicle(Validators.plate(plate), type,
                Validators.required(color, "Color"), Validators.required(make, "Marca"),
                Validators.required(owner, "Propietario"), Validators.phone(phone));
        ParkingServiceRecord record = services.registerEntry(vehicle, LocalDateTime.now());
        present.addFirst(record);
        return record;
    }

    /** Registra la salida; el importe suma al turno de quien cobra ({@code shift}). */
    public ExitSummary exit(String plate, Shift shift) {
        requireOpenShift(shift);
        String normalized = Validators.plate(plate);
        ParkingServiceRecord active = present.find(normalized);
        if (active == null) throw new ParkingException("No se encontró un vehículo presente con esa chapa.");
        LocalDateTime now = LocalDateTime.now();
        Rates currentRates = rates.get();
        Set<YearMonth> covered = subscriptions.coveredMonths(normalized,
                YearMonth.from(active.entryAt()), YearMonth.from(now));
        Charge charge = ParkingCalculator.charge(active.entryAt(), now, currentRates,
                active.vehicle().type(), covered);
        double hourly = currentRates.hourlyFor(active.vehicle().type());
        ParkingServiceRecord closed = services.registerExit(normalized, now, hourly, charge.amount(), shift.id());
        present.remove(normalized);
        Path receipt = null;
        try {
            receipt = ReportWriter.service(closed);
        } catch (ParkingException e) {
            // La salida ya quedó registrada; solo falló el archivo del recibo.
        }
        return new ExitSummary(closed, charge, receipt);
    }

    public ShiftSummary closeShift(Shift shift) {
        return shifts.close(shift.id(), LocalDateTime.now());
    }

    private void requireOpenShift(Shift shift) {
        if (shift == null || !shifts.isOpen(shift.id())) {
            throw new ParkingException("Su turno está cerrado: no puede registrar entradas ni salidas.");
        }
    }

    public int currentCarOccupancy() {
        return present.count(VehicleType.AUTO) + present.count(VehicleType.TRICICLO);
    }

    public int currentMotorcycleOccupancy() {
        return present.count(VehicleType.MOTO);
    }

    // ------------------------------------------------------------------
    // Cuadre del día
    // ------------------------------------------------------------------

    /** Suma los totales de los turnos cerrados de hoy y guarda el archivo del cuadre. */
    public DayClosure closeDay() {
        LocalDate today = LocalDate.now();
        List<Shift> todayShifts = shifts.findDay(today);
        double total = 0;
        int open = 0;
        for (Shift shift : todayShifts) {
            if (shift.endedAt() == null) open++;
            else total += shift.total();
        }
        int serviceCount = shifts.serviceCountOfClosedShiftsOn(today);
        Path file = ReportWriter.daily(today, todayShifts, total, serviceCount);
        dailyCloses.save(today, total);
        return new DayClosure(file, todayShifts.size(), open, serviceCount, total);
    }

    public boolean isTodayClosed() {
        return dailyCloses.isClosed(LocalDate.now());
    }

    // ------------------------------------------------------------------
    // Cuentas, tarifas, capacidad y abonos
    // ------------------------------------------------------------------

    public void createAccount(String name, String username, String password, Role role) {
        accounts.create(Validators.required(name, "Nombre del parqueador"),
                Validators.required(username, "Nombre de usuario"),
                PasswordHasher.hash(Validators.required(password, "Contraseña").toCharArray()), role);
    }

    public void deleteAccount(Account account) {
        accounts.delete(account.id());
    }

    public Account changePassword(Account account, String current, String next) {
        if (!PasswordHasher.verify(current.toCharArray(), account.passwordHash())) {
            throw new ParkingException("La contraseña actual es incorrecta.");
        }
        if (next == null || next.trim().length() < 4) {
            throw new ParkingException("La nueva contraseña debe tener al menos cuatro caracteres.");
        }
        String hash = PasswordHasher.hash(next.toCharArray());
        accounts.changePassword(account.id(), hash);
        return new Account(account.id(), account.parkerName(), account.username(), hash, account.role());
    }

    public void updateRates(double car, double moto, double night) {
        if (car < 0 || moto < 0 || night < 0) throw new ParkingException("Las tarifas no pueden ser negativas.");
        Rates updated = new Rates(car, moto, night);
        rates.save(updated);
        ReportWriter.rates(car, moto, night);
    }

    /** La capacidad nueva no puede ser menor que los vehículos que ya están dentro. */
    public void updateCapacity(int cars, int motorcycles) {
        if (cars < 0 || motorcycles < 0) throw new ParkingException("La capacidad no puede ser negativa.");
        int carsInside = currentCarOccupancy();
        int motosInside = currentMotorcycleOccupancy();
        if (cars < carsInside) {
            throw new ParkingException("La capacidad de autos y triciclos no puede ser menor que los "
                    + carsInside + " vehículos que están dentro ahora.");
        }
        if (motorcycles < motosInside) {
            throw new ParkingException("La capacidad de motos no puede ser menor que las "
                    + motosInside + " motos que están dentro ahora.");
        }
        Capacity updated = new Capacity(cars, motorcycles);
        capacities.save(updated);
        capacity = updated;
    }

    public void createSubscription(String plate, int month, int year, double amount) {
        subscriptions.create(Validators.plate(plate), month, year, amount);
    }

    public void deleteSubscription(long id) {
        subscriptions.delete(id);
    }
}
