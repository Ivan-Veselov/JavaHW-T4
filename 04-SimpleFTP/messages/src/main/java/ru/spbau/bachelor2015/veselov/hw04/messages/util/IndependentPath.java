package ru.spbau.bachelor2015.veselov.hw04.messages.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A file system independent path representation. Namely it's just a list of names.
 */
public class IndependentPath implements Serializable {
    private final @NotNull List<String> names;

    /**
     * Creates a file system independent path from file system dependent java.nio.Path
     *
     * @param path a path which will be converted in independent path.
     */
    public IndependentPath(final @NotNull Path path) {
        names = StreamSupport.stream(Spliterators.spliteratorUnknownSize(path.iterator(),
                                                                         Spliterator.ORDERED),
                                    false)
                             .map(Path::toString)
                             .collect(Collectors.toList());
    }

    /**
     * Returns an independent path converted to java.nio.Path with default file system.
     */
    public @NotNull Path toPath() {
        return Paths.get("", names.toArray(new String[0]));
    }
}
