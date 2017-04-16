package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {1, 2, 3};

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
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        continue;
                    }
                }

                selector.selectedKeys().clear();
            }
        }
    }
}
