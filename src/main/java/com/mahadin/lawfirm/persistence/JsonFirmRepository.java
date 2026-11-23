package com.mahadin.lawfirm.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mahadin.lawfirm.domain.CaseFactory;
import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.ClientFactory;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LawyerFactory;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.json.JsonFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the firm's data as a readable JSON file.
 * Each domain class knows how to turn itself into JSON (toJson) and the
 * factories (ClientFactory, LawyerFactory, CaseFactory, ...) create the objects
 * again from JSON, through their constructors, so every rule is checked again
 * when a file is loaded. This class only deals with the file.
 */
public class JsonFirmRepository {

    private final String filePath;

    public JsonFirmRepository(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ValidationException("File path cannot be empty.");
        }
        this.filePath = filePath;
    }

    public void save(FirmData data) throws IOException {
        File file = new File(filePath);
        File folder = file.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        JsonArray clients = new JsonArray();
        for (Client client : data.getClients()) {
            clients.add(client.toJson());
        }
        JsonArray lawyers = new JsonArray();
        for (Lawyer lawyer : data.getLawyers()) {
            lawyers.add(lawyer.toJson());
        }
        JsonArray cases = new JsonArray();
        for (LegalCase legalCase : data.getCases()) {
            cases.add(legalCase.toJson());
        }
        JsonObject root = new JsonObject();
        root.add("clients", clients);
        root.add("lawyers", lawyers);
        root.add("cases", cases);
        try (Writer out = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            JsonFactory.create().toJson(root, out);
        }
    }

    public FirmData load() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new FirmData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(in).getAsJsonObject();

            Map<Integer, Client> clients = new HashMap<>();
            List<Client> clientList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("clients")) {
                Client client = ClientFactory.fromJson(element.getAsJsonObject());
                clients.put(client.getId(), client);
                clientList.add(client);
            }

            Map<Integer, Lawyer> lawyers = new HashMap<>();
            List<Lawyer> lawyerList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("lawyers")) {
                Lawyer lawyer = LawyerFactory.fromJson(element.getAsJsonObject());
                lawyers.put(lawyer.getId(), lawyer);
                lawyerList.add(lawyer);
            }

            List<LegalCase> caseList = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("cases")) {
                caseList.add(CaseFactory.fromJson(element.getAsJsonObject(), clients, lawyers));
            }
            return new FirmData(clientList, lawyerList, caseList);
        } catch (RuntimeException e) {
            // bad JSON, a missing field or a value the domain classes refuse
            throw new IOException("The save file is not in the expected format.", e);
        }
    }
}
