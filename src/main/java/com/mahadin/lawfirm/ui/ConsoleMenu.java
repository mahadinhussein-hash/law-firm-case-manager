package com.mahadin.lawfirm.ui;

import com.mahadin.lawfirm.billing.ContingencyBilling;
import com.mahadin.lawfirm.billing.FlatFeeBilling;
import com.mahadin.lawfirm.billing.HourlyBilling;
import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.CaseClosedException;
import com.mahadin.lawfirm.exception.CaseLoadExceededException;
import com.mahadin.lawfirm.exception.ConflictOfInterestException;
import com.mahadin.lawfirm.exception.LawFirmException;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.persistence.DataStore;
import com.mahadin.lawfirm.service.LawFirmService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final LawFirmService service;
    private final DataStore dataStore;
    private final Scanner scanner;

    public ConsoleMenu(LawFirmService service, DataStore dataStore, Scanner scanner) {
        this.service = service;
        this.dataStore = dataStore;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("=== Law Firm Case Manager ===");
        loadData();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> addClient();
                    case 2 -> addLawyer();
                    case 3 -> listClientsAndLawyers();
                    case 4 -> openCase();
                    case 5 -> logHours();
                    case 6 -> chooseBilling();
                    case 7 -> closeCase();
                    case 8 -> listCases();
                    case 0 -> running = false;
                    default -> System.out.println("Unknown option, please try again.");
                }
            } catch (ValidationException | CaseClosedException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (LawFirmException e) {
                System.out.println("Refused: " + e.getMessage());
            }
        }
        saveData();
        System.out.println("Goodbye!");
    }

    private void loadData() {
        try {
            service.importData(dataStore.load());
            System.out.println("Loaded " + service.getClients().size() + " clients, "
                    + service.getLawyers().size() + " lawyers and "
                    + service.getCases().size() + " cases.");
        } catch (IOException e) {
            System.out.println("Could not load saved data (" + e.getMessage() + "). Starting with empty data.");
        }
    }

    private void saveData() {
        try {
            dataStore.save(service.exportData());
            System.out.println("Data saved.");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Add client");
        System.out.println("2. Add lawyer");
        System.out.println("3. List clients and lawyers");
        System.out.println("4. Open a case");
        System.out.println("5. Log hours on a case");
        System.out.println("6. Choose billing method for a case");
        System.out.println("7. Close a case");
        System.out.println("8. List cases (sorted)");
        System.out.println("0. Save and exit");
    }

    private void addClient() {
        String name = readText("Name: ");
        String email = readText("Email: ");
        String phone = readText("Phone: ");
        Client client = service.addClient(name, email, phone);
        System.out.println("Added " + client);
    }

    private void addLawyer() {
        String name = readText("Name: ");
        double rate = readDouble("Hourly rate (EUR): ");
        Lawyer lawyer = service.addLawyer(name, rate);
        System.out.println("Added " + lawyer);
    }

    private void listClientsAndLawyers() {
        System.out.println("Clients:");
        for (Client client : service.getClients()) {
            System.out.println("  " + client);
        }
        System.out.println("Lawyers:");
        for (Lawyer lawyer : service.getLawyers()) {
            System.out.println("  " + lawyer + " - active cases: " + service.countActiveCases(lawyer));
        }
    }

    private void openCase() throws CaseLoadExceededException, ConflictOfInterestException {
        System.out.println("Case type: 1 = Criminal, 2 = Family, 3 = Corporate");
        int type = readInt("Type: ");
        if (type < 1 || type > 3) {
            System.out.println("Unknown case type.");
            return;
        }
        String title = readText("Title: ");
        Client client = service.findClient(readInt("Client id: "));
        String opposingParty = readText("Opposing party: ");
        LocalDate deadline = readDate("Deadline (YYYY-MM-DD): ");
        Lawyer lawyer = service.findLawyer(readInt("Lead lawyer id: "));

        LegalCase legalCase;
        if (type == 1) {
            String charge = readText("Charge: ");
            legalCase = service.openCriminalCase(title, client, opposingParty, deadline, lawyer, charge);
        } else if (type == 2) {
            String matterType = readText("Matter type (e.g. divorce, custody): ");
            legalCase = service.openFamilyCase(title, client, opposingParty, deadline, lawyer, matterType);
        } else {
            String companyName = readText("Company name: ");
            legalCase = service.openCorporateCase(title, client, opposingParty, deadline, lawyer, companyName);
        }
        System.out.println("Opened " + legalCase);
    }

    private void logHours() {
        LegalCase legalCase = service.findCase(readInt("Case id: "));
        LocalDate date = readDate("Date (YYYY-MM-DD): ");
        double hours = readDouble("Hours: ");
        String description = readText("Description: ");
        service.logHours(legalCase, date, hours, description);
        System.out.println("Logged. Total hours on this case: " + legalCase.getTotalHours());
    }

    private void chooseBilling() {
        LegalCase legalCase = service.findCase(readInt("Case id: "));
        System.out.println("Billing: 1 = Hourly, 2 = Flat fee, 3 = Contingency");
        int choice = readInt("Billing method: ");
        if (choice == 1) {
            service.chooseBilling(legalCase, new HourlyBilling());
        } else if (choice == 2) {
            service.chooseBilling(legalCase, new FlatFeeBilling(readDouble("Flat fee amount (EUR): ")));
        } else if (choice == 3) {
            double percentage = readDouble("Percentage of settlement: ");
            double settlement = readDouble("Settlement amount (EUR): ");
            service.chooseBilling(legalCase, new ContingencyBilling(percentage, settlement));
        } else {
            System.out.println("Unknown billing method.");
            return;
        }
        System.out.printf("Billing set. Current fee: %.2f EUR%n", legalCase.calculateFee());
    }

    private void closeCase() {
        LegalCase legalCase = service.findCase(readInt("Case id: "));
        System.out.println("Outcome: 1 = Won, 2 = Lost, 3 = Settled");
        int choice = readInt("Outcome: ");
        CaseStatus outcome;
        if (choice == 1) {
            outcome = CaseStatus.WON;
        } else if (choice == 2) {
            outcome = CaseStatus.LOST;
        } else if (choice == 3) {
            outcome = CaseStatus.SETTLED;
        } else {
            System.out.println("Unknown outcome.");
            return;
        }
        service.closeCase(legalCase, outcome);
        System.out.printf("Case closed as %s. Final fee: %.2f EUR%n", outcome, legalCase.calculateFee());
    }

    private void listCases() {
        System.out.println("Sort by: 1 = Deadline, 2 = Fee (highest first), 3 = Client name");
        int choice = readInt("Sort order: ");
        Comparator<LegalCase> order;
        if (choice == 1) {
            order = Comparator.comparing(legalCase -> legalCase.getDeadline());
        } else if (choice == 2) {
            order = Comparator.comparingDouble((LegalCase legalCase) -> legalCase.calculateFee()).reversed();
        } else if (choice == 3) {
            order = Comparator.comparing(legalCase -> legalCase.getClient().getName());
        } else {
            System.out.println("Unknown sort order.");
            return;
        }
        List<LegalCase> sorted = service.getCasesSorted(order);
        if (sorted.isEmpty()) {
            System.out.println("No cases yet.");
        }
        for (LegalCase legalCase : sorted) {
            System.out.println("  " + legalCase);
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            String input = readText(prompt).trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            String input = readText(prompt).trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number, e.g. 120 or 99.5.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String input = readText(prompt).trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Please use the format YYYY-MM-DD, e.g. 2026-12-31.");
            }
        }
    }
}
