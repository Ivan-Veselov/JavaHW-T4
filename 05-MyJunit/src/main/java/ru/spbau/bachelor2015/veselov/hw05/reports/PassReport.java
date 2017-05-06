package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class PassReport implements TestReport {
    private final long estimatedMillis;

    public PassReport(final long estimatedMillis) {
        this.estimatedMillis = estimatedMillis;
    }

    @Override
    public @NotNull String report() {
        return "Test passed in " + estimatedMillis;
    }
}
