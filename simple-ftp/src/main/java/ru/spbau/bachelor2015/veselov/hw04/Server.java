package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {1, 2, 3};

    // TODO: handle exceptions
    public Server() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(data);
        buffer.rewind();

        Selector selector = Selector.open();

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.channel().equals(serverChannel)) {
                    SocketChannel channel = serverChannel.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE);
                    continue;
                }

                SocketChannel channel = (SocketChannel) key.channel();
                channel.write(buffer); // TODO: add while cycle
                buffer.rewind();
                channel.close();
            }

            selector.selectedKeys().clear();
        }
    }
}
