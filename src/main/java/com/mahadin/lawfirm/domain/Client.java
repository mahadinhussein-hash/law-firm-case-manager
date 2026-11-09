package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

public class Client extends Person {
    private static final long serialVersionUID = 1L;

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
            throw new ValidationException("Email must contain '@'.");
        }
        this.email = email.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        this.phone = phone.trim();
    }

    @Override
    public String toString() {
        return "Client #" + getId() + ": " + getName() + " (" + email + ", " + phone + ")";
    }
}
