package com.mahadin.lawfirm.persistence;

import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.LawFirmException;
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

class DataStoreTest {

    @TempDir
    Path tempFolder;

    @Test
    void missingFileGivesEmptyData() throws IOException {
        DataStore store = new DataStore(tempFolder.resolve("nothing-here.ser").toString());

        FirmData data = store.load();

        assertTrue(data.getClients().isEmpty());
        assertTrue(data.getLawyers().isEmpty());
        assertTrue(data.getCases().isEmpty());
    }

    @Test
    void savedDataCanBeLoadedAgain() throws IOException, LawFirmException {
        LawFirmService service = new LawFirmService();
        Client anna = service.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Lawyer peter = service.addLawyer("Peter Nagy", 100.0);
        LegalCase legalCase = service.openFamilyCase("Divorce", anna, "Gabor Kovacs",
                LocalDate.of(2026, 12, 31), peter, "divorce");
        service.logHours(legalCase, LocalDate.of(2026, 11, 1), 3, "First meeting");

        DataStore store = new DataStore(tempFolder.resolve("data/firm.ser").toString());
        store.save(service.exportData());

        LawFirmService loaded = new LawFirmService();
        loaded.importData(store.load());

        assertEquals(1, loaded.getClients().size());
        assertEquals(1, loaded.getLawyers().size());
        assertEquals(1, loaded.getCases().size());
        assertEquals("Divorce", loaded.getCases().get(0).getTitle());
        assertEquals(3.0, loaded.getCases().get(0).getTotalHours(), 0.001);
    }

    @Test
    void idsContinueAfterLoading() throws IOException {
        LawFirmService service = new LawFirmService();
        service.addClient("Anna Kovacs", "anna@example.com", "123");
        service.addClient("Bela Szabo", "bela@example.com", "456");

        DataStore store = new DataStore(tempFolder.resolve("firm.ser").toString());
        store.save(service.exportData());

        LawFirmService loaded = new LawFirmService();
        loaded.importData(store.load());
        Client third = loaded.addClient("Csilla Varga", "csilla@example.com", "789");

        assertEquals(3, third.getId());
    }

    @Test
    void brokenFileGivesIOException() throws IOException {
        Path file = tempFolder.resolve("broken.ser");
        Files.writeString(file, "this is not saved firm data");
        DataStore store = new DataStore(file.toString());

        assertThrows(IOException.class, () -> store.load());
    }
}
