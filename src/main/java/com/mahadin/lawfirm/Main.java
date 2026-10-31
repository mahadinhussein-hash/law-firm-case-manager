package com.mahadin.lawfirm;

import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.CaseLoadExceededException;
import com.mahadin.lawfirm.exception.ConflictOfInterestException;
import com.mahadin.lawfirm.service.LawFirmService;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Temporary demo of US1 + US2. Replaced by the console menu later.
        LawFirmService firm = new LawFirmService();

        Client anna = firm.addClient("Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Client bob = firm.addClient("Bob Szabo", "bob@example.com", "+36 20 555 1234");
        Lawyer peter = firm.addLawyer("Peter Nagy", 120.0);
        Lawyer eva = firm.addLawyer("Eva Toth", 150.0);

        try {
            firm.openCriminalCase("Theft accusation", anna, "State of Hungary",
                    LocalDate.of(2026, 12, 1), peter, "Theft");
            firm.openFamilyCase("Custody dispute", bob, "Clara Szabo",
                    LocalDate.of(2027, 1, 15), eva, "Custody");
            firm.openCorporateCase("Contract breach", anna, "Acme Ltd",
                    LocalDate.of(2027, 3, 10), eva, "Kovacs Bakery Kft");
        } catch (CaseLoadExceededException | ConflictOfInterestException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println("All cases:");
        for (LegalCase legalCase : firm.getCases()) {
            System.out.println("  " + legalCase + " (rate x" + legalCase.getRateMultiplier() + ")");
        }

        // Rule 1: conflict of interest - Peter represents Anna, so he cannot act against her.
        try {
            firm.openFamilyCase("Divorce", bob, "Anna Kovacs", LocalDate.of(2027, 2, 1), peter, "Divorce");
        } catch (CaseLoadExceededException | ConflictOfInterestException e) {
            System.out.println("Refused: " + e.getMessage());
        }

        // Rule 2: case limit - give Eva cases until she reaches the maximum of 5.
        try {
            for (int i = 1; i <= 4; i++) {
                firm.openCorporateCase("Audit " + i, bob, "Company " + i,
                        LocalDate.of(2027, 4, i), eva, "Szabo Holding");
            }
        } catch (CaseLoadExceededException | ConflictOfInterestException e) {
            System.out.println("Refused: " + e.getMessage());
        }
        System.out.println("Eva's active cases: " + firm.countActiveCases(eva));
    }
}
