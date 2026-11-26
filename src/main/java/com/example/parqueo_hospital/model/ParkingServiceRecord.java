package com.example.parqueo_hospital.model;

import java.time.LocalDateTime;

public record ParkingServiceRecord(long id, Vehicle vehicle, LocalDateTime entryAt,
                                  LocalDateTime exitAt, double hourlyRate, double amount,
                                  String parkerName, long shiftId) {
}
