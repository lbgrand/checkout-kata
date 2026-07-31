package com.fluro.checkout.cli;

import com.fluro.checkout.Checkout;
import com.fluro.checkout.PricingRules;
import com.fluro.checkout.SamplePricingRules;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

/**
 * Command-line front end for the supermarket checkout.
 *
 * <ul>
 *   <li>{@code checkout} - starts an interactive session using this week's default prices.</li>
 *   <li>{@code checkout --prices rules.txt} - starts an interactive session using a custom
 *       pricing sheet, loaded fresh for this transaction (see {@link PricingRulesFileParser}).</li>
 *   <li>{@code checkout ABCDABA} - scans the given SKUs, prints the total and exits (handy for
 *       scripting); can be combined with {@code --prices}.</li>
 * </ul>
 */
public final class CheckoutCli {

    private CheckoutCli() {
    }

    public static void main(String[] args) {
        try {
            run(args, System.in, System.out);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static void run(String[] args, InputStream in, PrintStream out) throws IOException {
        CliArgs cliArgs = CliArgs.parse(args);
        PricingRules pricingRules = loadPricingRules(cliArgs, out);

        if (cliArgs.basket().isPresent()) {
            Checkout checkout = new Checkout(pricingRules);
            scanAll(checkout, cliArgs.basket().get(), out);
            out.println(formatPrice(checkout.totalPriceInPence()));
            return;
        }

        runInteractiveSession(pricingRules, in, out);
    }

    private static PricingRules loadPricingRules(CliArgs cliArgs, PrintStream out) throws IOException {
        if (cliArgs.pricesFile().isEmpty()) {
            out.println("Using this week's default pricing rules.");
            return SamplePricingRules.thisWeek();
        }
        Path path = cliArgs.pricesFile().get();
        try (Reader reader = Files.newBufferedReader(path)) {
            PricingRules rules = PricingRulesFileParser.parse(reader);
            out.println("Loaded pricing rules from " + path);
            return rules;
        }
    }

    private static void runInteractiveSession(PricingRules pricingRules, InputStream in, PrintStream out) {
        out.println("Welcome to the supermarket checkout!");
        printHelp(out);
        Checkout checkout = new Checkout(pricingRules);

        Scanner scanner = new Scanner(in);
        while (true) {
            out.print("> ");
            out.flush();
            if (!scanner.hasNextLine()) {
                out.println();
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            switch (line.toLowerCase(Locale.ROOT)) {
                case "exit", "quit" -> {
                    out.println("Goodbye!");
                    return;
                }
                case "help" -> printHelp(out);
                case "total" -> out.println("Total: " + formatPrice(checkout.totalPriceInPence()));
                case "reset" -> {
                    checkout = new Checkout(pricingRules);
                    out.println("Basket reset.");
                }
                default -> scanAll(checkout, line, out);
            }
        }
    }

    private static void scanAll(Checkout checkout, String skus, PrintStream out) {
        for (char c : skus.toCharArray()) {
            if (Character.isWhitespace(c) || c == ',') {
                continue;
            }
            try {
                checkout.scan(String.valueOf(c));
            } catch (IllegalArgumentException e) {
                out.println("Skipping '" + c + "': " + e.getMessage());
            }
        }
    }

    private static void printHelp(PrintStream out) {
        out.println("Scan items by typing their SKUs (e.g. \"ABCDABA\"), then use one of:");
        out.println("  total  - show the current basket's total price");
        out.println("  reset  - empty the basket and start again");
        out.println("  help   - show this message");
        out.println("  exit   - quit");
    }

    private static String formatPrice(long priceInPence) {
        return String.format(Locale.ROOT, "£%d.%02d", priceInPence / 100, Math.abs(priceInPence % 100));
    }
}
