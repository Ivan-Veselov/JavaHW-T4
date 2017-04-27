package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

/**
 * File transmitter is a data writer which is responsible for writing a content of a specified file to a specified
 * channel.
 */
public class FileTransmitter implements DataWriter {
    private static final int CHUNK_SIZE = 4096;

    private final @NotNull WritableByteChannel channel;

    private final @NotNull ReadableByteChannel fileChannel;

    private final @NotNull ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);

    /**
     * Creates a new file transmitter.
     *
     * @param channel a channel to which this transmitter will be writing.
     * @param path a path to a file which content transmitter will be writing.
     * @throws IOException if any IO exception occurs during file opening.
     */
    public FileTransmitter(final @NotNull WritableByteChannel channel, final @NotNull Path path) throws IOException {
        this.channel = channel;

        RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
        fileChannel = file.getChannel();

        buffer.putLong(file.length());
        buffer.flip();
    }

    /**
     * Makes an attempt to write file content to channel.
     *
     * @return true if file content was fully written.
     * @throws IOException if any IO exception occurs during writing process.
     */
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
