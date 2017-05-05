package ru.spbau.bachelor2015.veselov.hw05.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidTestClassException extends Exception {
    public InvalidTestClassException(final @NotNull Exception cause) {
        super(cause);
    }
}
