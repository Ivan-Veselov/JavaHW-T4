package ru.spbau.bachelor2015.veselov.hw04.server.exceptions;

import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;

/**
 * An exception which might be thrown by server if path in incoming request is invalid.
 */
public class InvalidPathException extends InvalidMessageException {}
