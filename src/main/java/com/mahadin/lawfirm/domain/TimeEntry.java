package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.json.JsonFactory;

import java.time.LocalDate;

public class TimeEntry {

    private final LocalDate date;
    private final double hours;
    private final String description;

    public TimeEntry(LocalDate date, double hours, String description) {
        if (date == null) {
            throw new ValidationException("Date is required.");
        }
        if (hours <= 0 || hours > 20) {
            throw new ValidationException("Hours must be between 0 and 20.");
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

    /** Gson reads the fields (date, hours, description) itself. */
    public JsonObject toJson() {
        return JsonFactory.create().toJsonTree(this).getAsJsonObject();
    }

    @Override
    public String toString() {
        return String.format("%s: %.1f h - %s", date, hours, description);
    }
}
