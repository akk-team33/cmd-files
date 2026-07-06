package de.team33.cmd.files.matching;

import org.junit.jupiter.api.Test;

import static java.util.regex.Pattern.quote;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WildcardStringTest {

    static final String NO_WILDCARD_HERE = "no wildcard here";

    @Test
    void toRegExp_STAR() {
        final String expected = ".*";
        final String result = WildcardString.toRegExp("*");
        assertEquals(expected, result);
    }

    @Test
    void toRegExp_QM() {
        final String expected = ".";
        final String result = WildcardString.toRegExp("?");
        assertEquals(expected, result);
    }

    @Test
    void toRegExp_NO_WILDCARD_HERE() {
        final String expected = quote(NO_WILDCARD_HERE);
        final String result = WildcardString.toRegExp(NO_WILDCARD_HERE);
        assertEquals(expected, result);
    }

    @Test
    void toRegExp() {
        final String expected = ".*" + quote("wdl") + "." + quote(".brm.") + ".*";
        final String result = WildcardString.toRegExp("*wdl?.brm.*");
        assertEquals(expected, result);
    }
}
