package ru.spbau.bachelor2015.veselov.hw04.messages;

import java.io.Serializable;

/**
 * An interface which represents an abstract message which ftp server can use to interact on its' connections.
 */
public interface FTPMessage extends Serializable {
    /**
     * Maximal possible length of a message. A longer message considered to be invalid.
     */
    int MAXIMAL_MESSAGE_LENGTH = 1024 * 1024; // 1 Mb
}
