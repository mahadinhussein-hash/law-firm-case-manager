package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;

public class FlatFeeBilling implements BillingStrategy {

    private static final long serialVersionUID = 1L;

    private final double amount;

    public FlatFeeBilling(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Flat fee must be greater than 0.");
        }
        this.amount = amount;
    }

    @Override
    public double calculateFee(LegalCase legalCase) {
        return amount;
    }

    @Override
    public String getName() {
        return String.format("Flat fee (%.2f EUR)", amount);
    }
}
