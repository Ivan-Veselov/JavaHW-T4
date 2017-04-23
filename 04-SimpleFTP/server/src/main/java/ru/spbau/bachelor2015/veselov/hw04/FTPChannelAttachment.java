package ru.spbau.bachelor2015.veselov.hw04;

import org.jetbrains.annotations.NotNull;

public class FTPChannelAttachment {
    private final @NotNull FTPMessageReader reader;

    private final @NotNull FTPMessageTransmitter transmitter;

    public FTPChannelAttachment(final @NotNull FTPMessageReader reader, final @NotNull FTPMessageTransmitter transmitter) {
        this.reader = reader;
        this.transmitter = transmitter;
    }

    public @NotNull FTPMessageReader getReader() {
        return reader;
    }

    public @NotNull FTPMessageTransmitter getTransmitter() {
        return transmitter;
    }
}
