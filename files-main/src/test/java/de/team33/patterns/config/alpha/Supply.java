package de.team33.patterns.config.alpha;

import de.team33.patterns.arbitrary.mimas.Generator;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

public class Supply implements Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public BigInteger anyBits(final int numBits) {
        return new BigInteger(numBits, RANDOM);
    }

    public final SampleConfig anySampleConfig() {
        return new SampleConfig(anyString(), anyEntry(), anyItemList());
    }

    private SampleConfig.Entry anyEntry() {
        return new SampleConfig.Entry(Instant.now().plus(anyInt(), ChronoUnit.MILLIS), anyLong());
    }

    private List<SampleConfig.Item> anyItemList() {
        return Stream.generate(this::anyItem)
                     .limit(anyInt(10))
                     .toList();
    }

    private SampleConfig.Item anyItem() {
        return new SampleConfig.Item(anyInt(), anyInt());
    }
}
