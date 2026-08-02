# Supermarket Checkout Kata

A checkout that totals a basket of items priced by SKU (single letters), supporting three kinds
of promotion:

- **Multipriced**: buy `n` of an item for a fixed total, e.g. "2 Bs for £1.25".
- **Buy N get M free**: e.g. "buy 3 Cs, get one free".
- **Meal deal / combo**: buy a set of *different* items together for a special price, e.g.
  "one D and one E for £3".

Pricing rules are not hard-coded into the checkout logic - a fresh set of rules can be built (or
loaded from a file) and handed to a checkout at the start of every transaction.

## Requirements

- Java 21
- Maven 3.9+

## Build & test

```bash
mvn test      # run the unit test suite
mvn package   # build target/checkout-kata.jar
```

## Running the CLI

```bash
# Interactive session using this week's default prices
java -jar target/checkout-kata.jar

# One-shot: scan a basket and print the total, then exit
java -jar target/checkout-kata.jar ABCDABA

# Use a custom pricing sheet for this transaction (see pricing-rules-example.txt)
java -jar target/checkout-kata.jar --prices pricing-rules-example.txt ABCDABA
```

Interactive session example:

```
$ java -jar target/checkout-kata.jar
Using this week's default pricing rules.
Welcome to the supermarket checkout!
Scan items by typing their SKUs (e.g. "ABCDABA"), then use one of:
  total  - show the current basket's total price
  reset  - empty the basket and start again
  help   - show this message
  exit   - quit
> B
> A
> B
> total
Total: £1.75
> exit
Goodbye!
```

### Supplying pricing rules per transaction

Pricing changes frequently, so the CLI can load a plain-text pricing sheet at startup with
`--prices <file>` instead of using the built-in defaults. See
[`pricing-rules-example.txt`](pricing-rules-example.txt) for the file format, which supports all
three promotion types:

```
UNIT A 50
UNIT B 75
MULTI_BUY B 2 125
UNIT C 25
BUY_GET_FREE C 3 1
UNIT D 150
UNIT E 200
COMBO 300 D:1 E:1
```

## Design

The core is small and deliberately separate from the CLI, so it can be reused (e.g. behind a REST
API) without change:

```
com.fluro.checkout
├── PricingRule            interface: Strategy for one promotion
├── RuleApplication         record: price charged + quantities consumed by a rule
├── PricingRules            immutable unit prices + ordered list of PricingRule, with a fluent Builder
├── Checkout                a single transaction: scan(sku), totalPriceInPence()
├── SamplePricingRules      "this week's prices" from the spec, used as the CLI default
└── rules/
    ├── MultiBuyRule        "N for £Y"
    ├── BuyNGetMFreeRule    "buy N get M free"
    └── ComboRule           "meal deal" across 2+ different SKUs

com.fluro.checkout.cli
├── CheckoutCli             entry point: interactive + one-shot modes
├── CliArgs                 argument parsing
└── PricingRulesFileParser  loads a PricingRules from a plain-text sheet
```

**Strategy pattern for promotions.** Each promotion type implements `PricingRule#apply`, which is
given the quantities of each SKU still available (not yet consumed by an earlier rule) and returns
how much it charged and how many units it used up (`RuleApplication`). `Checkout` runs every rule
in order, deducting what each one consumes, and finally charges anything left over at plain unit
price. This means:

- Adding a new promotion type only means implementing one small interface - `Checkout` and
  `PricingRules` never need to change.
- A single `ComboRule` implementation naturally generalises "meal deals" to any number of SKUs and
  any quantities per combo, not just "one of each of two items".
- Leftover items that don't complete a group (e.g. 5 Cs under "buy 3 get 1 free") fall through to
  unit pricing automatically, without special-casing in each rule.
- Only one special rule can be applied to a single item in the basket. This is by design, since 
  combining multiple special rules could lead to unexpectedly generous promotions in right combinations.
  Therefore, most companies would avoid multiple promos per item.

**Pluggable, per-transaction pricing.** `PricingRules` is immutable and built via a fluent
`Builder` (`PricingRules.builder().unitPrice("A", 50).multiBuy(...)...build()`), or parsed from a
text file by `PricingRulesFileParser`. A new instance can be constructed for every checkout
transaction, satisfying the requirement that pricing can change frequently. `Checkout` only
depends on the `PricingRules` abstraction, not on how it was built.

**Money as pence (`long`).** All prices are handled as integer pence rather than floating-point
pounds, avoiding rounding errors; the CLI formats pence as `£x.xx` only at the presentation layer.

**Basket order doesn't matter.** `Checkout` tallies quantities per SKU as items are scanned, so
promotions are applied the same way regardless of scan order (e.g. scanning `B, A, B` gives the
same total as `A, B, B`).
