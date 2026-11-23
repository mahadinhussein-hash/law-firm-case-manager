package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.json.JsonFactory;

public class Lawyer extends Person {

    public static final int MAX_ACTIVE_CASES = 5;

    private double hourlyRate;

    public Lawyer(int id, String name, double hourlyRate) {
        super(id, name);
        setHourlyRate(hourlyRate);
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate <= 0) {
            throw new ValidationException("Hourly rate must be greater than 0.");
        }
        this.hourlyRate = hourlyRate;
    }

    /** Gson reads the fields (id, name, hourlyRate) itself. */
    public JsonObject toJson() {
        return JsonFactory.create().toJsonTree(this).getAsJsonObject();
    }

    @Override
    public String toString() {
        return String.format("Lawyer #%d: %s (%.2f EUR/hour)", getId(), getName(), hourlyRate);
    }
}
