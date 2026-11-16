package com.mahadin.lawfirm.billing;

import com.mahadin.lawfirm.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingFactoryTest {

    @Test
    void createsTheRightStrategy() {
        assertInstanceOf(HourlyBilling.class, BillingFactory.create("hourly", 0, 0, 0));
        assertInstanceOf(FlatFeeBilling.class, BillingFactory.create("flat", 500, 0, 0));
        assertInstanceOf(ContingencyBilling.class, BillingFactory.create("contingency", 0, 20, 5000));
    }

    @Test
    void unknownMethodIsRefused() {
        assertThrows(ValidationException.class, () -> BillingFactory.create("free", 0, 0, 0));
        assertThrows(ValidationException.class, () -> BillingFactory.create(null, 0, 0, 0));
    }

    @Test
    void valuesStillGoThroughTheStrategyRules() {
        assertThrows(ValidationException.class, () -> BillingFactory.create("flat", 0, 0, 0));
        assertThrows(ValidationException.class, () -> BillingFactory.create("contingency", 0, 150, 100));
    }
}
