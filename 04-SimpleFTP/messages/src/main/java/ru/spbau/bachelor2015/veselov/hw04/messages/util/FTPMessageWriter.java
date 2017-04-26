package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class FTPMessageWriter implements DataWriter {
    private final @NotNull WritableByteChannel channel;

    private final @NotNull ByteBuffer buffer;

    public FTPMessageWriter(final @NotNull WritableByteChannel channel, final @NotNull FTPMessage message) {
        this.channel = channel;

        byte[] data = SerializationUtils.serialize(message);

        buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
    }

    public boolean write() throws IOException {
        channel.write(buffer);
        return !buffer.hasRemaining();
    }
}
