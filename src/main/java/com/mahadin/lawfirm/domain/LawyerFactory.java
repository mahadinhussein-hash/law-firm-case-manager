package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.json.JsonFactory;

/** Factory for lawyers. It creates a Lawyer from the JSON written by Lawyer.toJson(). */
public final class LawyerFactory {

    private LawyerFactory() {
    }

    private record Data(int id, String name, double hourlyRate) {
    }

    public static Lawyer fromJson(JsonObject o) {
        Data data = JsonFactory.create().fromJson(o, Data.class);
        return new Lawyer(data.id(), data.name(), data.hourlyRate());
    }
}
