package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.domain.LegalCase;

import java.io.Serializable;

public interface BillingStrategy extends Serializable {

    double calculateFee(LegalCase legalCase);

    String getName();
}
