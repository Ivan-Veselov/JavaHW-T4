package ru.spbau.bachelor2015.veselov.hw04;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.InvalidFTPMessageException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.InvalidPathException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.NoSuchMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.exceptions.InvalidMessageException;

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
import java.util.Optional;

/**
 * TODO: limit a length of an incoming message
 * TODO: add javadocs to Server
 * TODO: add FTPMessageTransmitter test
 * TODO: add javadocs to exceptions
 * TODO: check if it is possible to see something outside of a tracked folder
 */
public class Server {
    private final static @NotNull Logger logger = LogManager.getLogger(Server.class.getCanonicalName());

    private volatile boolean shouldRun;

    private final int port;

    private final @NotNull Path trackedFolder;

    private final @NotNull Thread serverThread;

    public Server(final @NotNull Path trackedFolder, final int port) {
        logger.info("New Server ({}) is created", this);

        this.trackedFolder = trackedFolder;
        this.port = port;

        this.serverThread =
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
                                        ((FTPChannelAttachment) key.attachment()).getTransmitter().write();
                                    }
                                } catch (InvalidMessageException | InvalidFTPMessageException e) {
                                    logger.error("Server ({}) received an invalid message", this);

                                    key.channel().close();
                                } catch (IOException ignored) {
                                    logger.error(
                                    "IOException occurred during interaction of Server ({}) with a connection",
                                    this);

                                    key.channel().close();
                                }
                            }

                            selector.selectedKeys().clear();
                        }

                        logger.info("Server ({}) is stopped", this);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    public void start() {
        shouldRun = true;
        serverThread.start();
    }

    public void stop() throws InterruptedException {
        shouldRun = false;
        serverThread.join();
    }

    private void acceptNewConnection(final @NotNull Selector selector,
                                     final @NotNull ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return;
        }

        logger.info("Server ({}) accepted new connection", this);

        socketChannel.configureBlocking(false);

        socketChannel.register(
                selector,
               SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                new FTPChannelAttachment(new FTPMessageReader(socketChannel), new FTPMessageTransmitter(socketChannel)));
    }

    private void readMessage(final @NotNull SelectionKey key)
            throws IOException, InvalidMessageException, InvalidFTPMessageException {
        FTPMessageReader reader = ((FTPChannelAttachment) key.attachment()).getReader();

        Optional<FTPMessage> optional = reader.read();
        if (!optional.isPresent()) {
            return;
        }

        logger.info("Server ({}) received new message", this);

        FTPMessage message = optional.get();
        if (message instanceof FTPListMessage) {
            handleMessage(key, (FTPListMessage) message);
        } else {
            throw new NoSuchMessageException();
        }
    }

    private void handleMessage(final @NotNull SelectionKey key, final @NotNull FTPListMessage message)
            throws IOException, InvalidFTPMessageException {
        Path path = Paths.get(message.getPath());

        if (path.isAbsolute()) {
            throw new InvalidPathException();
        }

        File[] files = trackedFolder.resolve(path).toFile().listFiles();

        List<FTPListAnswerMessage.Entry> entries = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                entries.add(new FTPListAnswerMessage.Entry(trackedFolder.relativize(file.toPath()).toString(),
                                                           file.isDirectory()));
            }
        }

        ((FTPChannelAttachment) key.attachment()).getTransmitter().addMessage(new FTPListAnswerMessage(entries));
    }
}
