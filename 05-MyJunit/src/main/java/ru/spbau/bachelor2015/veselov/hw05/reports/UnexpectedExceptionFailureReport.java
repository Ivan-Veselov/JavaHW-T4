package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * A report on test-case which failed because of an unexpected exception.
 */
public class UnexpectedExceptionFailureReport extends MyJunitReport {
    private final @NotNull Throwable cause;

    private final long estimatedMillis;

    /**
     * Creates a report.
     *
     * @param className a name of a class which method is considered as a test-case.
     * @param methodName a name of a method which is considered as a test-case.
     * @param cause an exception which caused failure.
     * @param estimatedMillis an estimated run time of a test.
     */
    public UnexpectedExceptionFailureReport(final @NotNull String className,
                                            final @NotNull String methodName,
                                            final @NotNull Throwable cause,
                                            final long estimatedMillis) {
        super(className, methodName);

        this.cause = cause;
        this.estimatedMillis = estimatedMillis;
    }

    /**
     * Returns an exception which caused failure.
     */
    public @NotNull Throwable getCause() {
        return cause;
    }

    /**
     * Returns a string description of this report.
     */
    @Override
    public @NotNull String report() {
        return super.report() + " Test failed with unexpected exception " + cause + " in " + estimatedMillis + "ms";
    }
}
