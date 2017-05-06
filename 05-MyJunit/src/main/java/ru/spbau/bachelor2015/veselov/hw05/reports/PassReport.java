package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class PassReport extends MyJunitReport {
    private final long estimatedMillis;

    public PassReport(final @NotNull String className, final @NotNull String methodName, final long estimatedMillis) {
        super(className, methodName);

        this.estimatedMillis = estimatedMillis;
    }

    @Override
    public @NotNull String report() {
        return super.report() + " Test passed in " + estimatedMillis;
    }
}
