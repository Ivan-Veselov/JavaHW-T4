package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * A report on an ignored test-case.
 */
public class IgnoreReport extends MyJunitReport {
    private final @NotNull String reason;

    /**
     * Creates a report.
     *
     * @param className a name of a class which method is considered as a test-case.
     * @param methodName a name of a method which is considered as a test-case.
     * @param reason a reason which describes why this test is ignored.
     */
    public IgnoreReport(final @NotNull String className,
                        final @NotNull String methodName,
                        final @NotNull String reason) {
        super(className, methodName);

        this.reason = reason;
    }

    /**
     * Returns a reason for ignore.
     */
    public @NotNull String getReason() {
        return reason;
    }

    /**
     * Returns a string description of this report.
     */
    @Override
    public @NotNull String report() {
        return super.report() + " Test is ignored. " + reason;
    }
}
