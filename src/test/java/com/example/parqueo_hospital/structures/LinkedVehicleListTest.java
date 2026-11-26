package com.example.parqueo_hospital.structures;

import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.model.Vehicle;
import com.example.parqueo_hospital.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LinkedVehicleListTest {
    private ParkingServiceRecord service(String plate, VehicleType type, int hoursAgo) {
        return new ParkingServiceRecord(1, new Vehicle(plate, type, "Negro", "Marca", "Dueño", "55555555"),
                LocalDateTime.now().minusHours(hoursAgo), null, 0, 0, "Parqueador", 1);
    }

    @Test
    void emptyListOperationsAreSafe() {
        LinkedVehicleList list = new LinkedVehicleList();
        assertEquals(0, list.size());
        assertNull(list.find("P123456"));
        assertNull(list.remove("P123456"));
        assertEquals(0, list.over24Hours(LocalDateTime.now()).size());
    }

    @Test
    void oneElementCanBeFoundCountedRemovedAndFiltered() {
        LinkedVehicleList list = new LinkedVehicleList();
        ParkingServiceRecord record = service("P123456", VehicleType.AUTO, 25);
        list.addFirst(record);
        assertEquals(record, list.find("p123456"));
        assertEquals(1, list.count(VehicleType.AUTO));
        assertEquals(1, list.over24Hours(LocalDateTime.now()).size());
        assertEquals(record, list.remove("P123456"));
        assertEquals(0, list.size());
        assertNull(list.head());
    }

    @Test
    void removesHeadMiddleAndTailAndRejectsDuplicate() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 1));
        list.addFirst(service("B000002", VehicleType.MOTO, 1));
        list.addFirst(service("F000003", VehicleType.TRICICLO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> list.addFirst(service("p000001", VehicleType.AUTO, 1)));
        assertNotNull(list.remove("F000003"));
        assertNotNull(list.remove("P000001"));
        assertNotNull(list.remove("B000002"));
        assertEquals(0, list.size());
    }

    @Test
    void countsTricyclesAsCarSpaceWithoutCountingThemAsCars() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 1));
        list.addFirst(service("B000002", VehicleType.TRICICLO, 1));
        list.addFirst(service("F000003", VehicleType.MOTO, 1));
        assertEquals(1, list.count(VehicleType.AUTO));
        assertEquals(1, list.count(VehicleType.TRICICLO));
        assertEquals(1, list.count(VehicleType.MOTO));
    }

    @Test
    void newEntriesGoFirstSoTheListIsInReverseArrivalOrder() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 3));
        list.addFirst(service("B000002", VehicleType.MOTO, 2));
        list.addFirst(service("F000003", VehicleType.TRICICLO, 1));
        assertEquals("F000003", list.head().service().vehicle().plate());
        assertEquals("B000002", list.head().next().service().vehicle().plate());
        assertEquals("P000001", list.head().next().next().service().vehicle().plate());
        assertNull(list.head().next().next().next());
    }

    /** Ejemplo de la orden: el nodo de la tercera posición se elimina y el segundo pasa a apuntar al cuarto. */
    @Test
    void removingTheThirdNodeLinksTheSecondToTheFourth() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("W000001", VehicleType.AUTO, 4));
        list.addFirst(service("P123456", VehicleType.AUTO, 3));
        list.addFirst(service("B000002", VehicleType.MOTO, 2));
        list.addFirst(service("F000003", VehicleType.TRICICLO, 1));
        // Cabeza -> F000003 -> B000002 -> P123456 -> W000001
        VehicleNode second = list.head().next();
        assertEquals("P123456", second.next().service().vehicle().plate());

        assertEquals("P123456", list.remove("P123456").vehicle().plate());

        assertEquals("W000001", second.next().service().vehicle().plate());
        assertEquals(3, list.size());
        assertNull(list.find("P123456"));
    }

    @Test
    void removingTheLastNodeLeavesItsPredecessorAsTheEnd() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 2));
        list.addFirst(service("B000002", VehicleType.MOTO, 1));
        assertNotNull(list.remove("P000001"));
        assertNull(list.head().next());
        assertEquals(1, list.size());
    }

    @Test
    void removingTheHeadMakesTheNextNodeTheNewHead() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 2));
        list.addFirst(service("B000002", VehicleType.MOTO, 1));
        assertNotNull(list.remove("B000002"));
        assertEquals("P000001", list.head().service().vehicle().plate());
    }

    @Test
    void over24HoursKeepsOnlyStaysOfAtLeast24HoursAndDoesNotModifyTheList() {
        LinkedVehicleList list = new LinkedVehicleList();
        list.addFirst(service("P000001", VehicleType.AUTO, 30));
        list.addFirst(service("B000002", VehicleType.MOTO, 5));
        list.addFirst(service("F000003", VehicleType.TRICICLO, 26));
        LinkedVehicleList old = list.over24Hours(LocalDateTime.now());
        assertEquals(2, old.size());
        assertNull(old.find("B000002"));
        assertEquals(3, list.size());
    }
}
