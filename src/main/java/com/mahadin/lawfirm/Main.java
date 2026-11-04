package com.mahadin.lawfirm;

import com.mahadin.lawfirm.persistence.DataStore;
import com.mahadin.lawfirm.service.LawFirmService;
import com.mahadin.lawfirm.ui.ConsoleMenu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        LawFirmService service = new LawFirmService();
        DataStore dataStore = new DataStore("data/lawfirm-data.ser");
        ConsoleMenu menu = new ConsoleMenu(service, dataStore, new Scanner(System.in));
        menu.run();
    }
}
