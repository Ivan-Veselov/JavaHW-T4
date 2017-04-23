package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.ftpmessages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.InvalidMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.MessageNotReadException;

import java.io.File;
import java.io.IOException;
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
 * TODO: more accurate writing (only when it is required)
 * TODO: limit a length of an incoming message
 * TODO: add javadocs to Server
 * TODO: add FTPMessageTransmitter test
 * TODO: add server test with multiple messages
 */
public class Server {
    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private volatile boolean shouldRun;

    private final int port;

    private final @NotNull Path trackedFolder;

    private @Nullable Map<SelectionKey, MessageReader> messageReaders = new HashMap<>();

    private @Nullable Map<SelectionKey, FTPMessageTransmitter> messageTransmitters = new HashMap<>();

    public Server(final @NotNull Path trackedFolder, final int port) {
        logger.info("New Server ({}) is created", this);

        this.trackedFolder = trackedFolder;
        this.port = port;
    }

    public void start() {
        shouldRun = true;

        messageReaders = new HashMap<>();
        messageTransmitters = new HashMap<>();

        new Thread(
            () -> {
                try (Selector selector = Selector.open();
                     ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                    serverSocketChannel.socket().bind(new InetSocketAddress(port));
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                    logger.info("Server ({}) is started", this);

                    while (shouldRun) {
                        selector.select();

                        for (SelectionKey key : selector.selectedKeys()) {
                            try {
                                if (key.isAcceptable()) {
                                    acceptNewConnection(selector, serverSocketChannel);
                                }

                                if (key.isReadable()) {
                                    readMessage(key);
                                }

                                if (key.isWritable()) {
                                    messageTransmitters.get(key).write();
                                }
                            } catch (InvalidMessageException e) {
                                logger.error("Server ({}) received an invalid message", this);
                            } catch (IOException ignored) {
                            } finally {
                                key.channel().close();
                            }
                        }

                        selector.selectedKeys().clear();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    messageReaders = null;
                    messageTransmitters = null;

                    logger.info("Server ({}) is stopped", this);
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

    private void readMessage(final @NotNull SelectionKey key) throws IOException, InvalidMessageException {
        MessageReader reader = messageReaders.get(key);

        if (!reader.read()) {
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

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull byte[] rawMessage)
            throws IOException, InvalidMessageException {
        FTPMessage message = SerializationUtils.deserialize(rawMessage);

        // TODO: use double dispatch
        if (message instanceof FTPListMessage) {
            handleMessage(key, (FTPListMessage) message);
        } else {
            throw new InvalidMessageException();
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
                entries.add(new FTPListAnswerMessage.Entry(trackedFolder.relativize(file.toPath()).toString(),
                                                           file.isDirectory()));
            }
        }

        messageTransmitters.get(key).addMessage(new FTPListAnswerMessage(entries));
    }
}
