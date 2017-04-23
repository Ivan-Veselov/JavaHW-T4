package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Ftp reader which allows transform sequence of bytes into ftp messages.
 */
public class FTPMessageReader {
    private final @NotNull MessageReader reader;

    /**
     * Creates a new reader for a specified channel.
     *
     * @param channel a channel which new reader will be tracking.
     */
    public FTPMessageReader(final @NotNull ReadableByteChannel channel) {
        reader = new MessageReader(channel);
    }

    /**
     * Makes an attempt to read a message from a channel.
     *
     * @return a result of reading. It might be READ if message is fully read, NOT_READ is the opposite result. It also
     *         might be CLOSED if remote side closed the connection.
     * @throws IOException if any IO exception occurs during reading.
     * @throws MessageWithNegativeLengthException if the length of the message is negative.
     */
    public @NotNull MessageReader.ReadingResult read() throws IOException, MessageWithNegativeLengthException {
        return reader.read();
    }

    /**
     * Returns read message. This method waits until message is fully read. A better performance may be achieved if
     * underlying channel is switched to blocking mode.
     *
     * @throws IOException if any IO exception occurs during reading.
     * @throws MessageWithNegativeLengthException if the length of the message is negative.
     * @throws ConnectionWasClosedException if remote side closed the connection.
     */
    public @NotNull FTPMessage waitUntilRead()
            throws IOException, MessageWithNegativeLengthException, ConnectionWasClosedException {
        boolean shouldTry = true;

        while (shouldTry) {
            switch (reader.read()) {
                case READ:
                    shouldTry = false;
                    break;

                case NOT_READ:
                    break;

                case CLOSED:
                    throw new ConnectionWasClosedException();
            }
        }

        try {
            return getMessage();
        } catch (MessageNotReadException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns read message.
     *
     * @throws MessageNotReadException if message hasn't been read.
     */
    public @NotNull FTPMessage getMessage() throws MessageNotReadException {
        byte[] data = reader.getMessage();
        reader.reset();

        return SerializationUtils.deserialize(data);
    }
}
