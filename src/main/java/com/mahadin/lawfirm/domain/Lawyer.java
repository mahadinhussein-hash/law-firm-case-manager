package com.mahadin.lawfirm.domain;

import java.io.Serializable;
import java.util.Objects;

public class Lawyer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_ACTIVE_CASES = 5;

    private final int id;
    private final String name;
    private double hourlyRate;

    public Lawyer(int id, String name, double hourlyRate) {
        if (id <= 0) {
            throw new IllegalArgumentException("Lawyer id must be positive.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Lawyer name cannot be empty.");
        }
        this.id = id;
        this.name = name.trim();
        setHourlyRate(hourlyRate);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("Hourly rate must be greater than 0.");
        }
        this.hourlyRate = hourlyRate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Lawyer)) {
            return false;
        }
        Lawyer lawyer = (Lawyer) other;
        return id == lawyer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Lawyer #%d: %s (%.2f EUR/hour)", id, name, hourlyRate);
    }
}
