package com.fluro.checkout;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A single supermarket checkout transaction. Items can be scanned in any order; the total price
 * accounts for whatever {@link PricingRules} (unit prices and promotions) were supplied when this
 * checkout was created.
 *
 * <p>Not thread-safe: a single {@code Checkout} instance is meant to represent one customer's
 * basket, scanned sequentially at a till.
 */
public final class Checkout {

    private final PricingRules pricingRules;
    private final Map<String, Integer> scannedQuantities = new LinkedHashMap<>();

    public Checkout(PricingRules pricingRules) {
        this.pricingRules = Objects.requireNonNull(pricingRules, "pricingRules must not be null");
    }

    /**
     * Scans a single item by its SKU.
     *
     * @throws IllegalArgumentException if the SKU has no known unit price
     */
    public void scan(String sku) {
        Objects.requireNonNull(sku, "sku must not be null");
        String normalized = sku.trim().toUpperCase();
        if (!pricingRules.hasUnitPrice(normalized)) {
            throw new IllegalArgumentException("Unknown SKU: '" + sku + "'");
        }
        scannedQuantities.merge(normalized, 1, Integer::sum);
    }

    /**
     * Computes the total price of everything scanned so far, in pence, applying every special
     * rule before charging any remaining units at their plain unit price.
     */
    public long totalPriceInPence() {
        Map<String, Integer> remaining = new LinkedHashMap<>(scannedQuantities);
        long total = 0L;

        for (PricingRule rule : pricingRules.specialRules()) {
            RuleApplication result = rule.apply(remaining);
            total += result.priceInPence();
            result.consumedQuantities().forEach((sku, consumed) -> remaining.merge(sku, -consumed, Integer::sum));
        }

        for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
            if (entry.getValue() > 0) {
                total += entry.getValue() * pricingRules.unitPrice(entry.getKey());
            }
        }

        return total;
    }

    /** Read-only snapshot of how many of each SKU have been scanned so far. */
    public Map<String, Integer> scannedQuantities() {
        return Map.copyOf(scannedQuantities);
    }
}
