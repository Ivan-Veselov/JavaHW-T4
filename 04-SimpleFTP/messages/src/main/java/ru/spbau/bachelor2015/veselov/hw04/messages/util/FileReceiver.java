package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class FileReceiver implements DataReader {
    private static final int CHUNK_SIZE = 4096;

    private final @NotNull ReadableByteChannel channel;

    private final @NotNull FileChannel fileChannel;

    private boolean isLengthRead = false;

    private long bytesLeft;

    private final @NotNull ByteBuffer lengthBuffer = ByteBuffer.allocate(Long.BYTES);

    private final @NotNull ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);

    public FileReceiver(final @NotNull ReadableByteChannel channel, final @NotNull Path path) {
        this.channel = channel;

        try {
            fileChannel = new RandomAccessFile(path.toString(), "w").getChannel();
        } catch (FileNotFoundException e) {
            /* Documentation for RandomAccessFile claims that FileNotFoundException might be thrown only if mode
               contains 'r'.
             */
            throw new RuntimeException(e);
        }
    }

    public @NotNull DataReader.ReadingResult read() throws IOException {
        if (!isLengthRead) {
            if (channel.read(lengthBuffer) == -1) {
                return DataReader.ReadingResult.CLOSED;
            }

            if (lengthBuffer.hasRemaining()) {
                return DataReader.ReadingResult.NOT_READ;
            }

            bytesLeft = lengthBuffer.getLong();
            isLengthRead = true;
        }

        while (bytesLeft > 0) {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                return DataReader.ReadingResult.CLOSED;
            }

            bytesLeft -= bytesRead;
            if (buffer.hasRemaining() && bytesRead > 0) {
                return DataReader.ReadingResult.NOT_READ;
            }

            buffer.flip();

            fileChannel.write(buffer);

            buffer.clear();
        }

        return DataReader.ReadingResult.READ;
    }
}
