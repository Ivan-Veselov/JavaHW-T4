package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
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
 * TODO: queue of message writers
 * TODO: more accurate writing (only when it is required)
 */
public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {1, 2, 3};

    private final @NotNull Map<SelectionKey, MessageReader> messageReaders = new HashMap<>();

    // TODO: handle exceptions
    public Server() throws IOException {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.channel().equals(serverSocketChannel)) {
                        if (!key.isAcceptable()) {
                            continue;
                        }

                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if (socketChannel == null) {
                            continue;
                        }

                        socketChannel.configureBlocking(false);
                        SelectionKey socketChannelKey = socketChannel.register(selector,
                                                                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        messageReaders.put(socketChannelKey, new MessageReader(socketChannel));
                        continue;
                    }

                    if (key.isReadable()) {
                        MessageReader reader = messageReaders.get(key);

                        try {
                            if (!reader.read()) {
                                continue;
                            }
                        } catch (MessageWithNegativeLengthException e) {
                            // TODO: send an answer
                        }

                        try {
                            reader.getMessage();
                        } catch (MessageNotReadException e) {
                            throw new RuntimeException(e);
                        }

                        reader.reset();
                        // TODO: send an answer
                    }
                }

                selector.selectedKeys().clear();
            }
        }
    }
}
