package com.mahadin.lawfirm;

import com.mahadin.lawfirm.persistence.DataStore;
import com.mahadin.lawfirm.service.LawFirmService;
import com.mahadin.lawfirm.ui.ConsoleMenu;
import com.mahadin.lawfirm.ui.WebServer;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        LawFirmService service = new LawFirmService();
        DataStore dataStore = new DataStore("data/lawfirm-data.ser");
        if (args.length > 0 && args[0].equals("--web")) {
            new WebServer(service, dataStore, 8080).start();
        } else {
            ConsoleMenu menu = new ConsoleMenu(service, dataStore, new Scanner(System.in));
            menu.run();
        }
    }
}
