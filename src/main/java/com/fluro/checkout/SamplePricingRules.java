package com.fluro.checkout;

import java.util.Map;

/** "This week's prices" from the kata specification, used as the CLI's default pricing sheet. */
public final class SamplePricingRules {

    private SamplePricingRules() {
    }

    public static PricingRules thisWeek() {
        return PricingRules.builder()
                .unitPrice("A", 50)
                .unitPrice("B", 75).multiBuy("B", 2, 125)
                .unitPrice("C", 25).buyNGetMFree("C", 3, 1)
                .unitPrice("D", 150)
                .unitPrice("E", 200)
                .combo(300, Map.of("D", 1, "E", 1))
                .build();
    }
}
