package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class IgnoreReport implements TestReport {
    private final @NotNull String reason;

    public IgnoreReport(final @NotNull String reason) {
        this.reason = reason;
    }

    public @NotNull String getReason() {
        return reason;
    }

    @Override
    public @NotNull String report() {
        return "Test is ignored. " + reason;
    }
}
