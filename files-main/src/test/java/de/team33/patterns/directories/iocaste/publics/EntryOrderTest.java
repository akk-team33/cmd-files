package de.team33.patterns.directories.iocaste.publics;

import de.team33.patterns.directories.iocaste.DirectoryStreamer;
import de.team33.patterns.directories.iocaste.EntryOrder;
import de.team33.patterns.directories.iocaste.FileEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryOrderTest {

    private static final Comparator<FileEntry> EXPECTED =
            Comparator.comparing(FileEntry::toString, EntryOrderTest::combined);
    private static final DirectoryStreamer STREAMER =
            DirectoryStreamer.DEFAULT.skip(entry -> entry.name().equals("target"));
    private static final Path SRC_PATH = Path.of(".");
    private static final Path PARENT_PATH = SRC_PATH.toAbsolutePath().normalize().getParent();

    private static int combined(final String left, final String right) {
        final int result = left.compareToIgnoreCase(right);
        return (0 == result) ? left.compareTo(right) : result;
    }

    private static Path relative(final FileEntry entry) {
        return PARENT_PATH.relativize(entry.path());
    }

    private static Stream<FileEntry> entries() {
        return STREAMER.stream(SRC_PATH);
    }

    static Stream<Case> cases() {
        return Stream.of(caseOf(EntryOrder.BY_PATH, EXPECTED),
                         caseOf(EntryOrder.BY_NAME, Comparator.comparing(FileEntry::name, EntryOrderTest::combined)
                                                              .thenComparing(EXPECTED)/**/),
                         caseOf(EntryOrder.BY_SIZE, Comparator.comparing(FileEntry::size)
                                                              .thenComparing(EXPECTED)/**/),
                         caseOf(EntryOrder.BY_UPDATE, Comparator.comparing(FileEntry::lastModified)
                                                                .thenComparing(EXPECTED)/**/));
    }

    private static Case caseOf(final Comparator<FileEntry> order, final Comparator<FileEntry> expected) {
        return new Case(order, entries().sorted(expected).map(EntryOrderTest::relative).toList());
    }

    @ParameterizedTest
    @MethodSource("cases")
    final void test(final Case given) {
        final List<Path> result = entries().sorted(given.order)
                                           .map(EntryOrderTest::relative)
                                           .toList();
        // System.out.println(result);
        assertEquals(given.expected, result);
    }

    record Case(Comparator<FileEntry> order, List<Path> expected) {
    }
}