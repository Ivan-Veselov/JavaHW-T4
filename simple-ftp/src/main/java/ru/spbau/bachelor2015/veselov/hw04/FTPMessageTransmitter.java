package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
        logger.info("New FTPMessageTransmitter ({}) is created", this);

        this.channel = channel;
    }

    /**
     * Adds a new message to the queue.
     *
     * @param message a message to add.
     */
    public void addMessage(final @NotNull FTPMessage message) throws IOException {
        logger.info("New message is added to FTPMessageTransmitter ({})", this);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

            writers.add(new MessageWriter(channel, byteArrayOutputStream.toByteArray()));
        }
    }

    /**
     * Makes an attempt to write a message to a channel.
     *
     * @throws IOException if any IO exception occurs during writing.
     */
    public void write() throws IOException {
        logger.debug("Write method of FTPMessageTransmitter ({}) is called", this);

        if (writers.isEmpty()) {
            return;
        }

        MessageWriter writer = writers.getFirst();
        if (writer.write()) {
            writers.poll();
        }
    }
}
