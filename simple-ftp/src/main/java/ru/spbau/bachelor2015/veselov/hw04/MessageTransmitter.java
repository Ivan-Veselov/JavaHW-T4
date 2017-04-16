package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;

/**
 * Transmitter can write a queued sequence of messages to a specified channel.
 */
public class MessageTransmitter {
    private final static @NotNull Logger logger = LogManager.getLogger(MessageTransmitter.class.getCanonicalName());

    private final @NotNull WritableByteChannel channel;

    private final @NotNull LinkedList<MessageWriter> writers = new LinkedList<>();

    /**
     * Creates a new transmitter.
     *
     * @param channel a channel to which new transmitter will be writing messages.
     */
    public MessageTransmitter(final @NotNull WritableByteChannel channel) {
        logger.info("New MessageTransmitter ({}) is created", this);

        this.channel = channel;
    }

    /**
     * Adds a new message to the queue.
     *
     * @param data a message content.
     */
    public void addMessage(final @NotNull byte[] data) {
        logger.info("New message is added to MessageTransmitter ({})", this);

        writers.add(new MessageWriter(channel, data));
    }

    /**
     * Makes an attempt to write a message to a channel.
     *
     * @throws IOException if any IO exception occurs during writing.
     */
    public void write() throws IOException {
        logger.debug("Write method of MessageTransmitter ({}) is called", this);

        if (writers.isEmpty()) {
            return;
        }

        MessageWriter writer = writers.getFirst();
        if (writer.write()) {
            writers.poll();
        }
    }
}
