package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.CorporateCase;
import com.mahadin.lawfirm.domain.CriminalCase;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.domain.TimeEntry;
import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingStrategyTest {

    private Client client;
    private Lawyer lawyer;

    @BeforeEach
    void setUp() {
        client = new Client(1, "Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        lawyer = new Lawyer(1, "Peter Nagy", 100.0);
    }

    private LegalCase criminalCaseWithTenHours() {
        LegalCase legalCase = new CriminalCase(1, "Theft", client, "State",
                LocalDate.of(2026, 12, 31), lawyer, "theft");
        legalCase.addTimeEntry(new TimeEntry(LocalDate.of(2026, 11, 1), 10, "Court preparation"));
        return legalCase;
    }

    @Test
    void newCaseUsesHourlyBillingByDefault() {
        assertEquals("Hourly", criminalCaseWithTenHours().getBillingStrategy().getName());
    }

    @Test
    void hourlyFeeUsesHoursRateAndCaseMultiplier() {
        // 10 hours * 100 EUR * 1.5 (criminal)
        assertEquals(1500.0, criminalCaseWithTenHours().calculateFee(), 0.001);
    }

    @Test
    void hourlyFeeIsDifferentForCorporateCase() {
        LegalCase corporate = new CorporateCase(2, "Merger", client, "Big Corp",
                LocalDate.of(2026, 12, 31), lawyer, "Small Kft");
        corporate.addTimeEntry(new TimeEntry(LocalDate.of(2026, 11, 1), 10, "Contract review"));

        // 10 hours * 100 EUR * 1.3 (corporate)
        assertEquals(1300.0, corporate.calculateFee(), 0.001);
    }

    @Test
    void flatFeeIgnoresHours() {
        LegalCase legalCase = criminalCaseWithTenHours();
        legalCase.setBillingStrategy(new FlatFeeBilling(800));

        assertEquals(800.0, legalCase.calculateFee(), 0.001);
    }

    @Test
    void contingencyPaysNothingUnlessWon() {
        LegalCase legalCase = criminalCaseWithTenHours();
        legalCase.setBillingStrategy(new ContingencyBilling(30, 10000));

        assertEquals(0.0, legalCase.calculateFee(), 0.001);

        legalCase.close(CaseStatus.LOST);
        assertEquals(0.0, legalCase.calculateFee(), 0.001);
    }

    @Test
    void contingencyPaysPercentageWhenWon() {
        LegalCase legalCase = criminalCaseWithTenHours();
        legalCase.setBillingStrategy(new ContingencyBilling(30, 10000));
        legalCase.close(CaseStatus.WON);

        assertEquals(3000.0, legalCase.calculateFee(), 0.001);
    }

    @Test
    void invalidBillingValuesAreRejected() {
        assertThrows(ValidationException.class, () -> new FlatFeeBilling(0));
        assertThrows(ValidationException.class, () -> new ContingencyBilling(150, 1000));
        assertThrows(ValidationException.class, () -> new ContingencyBilling(30, -1));
    }
}
