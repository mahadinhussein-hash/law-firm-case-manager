package com.mahadin.lawfirm;

import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;

public class Main {
    public static void main(String[] args) {
        // Temporary demo of the domain classes. Replaced by the console menu later.
        Client client = new Client(1, "Anna Kovacs", "anna@example.com", "+36 30 123 4567");
        Lawyer lawyer = new Lawyer(1, "Peter Nagy", 120.0);
        System.out.println(client);
        System.out.println(lawyer);

        client.setPhone("+36 20 999 8888");
        lawyer.setHourlyRate(150.0);
        System.out.println("After updates:");
        System.out.println(client);
        System.out.println(lawyer);

        try {
            new Client(2, "", "bob@example.com", "123");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        try {
            lawyer.setHourlyRate(-50);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
