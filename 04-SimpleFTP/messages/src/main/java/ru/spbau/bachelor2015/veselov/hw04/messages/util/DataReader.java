package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import java.io.IOException;

/**
 * An interface which represents an entity which has an ability to read. It is assumed that such entity reads from a
 * socket channel.
 */
public interface DataReader {
    /**
     * Makes an attempt to read data from data source and returns the result of reading.
     *
     * @throws IOException if any IO exception occurs during reading process.
     */
    ReadingResult read() throws IOException;

    /**
     * Possible results of reading operation. CLOSED means that peer has closed the connection.
     */
    enum ReadingResult {
        READ, NOT_READ, CLOSED
    }
}
