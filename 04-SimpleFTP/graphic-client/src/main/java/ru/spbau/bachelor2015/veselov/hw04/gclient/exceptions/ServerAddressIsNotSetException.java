package ru.spbau.bachelor2015.veselov.hw04.gclient.exceptions;

/**
 * This exception might be thrown by ApplicationModel class if some action which requires server address was invoked
 * while server address hasn't been set.
 */
public class ServerAddressIsNotSetException extends Exception {}
