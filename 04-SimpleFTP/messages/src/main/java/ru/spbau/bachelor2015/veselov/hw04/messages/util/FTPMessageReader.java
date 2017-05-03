package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.LongMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageWithNonpositiveLengthException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Message reader is a data reader which is responsible for reading a single ftp message from a specified channel.
 */
public class FTPMessageReader implements DataReader {
    private final @NotNull ReadableByteChannel channel;

    private boolean isLengthRead = false;

    private @NotNull final ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);

    private @Nullable ByteBuffer messageBuffer;

    /**
     * Creates a message reader for a specified channel.
     *
     * @param channel a channel which this reader will be reading from.
     */
    public FTPMessageReader(final @NotNull ReadableByteChannel channel) {
        this.channel = channel;
    }

    /**
     * Makes an attempt to read a message.
     *
     * @return the result of current attempt.
     * @throws IOException if any IO exception occurs during reading process or if an invalid message was received.
     */
    public DataReader.ReadingResult read() throws IOException {
        if (!isLengthRead) {
            if (channel.read(lengthBuffer) == -1) {
                return DataReader.ReadingResult.CLOSED;
            }

            if (lengthBuffer.hasRemaining()) {
                return DataReader.ReadingResult.NOT_READ;
            }

            isLengthRead = true;

            lengthBuffer.flip();
            int length = lengthBuffer.getInt();

            if (length <= 0) {
                throw new MessageWithNonpositiveLengthException();
            }

            if (length > FTPMessage.MAXIMAL_MESSAGE_LENGTH) {
                throw new LongMessageException();
            }

            messageBuffer = ByteBuffer.allocate(length);
        }

        if (channel.read(messageBuffer) == -1) {
            return DataReader.ReadingResult.CLOSED;
        }

        if (messageBuffer.hasRemaining()) {
            return DataReader.ReadingResult.NOT_READ;
        }

        return DataReader.ReadingResult.READ;
    }

    /**
     * Returns a read ftp message.
     *
     * @throws MessageNotReadException if the message hasn't been read yet.
     */
    public @NotNull FTPMessage getMessage() throws MessageNotReadException {
        if (messageBuffer == null || messageBuffer.hasRemaining()) {
            throw new MessageNotReadException();
        }

        return SerializationUtils.deserialize(messageBuffer.array());
    }

    /**
     * Resets message reader so that it can be used to read a new message.
     */
    public void reset() {
        isLengthRead = false;
        lengthBuffer.clear();
        messageBuffer = null;
    }
}
