package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class NoExceptionFailureReport extends MyJunitReport {
    private final @NotNull Class<?> expectedException;

    private final long estimatedMillis;

    public NoExceptionFailureReport(final @NotNull String className,
                                    final @NotNull String methodName,
                                    final @NotNull Class<?> expectedException,
                                    final long estimatedMillis) {
        super(className, methodName);

        this.expectedException = expectedException;
        this.estimatedMillis = estimatedMillis;
    }

    public @NotNull Class<?> getExpectedException() {
        return expectedException;
    }

    @Override
    public @NotNull String report() {
        return super.report() + " Test failed in " + estimatedMillis +
                ". Expected " + expectedException + ", but no exception was thrown";
    }
}
