package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

import java.io.Serializable;
import java.time.LocalDate;

public class TimeEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final double hours;
    private final String description;

    public TimeEntry(LocalDate date, double hours, String description) {
        if (date == null) {
            throw new ValidationException("Date is required.");
        }
        if (hours <= 0 || hours > 24) {
            throw new ValidationException("Hours must be between 0 and 24.");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Description cannot be empty.");
        }
        this.date = date;
        this.hours = hours;
        this.description = description.trim();
    }

    public LocalDate getDate() {
        return date;
    }

    public double getHours() {
        return hours;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s: %.1f h - %s", date, hours, description);
    }
}
