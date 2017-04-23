package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.MessageReader;

public class FTPChannelAttachment {
    private final @NotNull MessageReader reader;

    private final @NotNull FTPMessageTransmitter transmitter;

    public FTPChannelAttachment(final @NotNull MessageReader reader, final @NotNull FTPMessageTransmitter transmitter) {
        this.reader = reader;
        this.transmitter = transmitter;
    }

    public @NotNull MessageReader getReader() {
        return reader;
    }

    public @NotNull FTPMessageTransmitter getTransmitter() {
        return transmitter;
    }
}
