package de.team33.cmd.files.common;

import java.util.Optional;
import java.util.function.Supplier;

public class Condition extends CoreCondition {

    private final String subCmdName;
    private final String cmdSpec;

    private Condition(final CoreCondition origin, final String subCmdName) {
        super(origin);
        this.subCmdName = subCmdName;
        this.cmdSpec = "%s %s".formatted(cmdName(), subCmdName);
    }

    public static Optional<Condition> of(final CoreCondition origin) {
        if (1 < origin.args().size()) {
            return Optional.of(new Condition(origin, origin.args().get(1).toLowerCase()));
        } else {
            return Optional.empty();
        }
    }

    public final String subCmdName() {
        return subCmdName;
    }

    public final String cmdSpec() {
        return cmdSpec;
    }

    public final Supplier<RequestException> toRequestException(final Class<?> refClass) {
        return () -> RequestException.format(refClass,refClass.getSimpleName() + ".txt",
                                             cmdLine(), cmdSpec());
    }
}
