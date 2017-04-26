package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

// TODO: add length writing
public class FileTransmitter implements DataWriter {
    private static final int CHUNK_SIZE = 4096;

    private final @NotNull WritableByteChannel channel;

    private final @NotNull ReadableByteChannel fileChannel;

    private final @NotNull ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);

    public FileTransmitter(final @NotNull WritableByteChannel channel, final @NotNull Path path)
            throws IOException {
        this.channel = channel;

        fileChannel = new RandomAccessFile(path.toFile(), "r").getChannel();

        fillBuffer();
    }

    public boolean write() throws IOException {
        while (!isTransmitted()) {
            channel.write(buffer);

            if (buffer.hasRemaining()) {
                break;
            }

            fillBuffer();
        }

        return isTransmitted();
    }

    private boolean isTransmitted() {
        return !fileChannel.isOpen() && !buffer.hasRemaining();
    }

    private void fillBuffer() throws IOException {
        buffer.clear();

        if (fileChannel.read(buffer) == -1) {
            fileChannel.close();
        }

        buffer.flip();
    }
}
