package com.fluro.checkout.rules;

import com.fluro.checkout.RuleApplication;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultiBuyRuleTest {

    private final MultiBuyRule twoBsFor125 = new MultiBuyRule("B", 2, 125);

    @Test
    void appliesNothingWhenNotEnoughItems() {
        RuleApplication result = twoBsFor125.apply(Map.of("B", 1));

        assertThat(result).isEqualTo(RuleApplication.none());
    }

    @Test
    void appliesOnceForExactlyOneGroup() {
        RuleApplication result = twoBsFor125.apply(Map.of("B", 2));

        assertThat(result.priceInPence()).isEqualTo(125);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("B", 2));
    }

    @Test
    void leavesRemainderUnconsumedForUnitPricing() {
        RuleApplication result = twoBsFor125.apply(Map.of("B", 5));

        assertThat(result.priceInPence()).isEqualTo(250);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("B", 4));
    }

    @Test
    void ignoresOtherSkus() {
        RuleApplication result = twoBsFor125.apply(Map.of("A", 10));

        assertThat(result).isEqualTo(RuleApplication.none());
    }

    @Test
    void rejectsGroupSizeSmallerThanTwo() {
        assertThatThrownBy(() -> new MultiBuyRule("A", 1, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be at least 2, got 1");
    }

    @Test
    void rejectsNullSku() {
        assertThatThrownBy(() -> new MultiBuyRule(null, 2, 125))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sku must not be blank");
    }

    @Test
    void rejectsBlankSku() {
        assertThatThrownBy(() -> new MultiBuyRule("  ", 2, 125))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sku must not be blank");
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThatThrownBy(() -> new MultiBuyRule("B", 2, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("priceInPence must be greater than zero");
    }
}
