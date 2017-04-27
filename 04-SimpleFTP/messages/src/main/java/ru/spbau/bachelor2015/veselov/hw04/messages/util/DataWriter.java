package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import java.io.IOException;

/**
 * An interface which represents an entity which has an ability to write. It is assumed that such entity writes to a
 * socket channel.
 */
public interface DataWriter {
    /**
     * Makes an attempt to write data to data consumer and returns the result of writing which is true in case when
     * writer has written all the data it had.
     *
     * @throws IOException if any IO exception occurs during writing process.
     */
    boolean write() throws IOException;
}
