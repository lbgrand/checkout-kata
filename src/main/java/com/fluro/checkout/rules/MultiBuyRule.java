package com.fluro.checkout.rules;

import com.fluro.checkout.PromotionRule;
import com.fluro.checkout.RuleApplication;

import java.util.Map;

/**
 * "Multipriced" promotion: buy exactly {@code quantity} of a single SKU for a fixed
 * {@code priceInPence}, e.g. "3 for £1.30". Any leftover units that don't form a full group are
 * left untouched, so they fall through to plain unit pricing.
 */
public final class MultiBuyRule implements PromotionRule {

    private final String sku;
    private final int quantity;
    private final long priceInPence;

    public MultiBuyRule(String sku, int quantity, long priceInPence) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (quantity < 2) {
            throw new IllegalArgumentException("quantity must be at least 2, got " + quantity);
        }
        if (priceInPence <= 0) {
            throw new IllegalArgumentException("priceInPence must be greater than zero");
        }
        this.sku = sku;
        this.quantity = quantity;
        this.priceInPence = priceInPence;
    }

    @Override
    public RuleApplication apply(Map<String, Integer> availableQuantities) {
        int available = availableQuantities.getOrDefault(sku, 0);
        int groups = available / quantity;
        if (groups == 0) {
            return RuleApplication.none();
        }
        return new RuleApplication(groups * priceInPence, Map.of(sku, groups * quantity));
    }
}
