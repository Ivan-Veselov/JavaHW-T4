package ru.spbau.bachelor2015.veselov.hw04.messages;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw04.messages.util.IndependentPath;

import java.nio.file.Path;

/**
 * A request ftp message. This message asks server to list the content of a specified folder.
 */
public class FTPListMessage implements FTPMessage {
    private final @NotNull IndependentPath path;

    /**
     * Creates a message.
     *
     * @param path a path to a folder which content is requested by this message.
     */
    public FTPListMessage(final @NotNull Path path) {
        this.path = new IndependentPath(path);
    }

    /**
     * Returns a path to a folder which content is requested by this message.
     */
    public @NotNull Path getPath() {
        return path.toPath();
    }
}
