package com.fluro.checkout.cli;

import com.fluro.checkout.PricingRules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a plain-text pricing sheet into a {@link PricingRules}, so a fresh set of rules can be
 * supplied at the start of every checkout transaction (e.g. via the {@code --prices} CLI flag)
 * without recompiling anything.
 *
 * <p>File format: one directive per line, blank lines and lines starting with {@code #} ignored.
 *
 * <pre>
 * # Unit prices
 * UNIT A 50
 * UNIT B 75
 * UNIT C 25
 * UNIT D 150
 * UNIT E 200
 *
 * # Special prices - unit price for the SKU must be declared with UNIT first
 * MULTI_BUY B 2 125       # 2 Bs for 125 pence
 * BUY_GET_FREE C 3 1      # buy 3 Cs, get 1 free
 * COMBO 300 D:1 E:1       # one D and one E for 300 pence
 * </pre>
 */
public final class PricingRulesFileParser {

    private PricingRulesFileParser() {
    }

    public static PricingRules parse(Reader reader) throws IOException {
        PricingRules.Builder builder = PricingRules.builder();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String rawLine;
            int lineNumber = 0;
            while ((rawLine = bufferedReader.readLine()) != null) {
                lineNumber++;
                parseLine(builder, stripComment(rawLine).trim(), lineNumber);
            }
        }
        return builder.build();
    }

    private static String stripComment(String line) {
        int hashIndex = line.indexOf('#');
        return hashIndex >= 0 ? line.substring(0, hashIndex) : line;
    }

    private static void parseLine(PricingRules.Builder builder, String line, int lineNumber) {
        if (line.isEmpty()) {
            return;
        }
        String[] tokens = line.split("\\s+");
        try {
            switch (tokens[0]) {
                case "UNIT" -> builder.unitPrice(tokens[1], Long.parseLong(tokens[2]));
                case "MULTI_BUY" -> builder.multiBuy(tokens[1], Integer.parseInt(tokens[2]), Long.parseLong(tokens[3]));
                case "BUY_GET_FREE" -> builder.buyNGetMFree(tokens[1], Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
                case "COMBO" -> builder.combo(Long.parseLong(tokens[1]), parseComboComponents(tokens, lineNumber));
                default -> throw new PricingRulesParseException(lineNumber, "Unknown directive '" + tokens[0] + "'");
            }
        } catch (PricingRulesParseException e) {
            throw e;
        } catch (Exception e) {
            throw new PricingRulesParseException(lineNumber, "Malformed line: '" + line + "' (" + e.getMessage() + ")", e);
        }
    }

    private static Map<String, Integer> parseComboComponents(String[] tokens, int lineNumber) {
        if (tokens.length < 4) {
            throw new PricingRulesParseException(lineNumber, "COMBO requires a price and at least 2 SKU:quantity components");
        }
        Map<String, Integer> components = new LinkedHashMap<>();
        for (String token : List.of(tokens).subList(2, tokens.length)) {
            String[] parts = token.split(":");
            if (parts.length != 2) {
                throw new PricingRulesParseException(lineNumber, "Expected SKU:quantity but got '" + token + "'");
            }
            components.put(parts[0], Integer.parseInt(parts[1]));
        }
        return components;
    }
}
