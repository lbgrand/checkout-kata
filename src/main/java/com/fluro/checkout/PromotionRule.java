package com.fluro.checkout;

import java.util.Map;

/**
 * A promotional pricing strategy (Strategy pattern). Implementations look at the quantities of
 * items still available (i.e. not yet consumed by an earlier rule) and decide how many of them
 * they can charge for as a group, e.g. "3 for the price of 2" or "one D and one E for £3".
 *
 * <p>Implementations must not mutate the {@code availableQuantities} map they are given; instead
 * they report back what they consumed via {@link RuleApplication}, and the caller (see
 * {@link Checkout}) is responsible for deducting it before running the next rule.
 */
public interface PromotionRule {

    /**
     * @param availableQuantities quantities of each SKU still available to be priced, keyed by SKU
     * @return the price charged and quantities consumed by this rule; {@link RuleApplication#none()}
     *     if the rule found nothing to apply (e.g. not enough items to form a group)
     */
    RuleApplication apply(Map<String, Integer> availableQuantities);
}
