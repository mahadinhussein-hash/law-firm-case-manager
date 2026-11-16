package com.mahadin.lawfirm.persistence;

import com.mahadin.lawfirm.exception.ValidationException;

import java.util.Locale;

/**
 * Factory for repositories. The caller says which storage format it wants
 * ("json" or "ser") and gets a repository that reads and writes that format.
 */
public final class RepositoryFactory {

    public static final String JSON_FILE = "data/lawfirm-data.json";
    public static final String SERIALIZED_FILE = "data/lawfirm-data.ser";

    private RepositoryFactory() {
    }

    public static FirmRepository create(String format) {
        String key = format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "json" -> new JsonFirmRepository(JSON_FILE);
            case "ser" -> new DataStore(SERIALIZED_FILE);
            default -> throw new ValidationException("Unknown storage format: " + format);
        };
    }
}
