package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A reader object must be created for reading messages of specific format from a ReadableByteChannel. Message format
 * is: four bytes denoting the length of the message, a sequence of bytes which amount is equal to the length denoted by
 * first four bytes.
 */
public class MessageReader {
    private final @NotNull ReadableByteChannel channel;

    private boolean isLengthRead = false;

    private @NotNull final ByteBuffer lengthBuffer = ByteBuffer.allocate(Message.LENGTH_BYTES);

    private @Nullable ByteBuffer messageBuffer;

    /**
     * Creates a new MessageReader which will read from a specified channel.
     *
     * @param channel a channel to read from.
     */
    public MessageReader(final @NotNull ReadableByteChannel channel) {
        this.channel = channel;
    }

    /**
     * Makes an attempt to read a message from a channel.
     *
     * @return a result of reading. It might be READ if message is fully read, NOT_READ is the opposite result. It also
     *         might be CLOSED if remote side closed the connection.
     * @throws IOException if any IO exception occurs during reading.
     * @throws MessageWithNegativeLengthException if the length of the message is negative.
     */
    public ReadingResult read() throws IOException, MessageWithNegativeLengthException {
        if (!isLengthRead) {
            if (channel.read(lengthBuffer) == -1) {
                return ReadingResult.CLOSED;
            }

            if (lengthBuffer.hasRemaining()) {
                return ReadingResult.NOT_READ;
            }

            isLengthRead = true;

            lengthBuffer.flip();
            int length = lengthBuffer.getInt();

            if (length < 0) {
                throw new MessageWithNegativeLengthException();
            }

            messageBuffer = ByteBuffer.allocate(length);
        }

        if (channel.read(messageBuffer) == -1) {
            return ReadingResult.CLOSED;
        }

        if (messageBuffer.hasRemaining()) {
            return ReadingResult.NOT_READ;
        }

        return ReadingResult.READ;
    }

    /**
     * Returns the content of the read message.
     *
     * @throws MessageNotReadException if message hasn't been read.
     */
    public @NotNull byte[] getMessage() throws MessageNotReadException {
        if (messageBuffer == null || messageBuffer.hasRemaining()) {
            throw new MessageNotReadException();
        }

        return messageBuffer.array().clone();
    }

    /**
     * Resets message reader so that it can be used to read a new message.
     */
    public void reset() {
        isLengthRead = false;
        lengthBuffer.clear();
        messageBuffer = null;
    }

    public enum ReadingResult { READ, NOT_READ, CLOSED }
}
