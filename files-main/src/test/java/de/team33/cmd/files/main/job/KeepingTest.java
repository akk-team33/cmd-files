package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KeepingTest implements Context {

    @Test
    final void run() {
        Keeping.job(this, Arrays.asList("files", "keep", ".", "jpg,jpe,jpeg", "tif,tiff")).run();
    }
}