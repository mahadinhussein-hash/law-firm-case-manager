package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.CaseClosedException;
import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalCaseTest {

    private Client client;
    private Lawyer lawyer;
    private LegalCase legalCase;

    @BeforeEach
    void setUp() {
        client = new Client(1, "Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        lawyer = new Lawyer(1, "Peter Nagy", 100.0);
        legalCase = new FamilyCase(1, "Divorce", client, "Gabor Kovacs",
                LocalDate.of(2026, 12, 31), lawyer, "divorce");
    }

    @Test
    void newCaseIsOpenAndHasNoHours() {
        assertEquals(CaseStatus.OPEN, legalCase.getStatus());
        assertTrue(legalCase.isActive());
        assertEquals(0.0, legalCase.getTotalHours(), 0.001);
    }

    @Test
    void totalHoursAddsUpAllTimeEntries() {
        legalCase.addTimeEntry(new TimeEntry(LocalDate.of(2026, 11, 1), 2.5, "First meeting"));
        legalCase.addTimeEntry(new TimeEntry(LocalDate.of(2026, 11, 2), 1.5, "Paperwork"));

        assertEquals(4.0, legalCase.getTotalHours(), 0.001);
    }

    @Test
    void closedCaseIsNoLongerActive() {
        legalCase.close(CaseStatus.WON);

        assertEquals(CaseStatus.WON, legalCase.getStatus());
        assertFalse(legalCase.isActive());
    }

    @Test
    void cannotLogHoursOnClosedCase() {
        legalCase.close(CaseStatus.LOST);

        assertThrows(CaseClosedException.class, () ->
                legalCase.addTimeEntry(new TimeEntry(LocalDate.of(2026, 11, 3), 1.0, "Too late")));
    }

    @Test
    void cannotCloseCaseTwice() {
        legalCase.close(CaseStatus.SETTLED);

        assertThrows(CaseClosedException.class, () -> legalCase.close(CaseStatus.WON));
    }

    @Test
    void cannotCloseCaseAsOpen() {
        assertThrows(ValidationException.class, () -> legalCase.close(CaseStatus.OPEN));
    }

    @Test
    void eachCaseTypeHasItsOwnMultiplier() {
        LegalCase criminal = new CriminalCase(2, "Theft", client, "State", LocalDate.of(2026, 12, 1), lawyer, "theft");
        LegalCase corporate = new CorporateCase(3, "Merger", client, "Big Corp", LocalDate.of(2026, 12, 1), lawyer, "Small Kft");

        assertEquals(1.5, criminal.getRateMultiplier(), 0.001);
        assertEquals(1.0, legalCase.getRateMultiplier(), 0.001);
        assertEquals(1.3, corporate.getRateMultiplier(), 0.001);
    }
}
