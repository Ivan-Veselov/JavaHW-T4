package ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidMessageException extends Exception {
    public InvalidMessageException() {}

    public InvalidMessageException(final @NotNull Exception cause) {
        super(cause);
    }
}
