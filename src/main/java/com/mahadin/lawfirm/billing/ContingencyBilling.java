package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.CaseStatus;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.ValidationException;

public class ContingencyBilling implements BillingStrategy {

    private static final long serialVersionUID = 1L;

    private final double percentage;
    private final double settlementAmount;

    public ContingencyBilling(double percentage, double settlementAmount) {
        if (percentage <= 0 || percentage > 100) {
            throw new ValidationException("Percentage must be between 0 and 100.");
        }
        if (settlementAmount < 0) {
            throw new ValidationException("Settlement amount cannot be negative.");
        }
        this.percentage = percentage;
        this.settlementAmount = settlementAmount;
    }

    @Override
    public double calculateFee(LegalCase legalCase) {
        if (legalCase.getStatus() != CaseStatus.WON) {
            return 0;
        }
        return settlementAmount * percentage / 100;
    }

    @Override
    public String getName() {
        return String.format("Contingency (%.0f%% of %.2f EUR, only if won)", percentage, settlementAmount);
    }
}
