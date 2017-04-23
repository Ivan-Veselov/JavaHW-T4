package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageWriter;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;

/**
 * Ftp transmitter can write a queued sequence of ftp messages to a specified channel.
 */
public class FTPMessageTransmitter {
    private final static @NotNull Logger logger = LogManager.getLogger(FTPMessageTransmitter.class.getCanonicalName());

    private final @NotNull WritableByteChannel channel;

    private final @NotNull LinkedList<MessageWriter> writers = new LinkedList<>();

    /**
     * Creates a new transmitter.
     *
     * @param channel a channel to which new transmitter will be writing messages.
     */
    public FTPMessageTransmitter(final @NotNull WritableByteChannel channel) {
        logger.debug("New FTPMessageTransmitter ({}) is created", this);

        this.channel = channel;
    }

    /**
     * Adds a new message to the queue.
     *
     * @param message a message to add.
     */
    public void addMessage(final @NotNull FTPMessage message) throws IOException {
        logger.debug("New message is added to FTPMessageTransmitter ({})", this);

        writers.add(new MessageWriter(channel, SerializationUtils.serialize(message)));
    }

    /**
     * Makes an attempt to write a message to a channel.
     *
     * @return true if there is no messages to write left.
     * @throws IOException if any IO exception occurs during writing.
     */
    public boolean write() throws IOException {
        logger.debug("Write method of FTPMessageTransmitter ({}) is called", this);

        if (!writers.isEmpty()) {
            MessageWriter writer = writers.getFirst();
            if (writer.write()) {
                writers.poll();
            }
        }

        return writers.isEmpty();
    }

    /**
     * Writes every message in queue into the underlying channel. A better performance may be achieved if
     * underlying channel is switched to blocking mode.
     *
     * @throws IOException if any IO exception occurs during writing.
     */
    public void waitUntilWritten() throws IOException {
        logger.debug("WaitUntilWritten method of FTPMessageTransmitter ({}) is called", this);

        while (!writers.isEmpty()) {
            MessageWriter writer = writers.getFirst();
            while (!writer.write());

            writers.poll();
        }
    }
}
