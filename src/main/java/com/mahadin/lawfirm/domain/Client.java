package com.mahadin.lawfirm.domain;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.exception.ValidationException;
import com.mahadin.lawfirm.json.JsonFactory;

public class Client extends Person {
    private String email;
    private String phone;

    public Client(int id, String name, String email, String phone) {
        super(id, name);
        setEmail(email);
        setPhone(phone);
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Email must contain '@' and a '.' after it.");
        }
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        if (at == 0 || dot < at + 2 || dot == email.length() - 1) {
            throw new ValidationException("Email must contain '@' and a '.' after it.");
        }
        this.email = email.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        for (char c : phone.trim().toCharArray()) {
            if (!Character.isDigit(c) && c != '+' && c != ' ' && c != '-') {
                throw new ValidationException("Phone number can only have digits, spaces, '+' and '-'.");
            }
        }
        this.phone = phone.trim();
    }

    /** Gson reads the fields (id, name, email, phone) itself. */
    public JsonObject toJson() {
        return JsonFactory.create().toJsonTree(this).getAsJsonObject();
    }

    @Override
    public String toString() {
        return "Client #" + getId() + ": " + getName() + " (" + email + ", " + phone + ")";
    }
}
