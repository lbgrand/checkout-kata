package com.fluro.checkout.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutCliTest {

    @Test
    void oneShotModePrintsTotalAndExits() throws IOException {
        String output = run(new String[] {"ABCDABA"}, "");

        // A*3=150, B*2=125 (2 for 125), C*1=25, D*1=150 -> 450 pence
        assertThat(output).contains("£4.50");
    }

    @Test
    void interactiveSessionSupportsScanTotalResetAndExit() throws IOException {
        String input = String.join("\n", "AB", "total", "reset", "total", "exit", "");

        String output = run(new String[0], input);

        assertThat(output).contains("Total: £1.25");
        assertThat(output).contains("Basket reset.");
        assertThat(output).contains("Total: £0.00");
        assertThat(output).contains("Goodbye!");
    }

    @Test
    void interactiveSessionReportsUnknownSkusButKeepsGoing() throws IOException {
        String output = run(new String[0], String.join("\n", "AZB", "total", "exit", ""));

        assertThat(output).contains("Skipping 'Z'");
        assertThat(output).contains("Total: £1.25"); // A + B still scanned
    }

    @Test
    void loadsCustomPricingRulesFromFile(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path pricesFile = tempDir.resolve("prices.txt");
        Files.writeString(pricesFile, "UNIT A 999\n");

        String output = run(new String[] {"--prices", pricesFile.toString(), "A"}, "");

        assertThat(output).contains("Loaded pricing rules from " + pricesFile);
        assertThat(output).contains("£9.99");
    }

    private static String run(String[] args, String stdin) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        CheckoutCli.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBytes, true, StandardCharsets.UTF_8));
        return outBytes.toString(StandardCharsets.UTF_8);
    }
}
