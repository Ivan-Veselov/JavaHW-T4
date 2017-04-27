package ru.spbau.bachelor2015.veselov.hw04.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.server.exceptions.NoDataWriterRegisteredException;
import ru.spbau.bachelor2015.veselov.hw04.server.exceptions.RegisteringSecondDataWriterException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.DataWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileTransmitter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.LongMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageNotReadException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

/**
 * Channel observer handles all operations on a particular socket channel and manages this socket channel lifecycle.
 */
public class FTPChannelObserver {
    private final static @NotNull Logger logger = LogManager.getLogger(FTPChannelObserver.class.getCanonicalName());

    private final @NotNull SocketChannel channel;

    private final @NotNull SelectionKey selectionKey;

    private final @NotNull Server server;

    private final @NotNull FTPMessageReader reader;

    private @Nullable DataWriter writer;

    /**
     * Creates a new observer.
     *
     * @param channel a channel for which a new observer will be created.
     * @param server a server which established a connection this channel represents.
     * @param selector a selector of a server.
     * @throws IOException if any IO exception occurs during observer creation.
     */
    public FTPChannelObserver(final @NotNull SocketChannel channel,
                              final @NotNull Server server,
                              final @NotNull Selector selector) throws IOException {
        logger.info("A new FTPChannelObserver ({}) has been created", this);

        this.channel = channel;
        this.server = server;

        channel.configureBlocking(false);

        selectionKey = channel.register(selector, SelectionKey.OP_READ, this);

        reader = new FTPMessageReader(channel);
    }

    /**
     * Registers a message writer for stored channel.
     *
     * @param message a message which this writer will be writing.
     * @throws RegisteringSecondDataWriterException if there is already a registered writer.
     * @throws LongMessageException if a message is too long.
     */
    public void registerMessageWriter(final @NotNull FTPMessage message)
            throws RegisteringSecondDataWriterException, LongMessageException {
        if (writer != null) {
            throw new RegisteringSecondDataWriterException();
        }

        logger.info("A new FTPMessageWriter has been registered in {}", this);

        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        writer = new FTPMessageWriter(channel, message);
    }

    /**
     * Registers a file transmitter for stored channel.
     *
     * @param path a path to a file which content will be transmitted.
     * @throws RegisteringSecondDataWriterException if there is already a registered writer.
     * @throws IOException if any IO exception occurs during file opening.
     */
    public void registerFileTransmitter(final @NotNull Path path)
            throws RegisteringSecondDataWriterException, IOException {
        if (writer != null) {
            throw new RegisteringSecondDataWriterException();
        }

        logger.info("A new FileTransmitter has been registered in {}", this);

        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        writer = new FileTransmitter(channel, path);
    }

    /**
     * Makes an attempt to read a message from stored channel.
     *
     * @throws IOException if any IO exception occurs during reading process.
     */
    public void read() throws IOException {
        try {
            switch (reader.read()) {
                case NOT_READ:
                    return;

                case READ:
                    FTPMessage message;

                    try {
                        message = reader.getMessage();
                    } catch (MessageNotReadException e) {
                        throw new RuntimeException(e);
                    }

                    reader.reset();

                    server.handleMessage(channel, message);
                    break;

                case CLOSED:
                    channel.close();
                    break;
            }
        } catch (InvalidMessageException e) {
            logger.info("{} has read an invalid message", this);

            channel.close();
        }
    }

    /**
     * Makes an attempt to write something with registered writer.
     *
     * @throws IOException if any IO exception occurs during writing process.
     * @throws NoDataWriterRegisteredException if there is no data writer registered.
     */
    public void write() throws IOException, NoDataWriterRegisteredException {
        if (writer == null) {
            throw new NoDataWriterRegisteredException();
        }

        if (writer.write()) {
            logger.info("{} has written data to a channel", this);

            writer = null;
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }
}
