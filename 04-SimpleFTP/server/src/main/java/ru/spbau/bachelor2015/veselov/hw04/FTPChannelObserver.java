package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.NoDataWriterRegisteredException;
import ru.spbau.bachelor2015.veselov.hw04.exceptions.RegisteringSecondDataWriterException;
import ru.spbau.bachelor2015.veselov.hw04.messages.FTPMessage;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.DataWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageReader;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FTPMessageWriter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileTransmitter;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.InvalidMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.LongMessageException;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.exceptions.MessageNotReadException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class FTPChannelObserver {
    private final @NotNull SocketChannel channel;

    private final @NotNull SelectionKey selectionKey;

    private final @NotNull Server server;

    private final @NotNull FTPMessageReader reader;

    private @Nullable DataWriter writer;

    public FTPChannelObserver(final @NotNull SocketChannel channel,
                              final @NotNull Selector selector,
                              final @NotNull Server server) throws IOException {
        this.channel = channel;
        this.server = server;

        channel.configureBlocking(false);

        selectionKey = channel.register(selector, SelectionKey.OP_READ, this);

        reader = new FTPMessageReader(channel);
    }

    public void registerMessageWriter(final @NotNull FTPMessage message)
            throws RegisteringSecondDataWriterException, LongMessageException {
        if (writer != null) {
            throw new RegisteringSecondDataWriterException();
        }

        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        writer = new FTPMessageWriter(channel, message);
    }

    public void registerFileTransmitter(final @NotNull Path path)
            throws RegisteringSecondDataWriterException, IOException {
        if (writer != null) {
            throw new RegisteringSecondDataWriterException();
        }

        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        writer = new FileTransmitter(channel, path);
    }

    public void read() throws IOException {
        try {
            switch (reader.read()) {
                case NOT_READ:
                    return;

                case READ:
                    FTPMessage message;

                    try {
                        message = reader.getMessage();
                    } catch (MessageNotReadException e) {
                        throw new RuntimeException(e);
                    }

                    reader.reset();

                    try {
                        server.handleMessage(channel, message);
                    } catch (InvalidMessageException e) {
                        channel.close();
                    }

                    break;

                case CLOSED:
                    channel.close();
                    break;
            }
        } catch (InvalidMessageException e) {
            channel.close();
        }
    }

    public void write() throws IOException, NoDataWriterRegisteredException {
        if (writer == null) {
            throw new NoDataWriterRegisteredException();
        }

        if (writer.write()) {
            writer = null;
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }
}
