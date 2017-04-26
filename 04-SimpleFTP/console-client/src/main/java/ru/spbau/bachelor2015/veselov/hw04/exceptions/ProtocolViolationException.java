package ru.spbau.bachelor2015.veselov.hw04.exceptions;

import org.jetbrains.annotations.NotNull;

public class ProtocolViolationException extends Exception {
    public ProtocolViolationException() {}

    public ProtocolViolationException(final @NotNull Exception cause) {
        super(cause);
    }
}
