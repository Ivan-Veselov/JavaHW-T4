package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class FailureReport implements TestReport {
    private final @NotNull Throwable cause;

    private final long estimatedMillis;

    public FailureReport(final @NotNull Throwable cause, final long estimatedMillis) {
        this.cause = cause;
        this.estimatedMillis = estimatedMillis;
    }

    public @NotNull Throwable getCause() {
        return cause;
    }

    @Override
    public @NotNull String report() {
        return "Test failed with unexpected exception " + cause + " in " + estimatedMillis;
    }
}
