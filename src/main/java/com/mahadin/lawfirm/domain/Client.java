package com.mahadin.lawfirm.domain;

import java.io.Serializable;
import java.util.Objects;

public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private String email;
    private String phone;

    public Client(int id, String name, String email, String phone) {
        if (id <= 0) {
            throw new IllegalArgumentException("Client id must be positive.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        this.id = id;
        this.name = name.trim();
        setEmail(email);
        setPhone(phone);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'.");
        }
        this.email = email.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phone = phone.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Client)) {
            return false;
        }
        Client client = (Client) other;
        return id == client.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Client #" + id + ": " + name + " (" + email + ", " + phone + ")";
    }
}
