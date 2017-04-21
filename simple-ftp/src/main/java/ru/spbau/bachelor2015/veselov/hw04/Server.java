package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.MessageWithNegativeLengthException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: handle connections which were closed.
 * TODO: more accurate writing (only when it is required)
 * TODO: limit a length of a message
 */
public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {'H', 'i', '!'};

    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private @Nullable Selector selector;

    private @Nullable ServerSocketChannel serverSocketChannel;

    private final @NotNull Map<SelectionKey, MessageReader> messageReaders = new HashMap<>();

    private final @NotNull Map<SelectionKey, MessageTransmitter> messageTransmitters = new HashMap<>();

    public Server() {
        logger.info("New Server ({}) is created", this);
    }

    // TODO: handle exceptions
    public void start() throws IOException {
        logger.info("Server ({}) is started", this);

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.channel().equals(serverSocketChannel)) {
                        if (key.isAcceptable()) {
                            acceptNewConnection();
                        }

                        continue;
                    }

                    if (key.isReadable()) {
                        readMessage(key);
                    }

                    if (key.isWritable()) {
                        messageTransmitters.get(key).write();
                    }
                }

                selector.selectedKeys().clear();
            }
        } finally {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }

            if (selector != null) {
                selector.close();
            }
        }
    }

    private void acceptNewConnection() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return;
        }

        logger.info("Server ({}) accept new connection", this);

        socketChannel.configureBlocking(false);

        // TODO: only SelectionKey.OP_READ required
        SelectionKey socketChannelKey = socketChannel.register(selector,
                                                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        messageReaders.put(socketChannelKey, new MessageReader(socketChannel));
        messageTransmitters.put(socketChannelKey, new MessageTransmitter(socketChannel));
    }

    private void readMessage(final @NotNull SelectionKey key) throws IOException {
        MessageReader reader = messageReaders.get(key);

        try {
            if (!reader.read()) {
                return;
            }
        } catch (MessageWithNegativeLengthException e) {
            return;
        }

        try {
            reader.getMessage();
        } catch (MessageNotReadException e) {
            throw new RuntimeException(e);
        }

        reader.reset();

        messageTransmitters.get(key).addMessage(data);
    }
}
