package com.mahadin.lawfirm.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;

/**
 * Factory for the Gson object used everywhere in the project. It is created in
 * one place so every class reads and writes JSON the same way: dates are
 * written as text like "2026-11-01" and the file is pretty printed.
 */
public final class JsonFactory {

    private JsonFactory() {
    }

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (date, type, context) -> new JsonPrimitive(date.toString()))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, context) -> LocalDate.parse(json.getAsString()))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }
}
