package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.exception.ValidationException;

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
        if (email == null || !email.contains("@") || email.indexOf('.', email.indexOf('@')) == -1) {
            throw new ValidationException("Email must contain '@' and a '.' after it.");
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
