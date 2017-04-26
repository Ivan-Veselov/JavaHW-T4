package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.ProtocolViolationException;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A client class establishes a connection with ftp server and allows to send a request messages to it.
 * TODO: client will freeze on write operation if server closed connection for some reason.
 * TODO: add interests switching
 */
public class Client implements AutoCloseable {
    private final @NotNull SocketChannel channel;

    private final @NotNull Selector selector;

    private final @NotNull MessageReader reader;

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

        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        reader = new MessageReader(channel);
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

    /**
     * Send a list request to a server.
     *
     * @param path a path to a directory which content is requested.
     * @return answer message.
     * @throws IOException if any IO exception occurs during interaction with server.
     * @throws ProtocolViolationException if remote side closed the connection.
     */
    public @NotNull FTPListAnswerMessage list(final @NotNull String path)
            throws IOException, ProtocolViolationException {
        FTPListMessage message = new FTPListMessage(path);

        MessageWriter writer = new MessageWriter(channel, message);

        while (true) {
            selector.select();
            SelectionKey key = channel.keyFor(selector);

            if (key.isWritable()) {
                if (writer.write()) {
                    break;
                }
            }

            if (key.isReadable()) {
                close();
                throw new ProtocolViolationException();
            }

            selector.selectedKeys().clear();
        }

        while (true) {
            selector.select();
            SelectionKey key = channel.keyFor(selector);

            if (key.isReadable()) {
                switch (reader.read()) {
                    case NOT_READ:
                        break;

                    case READ:
                        FTPListAnswerMessage answer;

                        try {
                            answer = (FTPListAnswerMessage) reader.getMessage();
                        } catch (MessageNotReadException e) {
                            throw new RuntimeException(e);
                        }

                        return answer;

                    case CLOSED:
                        throw new ProtocolViolationException();
                }
            }

            selector.selectedKeys().clear();
        }
    }
}
