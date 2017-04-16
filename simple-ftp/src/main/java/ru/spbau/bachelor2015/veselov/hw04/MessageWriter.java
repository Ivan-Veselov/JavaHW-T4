package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A writer object must be created for writing messages of specific format to a WritableByteChannel. Message format is
 * the same as in MessageReader class.
 */
public class MessageWriter {
    private final @NotNull WritableByteChannel channel;

    private final @NotNull ByteBuffer buffer;

    /**
     * Creates a new MessageWriter which will write to a specified channel.
     *
     * @param channel a channel to write to.
     * @param data a data which will be written.
     */
    public MessageWriter(final @NotNull WritableByteChannel channel, final @NotNull byte[] data) {
        this.channel = channel;

        buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
    }

    /**
     * Makes an attempt to write a message to a channel.
     *
     * @return true if the message was fully written, false otherwise.
     * @throws IOException if any IO exception occurs during writing.
     */
    public boolean write() throws IOException {
        channel.write(buffer);
        return !buffer.hasRemaining();
    }
}
