package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;

public interface BillingStrategy {

    double calculateFee(LegalCase legalCase);

    String getName();
}
