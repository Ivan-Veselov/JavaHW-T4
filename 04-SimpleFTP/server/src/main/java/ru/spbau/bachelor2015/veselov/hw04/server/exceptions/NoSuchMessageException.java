package ru.spbau.bachelor2015.veselov.hw04.server.exceptions;

import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;

/**
 * An exception which might be thrown by a Server if it can't recognize an incoming message.
 */
public class NoSuchMessageException extends InvalidMessageException {}
