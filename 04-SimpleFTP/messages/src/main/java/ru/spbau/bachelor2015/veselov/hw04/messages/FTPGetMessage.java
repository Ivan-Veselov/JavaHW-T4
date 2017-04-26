package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;

public class FTPGetMessage implements FTPMessage {
    private final @NotNull String path;

    public FTPGetMessage(final @NotNull String path) {
        this.path = path;
    }

    public @NotNull String getPath() {
        return path;
    }
}
