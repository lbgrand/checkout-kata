package com.fluro.checkout.rules;

import com.fluro.checkout.RuleApplication;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(IllegalArgumentException.class, () -> new MultiBuyRule("A", 1, 50));
    }
}
