package com.mahadin.lawfirm.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class DataStore {

    private final String filePath;

    public DataStore(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty.");
        }
        this.filePath = filePath;
    }

    public void save(FirmData data) throws IOException {
        File file = new File(filePath);
        File folder = file.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        }
    }

    public FirmData load() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new FirmData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (FirmData) in.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("The save file is not in the expected format.", e);
        }
    }
}
