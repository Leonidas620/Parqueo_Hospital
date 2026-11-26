package com.example.parqueo_hospital.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Shift(long id, LocalDate date, String parkerName,
                    LocalDateTime startedAt, LocalDateTime endedAt, double total) {
}
