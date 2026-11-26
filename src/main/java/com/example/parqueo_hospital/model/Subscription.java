package com.example.parqueo_hospital.model;

public record Subscription(long id, String plate, int month, int year, double amount) {
    public String monthName() {
        return java.time.Month.of(month).getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("es"));
    }
}
