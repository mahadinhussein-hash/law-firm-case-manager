package com.mahadin.lawfirm.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mahadin.lawfirm.billing.BillingFactory;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.json.JsonFactory;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Builds a case from the JSON written by its toJson() method. The case refers
     * to its client and lawyer by id, so the maps of already loaded people are needed.
     */
    public static LegalCase fromJson(JsonObject o, Map<Integer, Client> clients, Map<Integer, Lawyer> lawyers) {
        Data data = JsonFactory.create().fromJson(o, Data.class);
        Client client = clients.get(data.clientId());
        Lawyer lawyer = lawyers.get(data.leadLawyerId());
        if (client == null || lawyer == null) {
            throw new ValidationException("A case refers to a client or lawyer that does not exist.");
        }
        LegalCase legalCase = create(data.type(), data.id(), data.title(), client,
                data.opposingParty(), data.deadline(), lawyer, data.detail());

        // hours are added while the case is still open, the status is set last
        for (JsonElement element : o.getAsJsonArray("timeEntries")) {
            legalCase.addTimeEntry(TimeEntryFactory.fromJson(element.getAsJsonObject()));
        }
        legalCase.setBillingStrategy(BillingFactory.fromJson(o.getAsJsonObject("billing")));
        CaseStatus status = CaseStatus.valueOf(data.status());
        if (status != CaseStatus.OPEN) {
            legalCase.close(status);
        }
        return legalCase;
    }

    // the plain values of a saved case; the client and lawyer are saved as ids
    private record Data(int id, String type, String title, int clientId, String opposingParty,
                        LocalDate deadline, int leadLawyerId, String detail, String status) {
    }
}
