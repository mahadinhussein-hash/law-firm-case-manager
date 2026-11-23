package com.mahadin.lawfirm.billing;

import com.google.gson.JsonObject;
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

    @Override
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("method", "hourly");
        return o;
    }
}
