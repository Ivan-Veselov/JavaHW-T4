package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.FileWithNegativeLengthException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

/**
 * File receiver is a data reader which is responsible for reading a content of a file from channel to a specified file.
 */
public class FileReceiver implements DataReader {
    private static final int CHUNK_SIZE = 4096;

    private final @NotNull ReadableByteChannel channel;

    private final @NotNull FileChannel fileChannel;

    private boolean isLengthRead = false;

    private long bytesLeft;

    private final @NotNull ByteBuffer lengthBuffer = ByteBuffer.allocate(Long.BYTES);

    private final @NotNull ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);

    /**
     * Creates a new file receiver.
     *
     * @param channel a channel from which file receiver will be reading content of a file.
     * @param path a path in a local file system where file receiver will be writing received data.
     * @throws FileNotFoundException if opening of a file has failed.
     */
    public FileReceiver(final @NotNull ReadableByteChannel channel, final @NotNull Path path)
            throws FileNotFoundException {
        this.channel = channel;

        fileChannel = new RandomAccessFile(path.toString(), "rw").getChannel();

    }

    /**
     * Makes an attempt to read a content of a file.
     *
     * @return the result of reading.
     * @throws IOException if any IO exception occurs during reading process or if an invalid data was received.
     */
    public @NotNull DataReader.ReadingResult read() throws IOException {
        if (!isLengthRead) {
            if (channel.read(lengthBuffer) == -1) {
                return DataReader.ReadingResult.CLOSED;
            }

            if (lengthBuffer.hasRemaining()) {
                return DataReader.ReadingResult.NOT_READ;
            }

            lengthBuffer.flip();
            bytesLeft = lengthBuffer.getLong();

            if (bytesLeft < 0) {
                fileChannel.close();
                throw new FileWithNegativeLengthException();
            }

            isLengthRead = true;
        }

        while (bytesLeft > 0) {
            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) {
                return DataReader.ReadingResult.CLOSED;
            }

            bytesLeft -= bytesRead;

            if (buffer.hasRemaining() && bytesLeft > 0) {
                return DataReader.ReadingResult.NOT_READ;
            }

            buffer.flip();

            fileChannel.write(buffer);

            buffer.clear();
        }

        return DataReader.ReadingResult.READ;
    }
}
