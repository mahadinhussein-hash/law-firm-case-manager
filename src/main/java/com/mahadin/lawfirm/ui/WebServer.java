package com.mahadin.lawfirm.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mahadin.lawfirm.billing.BillingFactory;
import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.exception.CaseClosedException;
import com.mahadin.lawfirm.exception.LawFirmException;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.persistence.JsonFirmRepository;
import com.mahadin.lawfirm.service.LawFirmService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A small web page for the same service the console menu uses.
 * It uses the JDK's built-in HTTP server, so no extra libraries are needed.
 */
public class WebServer {

    private final LawFirmService service;
    private final JsonFirmRepository repository;
    private final int port;

    public WebServer(LawFirmService service, JsonFirmRepository repository, int port) {
        this.service = service;
        this.repository = repository;
        this.port = port;
    }

    public void start() throws IOException {
        loadData();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/", this::showPage);
        server.createContext("/api/state", this::showState);
        server.createContext("/api/client", exchange -> change(exchange, this::addClient));
        server.createContext("/api/lawyer", exchange -> change(exchange, this::addLawyer));
        server.createContext("/api/case", exchange -> change(exchange, this::openCase));
        server.createContext("/api/hours", exchange -> change(exchange, this::logHours));
        server.createContext("/api/billing", exchange -> change(exchange, this::chooseBilling));
        server.createContext("/api/close", exchange -> change(exchange, this::closeCase));
        server.start();
        System.out.println("Web page running at http://localhost:" + port + "  (press Ctrl+C to stop)");
    }

    private void loadData() {
        try {
            service.importData(repository.load());
            System.out.println("Loaded " + service.getClients().size() + " clients, "
                    + service.getLawyers().size() + " lawyers and "
                    + service.getCases().size() + " cases.");
        } catch (IOException e) {
            System.out.println("Could not load saved data (" + e.getMessage() + "). Starting with empty data.");
        }
    }

    // ---------- the actions: each one reads the form fields and calls the service ----------

    private String addClient(Map<String, String> form) {
        Client client = service.addClient(form.get("name"), form.get("email"), form.get("phone"));
        return "Added " + client;
    }

    private String addLawyer(Map<String, String> form) {
        Lawyer lawyer = service.addLawyer(form.get("name"), number(form, "rate"));
        return "Added " + lawyer;
    }

    private String openCase(Map<String, String> form) throws LawFirmException {
        String title = form.get("title");
        Client client = service.findClient(whole(form, "clientId"));
        String opposingParty = form.get("opposingParty");
        LocalDate deadline = date(form, "deadline");
        Lawyer lawyer = service.findLawyer(whole(form, "lawyerId"));
        String extra = form.get("extra");
        LegalCase legalCase = service.openCase(form.get("type"), title, client, opposingParty, deadline, lawyer, extra);
        return "Opened " + legalCase;
    }

    private String logHours(Map<String, String> form) {
        LegalCase legalCase = service.findCase(whole(form, "caseId"));
        service.logHours(legalCase, date(form, "date"), number(form, "hours"), form.get("description"));
        return "Logged. Total hours on this case: " + legalCase.getTotalHours();
    }

    private String chooseBilling(Map<String, String> form) {
        LegalCase legalCase = service.findCase(whole(form, "caseId"));
        service.chooseBilling(legalCase, BillingFactory.create(form.get("method"),
                optionalNumber(form, "amount"), optionalNumber(form, "percentage"), optionalNumber(form, "settlement")));
        return String.format(Locale.ROOT, "Billing set. Current fee: %.2f EUR", legalCase.calculateFee());
    }

    private String closeCase(Map<String, String> form) {
        LegalCase legalCase = service.findCase(whole(form, "caseId"));
        CaseStatus outcome;
        try {
            outcome = CaseStatus.valueOf(String.valueOf(form.get("outcome")));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Outcome must be WON, LOST or SETTLED.");
        }
        service.closeCase(legalCase, outcome);
        return String.format(Locale.ROOT, "Case closed as %s. Final fee: %.2f EUR", outcome, legalCase.calculateFee());
    }

    // ---------- HTTP plumbing ----------

    private interface Action {
        String run(Map<String, String> form) throws LawFirmException;
    }

    /** Runs one action and answers with JSON. The catches match the ones in ConsoleMenu.run. */
    private void change(HttpExchange exchange, Action action) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Use POST");
            return;
        }
        try {
            String message = action.run(readForm(exchange));
            saveData();
            send(exchange, 200, "application/json", reply("message", message));
        } catch (ValidationException | CaseClosedException e) {
            send(exchange, 400, "application/json", reply("error", "Error: " + e.getMessage()));
        } catch (LawFirmException e) {
            send(exchange, 409, "application/json", reply("error", "Refused: " + e.getMessage()));
        }
    }

    private void showPage(HttpExchange exchange) throws IOException {
        try (InputStream page = WebServer.class.getResourceAsStream("/web/index.html")) {
            if (!"/".equals(exchange.getRequestURI().getPath()) || page == null) {
                send(exchange, 404, "text/plain", "Not found");
                return;
            }
            send(exchange, 200, "text/html; charset=utf-8", new String(page.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** All data for the page. ?sort=deadline | fee | client picks the case order, like menu option 8. */
    private void showState(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Comparator<LegalCase> order = Comparator.comparingInt(LegalCase::getId);
        if ("sort=deadline".equals(query)) {
            order = Comparator.comparing(legalCase -> legalCase.getDeadline());
        } else if ("sort=fee".equals(query)) {
            order = Comparator.comparingDouble((LegalCase legalCase) -> legalCase.calculateFee()).reversed();
        } else if ("sort=client".equals(query)) {
            order = Comparator.comparing(legalCase -> legalCase.getClient().getName());
        }

        JsonArray clients = new JsonArray();
        for (Client client : service.getClients()) {
            clients.add(client.toJson());
        }
        JsonArray lawyers = new JsonArray();
        for (Lawyer lawyer : service.getLawyers()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", lawyer.getId());
            o.addProperty("name", lawyer.getName());
            o.addProperty("rate", lawyer.getHourlyRate());
            o.addProperty("activeCases", service.countActiveCases(lawyer));
            lawyers.add(o);
        }
        JsonArray cases = new JsonArray();
        for (LegalCase legalCase : service.getCasesSorted(order)) {
            JsonObject o = new JsonObject();
            o.addProperty("id", legalCase.getId());
            o.addProperty("type", legalCase.getCaseType());
            o.addProperty("title", legalCase.getTitle());
            o.addProperty("client", legalCase.getClient().getName());
            o.addProperty("lawyer", legalCase.getLeadLawyer().getName());
            o.addProperty("opposingParty", legalCase.getOpposingParty());
            o.addProperty("deadline", legalCase.getDeadline().toString());
            o.addProperty("status", legalCase.getStatus().toString());
            o.addProperty("hours", legalCase.getTotalHours());
            o.addProperty("billing", legalCase.getBillingStrategy().getName());
            o.addProperty("fee", legalCase.calculateFee());
            cases.add(o);
        }
        JsonObject state = new JsonObject();
        state.add("clients", clients);
        state.add("lawyers", lawyers);
        state.add("cases", cases);
        send(exchange, 200, "application/json", state.toString());
    }

    private void saveData() {
        try {
            repository.save(service.exportData());
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        for (String pair : body.split("&")) {
            int equals = pair.indexOf('=');
            if (equals > 0) {
                form.put(URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private int whole(Map<String, String> form, String field) {
        try {
            return Integer.parseInt(form.get(field).trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException("Please choose a value for " + field + ".");
        }
    }

    // a field the chosen option does not use may be missing; the factory ignores it
    private double optionalNumber(Map<String, String> form, String field) {
        String text = form.get(field);
        return text == null || text.isBlank() ? 0 : number(form, field);
    }

    private double number(Map<String, String> form, String field) {
        try {
            return Double.parseDouble(form.get(field).trim().replace(',', '.'));
        } catch (NumberFormatException | NullPointerException e) {
            throw new ValidationException("Please enter a number for " + field + ".");
        }
    }

    private LocalDate date(Map<String, String> form, String field) {
        try {
            return LocalDate.parse(form.get(field).trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ValidationException("Please enter the date as YYYY-MM-DD.");
        }
    }

    private void send(HttpExchange exchange, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** A small JSON answer such as {"message": "..."}; Gson takes care of the escaping. */
    private String reply(String key, String text) {
        JsonObject o = new JsonObject();
        o.addProperty(key, text);
        return o.toString();
    }
}
