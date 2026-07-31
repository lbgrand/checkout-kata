package com.fluro.checkout.rules;

import com.fluro.checkout.PricingRule;
import com.fluro.checkout.RuleApplication;

import java.util.Map;

/**
 * "Buy N get M free" promotion for a single SKU, e.g. "buy 3, get one free" is
 * {@code new BuyNGetMFreeRule("C", 3, 1, 25)}. Every full group of {@code payQuantity +
 * freeQuantity} items is charged as {@code payQuantity} units at {@code unitPriceInPence}.
 * Leftover units that don't complete a group fall through to plain unit pricing.
 */
public final class BuyNGetMFreeRule implements PricingRule {

    private final String sku;
    private final int payQuantity;
    private final int freeQuantity;
    private final long unitPriceInPence;

    public BuyNGetMFreeRule(String sku, int payQuantity, int freeQuantity, long unitPriceInPence) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (payQuantity < 1) {
            throw new IllegalArgumentException("payQuantity must be at least 1, got " + payQuantity);
        }
        if (freeQuantity < 1) {
            throw new IllegalArgumentException("freeQuantity must be at least 1, got " + freeQuantity);
        }
        if (unitPriceInPence < 0) {
            throw new IllegalArgumentException("unitPriceInPence must not be negative");
        }
        this.sku = sku;
        this.payQuantity = payQuantity;
        this.freeQuantity = freeQuantity;
        this.unitPriceInPence = unitPriceInPence;
    }

    @Override
    public RuleApplication apply(Map<String, Integer> availableQuantities) {
        int available = availableQuantities.getOrDefault(sku, 0);
        int groupSize = payQuantity + freeQuantity;
        int groups = available / groupSize;
        if (groups == 0) {
            return RuleApplication.none();
        }
        long price = (long) groups * payQuantity * unitPriceInPence;
        return new RuleApplication(price, Map.of(sku, groups * groupSize));
    }
}
