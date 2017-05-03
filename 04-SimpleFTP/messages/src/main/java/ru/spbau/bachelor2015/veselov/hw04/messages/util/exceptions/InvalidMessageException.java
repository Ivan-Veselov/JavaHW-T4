package ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * An exception which might be thrown by DataReaders when they have read invalid data.
 */
public class InvalidMessageException extends IOException {
    /**
     * An empty constructor.
     */
    public InvalidMessageException() {}

    /**
     * Constructor from cause.
     *
     * @param cause cause of the exception.
     */
    public InvalidMessageException(final @NotNull Exception cause) {
        super(cause);
    }
}
