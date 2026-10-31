package com.mahadin.lawfirm.domain;

import java.time.LocalDate;

public class CriminalCase extends LegalCase {

    private static final long serialVersionUID = 1L;

    private final String charge;

    public CriminalCase(int id, String title, Client client, String opposingParty,
                        LocalDate deadline, Lawyer leadLawyer, String charge) {
        super(id, title, client, opposingParty, deadline, leadLawyer);
        if (charge == null || charge.isBlank()) {
            throw new IllegalArgumentException("Charge cannot be empty.");
        }
        this.charge = charge.trim();
    }

    public String getCharge() {
        return charge;
    }

    @Override
    public String getCaseType() {
        return "Criminal";
    }

    @Override
    public double getRateMultiplier() {
        return 1.5;
    }

    @Override
    public String toString() {
        return super.toString() + " | charge: " + charge;
    }
}
