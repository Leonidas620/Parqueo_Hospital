package com.example.parqueo_hospital.structures;

import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.model.VehicleType;

import java.time.LocalDateTime;

public final class LinkedVehicleList {
    private VehicleNode head;
    private int size;

    public void addFirst(ParkingServiceRecord service) {
        if (find(service.vehicle().plate()) != null) {
            throw new IllegalArgumentException("La chapa ya está en la lista de vehículos presentes.");
        }
        head = new VehicleNode(service, head);
        size++;
    }

    public ParkingServiceRecord remove(String plate) {
        VehicleNode previous = null;
        VehicleNode current = head;
        while (current != null) {
            if (current.service().vehicle().plate().equalsIgnoreCase(plate.trim())) {
                if (previous == null) head = current.next();
                else previous.setNext(current.next());
                size--;
                return current.service();
            }
            previous = current;
            current = current.next();
        }
        return null;
    }

    public ParkingServiceRecord find(String plate) {
        VehicleNode current = head;
        while (current != null) {
            if (current.service().vehicle().plate().equalsIgnoreCase(plate.trim())) {
                return current.service();
            }
            current = current.next();
        }
        return null;
    }

    public int count(VehicleType type) {
        int count = 0;
        VehicleNode current = head;
        while (current != null) {
            if (current.service().vehicle().type() == type) count++;
            current = current.next();
        }
        return count;
    }

    public int size() {
        return size;
    }

    public VehicleNode head() {
        return head;
    }

    public LinkedVehicleList over24Hours(LocalDateTime now) {
        int matches = 0;
        VehicleNode current = head;
        while (current != null) {
            if (!current.service().entryAt().plusHours(24).isAfter(now)) matches++;
            current = current.next();
        }
        ParkingServiceRecord[] selected = new ParkingServiceRecord[matches];
        int index = 0;
        current = head;
        while (current != null) {
            if (!current.service().entryAt().plusHours(24).isAfter(now)) {
                selected[index++] = current.service();
            }
            current = current.next();
        }
        LinkedVehicleList result = new LinkedVehicleList();
        for (int i = selected.length - 1; i >= 0; i--) {
            result.addFirst(selected[i]);
        }
        return result;
    }
}
