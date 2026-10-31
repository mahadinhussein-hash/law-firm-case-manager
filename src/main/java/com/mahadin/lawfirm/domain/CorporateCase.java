package com.mahadin.lawfirm.domain;

import java.time.LocalDate;

public class CorporateCase extends LegalCase {

    private static final long serialVersionUID = 1L;

    private final String companyName;

    public CorporateCase(int id, String title, Client client, String opposingParty,
                         LocalDate deadline, Lawyer leadLawyer, String companyName) {
        super(id, title, client, opposingParty, deadline, leadLawyer);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        this.companyName = companyName.trim();
    }

    public String getCompanyName() {
        return companyName;
    }

    @Override
    public String getCaseType() {
        return "Corporate";
    }

    @Override
    public double getRateMultiplier() {
        return 1.3;
    }

    @Override
    public String toString() {
        return super.toString() + " | company name: " + companyName;
    }
}
