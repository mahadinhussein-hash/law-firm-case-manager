package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;

public class HourlyBilling implements BillingStrategy {

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
