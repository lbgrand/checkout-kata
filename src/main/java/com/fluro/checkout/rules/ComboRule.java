package com.fluro.checkout.rules;

import com.fluro.checkout.PricingRule;
import com.fluro.checkout.RuleApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * "Meal deal" promotion: buy a fixed combination of different SKUs together for a special total
 * price, e.g. "one D and one E for £3" is
 * {@code new ComboRule(Map.of("D", 1, "E", 1), 300)}. The rule forms as many complete combos as
 * the available stock allows; any items left over (because there weren't enough of one of the
 * SKUs to form another combo) fall through to plain unit pricing.
 */
public final class ComboRule implements PricingRule {

    private final Map<String, Integer> componentsPerCombo;
    private final long priceInPence;

    public ComboRule(Map<String, Integer> componentsPerCombo, long priceInPence) {
        if (componentsPerCombo == null || componentsPerCombo.size() < 2) {
            throw new IllegalArgumentException("a combo must contain at least 2 distinct SKUs");
        }
        componentsPerCombo.forEach((sku, quantity) -> {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku must not be blank");
            }
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("quantity for " + sku + " must be at least 1");
            }
        });
        if (priceInPence < 0) {
            throw new IllegalArgumentException("priceInPence must not be negative");
        }
        this.componentsPerCombo = Map.copyOf(componentsPerCombo);
        this.priceInPence = priceInPence;
    }

    @Override
    public RuleApplication apply(Map<String, Integer> availableQuantities) {
        int combos = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> component : componentsPerCombo.entrySet()) {
            int available = availableQuantities.getOrDefault(component.getKey(), 0);
            combos = Math.min(combos, available / component.getValue());
        }
        if (combos <= 0) {
            return RuleApplication.none();
        }
        Map<String, Integer> consumed = new HashMap<>();
        int finalCombos = combos;
        componentsPerCombo.forEach((sku, quantity) -> consumed.put(sku, quantity * finalCombos));
        return new RuleApplication((long) combos * priceInPence, consumed);
    }
}
