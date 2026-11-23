package com.mahadin.lawfirm.persistence;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.billing.BillingFactory;
import com.mahadin.lawfirm.billing.BillingStrategy;
import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.ClientFactory;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LawyerFactory;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.domain.TimeEntry;
import com.mahadin.lawfirm.domain.TimeEntryFactory;
import com.mahadin.lawfirm.exception.LawFirmException;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.service.LawFirmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFirmRepositoryTest {

    @TempDir
    Path tempFolder;

    @Test
    void clientAndBillingCanBeRebuiltFromTheirOwnJson() {
        Client anna = new Client(1, "Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        JsonObject clientJson = anna.toJson();
        assertEquals(anna, ClientFactory.fromJson(clientJson));
        assertEquals("anna@example.com", ClientFactory.fromJson(clientJson).getEmail());

        BillingStrategy flat = BillingFactory.create("flat", 800, 0, 0);
        assertEquals(flat.getName(), BillingFactory.fromJson(flat.toJson()).getName());
    }

    @Test
    void factoriesStillCheckTheRulesWhenLoading() {
        JsonObject noName = new Client(1, "Anna Kovacs", "anna@example.com", "123").toJson();
        noName.addProperty("name", "Anna");
        JsonObject badRate = new Lawyer(1, "Peter Nagy", 100).toJson();
        badRate.addProperty("hourlyRate", -5);

        assertThrows(ValidationException.class, () -> ClientFactory.fromJson(noName));
        assertThrows(ValidationException.class, () -> LawyerFactory.fromJson(badRate));
    }

    @Test
    void lawyerAndTimeEntryCanBeRebuiltFromTheirOwnJson() {
        Lawyer peter = new Lawyer(2, "Peter Nagy", 100);
        TimeEntry entry = new TimeEntry(LocalDate.of(2026, 11, 1), 3, "First meeting");

        assertEquals(100.0, LawyerFactory.fromJson(peter.toJson()).getHourlyRate());
        assertEquals("2026-11-01", entry.toJson().get("date").getAsString());
        assertEquals(LocalDate.of(2026, 11, 1), TimeEntryFactory.fromJson(entry.toJson()).getDate());
    }

    @Test
    void caseJsonHoldsTheTypeSpecificDetail() throws LawFirmException {
        LawFirmService service = new LawFirmService();
        Client anna = service.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Lawyer peter = service.addLawyer("Peter Nagy", 100.0);
        LegalCase criminal = service.openCase("criminal", "Theft", anna, "State",
                LocalDate.of(2027, 1, 15), peter, "theft");

        JsonObject json = criminal.toJson();

        assertEquals("criminal", json.get("type").getAsString());
        assertEquals("theft", json.get("detail").getAsString());
        assertEquals(1, json.get("clientId").getAsInt());
    }

    @Test
    void missingFileGivesEmptyData() throws IOException {
        JsonFirmRepository repository = new JsonFirmRepository(tempFolder.resolve("nothing-here.json").toString());

        FirmData data = repository.load();

        assertTrue(data.getClients().isEmpty());
        assertTrue(data.getLawyers().isEmpty());
        assertTrue(data.getCases().isEmpty());
    }

    @Test
    void savedDataCanBeLoadedAgain() throws IOException, LawFirmException {
        LawFirmService service = new LawFirmService();
        Client anna = service.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Lawyer peter = service.addLawyer("Peter Nagy", 100.0);
        LegalCase family = service.openCase("family", "Divorce", anna, "Gabor Kovacs",
                LocalDate.of(2026, 12, 31), peter, "divorce");
        service.logHours(family, LocalDate.of(2026, 11, 1), 3, "First meeting");
        LegalCase criminal = service.openCase("criminal", "Theft", anna, "State",
                LocalDate.of(2027, 1, 15), peter, "theft");
        service.chooseBilling(criminal, BillingFactory.create("contingency", 0, 20, 5000));
        service.closeCase(criminal, CaseStatus.WON);

        JsonFirmRepository repository = new JsonFirmRepository(tempFolder.resolve("data/firm.json").toString());
        repository.save(service.exportData());

        LawFirmService loaded = new LawFirmService();
        loaded.importData(repository.load());

        assertEquals(1, loaded.getClients().size());
        assertEquals(1, loaded.getLawyers().size());
        assertEquals(2, loaded.getCases().size());
        assertEquals("Divorce", loaded.findCase(1).getTitle());
        assertEquals(3.0, loaded.findCase(1).getTotalHours(), 0.001);
        assertEquals("Family", loaded.findCase(1).getCaseType());
        assertEquals(CaseStatus.WON, loaded.findCase(2).getStatus());
        assertEquals(1000.0, loaded.findCase(2).calculateFee(), 0.001);
    }

    @Test
    void fileIsReadableText() throws IOException {
        LawFirmService service = new LawFirmService();
        service.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Path file = tempFolder.resolve("firm.json");

        new JsonFirmRepository(file.toString()).save(service.exportData());

        String text = Files.readString(file);
        assertTrue(text.contains("\"name\": \"Anna Kovacs\""));
    }

    @Test
    void brokenFileIsReportedAsIoException() throws IOException {
        Path file = tempFolder.resolve("broken.json");
        Files.writeString(file, "this is not json");

        JsonFirmRepository repository = new JsonFirmRepository(file.toString());

        assertThrows(IOException.class, repository::load);
    }

    @Test
    void emptyPathIsRefused() {
        assertThrows(ValidationException.class, () -> new JsonFirmRepository(" "));
    }
}
