package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.FileEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * An ftp message which represents an answer on list request message.
 */
public class FTPListAnswerMessage implements FTPMessage {
    private final @NotNull List<FileEntry> content;

    /**
     * Creates a message.
     *
     * @param content a list of entries which this message stores.
     */
    public FTPListAnswerMessage(final @NotNull List<FileEntry> content) {
        this.content = new ArrayList<>(content);
    }

    /**
     * Returns a list of entries which this message stores.
     */
    public @NotNull List<FileEntry> getContent() {
        return new ArrayList<>(content);
    }

}
