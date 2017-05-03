package ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions;

/**
 * An exception which might be thrown by a FTPMessageReader if it's getMessage method invoked while the actual message
 * hasn't been read.
 */
public class MessageNotReadException extends Exception {}
