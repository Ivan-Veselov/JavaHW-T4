package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.IndependentPath;

import java.nio.file.Path;

/**
 * An ftp request message. This message asks server to send a content of a specified file.
 */
public class FTPGetMessage implements FTPMessage {
    private final @NotNull IndependentPath path;

    /**
     * Creates a message.
     *
     * @param path a path to a file which content is requested by this message.
     */
    public FTPGetMessage(final @NotNull Path path) {
        this.path = new IndependentPath(path);
    }

    /**
     * Returns a path to a file which content is requested by this message.
     */
    public @NotNull Path getPath() {
        return path.toPath();
    }
}
