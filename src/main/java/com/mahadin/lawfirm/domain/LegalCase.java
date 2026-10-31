package com.mahadin.lawfirm.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public abstract class LegalCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String title;
    private final Client client;
    private final String opposingParty;
    private final LocalDate deadline;
    private Lawyer leadLawyer;
    private CaseStatus status;

    protected LegalCase(int id, String title, Client client, String opposingParty,
                        LocalDate deadline, Lawyer leadLawyer) {
        if (id <= 0) {
            throw new IllegalArgumentException("Case id must be positive.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Case title cannot be empty.");
        }
        if (client == null) {
            throw new IllegalArgumentException("A case must belong to a client.");
        }
        if (opposingParty == null || opposingParty.isBlank()) {
            throw new IllegalArgumentException("Opposing party cannot be empty.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline is required.");
        }
        this.id = id;
        this.title = title.trim();
        this.client = client;
        this.opposingParty = opposingParty.trim();
        this.deadline = deadline;
        setLeadLawyer(leadLawyer);
        this.status = CaseStatus.OPEN;
    }

    public abstract String getCaseType();

    public abstract double getRateMultiplier();

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Client getClient() {
        return client;
    }

    public String getOpposingParty() {
        return opposingParty;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Lawyer getLeadLawyer() {
        return leadLawyer;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == CaseStatus.OPEN;
    }

    public void setLeadLawyer(Lawyer leadLawyer) {
        if (leadLawyer == null) {
            throw new IllegalArgumentException("A case must have a lead lawyer.");
        }
        this.leadLawyer = leadLawyer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LegalCase)) {
            return false;
        }
        LegalCase legalCase = (LegalCase) other;
        return id == legalCase.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s case #%d: %s | client: %s vs %s | lead: %s | deadline: %s | %s",
                getCaseType(), id, title, client.getName(), opposingParty,
                leadLawyer.getName(), deadline, status);
    }
}
