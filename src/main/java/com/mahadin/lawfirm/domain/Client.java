package com.mahadin.lawfirm.domain; // package that groups classes together

import com.mahadin.lawfirm.exception.ValidationException;

//creating a class to define the variables needed its public so other packages can access it
// its called client, it extends Person so it gets the id and name from there
public class Client extends Person {
// private means it can only be accessed in client class to keep client data safe
//static means it belongs to the whole class not just one client
//long means whole number
// serialization label
//gives the class a version number so Java can check it when loading saved objects.
    private static final long serialVersionUID = 1L;
//defining variables
    private String email;
    private String phone;
// contructer which steps up new client object, object contains client info
    public Client(int id, String name, String email, String phone) {
        super(id, name);
        setEmail(email);
        setPhone(phone);
    }
    //public means other classes can use this method to return this data
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
//returns all clients info as readable text
    @Override
    public String toString() {
        return "Client #" + getId() + ": " + getName() + " (" + email + ", " + phone + ")";
    }
}
