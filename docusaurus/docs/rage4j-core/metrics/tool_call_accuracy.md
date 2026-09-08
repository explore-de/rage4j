# Tool Call Accuracy

**Evaluator**: `ToolCallAccuracyEvaluator`

The Tool Call Accuracy metric measures how many of the tool calls a test expects were actually performed by the
language model. It compares the expected tool calls of a `Sample` against the tool calls the model reported, without
involving a language model itself.

An expected call matches an actual one when the tool names are equal and every expected argument is present with an
equivalent value. Expected arguments are therefore a subset of the actual ones, so an expectation without arguments
matches any call of the same name. Top level numbers are compared by numeric value, so an `Integer` argument matches
an equivalent `Long`. Values nested inside a map or list are compared with `equals`, so numbers within them must also
match in type.

## Required Sample Fields

- `expectedToolCalls` – Required. The tool calls the test expects to have been performed.
- `toolCalls` – Required. The tool calls the model actually performed.

## How It Works

1. Pairs the expected tool calls with the actual ones so that each actual call satisfies at most one expectation.
2. The pairing is a maximum matching, not a first-match-wins scan. An expectation without arguments matches any call
   of its name, and taking the first match would let it consume the very call a more specific expectation of the same
   tool needs — reporting a failure even though a pairing satisfying both exists.
3. The score is the number of matched expectations divided by the total number of expected tool calls.
4. Additional actual calls that no expectation covers do not lower the score.

Throws `IllegalArgumentException` if the sample has no expected tool calls, or if no actual tool calls were recorded
on it.

## Score Interpretation

- **Range**: `0.0` (no expected call was performed) to `1.0` (every expected call was performed)
- **Higher scores** indicate the model performed more of the tool calls a test expects.
- **Lower scores** indicate the model skipped expected tool calls or called a tool with different arguments than
  expected.
- Unexpected extra calls are not penalized by this metric. Use `assertNoUnexpectedToolCalls` if extra calls should
  fail the test.

## Declaring Expectations by Method Reference

In the fluent assertion API an expected tool call can be declared by pointing at the tool method itself instead of
naming it as a string:

```java
rageAssert.given()
    .question("Wie wird das Wetter morgen in Berlin?")
    .expectedToolCall(WeatherTools::getWeather)
    .withArgument("city", "Berlin")
    .when()
    .answerFrom(assistant::chat)
    .then()
    .assertToolCallAccuracy(1.0);
```

This keeps the expectation refactoring-safe and honours a `@Tool(name = "...")` override, which a hand-written string
would get wrong. Two limitations apply:

- **Overloaded tool methods** cannot be referenced this way, because the type of the method reference cannot be
  inferred. Use the `expectedToolCall(String)` overload for those, and for tools that have no Java method at all, such
  as MCP tools.
- **Under JPMS** the module declaring the tool class must be open, because the tool name is resolved by reading the
  method reference's `writeReplace`. Projects using Rage4J from the classpath are unaffected.

## Example Usage

```java
ToolCallAccuracyEvaluator evaluator = new ToolCallAccuracyEvaluator();
Evaluation result = evaluator.evaluate(sample);
double toolCallAccuracy = result.getValue();
```
