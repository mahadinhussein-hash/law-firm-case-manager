package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseFactoryTest {

    private final Client client = new Client(1, "Anna Kovacs", "anna@example.com", "123");
    private final Lawyer lawyer = new Lawyer(1, "Peter Nagy", 100.0);
    private final LocalDate deadline = LocalDate.of(2026, 12, 31);

    private LegalCase create(String type) {
        return CaseFactory.create(type, 1, "Title", client, "Other Party", deadline, lawyer, "detail");
    }

    @Test
    void createsTheRightSubclass() {
        assertInstanceOf(CriminalCase.class, create("criminal"));
        assertInstanceOf(FamilyCase.class, create("family"));
        assertInstanceOf(CorporateCase.class, create("corporate"));
    }

    @Test
    void typeIsNotCaseSensitive() {
        assertInstanceOf(FamilyCase.class, create(" Family "));
    }

    @Test
    void unknownTypeIsRefused() {
        assertThrows(ValidationException.class, () -> create("tax"));
        assertThrows(ValidationException.class, () -> create(null));
    }
}
