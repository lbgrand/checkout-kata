package com.fluro.checkout.rules;

import com.fluro.checkout.RuleApplication;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuyNGetMFreeRuleTest {

    // "Buy 3, get one free" on a 25p item.
    private final BuyNGetMFreeRule buy3Get1Free = new BuyNGetMFreeRule("C", 3, 1, 25);

    @Test
    void appliesNothingBelowGroupSize() {
        RuleApplication result = buy3Get1Free.apply(Map.of("C", 3));

        assertThat(result).isEqualTo(RuleApplication.none());
    }

    @Test
    void chargesForPaidItemsOnlyWhenGroupComplete() {
        RuleApplication result = buy3Get1Free.apply(Map.of("C", 4));

        assertThat(result.priceInPence()).isEqualTo(75); // 3 * 25p, 1 free
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("C", 4));
    }

    @Test
    void leavesRemainderForUnitPricing() {
        RuleApplication result = buy3Get1Free.apply(Map.of("C", 5));

        assertThat(result.priceInPence()).isEqualTo(75);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("C", 4));
    }

    @Test
    void handlesMultipleGroups() {
        RuleApplication result = buy3Get1Free.apply(Map.of("C", 8));

        assertThat(result.priceInPence()).isEqualTo(150); // 2 groups * 3 * 25p
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("C", 8));
    }

    @Test
    void rejectsNullSku() {
        assertThatThrownBy(() -> new BuyNGetMFreeRule(null, 3, 1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sku must not be blank");
    }

    @Test
    void rejectsBlankSku() {
        assertThatThrownBy(() -> new BuyNGetMFreeRule("  ", 3, 1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sku must not be blank");
    }

    @Test
    void rejectsPayQuantityBelowOne() {
        assertThatThrownBy(() -> new BuyNGetMFreeRule("C", 0, 1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("paidQuantity must be at least 1, got 0");
    }

    @Test
    void rejectsFreeQuantityBelowOne() {
        assertThatThrownBy(() -> new BuyNGetMFreeRule("C", 3, 0, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("freeQuantity must be at least 1, got 0");
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThatThrownBy(() -> new BuyNGetMFreeRule("C", 3, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unitPriceInPence must be greater than zero");
    }
}
