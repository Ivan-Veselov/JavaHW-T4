package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public abstract class MyJunitReport implements TestReport {
    private final @NotNull String className;

    private final @NotNull String methodName;

    public MyJunitReport(final @NotNull String className, final @NotNull String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public @NotNull String report() {
        return className + "." + methodName + ":";
    }
}
