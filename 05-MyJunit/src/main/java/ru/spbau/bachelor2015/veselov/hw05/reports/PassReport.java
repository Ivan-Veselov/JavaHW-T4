package ru.spbau.bachelor2015.veselov.hw05.reports;

import org.jetbrains.annotations.NotNull;

public class PassReport implements TestReport {
    @Override
    public @NotNull String report() {
        return "Test passed";
    }
}
