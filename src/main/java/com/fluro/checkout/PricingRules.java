package com.fluro.checkout;

import com.fluro.checkout.rules.BuyNGetMFreeRule;
import com.fluro.checkout.rules.ComboRule;
import com.fluro.checkout.rules.MultiBuyRule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A complete, immutable set of pricing rules for a checkout transaction: the plain unit price of
 * every known SKU, plus zero or more {@link PromotionRule} promotions that can override part of
 * that price. Because pricing changes frequently, a new {@code PricingRules} instance can be
 * built and handed to a {@link Checkout} at the start of a new checkout transaction.
 */
public final class PricingRules {

    private final Map<String, Long> unitPricesInPence;
    private final List<PromotionRule> promotionRules;

    private PricingRules(Map<String, Long> unitPricesInPence, List<PromotionRule> promotionRules) {
        this.unitPricesInPence = Map.copyOf(unitPricesInPence);
        this.promotionRules = List.copyOf(promotionRules);
    }

    public boolean hasUnitPrice(String sku) {
        return unitPricesInPence.containsKey(sku);
    }

    public long unitPrice(String sku) {
        Long price = unitPricesInPence.get(sku);
        if (price == null) {
            throw new IllegalArgumentException("No unit price configured for SKU '" + sku + "'");
        }
        return price;
    }

    public List<PromotionRule> promotionRules() {
        return promotionRules;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder mirroring the "Item | Unit Price | Special Price" shape of a pricing sheet. */
    public static final class Builder {

        private final Map<String, Long> unitPrices = new LinkedHashMap<>();
        private final List<PromotionRule> rules = new ArrayList<>();

        private Builder() {
        }

        /** Registers the plain unit price (in pence) for a SKU. Must be called before any promotion rule referencing that SKU. */
        public Builder unitPrice(String sku, long priceInPence) {
            String normalized = normalizeSku(sku);
            if (unitPrices.containsKey(normalized)) {
                throw new IllegalArgumentException("Unit price for SKU '" + normalized + "' already defined");
            }
            if (priceInPence <= 0) {
                throw new IllegalArgumentException("Price for SKU '" + normalized + "' must be greater than zero");
            }

            unitPrices.put(normalized, priceInPence);
            return this;
        }

        /** "N for £Y": buy {@code quantity} of {@code sku} for a fixed total of {@code priceInPence}. */
        public Builder multiBuy(String sku, int quantity, long priceInPence) {
            String normalized = requireKnownSku(sku);
            rules.add(new MultiBuyRule(normalized, quantity, priceInPence));
            return this;
        }

        /** "Buy N get M free" for a single SKU, priced at that SKU's already-registered unit price. */
        public Builder buyNGetMFree(String sku, int payQuantity, int freeQuantity) {
            String normalized = requireKnownSku(sku);
            rules.add(new BuyNGetMFreeRule(normalized, payQuantity, freeQuantity, unitPrices.get(normalized)));
            return this;
        }

        /** "Meal deal": buy the given combination of different SKUs together for {@code priceInPence}. */
        public Builder combo(long priceInPence, Map<String, Integer> componentsPerCombo) {
            Map<String, Integer> normalized = new LinkedHashMap<>();
            componentsPerCombo.forEach((sku, quantity) -> normalized.put(requireKnownSku(sku), quantity));
            rules.add(new ComboRule(normalized, priceInPence));
            return this;
        }

        /** Registers an arbitrary custom {@link PromotionRule}, for promotion types beyond the built-in ones. */
        public Builder rule(PromotionRule rule) {
            rules.add(rule);
            return this;
        }

        public PricingRules build() {
            return new PricingRules(unitPrices, rules);
        }

        private String requireKnownSku(String sku) {
            String normalized = normalizeSku(sku);
            if (!unitPrices.containsKey(normalized)) {
                throw new IllegalArgumentException(
                        "Cannot add a special rule for unknown SKU '" + normalized + "'; call unitPrice(...) first");
            }
            return normalized;
        }

        private static String normalizeSku(String sku) {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku must not be blank");
            }
            return sku.trim().toUpperCase();
        }
    }
}
