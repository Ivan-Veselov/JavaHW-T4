package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * A report on test-case which failed because of an exception wasn't thrown.
 */
public class NoExceptionFailureReport extends MyJunitReport {
    private final @NotNull Class<?> expectedException;

    private final long estimatedMillis;

    /**
     * Creates a report.
     *
     * @param className a name of a class which method is considered as a test-case.
     * @param methodName a name of a method which is considered as a test-case.
     * @param expectedException a type of an exception which was expected to be thrown during test execution.
     * @param estimatedMillis an estimated run time of a test.
     */
    public NoExceptionFailureReport(final @NotNull String className,
                                    final @NotNull String methodName,
                                    final @NotNull Class<?> expectedException,
                                    final long estimatedMillis) {
        super(className, methodName);

        this.expectedException = expectedException;
        this.estimatedMillis = estimatedMillis;
    }

    /**
     * Returns a type of an exception which was expected to be thrown during test execution.
     */
    public @NotNull Class<?> getExpectedException() {
        return expectedException;
    }

    /**
     * Returns a string description of this report.
     */
    @Override
    public @NotNull String report() {
        return super.report() + " Test failed in " + estimatedMillis + "ms" +
                ". Expected " + expectedException + ", but no exception was thrown";
    }
}
