package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.FTPMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class MessageWriter implements DataWriter {
    private final @NotNull WritableByteChannel channel;

    private final @NotNull ByteBuffer buffer;

    public MessageWriter(final @NotNull WritableByteChannel channel, final @NotNull FTPMessage message) {
        this.channel = channel;

        byte[] data = SerializationUtils.serialize(message);

        buffer = ByteBuffer.allocate(Message.LENGTH_BYTES + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
    }

    public boolean write() throws IOException {
        channel.write(buffer);
        return !buffer.hasRemaining();
    }
}
