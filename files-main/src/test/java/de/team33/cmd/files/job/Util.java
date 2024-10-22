package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.CoreCondition;
import de.team33.cmd.files.common.Output;

class Util {

    static Condition condition(final Output out, final String ... args) {
        return Condition.of(CoreCondition.of(out, args)).orElseThrow();
    }
}
