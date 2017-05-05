package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class FailureReport implements TestReport {
    private final @NotNull Throwable cause;

    public FailureReport(final @NotNull Throwable cause) {
        this.cause = cause;
    }

    public @NotNull Throwable getCause() {
        return cause;
    }

    @Override
    public @NotNull String report() {
        return "Test failed with " + cause;
    }
}
