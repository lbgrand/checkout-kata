package com.fluro.checkout.rules;

import com.fluro.checkout.RuleApplication;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> new ComboRule(Map.of("D", 1), 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a combo must contain at least 2 distinct SKUs");
    }

    @Test
    void rejectsNullComponents() {
        assertThatThrownBy(() -> new ComboRule(null, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a combo must contain at least 2 distinct SKUs");
    }

    @Test
    void rejectsEmptyComponents() {
        assertThatThrownBy(() -> new ComboRule(Map.of(), 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a combo must contain at least 2 distinct SKUs");
    }

    @Test
    void rejectsBlankSkuInComponents() {
        Map<String, Integer> components = new HashMap<>();
        components.put("D", 1);
        components.put(" ", 1);

        assertThatThrownBy(() -> new ComboRule(components, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sku must not be blank");
    }

    @Test
    void rejectsNullQuantityInComponents() {
        Map<String, Integer> components = new HashMap<>();
        components.put("D", 1);
        components.put("E", null);

        assertThatThrownBy(() -> new ComboRule(components, 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity for E must be at least 1");
    }

    @Test
    void rejectsNonPositiveQuantityInComponents() {
        assertThatThrownBy(() -> new ComboRule(Map.of("D", 1, "E", 0), 300))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity for E must be at least 1");
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThatThrownBy(() -> new ComboRule(Map.of("D", 1, "E", 1), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("priceInPence must be positive");
    }
}
