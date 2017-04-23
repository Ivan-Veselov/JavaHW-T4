package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.InvalidFTPMessageException;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * An interface which represents a request message to a server.
 */
public interface FTPRequestMessage extends FTPMessage {
    /**
     * Method to implement double dispatch pattern. This method should call handleMessage method of given server.
     *
     * @param server a server which will handle this message.
     * @param key a selection key of a channel which produced this message. This key should be passed to handleMessage
     *            method of a server.
     * @throws IOException if any IO exception occurs while server handles this message.
     * @throws InvalidFTPMessageException if this message is invalid.
     */
    void accept(@NotNull Server server, @NotNull SelectionKey key) throws IOException, InvalidFTPMessageException;
}
