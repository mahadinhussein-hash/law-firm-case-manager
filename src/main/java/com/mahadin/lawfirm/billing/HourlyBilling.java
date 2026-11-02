package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;

public class HourlyBilling implements BillingStrategy {

    private static final long serialVersionUID = 1L;

    @Override
    public double calculateFee(LegalCase legalCase) {
        return legalCase.getTotalHours()
                * legalCase.getLeadLawyer().getHourlyRate()
                * legalCase.getRateMultiplier();
    }

    @Override
    public String getName() {
        return "Hourly";
    }
}
