package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * Base class for reports.
 */
public abstract class MyJunitReport implements TestReport {
    private final @NotNull String className;

    private final @NotNull String methodName;

    /**
     * Create a report for a particular method.
     *
     * @param className a name of a class which method is considered as a test-case.
     * @param methodName a name of a method which is considered as a test-case.
     */
    public MyJunitReport(final @NotNull String className, final @NotNull String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    /**
     * Returns a string description of this report.
     */
    public @NotNull String report() {
        return className + "." + methodName + ":";
    }
}
