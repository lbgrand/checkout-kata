package com.fluro.checkout;

import java.util.Map;

/**
 * The outcome of applying a {@link PromotionRule} to a basket: how much it charged, and how many
 * units of each SKU it "used up" while doing so. Consumed units are removed from the pool before
 * remaining rules (and finally plain unit pricing) run, so the same units are never charged twice.
 */
public record RuleApplication(long priceInPence, Map<String, Integer> consumedQuantities) {

    private static final RuleApplication NONE = new RuleApplication(0L, Map.of());

    public RuleApplication {
        if (priceInPence < 0) {
            throw new IllegalArgumentException("priceInPence must not be negative");
        }
        consumedQuantities = Map.copyOf(consumedQuantities);
    }

    /** Represents a rule that did not find anything applicable in the current basket. */
    public static RuleApplication none() {
        return NONE;
    }
}
