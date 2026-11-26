package com.example.parqueo_hospital.structures;

import com.example.parqueo_hospital.model.ParkingServiceRecord;

public final class VehicleNode {
    private final ParkingServiceRecord service;
    private VehicleNode next;

    VehicleNode(ParkingServiceRecord service, VehicleNode next) {
        this.service = service;
        this.next = next;
    }

    public ParkingServiceRecord service() {
        return service;
    }

    public VehicleNode next() {
        return next;
    }

    void setNext(VehicleNode next) {
        this.next = next;
    }
}
