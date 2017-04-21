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
import java.util.List;
import java.util.Map;

/**
 * TODO: handle connections which were closed
 * TODO: close connections which sent incorrect messages
 * TODO: more accurate writing (only when it is required)
 * TODO: limit a length of an incoming message
 * TODO: add javadocs
 * TODO: add list message test
 * TODO: add FTPMessageTransmitter test
 * TODO: add bytes for message length constant
 * TODO: fix multiple 1 in MessageWriterTest
 * TODO: add javadocs to Main
 */
public class Server {
    private final static int port = 10000;

    private final static @NotNull byte[] data = new byte[] {'H', 'i', '!'};

    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private volatile boolean shouldRun;

    private final @NotNull Path trackedFolder;

    private @Nullable Map<SelectionKey, MessageReader> messageReaders = new HashMap<>();

    private @Nullable Map<SelectionKey, FTPMessageTransmitter> messageTransmitters = new HashMap<>();

    public Server(final @NotNull Path trackedFolder) {
        logger.info("New Server ({}) is created", this);

        this.trackedFolder = trackedFolder;
    }

    public void start() {
        logger.info("Server ({}) is started", this);

        shouldRun = true;

        messageReaders = new HashMap<>();
        messageTransmitters = new HashMap<>();

        new Thread(
            () -> {
                // TODO: handle exceptions
                try (Selector selector = Selector.open();
                     ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                    serverSocketChannel.socket().bind(new InetSocketAddress(port));
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                    while (shouldRun) {
                        selector.select();

                        for (SelectionKey key : selector.selectedKeys()) {
                            if (key.channel().equals(serverSocketChannel)) {
                                if (key.isAcceptable()) {
                                    acceptNewConnection(selector, serverSocketChannel);
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    messageReaders = null;
                    messageTransmitters = null;
                }
            }
        ).start();
    }

    public void stop() {
        shouldRun = false;
    }

    private void acceptNewConnection(final @NotNull Selector selector,
                                     final @NotNull ServerSocketChannel serverSocketChannel) throws IOException {
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
