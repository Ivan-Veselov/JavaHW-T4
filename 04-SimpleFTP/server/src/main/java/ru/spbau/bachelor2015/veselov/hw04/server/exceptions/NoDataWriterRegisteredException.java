package ru.spbau.bachelor2015.veselov.hw04.server.exceptions;

/**
 * An exception which might be thrown by FTPChannelObserver if it's write method is invoked while no DataWriter was
 * registered.
 */
public class NoDataWriterRegisteredException extends Exception {}
