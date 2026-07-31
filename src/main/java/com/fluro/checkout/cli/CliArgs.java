package com.fluro.checkout.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parsed command-line arguments for {@link CheckoutCli}.
 *
 * <p>Usage: {@code checkout [--prices <file>] [BASKET]}, where {@code BASKET} is an optional
 * string of SKUs (e.g. {@code ABCDABA}) to price immediately in one shot; if omitted, an
 * interactive session is started instead.
 */
record CliArgs(Optional<Path> pricesFile, Optional<String> basket) {

    static CliArgs parse(String[] args) {
        Optional<Path> pricesFile = Optional.empty();
        List<String> positional = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if ("--prices".equals(args[i])) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--prices requires a file path argument");
                }
                pricesFile = Optional.of(Path.of(args[++i]));
            } else {
                positional.add(args[i]);
            }
        }

        Optional<String> basket = positional.isEmpty() ? Optional.empty() : Optional.of(String.join("", positional));
        return new CliArgs(pricesFile, basket);
    }
}
