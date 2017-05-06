package ru.spbau.bachelor2015.veselov.hw05.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * This exception be might thrown by Tester if tested class has some problems with it's structure.
 */
public class InvalidTestClassException extends Exception {
    public InvalidTestClassException(final @NotNull Exception cause) {
        super(cause);
    }
}
