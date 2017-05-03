package ru.spbau.bachelor2015.veselov.hw04.server.exceptions;

/**
 * An exception which might be thrown by a FTPChannelObserver if there is an attempt to register DataWriter while
 * another one already registered.
 */
public class RegisteringSecondDataWriterException extends Exception {}
