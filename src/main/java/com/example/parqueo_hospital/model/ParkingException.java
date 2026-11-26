package com.example.parqueo_hospital.model;

public class ParkingException extends RuntimeException {
    public ParkingException(String message) {
        super(message);
    }

    public ParkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
