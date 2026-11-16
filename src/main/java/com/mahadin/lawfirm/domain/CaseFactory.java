package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Factory for legal cases. The caller says which type it wants ("criminal",
 * "family" or "corporate") and gets the matching subclass back, so the choice
 * of subclass is written in one place only.
 */
public final class CaseFactory {

    private CaseFactory() {
    }

    /**
     * @param detail the type-specific value: the charge (criminal),
     *               the matter type (family) or the company name (corporate)
     */
    public static LegalCase create(String type, int id, String title, Client client,
                                   String opposingParty, LocalDate deadline,
                                   Lawyer leadLawyer, String detail) {
        String key = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "criminal" -> new CriminalCase(id, title, client, opposingParty, deadline, leadLawyer, detail);
            case "family" -> new FamilyCase(id, title, client, opposingParty, deadline, leadLawyer, detail);
            case "corporate" -> new CorporateCase(id, title, client, opposingParty, deadline, leadLawyer, detail);
            default -> throw new ValidationException("Unknown case type.");
        };
    }
}
