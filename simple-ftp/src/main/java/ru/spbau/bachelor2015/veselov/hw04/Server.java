package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.MessageNotReadException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.MessageWithNegativeLengthException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * TODO: handle connections which were closed
 * TODO: close connections which sent incorrect messages
 * TODO: more accurate writing (only when it is required)
 * TODO: limit a length of an incoming message
 * TODO: add javadocs
 * TODO: add server thread
 * TODO: implement main
 * TODO: add list message test
 */
public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {'H', 'i', '!'};

    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private final @NotNull Path trackedFolder;

    private @Nullable Selector selector;

    private @Nullable ServerSocketChannel serverSocketChannel;

    private final @NotNull Map<SelectionKey, MessageReader> messageReaders = new HashMap<>();

    private final @NotNull Map<SelectionKey, FTPMessageTransmitter> messageTransmitters = new HashMap<>();

    public Server(final @NotNull Path trackedFolder) {
        logger.info("New Server ({}) is created", this);

        this.trackedFolder = trackedFolder;
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

        logger.info("Server ({}) accepted new connection", this);

        socketChannel.configureBlocking(false);

        // TODO: only SelectionKey.OP_READ required
        SelectionKey socketChannelKey = socketChannel.register(selector,
                                                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        messageReaders.put(socketChannelKey, new MessageReader(socketChannel));
        messageTransmitters.put(socketChannelKey, new FTPMessageTransmitter(socketChannel));
    }

    private void readMessage(final @NotNull SelectionKey key) throws IOException {
        MessageReader reader = messageReaders.get(key);

        try {
            if (!reader.read()) {
                return;
            }
        } catch (MessageWithNegativeLengthException e) {
            logger.error("Server ({}) received a message with negative length", this);
            return;
        }

        byte[] message;
        try {
            message = reader.getMessage();
        } catch (MessageNotReadException e) {
            throw new RuntimeException(e);
        }

        reader.reset();

        handleMessage(key, message);
    }

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull byte[] rawMessage) throws IOException {
        FTPMessage message;
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rawMessage);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            message = (FTPMessage) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            logger.error("Server ({}) received a message with class which can't be deserialized", this);
            return;
        }

        // TODO: use double dispatch
        if (message instanceof FTPListMessage) {
            handleMessage(key, (FTPListMessage) message);
        } else {
            logger.error("Server ({}) received an unknown message", this);
        }
    }

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull FTPListMessage message)
            throws IOException {
        Path path = Paths.get(message.getPath());

        if (path.isAbsolute()) {
            logger.error("Server ({}) received a message with incorrect trackedFolder", this);
            return;
        }

        File[] files = trackedFolder.resolve(path).toFile().listFiles();

        List<FTPListAnswerMessage.Entry> entries = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                entries.add(new FTPListAnswerMessage.Entry(file.getPath(), file.isDirectory()));
            }
        }

        messageTransmitters.get(key).addMessage(new FTPListAnswerMessage(entries));
    }
}
