package ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions;

/**
 * An exception which might be thrown by DataReaders and DataWriters when they encounter a message which length exceeds
 * a predefined limit.
 */
public class LongMessageException extends InvalidMessageException {}
