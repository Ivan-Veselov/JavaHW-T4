package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * A report on a passed test-case.
 */
public class PassReport extends MyJunitReport {
    private final long estimatedMillis;

    /**
     * Creates a report.
     *
     * @param className a name of a class which method is considered as a test-case.
     * @param methodName a name of a method which is considered as a test-case.
     * @param estimatedMillis an estimated run time of a test.
     */
    public PassReport(final @NotNull String className, final @NotNull String methodName, final long estimatedMillis) {
        super(className, methodName);

        this.estimatedMillis = estimatedMillis;
    }

    /**
     * Returns a string description of this report.
     */
    @Override
    public @NotNull String report() {
        return super.report() + " Test passed in " + estimatedMillis + "ms";
    }
}
