package de.team33.cmd.files.listing;

import de.team33.patterns.directories.iocaste.FileEntry;

public interface Report {

    void addTotal(FileEntry entry);
}
