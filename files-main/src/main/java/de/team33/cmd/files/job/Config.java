package de.team33.cmd.files.job;

import de.team33.patterns.config.alpha.ConfigLevel;
import de.team33.patterns.config.alpha.ConfigRepo;
import de.team33.patterns.records.triton.Triton;

record Config(ListerConfig list, MovingConfig move, RegistrarConfig register) {

    private static final Config DEFAULT = new Config(ListerConfig.DEFAULT,
                                                     MovingConfig.DEFAULT,
                                                     RegistrarConfig.DEFAULT);
    private static final ConfigRepo<Config> REPO = new ConfigRepo<>(Config.class, DEFAULT);

    static Config read() {
        return REPO.read();
    }

    static Config read(final ConfigLevel level) {
        return REPO.read(level);
    }

    final void write(final ConfigLevel level) {
        REPO.write(level, this);
    }

    @Override
    public final String toString() {
        return Triton.toJson(this);
    }
}
