package com.mahadin.lawfirm.billing;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.exception.ValidationException;

import java.util.Locale;

/**
 * Factory for billing strategies. The caller says which method it wants
 * ("hourly", "flat" or "contingency") and gets the matching strategy back.
 * Values that a method does not use are ignored.
 */
public final class BillingFactory {

    private BillingFactory() {
    }

    public static BillingStrategy create(String method, double amount, double percentage, double settlement) {
        String key = method == null ? "" : method.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "hourly" -> new HourlyBilling();
            case "flat" -> new FlatFeeBilling(amount);
            case "contingency" -> new ContingencyBilling(percentage, settlement);
            default -> throw new ValidationException("Unknown billing method.");
        };
    }

    /** Builds a strategy from the JSON written by its toJson() method. */
    public static BillingStrategy fromJson(JsonObject o) {
        return create(o.get("method").getAsString(), number(o, "amount"),
                number(o, "percentage"), number(o, "settlement"));
    }

    private static double number(JsonObject o, String name) {
        return o.has(name) ? o.get(name).getAsDouble() : 0;
    }
}
