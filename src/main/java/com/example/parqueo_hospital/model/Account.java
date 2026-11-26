package com.example.parqueo_hospital.model;

public record Account(long id, String parkerName, String username, String passwordHash, Role role) {
    @Override
    public String toString() {
        return parkerName + " (" + username + ")";
    }
}
