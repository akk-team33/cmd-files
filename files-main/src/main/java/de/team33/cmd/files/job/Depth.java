package de.team33.cmd.files.job;

import de.team33.cmd.files.listing.Recursion;

enum Depth {

    FLAT(Recursion.FLAT),
    DEEP(Recursion.DEEP);

    private final Recursion recursion;

    Depth(final Recursion recursion) {
        this.recursion = recursion;
    }

    final Recursion recursion() {
        return recursion;
    }
}
