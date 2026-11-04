package com.mahadin.lawfirm.service;

import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.CaseLoadExceededException;
import com.mahadin.lawfirm.exception.ConflictOfInterestException;
import com.mahadin.lawfirm.exception.LawFirmException;
import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LawFirmServiceTest {

    private LawFirmService service;
    private Client anna;
    private Lawyer peter;

    @BeforeEach
    void setUp() {
        service = new LawFirmService();
        anna = service.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        peter = service.addLawyer("Peter Nagy", 100.0);
    }

    private LegalCase openFamilyCase(String title, LocalDate deadline) throws LawFirmException {
        return service.openFamilyCase(title, anna, "Someone Else", deadline, peter, "custody");
    }

    @Test
    void idsAreGivenOutInOrder() {
        Client second = service.addClient("Bela Szabo", "bela@example.com", "123");

        assertEquals(1, anna.getId());
        assertEquals(2, second.getId());
    }

    @Test
    void lawyerCannotTakeMoreThanFiveActiveCases() throws LawFirmException {
        for (int i = 1; i <= Lawyer.MAX_ACTIVE_CASES; i++) {
            openFamilyCase("Case " + i, LocalDate.of(2026, 12, i));
        }

        assertEquals(5, service.countActiveCases(peter));
        assertThrows(CaseLoadExceededException.class, () ->
                openFamilyCase("One too many", LocalDate.of(2026, 12, 20)));
    }

    @Test
    void closingCaseFreesASlot() throws LawFirmException {
        LegalCase first = openFamilyCase("Case 1", LocalDate.of(2026, 12, 1));
        for (int i = 2; i <= Lawyer.MAX_ACTIVE_CASES; i++) {
            openFamilyCase("Case " + i, LocalDate.of(2026, 12, i));
        }

        service.closeCase(first, CaseStatus.SETTLED);

        assertDoesNotThrow(() -> openFamilyCase("New case", LocalDate.of(2026, 12, 20)));
    }

    @Test
    void lawyerCannotActAgainstOwnClient() throws LawFirmException {
        Client bela = service.addClient("Bela Szabo", "bela@example.com", "123");
        openFamilyCase("Custody", LocalDate.of(2026, 12, 1));

        assertThrows(ConflictOfInterestException.class, () ->
                service.openCorporateCase("Contract dispute", bela, "anna kovacs",
                        LocalDate.of(2026, 12, 15), peter, "Szabo Kft"));
    }

    @Test
    void otherLawyerCanTakeTheCaseInstead() throws LawFirmException {
        Client bela = service.addClient("Bela Szabo", "bela@example.com", "123");
        Lawyer eva = service.addLawyer("Eva Toth", 120.0);
        openFamilyCase("Custody", LocalDate.of(2026, 12, 1));

        assertDoesNotThrow(() -> service.openCorporateCase("Contract dispute", bela, "Anna Kovacs",
                LocalDate.of(2026, 12, 15), eva, "Szabo Kft"));
    }

    @Test
    void findingUnknownIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.findClient(99));
        assertThrows(ValidationException.class, () -> service.findLawyer(99));
        assertThrows(ValidationException.class, () -> service.findCase(99));
    }

    @Test
    void casesCanBeSortedByDeadline() throws LawFirmException {
        openFamilyCase("Late", LocalDate.of(2027, 3, 1));
        openFamilyCase("Early", LocalDate.of(2026, 11, 20));
        openFamilyCase("Middle", LocalDate.of(2027, 1, 10));

        List<LegalCase> sorted = service.getCasesSorted(Comparator.comparing(legalCase -> legalCase.getDeadline()));

        assertEquals("Early", sorted.get(0).getTitle());
        assertEquals("Middle", sorted.get(1).getTitle());
        assertEquals("Late", sorted.get(2).getTitle());
    }

    @Test
    void returnedListsCannotBeChangedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> service.getClients().clear());
    }
}
