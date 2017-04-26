package ru.spbau.bachelor2015.veselov.hw04.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.client.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPGetMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.DataReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileReceiver;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageNotReadException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.List;

/**
 * A client class establishes a connection with ftp server and allows to send a request messages to it.
 * TODO: some exceptions look weired
 */
public class Client implements AutoCloseable {
    private final static @NotNull Logger logger = LogManager.getLogger(Client.class.getCanonicalName());

    private final @NotNull SocketChannel channel;

    private final @NotNull Selector selector;

    private final @NotNull FTPMessageReader messageReader;

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

        messageReader = new FTPMessageReader(channel);

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

    public @NotNull List<FTPListAnswerMessage.Entry> list(final @NotNull String path)
            throws IOException, ConnectionWasClosedException {
        writeMessage(new FTPListMessage(path));

        read(messageReader);

        FTPMessage answer;

        try {
            answer = messageReader.getMessage();
        } catch (MessageNotReadException e) {
            throw new RuntimeException(e);
        }

        messageReader.reset();

        logger.info("Client ({}) received a message from server", this);

        return ((FTPListAnswerMessage) answer).getContent();
    }

    public void get(final @NotNull String pathToSource, final @NotNull Path pathToDestination)
            throws ConnectionWasClosedException, IOException {
        writeMessage(new FTPGetMessage(pathToSource));

        FileReceiver receiver = new FileReceiver(channel, pathToDestination);
        read(receiver);

        logger.info("Client ({}) received a file from server", this);
    }

    private void writeMessage(final @NotNull FTPMessage message)
            throws IOException, ConnectionWasClosedException {
        logger.info("Client ({}) began sending a message to server", this);

        FTPMessageWriter writer = new FTPMessageWriter(channel, message);

        SelectionKey key = channel.keyFor(selector);
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

        boolean shouldRun = true;
        while (shouldRun) {
            selector.select();

            if (key.isWritable()) {
                if (writer.write()) {
                    shouldRun = false;
                }
            }

            if (shouldRun && key.isReadable()) {
                int bytesRead = channel.read(ByteBuffer.allocate(1));

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

    private void read(final @NotNull DataReader reader)
            throws IOException, ConnectionWasClosedException {
        logger.info("Client ({}) began waiting data from server", this);

        boolean shouldRun = true;
        while (shouldRun) {
            selector.select();

            try {
                switch (reader.read()) {
                    case NOT_READ:
                        break;

                    case READ:
                        shouldRun = false;
                        break;

                    case CLOSED:
                        throw new ConnectionWasClosedException();
                }
            } catch (InvalidMessageException | ConnectionWasClosedException e) {
                channel.close();

                throw e;
            }

            selector.selectedKeys().clear();
        }
    }
}
