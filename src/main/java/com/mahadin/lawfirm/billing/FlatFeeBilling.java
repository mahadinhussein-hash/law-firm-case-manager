package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.ValidationException;

public class FlatFeeBilling implements BillingStrategy {

    private final double amount;

    public FlatFeeBilling(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Flat fee must be greater than 0.");
        }
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
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
