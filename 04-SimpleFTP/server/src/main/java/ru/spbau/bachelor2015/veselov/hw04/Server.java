package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.InvalidPathException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.NoDataWriterRegisteredException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.NoSuchMessageException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.RegisteringSecondDataWriterException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPGetMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListAnswerMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPListMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;

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
import java.util.List;

/**
 * TODO: split everything on packages
 * TODO: javadocs
 * TODO: logging
 * TODO: do refactoring
 * TODO: tests
 * TODO: remove logging in client interface
 * TODO: look at gradle files
 */
public class Server {
    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private volatile boolean shouldRun;

    private final int port;

    private final @NotNull Path trackedFolder;

    private final @NotNull Thread serverThread;

    private volatile @Nullable Selector selector;

    public Server(final @NotNull Path trackedFolder, final int port) {
        logger.info("New Server ({}) is created", this);

        this.trackedFolder = trackedFolder;
        this.port = port;

        this.serverThread =
            new Thread(
                () -> {
                    try (Selector selector = Selector.open();
                         ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                        serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
                        serverSocketChannel.configureBlocking(false);
                        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                        this.selector = selector;

                        logger.info("Server ({}) is started", this);

                        while (shouldRun) {
                            selector.select();

                            for (SelectionKey key : selector.selectedKeys()) {
                                try {
                                    if (key.isAcceptable()) {
                                        acceptNewConnection(serverSocketChannel);
                                    }

                                    if (key.isReadable()) {
                                        ((FTPChannelObserver) key.attachment()).read();

                                        if (!key.channel().isOpen()) {
                                            continue;
                                        }
                                    }

                                    if (key.isWritable()) {
                                        try {
                                            ((FTPChannelObserver) key.attachment()).write();
                                        } catch (NoDataWriterRegisteredException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                } catch (IOException e) {
                                    logger.error(
                                    "IOException occurred during interaction of Server ({}) " +
                                            "with a connection.\n{}",
                                    this, e);

                                    key.channel().close();
                                }
                            }

                            selector.selectedKeys().clear();
                        }

                        for (SelectionKey key : selector.keys()) {
                            key.channel().close();
                        }

                        logger.info("Server ({}) is stopped", this);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        selector = null;
                        shouldRun = false;
                    }
                }
            );
    }

    public void start() {
        shouldRun = true;
        serverThread.start();
    }

    public void stop() throws InterruptedException {
        if (!serverThread.isAlive()) {
            return;
        }

        shouldRun = false;
        selector.wakeup();
        serverThread.join();
    }

    private void acceptNewConnection(final @NotNull ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return;
        }

        logger.info("Server ({}) accepted new connection", this);
        new FTPChannelObserver(socketChannel, selector, this);
    }

    void handleMessage(final @NotNull SocketChannel channel, final @NotNull FTPMessage message) throws IOException {
        logger.info("Server ({}) received new message", this);

        // TODO: add double dispatch
        if (message instanceof FTPListMessage) {
            handleMessage(channel.keyFor(selector), (FTPListMessage) message);
        } else if (message instanceof FTPGetMessage) {
            handleMessage(channel.keyFor(selector), (FTPGetMessage) message);
        } else {
            throw new NoSuchMessageException();
        }
    }

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull FTPListMessage message)
            throws IOException {
        Path path = realPath(Paths.get(message.getPath()));

        File[] files = path.toFile().listFiles();

        List<FTPListAnswerMessage.Entry> entries = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                entries.add(new FTPListAnswerMessage.Entry(trackedFolder.relativize(file.toPath()).toString(),
                                                           file.isDirectory()));
            }
        }

        try {
            ((FTPChannelObserver) key.attachment()).registerMessageWriter(new FTPListAnswerMessage(entries));
        } catch (RegisteringSecondDataWriterException e) {
            throw new InvalidMessageException(e);
        }
    }

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull FTPGetMessage message)
            throws IOException {
        Path path = realPath(Paths.get(message.getPath()));

        try {
            ((FTPChannelObserver) key.attachment()).registerFileTransmitter(path);
        } catch (RegisteringSecondDataWriterException e) {
            throw new InvalidMessageException(e);
        }
    }

    private @NotNull Path realPath(final @NotNull Path path) throws InvalidPathException {
        if (path.isAbsolute()) {
            throw new InvalidPathException();
        }

        Path real = trackedFolder.resolve(path).normalize();
        if (!trackedFolder.equals(real) && trackedFolder.startsWith(real)) {
            throw new InvalidPathException();
        }

        return real;
    }
}
