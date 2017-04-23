package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

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
     * Returns read message or empty Optional if reader was unable to read the whole message.
     *
     * @throws IOException if any IO exception occurs during reading.
     * @throws MessageWithNegativeLengthException if the length of the message is negative.
     */
    public @NotNull Optional<FTPMessage> read() throws IOException, MessageWithNegativeLengthException {
        if (!reader.read()) {
            return Optional.empty();
        }

        return Optional.of(getMessage());
    }

    /**
     * Returns read message. This method waits until message is fully read. A better performance may be achieved if
     * underlying channel is switched to blocking mode.
     *
     * @throws IOException if any IO exception occurs during reading.
     * @throws MessageWithNegativeLengthException if the length of the message is negative.
     */
    public @NotNull FTPMessage waitUntilRead() throws IOException, MessageWithNegativeLengthException {
        while (!reader.read());

        return getMessage();
    }

    private @NotNull FTPMessage getMessage() {
        byte[] data;
        try {
            data = reader.getMessage();
        } catch (MessageNotReadException e) {
            throw new RuntimeException(e);
        }

        reader.reset();
        return SerializationUtils.deserialize(data);
    }
}
