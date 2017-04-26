package ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class InvalidMessageException extends IOException {
    public InvalidMessageException() {}

    public InvalidMessageException(final @NotNull Exception cause) {
        super(cause);
    }
}
