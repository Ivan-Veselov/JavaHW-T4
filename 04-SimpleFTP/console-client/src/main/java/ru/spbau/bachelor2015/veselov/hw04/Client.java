package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.ConnectionWasClosedException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * A client class establishes a connection with ftp server and allows to send a request messages to it.
 * TODO: client will freeze on write operation if server closed connection for some reason.
 */
public class Client implements AutoCloseable {
    private final @NotNull SocketChannel channel;

    private final @NotNull FTPMessageTransmitter transmitter;

    private final @NotNull FTPMessageReader reader;

    /**
     * Establishes a connection.
     *
     * @param host a host to connect to.
     * @param port a port to connect to.
     * @throws IOException if any IO exception occurs during establishment of connection.
     */
    public Client(final @NotNull String host, final int port) throws IOException {
        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(host, port));

        transmitter = new FTPMessageTransmitter(channel);
        reader = new FTPMessageReader(channel);
    }

    /**
     * Closes the connection.
     *
     * @throws IOException if any IO exception occurs during closing of connection.
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Send a list request to a server.
     *
     * @param path a path to a directory which content is requested.
     * @return answer message.
     * @throws IOException if any IO exception occurs during interaction with server.
     * @throws ConnectionWasClosedException if remote side closed the connection.
     */
    public @NotNull FTPListAnswerMessage list(final @NotNull String path)
            throws IOException, ConnectionWasClosedException {
        FTPListMessage message = new FTPListMessage(path);

        transmitter.addMessage(message);
        transmitter.waitUntilWritten();

        try {
            return (FTPListAnswerMessage) reader.waitUntilRead();
        } catch (MessageWithNegativeLengthException e) {
            channel.close();
            throw new RuntimeException(e);
        } catch (ConnectionWasClosedException e) {
            channel.close();
            throw e;
        }
    }
}
