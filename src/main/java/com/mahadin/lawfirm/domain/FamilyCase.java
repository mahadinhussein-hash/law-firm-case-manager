package com.mahadin.lawfirm.domain;

import java.time.LocalDate;

public class FamilyCase extends LegalCase {

    private static final long serialVersionUID = 1L;

    private final String matterType;

    public FamilyCase(int id, String title, Client client, String opposingParty,
                      LocalDate deadline, Lawyer leadLawyer, String matterType) {
        super(id, title, client, opposingParty, deadline, leadLawyer);
        if (matterType == null || matterType.isBlank()) {
            throw new IllegalArgumentException("Matter type cannot be empty.");
        }
        this.matterType = matterType.trim();
    }

    public String getMatterType() {
        return matterType;
    }

    @Override
    public String getCaseType() {
        return "Family";
    }

    @Override
    public double getRateMultiplier() {
        return 1.0;
    }

    @Override
    public String toString() {
        return super.toString() + " | matter type: " + matterType;
    }
}
