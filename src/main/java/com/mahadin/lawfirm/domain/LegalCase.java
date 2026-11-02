package com.mahadin.lawfirm.domain;

import com.mahadin.lawfirm.billing.BillingStrategy;
import com.mahadin.lawfirm.billing.HourlyBilling;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<TimeEntry> timeEntries = new ArrayList<>();
    private BillingStrategy billingStrategy;

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
        this.billingStrategy = new HourlyBilling();
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

    public List<TimeEntry> getTimeEntries() {
        return Collections.unmodifiableList(timeEntries);
    }

    public BillingStrategy getBillingStrategy() {
        return billingStrategy;
    }

    public void addTimeEntry(TimeEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Time entry cannot be null.");
        }
        if (!isActive()) {
            throw new IllegalStateException("Cannot log hours on a closed case.");
        }
        timeEntries.add(entry);
    }

    public double getTotalHours() {
        double total = 0;
        for (TimeEntry entry : timeEntries) {
            total += entry.getHours();
        }
        return total;
    }

    public void setBillingStrategy(BillingStrategy billingStrategy) {
        if (billingStrategy == null) {
            throw new IllegalArgumentException("Billing strategy cannot be null.");
        }
        this.billingStrategy = billingStrategy;
    }

    public double calculateFee() {
        return billingStrategy.calculateFee(this);
    }

    public void close(CaseStatus outcome) {
        if (outcome == null || outcome == CaseStatus.OPEN) {
            throw new IllegalArgumentException("Outcome must be WON, LOST or SETTLED.");
        }
        if (!isActive()) {
            throw new IllegalStateException("Case is already closed.");
        }
        this.status = outcome;
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
        return String.format("%s case #%d: %s | client: %s vs %s | lead: %s | deadline: %s | %s | %s | fee: %.2f EUR",
                getCaseType(), id, title, client.getName(), opposingParty,
                leadLawyer.getName(), deadline, status, billingStrategy.getName(), calculateFee());
    }
}
