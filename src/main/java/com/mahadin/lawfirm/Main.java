package com.mahadin.lawfirm;

import com.mahadin.lawfirm.billing.ContingencyBilling;
import com.mahadin.lawfirm.billing.FlatFeeBilling;
import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.CaseLoadExceededException;
import com.mahadin.lawfirm.exception.ConflictOfInterestException;
import com.mahadin.lawfirm.service.LawFirmService;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Temporary demo of US1-US3. Replaced by the console menu later.
        LawFirmService firm = new LawFirmService();

        Client anna = firm.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Client bob = firm.addClient("Bob Szabo", "bob@example.com", "+36 20 555 1234");
        Lawyer peter = firm.addLawyer("Peter Nagy", 120.0);
        Lawyer eva = firm.addLawyer("Eva Toth", 150.0);

        try {
            LegalCase theft = firm.openCriminalCase("Theft accusation", anna, "State of Hungary",
                    LocalDate.of(2026, 12, 1), peter, "Theft");
            LegalCase custody = firm.openFamilyCase("Custody dispute", bob, "Clara Szabo",
                    LocalDate.of(2027, 1, 15), eva, "Custody");
            LegalCase contract = firm.openCorporateCase("Contract breach", anna, "Acme Ltd",
                    LocalDate.of(2027, 3, 10), eva, "Kovacs Bakery Kft");

            // US3: log hours
            firm.logHours(theft, LocalDate.of(2026, 11, 2), 3.0, "Client meeting");
            firm.logHours(theft, LocalDate.of(2026, 11, 3), 2.5, "Reviewing evidence");
            firm.logHours(custody, LocalDate.of(2026, 11, 2), 4.0, "Court preparation");

            // US3: choose a billing strategy per case (the theft case keeps the default hourly billing)
            firm.chooseBilling(custody, new FlatFeeBilling(800));
            firm.chooseBilling(contract, new ContingencyBilling(20, 50000));

            System.out.println("Fees while all cases are open:");
            for (LegalCase legalCase : firm.getCases()) {
                System.out.println("  " + legalCase);
            }

            // US3: close cases with an outcome
            firm.closeCase(contract, CaseStatus.WON);
            firm.closeCase(custody, CaseStatus.SETTLED);

            System.out.println("Fees after closing two cases:");
            for (LegalCase legalCase : firm.getCases()) {
                System.out.println("  " + legalCase);
            }

            firm.logHours(contract, LocalDate.of(2026, 11, 4), 1.0, "Extra work");
        } catch (CaseLoadExceededException | ConflictOfInterestException e) {
            System.out.println("Refused: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Refused: " + e.getMessage());
        }
    }
}
