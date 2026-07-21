package de.team33.patterns.io.gamma.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class JsonParserTest {

    static Stream<ParseCase> parseCases() {
        return Stream.of(new ParseCase("null", JsonValue.NULL),
                         new ParseCase(" \n\t null \r\f", JsonValue.NULL),
                         new ParseCase(" true", new JsonBoolean(true)),
                         new ParseCase("false ", new JsonBoolean(false)),
                         new ParseCase("1", new JsonNumber(BigDecimal.ONE)),
                         new ParseCase("278", new JsonNumber(BigDecimal.valueOf(278))),
                         new ParseCase(" 123456789.123456789",
                                       new JsonNumber(new BigDecimal("123456789.123456789"))),
                         new ParseCase(" 0.123E1234  ", new JsonNumber(new BigDecimal("0.123E1234"))),
                         new ParseCase("-1", new JsonNumber(BigDecimal.valueOf(-1))),
                         new ParseCase("0", new JsonNumber(BigDecimal.valueOf(0))),
                         new ParseCase("-123.45", new JsonNumber(BigDecimal.valueOf(-123.45))),
                // TODO: new ParseCase("1E5", new JsonNumber(BigDecimal.valueOf(1E5))),
                         new ParseCase("-1.2e-5", new JsonNumber(BigDecimal.valueOf(-1.2e-5))),
                         new ParseCase(" \" abc \" ", new JsonString(" abc ")),
                         new ParseCase("\t \"\\\\\\\"\\b\\f\\n\\r\\t\" \n",
                                       new JsonString("\\\"\b\f\n\r\t")),
                // TODO:
                         new ParseCase("{}", JsonObject.builder().build()),
                         new ParseCase("{\n" +
                                       "        \"name1\" : \"value1\" ,\n" +
                                       "        \"name2\" : \"value2\"\n,\n" +
                                       "        \"name3\" : \"value3\"\n" +
                                       "    }",
                                       JsonObject.builder()
                                                 .put("name1", new JsonString("value1"))
                                                 .put("name2", new JsonString("value2"))
                                                 .put("name3", new JsonString("value3"))
                                                 .build()));
    }

    static Stream<String> failCases() {
        return Stream.of("",
                         "{null}",
                         "{",
                         "}",
                // TODO: "[",
                         "]",
                         "tru",
                         "nul",
                         "fal",
                // TODO: "01",
                // TODO: "1.",
                // TODO: ".5",
                         "{\"a\"}",
                         "{:\"b\"}",
                         "{\"a\":}",
                         "{\"a\",1}",
                         "{,\"a\":1}",
                         "{\"a\":1,}",
                         "\"abc",
                         "\"\\q\"");
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void parse(final ParseCase given) {
        final JsonValue result = JsonParser.parse(given.source);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @MethodSource("failCases")
    final void parse_fail(final String given) {
        try {
            final JsonValue result = JsonParser.parse(given);
            fail("expected to fail - but was %s".formatted(result));
        } catch (final IllegalArgumentException e) {
            // as expected
        }
    }

    record ParseCase(String source, JsonValue expected) {
    }
}