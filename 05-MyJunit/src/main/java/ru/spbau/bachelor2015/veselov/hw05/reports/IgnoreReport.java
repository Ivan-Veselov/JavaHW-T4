package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class IgnoreReport extends MyJunitReport {
    private final @NotNull String reason;

    public IgnoreReport(final @NotNull String className,
                        final @NotNull String methodName,
                        final @NotNull String reason) {
        super(className, methodName);

        this.reason = reason;
    }

    public @NotNull String getReason() {
        return reason;
    }

    @Override
    public @NotNull String report() {
        return super.report() + " Test is ignored. " + reason;
    }
}
