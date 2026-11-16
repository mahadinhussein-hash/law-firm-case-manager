package com.mahadin.lawfirm.persistence;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mahadin.lawfirm.billing.BillingFactory;
import com.mahadin.lawfirm.billing.BillingStrategy;
import com.mahadin.lawfirm.billing.ContingencyBilling;
import com.mahadin.lawfirm.billing.FlatFeeBilling;
import com.mahadin.lawfirm.domain.CaseFactory;
import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.CorporateCase;
import com.mahadin.lawfirm.domain.CriminalCase;
import com.mahadin.lawfirm.domain.FamilyCase;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.domain.TimeEntry;
import com.mahadin.lawfirm.exception.ValidationException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the firm's data as a readable JSON file.
 * The domain classes know nothing about JSON: this class converts them to and
 * from JSON by hand, and rebuilds them through their constructors and the
 * factories, so every rule is checked again when a file is loaded.
 */
public class JsonFirmRepository implements FirmRepository {

    private final String filePath;

    public JsonFirmRepository(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ValidationException("File path cannot be empty.");
        }
        this.filePath = filePath;
    }

    @Override
    public void save(FirmData data) throws IOException {
        File file = new File(filePath);
        File folder = file.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        JsonObject root = new JsonObject();
        root.add("clients", clientsToJson(data.getClients()));
        root.add("lawyers", lawyersToJson(data.getLawyers()));
        root.add("cases", casesToJson(data.getCases()));
        try (Writer out = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root, out);
        }
    }

    @Override
    public FirmData load() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new FirmData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(in).getAsJsonObject();

            Map<Integer, Client> clients = new HashMap<>();
            List<Client> clientList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("clients")) {
                JsonObject o = element.getAsJsonObject();
                Client client = new Client(o.get("id").getAsInt(), o.get("name").getAsString(),
                        o.get("email").getAsString(), o.get("phone").getAsString());
                clients.put(client.getId(), client);
                clientList.add(client);
            }

            Map<Integer, Lawyer> lawyers = new HashMap<>();
            List<Lawyer> lawyerList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("lawyers")) {
                JsonObject o = element.getAsJsonObject();
                Lawyer lawyer = new Lawyer(o.get("id").getAsInt(), o.get("name").getAsString(),
                        o.get("hourlyRate").getAsDouble());
                lawyers.put(lawyer.getId(), lawyer);
                lawyerList.add(lawyer);
            }

            List<LegalCase> caseList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("cases")) {
                caseList.add(caseFromJson(element.getAsJsonObject(), clients, lawyers));
            }
            return new FirmData(clientList, lawyerList, caseList);
        } catch (RuntimeException e) {
            // bad JSON, a missing field or a value the domain classes refuse
            throw new IOException("The save file is not in the expected format.", e);
        }
    }

    // ---------- object -> JSON ----------

    private JsonArray clientsToJson(List<Client> clients) {
        JsonArray array = new JsonArray();
        for (Client client : clients) {
            JsonObject o = new JsonObject();
            o.addProperty("id", client.getId());
            o.addProperty("name", client.getName());
            o.addProperty("email", client.getEmail());
            o.addProperty("phone", client.getPhone());
            array.add(o);
        }
        return array;
    }

    private JsonArray lawyersToJson(List<Lawyer> lawyers) {
        JsonArray array = new JsonArray();
        for (Lawyer lawyer : lawyers) {
            JsonObject o = new JsonObject();
            o.addProperty("id", lawyer.getId());
            o.addProperty("name", lawyer.getName());
            o.addProperty("hourlyRate", lawyer.getHourlyRate());
            array.add(o);
        }
        return array;
    }

    private JsonArray casesToJson(List<LegalCase> cases) {
        JsonArray array = new JsonArray();
        for (LegalCase legalCase : cases) {
            JsonObject o = new JsonObject();
            o.addProperty("id", legalCase.getId());
            o.addProperty("type", legalCase.getCaseType().toLowerCase());
            o.addProperty("title", legalCase.getTitle());
            o.addProperty("clientId", legalCase.getClient().getId());
            o.addProperty("opposingParty", legalCase.getOpposingParty());
            o.addProperty("deadline", legalCase.getDeadline().toString());
            o.addProperty("leadLawyerId", legalCase.getLeadLawyer().getId());
            o.addProperty("detail", detailOf(legalCase));
            o.addProperty("status", legalCase.getStatus().name());
            o.add("billing", billingToJson(legalCase.getBillingStrategy()));
            JsonArray entries = new JsonArray();
            for (TimeEntry entry : legalCase.getTimeEntries()) {
                JsonObject e = new JsonObject();
                e.addProperty("date", entry.getDate().toString());
                e.addProperty("hours", entry.getHours());
                e.addProperty("description", entry.getDescription());
                entries.add(e);
            }
            o.add("timeEntries", entries);
            array.add(o);
        }
        return array;
    }

    // The type-specific value of a case. It is read here, not through a method on
    // LegalCase, so the domain classes stay free of storage code.
    private String detailOf(LegalCase legalCase) {
        if (legalCase instanceof CriminalCase criminal) {
            return criminal.getCharge();
        }
        if (legalCase instanceof FamilyCase family) {
            return family.getMatterType();
        }
        if (legalCase instanceof CorporateCase corporate) {
            return corporate.getCompanyName();
        }
        throw new IllegalStateException("Unknown case type: " + legalCase.getCaseType());
    }

    private JsonObject billingToJson(BillingStrategy strategy) {
        JsonObject o = new JsonObject();
        if (strategy instanceof FlatFeeBilling flat) {
            o.addProperty("method", "flat");
            o.addProperty("amount", flat.getAmount());
        } else if (strategy instanceof ContingencyBilling contingency) {
            o.addProperty("method", "contingency");
            o.addProperty("percentage", contingency.getPercentage());
            o.addProperty("settlement", contingency.getSettlementAmount());
        } else {
            o.addProperty("method", "hourly");
        }
        return o;
    }

    // ---------- JSON -> object ----------

    private LegalCase caseFromJson(JsonObject o, Map<Integer, Client> clients, Map<Integer, Lawyer> lawyers) {
        Client client = clients.get(o.get("clientId").getAsInt());
        Lawyer lawyer = lawyers.get(o.get("leadLawyerId").getAsInt());
        if (client == null || lawyer == null) {
            throw new ValidationException("A case refers to a client or lawyer that does not exist.");
        }
        LegalCase legalCase = CaseFactory.create(o.get("type").getAsString(), o.get("id").getAsInt(),
                o.get("title").getAsString(), client, o.get("opposingParty").getAsString(),
                LocalDate.parse(o.get("deadline").getAsString()), lawyer, o.get("detail").getAsString());

        // hours are added while the case is still open, the status is set last
        for (JsonElement element : o.getAsJsonArray("timeEntries")) {
            JsonObject e = element.getAsJsonObject();
            legalCase.addTimeEntry(new TimeEntry(LocalDate.parse(e.get("date").getAsString()),
                    e.get("hours").getAsDouble(), e.get("description").getAsString()));
        }
        JsonObject billing = o.getAsJsonObject("billing");
        legalCase.setBillingStrategy(BillingFactory.create(billing.get("method").getAsString(),
                number(billing, "amount"), number(billing, "percentage"), number(billing, "settlement")));
        CaseStatus status = CaseStatus.valueOf(o.get("status").getAsString());
        if (status != CaseStatus.OPEN) {
            legalCase.close(status);
        }
        return legalCase;
    }

    private double number(JsonObject o, String name) {
        return o.has(name) ? o.get(name).getAsDouble() : 0;
    }
}
