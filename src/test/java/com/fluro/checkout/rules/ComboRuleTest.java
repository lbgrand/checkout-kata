package com.fluro.checkout.rules;

import com.fluro.checkout.RuleApplication;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComboRuleTest {

    // "One D and one E for £3".
    private final ComboRule dAndEMealDeal = new ComboRule(Map.of("D", 1, "E", 1), 300);

    @Test
    void appliesNothingWhenAnIngredientIsMissing() {
        RuleApplication result = dAndEMealDeal.apply(Map.of("D", 1));

        assertThat(result).isEqualTo(RuleApplication.none());
    }

    @Test
    void appliesOnceWhenOneOfEachAvailable() {
        RuleApplication result = dAndEMealDeal.apply(Map.of("D", 1, "E", 1));

        assertThat(result.priceInPence()).isEqualTo(300);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("D", 1, "E", 1));
    }

    @Test
    void isLimitedByTheScarcerIngredient() {
        RuleApplication result = dAndEMealDeal.apply(Map.of("D", 3, "E", 1));

        assertThat(result.priceInPence()).isEqualTo(300);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("D", 1, "E", 1));
    }

    @Test
    void supportsCombosWithUnevenQuantities() {
        ComboRule twoAppleOneOrangeDeal = new ComboRule(Map.of("A", 2, "O", 1), 120);

        RuleApplication result = twoAppleOneOrangeDeal.apply(Map.of("A", 5, "O", 2));

        // Limited to 2 combos: needs 4 A's (have 5) and 2 O's (have 2).
        assertThat(result.priceInPence()).isEqualTo(240);
        assertThat(result.consumedQuantities()).isEqualTo(Map.of("A", 4, "O", 2));
    }

    @Test
    void rejectsSingleSkuCombo() {
        assertThrows(IllegalArgumentException.class, () -> new ComboRule(Map.of("D", 1), 300));
    }
}
