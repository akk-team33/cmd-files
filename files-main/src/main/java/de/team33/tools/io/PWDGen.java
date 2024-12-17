package de.team33.tools.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class PWDGen {

    private static final String CHARS = "abcdefghijklmnupqrstuvwxyz-ABCDEFGHIJKLMNUPQRSTUVWXYZ_0123456789.@!§$%&/=?";
    private static final BigInteger CHARS_LEN = BigInteger.valueOf(CHARS.length());

    public static void main(String[] args) throws IOException {
        assert 0 < args.length : "Argument missing!";
        for (int index = 0; index < 10; ++index) {
            final String pwd = newPWD(args[0], index);
            System.out.printf("%d: %s%n", index, pwd);
        }
    }

    private static String newPWD(final String arg, final int index) throws IOException {
        final InputStream in = new ByteArrayInputStream("%d:%s".formatted(index, arg).getBytes(StandardCharsets.UTF_8));
        final byte[] hash = StreamHashing.MD5.hash(in);
        final StringBuilder result = new StringBuilder();
        for (BigInteger bigInteger = new BigInteger(hash);
             BigInteger.ZERO.compareTo(bigInteger) != 0;
             bigInteger = bigInteger.divide(CHARS_LEN)) {
            final int nextCharIndex = bigInteger.mod(CHARS_LEN).intValue();
            result.append(CHARS.charAt(nextCharIndex));
        }
        return result.toString();
    }
}
