package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.exception.ValidationException;

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

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("id", getId());
        o.addProperty("name", getName());
        o.addProperty("hourlyRate", hourlyRate);
        return o;
    }

    public static Lawyer fromJson(JsonObject o) {
        return new Lawyer(o.get("id").getAsInt(), o.get("name").getAsString(), o.get("hourlyRate").getAsDouble());
    }

    @Override
    public String toString() {
        return String.format("Lawyer #%d: %s (%.2f EUR/hour)", getId(), getName(), hourlyRate);
    }
}
