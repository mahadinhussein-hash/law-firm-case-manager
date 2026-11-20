package com.mahadin.lawfirm.persistence;

import com.mahadin.lawfirm.exception.ValidationException;

import java.util.Locale;

/**
 * Factory for repositories. The caller says which storage format it wants
 * (for now only "json") and gets a repository that reads and writes that format.
 * A new format is added here with one more case.
 */
public final class RepositoryFactory {

    public static final String JSON_FILE = "data/lawfirm-data.json";

    private RepositoryFactory() {
    }

    public static FirmRepository create(String format) {
        String key = format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "json" -> new JsonFirmRepository(JSON_FILE);
            default -> throw new ValidationException("Unknown storage format: " + format);
        };
    }
}
