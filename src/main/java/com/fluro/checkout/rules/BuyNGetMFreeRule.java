package com.fluro.checkout.rules;

import com.fluro.checkout.PromotionRule;
import com.fluro.checkout.RuleApplication;

import java.util.Map;

/**
 * "Buy N get M free" promotion for a single SKU, e.g. "buy 3, get one free" is
 * {@code new BuyNGetMFreeRule("C", 3, 1, 25)}. Every full group of {@code payQuantity +
 * freeQuantity} items is charged as {@code payQuantity} units at {@code unitPriceInPence}.
 * Leftover units that don't complete a group fall through to plain unit pricing.
 */
public final class BuyNGetMFreeRule implements PromotionRule {

    private final String sku;
    private final int paidQuantity;
    private final int freeQuantity;
    private final long unitPriceInPence;

    public BuyNGetMFreeRule(String sku, int paidQuantity, int freeQuantity, long unitPriceInPence) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (paidQuantity < 1) {
            throw new IllegalArgumentException("paidQuantity must be at least 1, got " + paidQuantity);
        }
        if (freeQuantity < 1) {
            throw new IllegalArgumentException("freeQuantity must be at least 1, got " + freeQuantity);
        }
        if (unitPriceInPence <= 0) {
            throw new IllegalArgumentException("unitPriceInPence must be greater than zero");
        }
        this.sku = sku;
        this.paidQuantity = paidQuantity;
        this.freeQuantity = freeQuantity;
        this.unitPriceInPence = unitPriceInPence;
    }

    @Override
    public RuleApplication apply(Map<String, Integer> availableQuantities) {
        int available = availableQuantities.getOrDefault(sku, 0);
        int groupSize = paidQuantity + freeQuantity;
        int groups = available / groupSize;
        if (groups == 0) {
            return RuleApplication.none();
        }
        long price = (long) groups * paidQuantity * unitPriceInPence;
        return new RuleApplication(price, Map.of(sku, groups * groupSize));
    }
}
