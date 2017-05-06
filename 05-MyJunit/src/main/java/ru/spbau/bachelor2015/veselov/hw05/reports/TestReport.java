package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

/**
 * This interface represents a report on execution of one particular test-case.
 */
public interface TestReport {
    /**
     * Returns a string description of this report.
     */
    @NotNull String report();
}
