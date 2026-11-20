package com.mahadin.lawfirm;

import com.mahadin.lawfirm.persistence.FirmRepository;
import com.mahadin.lawfirm.persistence.RepositoryFactory;
import com.mahadin.lawfirm.service.LawFirmService;
import com.mahadin.lawfirm.ui.ConsoleMenu;
import com.mahadin.lawfirm.ui.WebServer;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> options = List.of(args);
        FirmRepository repository = RepositoryFactory.create("json");
        LawFirmService service = new LawFirmService();
        if (options.contains("--web")) {
            new WebServer(service, repository, 8080).start();
        } else {
            ConsoleMenu menu = new ConsoleMenu(service, repository, new Scanner(System.in));
            menu.run();
        }
    }
}
