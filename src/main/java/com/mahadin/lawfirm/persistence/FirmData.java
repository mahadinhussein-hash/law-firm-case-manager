package com.mahadin.lawfirm.persistence;

import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FirmData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Client> clients;
    private final List<Lawyer> lawyers;
    private final List<LegalCase> cases;

    public FirmData(List<Client> clients, List<Lawyer> lawyers, List<LegalCase> cases) {
        this.clients = new ArrayList<>(clients);
        this.lawyers = new ArrayList<>(lawyers);
        this.cases = new ArrayList<>(cases);
    }

    public List<Client> getClients() {
        return clients;
    }

    public List<Lawyer> getLawyers() {
        return lawyers;
    }

    public List<LegalCase> getCases() {
        return cases;
    }
}
