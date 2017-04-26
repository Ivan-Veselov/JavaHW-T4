package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageNotReadException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * A client class establishes a connection with ftp server and allows to send a request messages to it.
 * TODO: client will freeze on write operation if server closed connection for some reason.
 */
public class Client implements AutoCloseable {
    private final static @NotNull Logger logger = LogManager.getLogger(Client.class.getCanonicalName());

    private final @NotNull SocketChannel channel;

    private final @NotNull Selector selector;

    private final @NotNull FTPMessageReader reader;

    /**
     * Establishes a connection.
     *
     * @param host a host to connect to.
     * @param port a port to connect to.
     * @throws IOException if any IO exception occurs during establishment of connection.
     */
    public Client(final @NotNull String host, final int port) throws IOException {
        selector = Selector.open();

        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(host, port));
        channel.configureBlocking(false);

        channel.register(selector, SelectionKey.OP_READ);

        reader = new FTPMessageReader(channel);

        logger.info("Client ({}) established a connection with a server on {}:{}", this, host, port);
    }

    /**
     * Closes the connection.
     *
     * @throws IOException if any IO exception occurs during closing of connection.
     */
    @Override
    public void close() throws IOException {
        channel.close();
        selector.close();
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public @NotNull List<FTPListAnswerMessage.Entry> list(final @NotNull String path)
            throws IOException, InvalidMessageException, ConnectionWasClosedException {
        writeMessage(new FTPListMessage(path));

        return ((FTPListAnswerMessage) readMessage()).getContent();
    }

    private void writeMessage(final @NotNull FTPListMessage message)
            throws IOException, InvalidMessageException, ConnectionWasClosedException {
        logger.info("Client ({}) began sending a message to server", this);

        FTPMessageWriter writer = new FTPMessageWriter(channel, message);

        SelectionKey key = channel.keyFor(selector);
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

        while (true) {
            selector.select();

            if (key.isWritable()) {
                if (writer.write()) {
                    break;
                }
            }

            if (key.isReadable()) {
                int bytesRead = channel.read((ByteBuffer) null);

                close();

                if (bytesRead == -1) {
                    throw new ConnectionWasClosedException();
                } else {
                    throw new InvalidMessageException();
                }
            }

            selector.selectedKeys().clear();
        }

        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        logger.info("Client ({}) sent a message to server", this);
    }

    private @NotNull FTPMessage readMessage()
            throws IOException, InvalidMessageException, ConnectionWasClosedException {
        logger.info("Client ({}) began waiting a message from server", this);

        while (true) {
            selector.select();
            SelectionKey key = channel.keyFor(selector);

            if (key.isReadable()) {
                try {
                    switch (reader.read()) {
                        case NOT_READ:
                            break;

                        case READ:
                            FTPMessage answer;

                            try {
                                answer = reader.getMessage();
                            } catch (MessageNotReadException e) {
                                throw new RuntimeException(e);
                            }

                            reader.reset();

                            logger.info("Client ({}) received a message from server", this);
                            return answer;

                        case CLOSED:
                            throw new ConnectionWasClosedException();
                    }
                } catch (InvalidMessageException | ConnectionWasClosedException e) {
                    channel.close();

                    throw e;
                }
            }

            selector.selectedKeys().clear();
        }
    }
}
