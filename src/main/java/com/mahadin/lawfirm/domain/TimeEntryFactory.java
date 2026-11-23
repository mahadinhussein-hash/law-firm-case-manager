package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.json.JsonFactory;

import java.time.LocalDate;

/** Factory for time entries. It creates a TimeEntry from the JSON written by TimeEntry.toJson(). */
public final class TimeEntryFactory {

    private TimeEntryFactory() {
    }

    private record Data(LocalDate date, double hours, String description) {
    }

    public static TimeEntry fromJson(JsonObject o) {
        Data data = JsonFactory.create().fromJson(o, Data.class);
        return new TimeEntry(data.date(), data.hours(), data.description());
    }
}
