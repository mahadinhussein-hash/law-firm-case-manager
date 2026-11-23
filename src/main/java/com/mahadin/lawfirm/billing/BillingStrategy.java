package com.mahadin.lawfirm.billing;

import com.google.gson.JsonObject;
import com.mahadin.lawfirm.domain.LegalCase;

public interface BillingStrategy {

    double calculateFee(LegalCase legalCase);

    String getName();

    JsonObject toJson();
}
