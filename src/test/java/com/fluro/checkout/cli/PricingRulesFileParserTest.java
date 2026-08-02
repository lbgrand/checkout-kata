package com.fluro.checkout.cli;

import com.fluro.checkout.Checkout;
import com.fluro.checkout.PricingRules;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingRulesFileParserTest {

    private static final String FULL_SHEET = """
            # Unit prices
            UNIT A 50
            UNIT B 75
            UNIT C 25
            UNIT D 150
            UNIT E 200

            # Special prices
            MULTI_BUY B 2 125   # 2 Bs for 125 pence
            BUY_GET_FREE C 3 1
            COMBO 300 D:1 E:1
            """;

    @Test
    void parsesAFullSheetMatchingTheSpec() throws IOException {
        PricingRules rules = PricingRulesFileParser.parse(new StringReader(FULL_SHEET));
        Checkout checkout = new Checkout(rules);

        "CCCCBAAB DE".chars().forEach(c -> {
            if (!Character.isWhitespace(c)) {
                checkout.scan(String.valueOf((char) c));
            }
        });

        // C*4=75, B*2=125, A*2=100, D+E=300
        assertThat(checkout.totalPriceInPence()).isEqualTo(75 + 125 + 100 + 300);
    }

    @Test
    void ignoresBlankLinesAndComments() throws IOException {
        PricingRules rules = PricingRulesFileParser.parse(new StringReader("""

                # just a comment

                UNIT A 50
                """));

        assertThat(rules.hasUnitPrice("A")).isTrue();
    }

    @Test
    void rejectsUnknownDirective() {
        assertThatThrownBy(() -> PricingRulesFileParser.parse(new StringReader("FOO A 1")))
                .isInstanceOf(PricingRulesParseException.class)
                .hasMessageContaining("Line 1: Unknown directive 'FOO'");
    }

    @Test
    void rejectsMalformedNumber() {
        assertThatThrownBy(() -> PricingRulesFileParser.parse(new StringReader("UNIT A fifty")))
                .isInstanceOf(PricingRulesParseException.class)
                .hasMessageContaining("Line 1: Malformed line: 'UNIT A fifty'");
    }

    @Test
    void rejectsSpecialRuleBeforeUnitPrice() {
        assertThatThrownBy(() -> PricingRulesFileParser.parse(new StringReader("MULTI_BUY B 2 125")))
                .isInstanceOf(PricingRulesParseException.class)
                .hasMessageContaining("Line 1: Malformed line: 'MULTI_BUY B 2 125'");
    }
}
