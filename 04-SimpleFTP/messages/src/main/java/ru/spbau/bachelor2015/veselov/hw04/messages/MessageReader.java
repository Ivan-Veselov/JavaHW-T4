package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MessageReader {
    private final @NotNull ReadableByteChannel channel;

    private boolean isLengthRead = false;

    private @NotNull final ByteBuffer lengthBuffer = ByteBuffer.allocate(Message.LENGTH_BYTES);

    private @Nullable ByteBuffer messageBuffer;

    public MessageReader(final @NotNull ReadableByteChannel channel) {
        this.channel = channel;
    }

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

    public enum ReadingResult { READ, NOT_READ, CLOSED }
}
