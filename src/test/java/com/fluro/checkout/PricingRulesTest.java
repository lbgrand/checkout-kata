package com.fluro.checkout;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingRulesTest {

    @Test
    void normalizesSkusToUppercase() {
        PricingRules rules = PricingRules.builder()
                .unitPrice("a", 50)
                .build();

        assertThat(rules.hasUnitPrice("A")).isTrue();
        assertThat(rules.unitPrice("A")).isEqualTo(50);
    }

    @Test
    void unknownSkuHasNoUnitPrice() {
        PricingRules rules = PricingRules.builder().unitPrice("A", 50).build();

        assertThat(rules.hasUnitPrice("Z")).isFalse();
        assertThatThrownBy(() -> rules.unitPrice("Z"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No unit price configured for SKU 'Z'");
    }

    @Test
    void rejectsDuplicateUnitPrice() {
        assertThatThrownBy(() -> PricingRules.builder().unitPrice("A", 50).unitPrice("A", 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit price for SKU 'A' already defined");
    }

    @Test
    void rejectsSpecialRuleForUnknownSku() {
        assertThatThrownBy(() -> PricingRules.builder().multiBuy("B", 2, 125))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot add a special rule for unknown SKU 'B'; call unitPrice(...) first");
    }

    @Test
    void rejectsComboReferencingUnknownSku() {
        assertThatThrownBy(() -> PricingRules.builder()
                .unitPrice("D", 150)
                .combo(300, Map.of("D", 1, "E", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot add a special rule for unknown SKU 'E'; call unitPrice(...) first");
    }

    @Test
    void buildsIndependentSnapshots() {
        PricingRules.Builder builder = PricingRules.builder().unitPrice("A", 50);
        PricingRules first = builder.build();
        builder.unitPrice("B", 75);
        PricingRules second = builder.build();

        assertThat(first.hasUnitPrice("B")).isFalse();
        assertThat(second.hasUnitPrice("B")).isTrue();
    }
}
