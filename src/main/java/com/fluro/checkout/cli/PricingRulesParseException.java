package com.fluro.checkout.cli;

/** Thrown when a pricing rules file cannot be parsed, pointing at the offending line. */
public final class PricingRulesParseException extends RuntimeException {

    public PricingRulesParseException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
    }

    public PricingRulesParseException(int lineNumber, String message, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
    }
}
