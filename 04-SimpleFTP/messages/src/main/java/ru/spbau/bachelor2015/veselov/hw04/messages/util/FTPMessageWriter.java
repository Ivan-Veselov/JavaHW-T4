package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.LongMessageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Message writer is a data writer which is responsible for writing a single ftp message to a specified channel.
 */
public class FTPMessageWriter implements DataWriter {
    private final @NotNull WritableByteChannel channel;

    private final @NotNull ByteBuffer buffer;

    /**
     * Creates a new message writer.
     *
     * @param channel a channel to which this message writer will be writing.
     * @param message a message which this writer is responsible for.
     * @throws LongMessageException if a given message is to long and can't be sent.
     */
    public FTPMessageWriter(final @NotNull WritableByteChannel channel, final @NotNull FTPMessage message)
            throws LongMessageException {
        this.channel = channel;

        byte[] data = SerializationUtils.serialize(message);

        if (data.length > FTPMessage.MAXIMAL_MESSAGE_LENGTH) {
            throw new LongMessageException();
        }

        buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
    }

    /**
     * Makes an attempt to write stored message to channel.
     *
     * @return true if message was fully written.
     * @throws IOException if any IO exception occurs during writing process.
     */
    public boolean write() throws IOException {
        channel.write(buffer);
        return !buffer.hasRemaining();
    }
}
