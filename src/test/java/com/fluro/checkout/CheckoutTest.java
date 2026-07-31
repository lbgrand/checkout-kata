package com.fluro.checkout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises {@link Checkout} against this week's pricing sheet from the kata specification:
 * <pre>
 * A | 50  |
 * B | 75  | 2 for 125
 * C | 25  | Buy 3, get one free
 * D | 150 | Buy D and E for 300
 * E | 200 | Buy D and E for 300
 * </pre>
 */
class CheckoutTest {

    private Checkout checkout;

    @BeforeEach
    void setUp() {
        checkout = new Checkout(SamplePricingRules.thisWeek());
    }

    @Test
    void emptyBasketCostsNothing() {
        assertThat(checkout.totalPriceInPence()).isZero();
    }

    @ParameterizedTest(name = "{0} totals {1} pence")
    @CsvSource({
            "A, 50",
            "AB, 125",
            "CDBA, 300",
            "AA, 100",
            "AAA, 150",
            "BB, 125",           // 2 for 125
            "BBB, 200",          // 1 group of 2 (125) + 1 at unit price (75)
            "BABB, 250",         // A(50) + BBB(125+75)
            "CC, 50",            // no discount yet
            "CCC, 75",           // still no discount (need 4 for the "get 1 free" group)
            "CCCC, 75",          // buy 3, get 1 free
            "CCCCC, 100",        // one free group (75) + one at unit price (25)
            "CCCCCCCC, 150",     // two free groups
            "DE, 300",           // meal deal
            "DDE, 450",          // one meal deal (300) + one D at unit price (150)
            "DEE, 500",          // one meal deal (300) + one E at unit price (200)
            "DDEE, 600",         // two meal deals
    })
    void computesExpectedTotal(String basket, long expectedTotalInPence) {
        basket.chars().forEach(c -> checkout.scan(String.valueOf((char) c)));

        assertThat(checkout.totalPriceInPence()).isEqualTo(expectedTotalInPence);
    }

    @Test
    void scanOrderDoesNotAffectTotal() {
        // "if we scan a B, then an A, then another B, we recognise the two B's"
        checkout.scan("B");
        checkout.scan("A");
        checkout.scan("B");

        assertThat(checkout.totalPriceInPence()).isEqualTo(125 + 50);
    }

    @Test
    void scanIsCaseInsensitive() {
        checkout.scan("a");
        checkout.scan("b");

        assertThat(checkout.totalPriceInPence()).isEqualTo(50 + 75);
    }

    @Test
    void rejectsUnknownSku() {
        assertThatThrownBy(() -> checkout.scan("Z")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exposesScannedQuantities() {
        checkout.scan("A");
        checkout.scan("A");
        checkout.scan("B");

        assertThat(checkout.scannedQuantities()).containsEntry("A", 2).containsEntry("B", 1);
    }

    @Test
    void fullBasketFromSpecExample() {
        "CCCCBAAB".chars().forEach(c -> checkout.scan(String.valueOf((char) c)));

        // C*4 -> 75 (buy 3 get 1 free), B*2 -> 125, A*2 -> 100
        assertThat(checkout.totalPriceInPence()).isEqualTo(75 + 125 + 100);
    }
}
