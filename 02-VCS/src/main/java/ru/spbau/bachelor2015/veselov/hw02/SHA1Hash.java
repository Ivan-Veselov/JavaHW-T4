package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class which represents a SHA1 hash.
 */
public class SHA1Hash implements Serializable {
    private final @NotNull String hexString;

    /**
     * Creates a hash from data.
     *
     * @param data a data to hash.
     */
    public SHA1Hash(final @NotNull byte[] data) {
        hexString = DigestUtils.sha1Hex(data);
    }

    /**
     * Creates a hash from file content.
     *
     * @param path a path to a file.
     * @throws IOException if any IO exception occurs during reading of data from file.
     */
    public SHA1Hash(final @NotNull Path path) throws IOException {
        hexString = DigestUtils.sha1Hex((Files.readAllBytes(path)));
    }

    /**
     * Compares this hash with a given object.
     *
     * @param o an object to compare with.
     * @return true if argument is instance of this class and hex strings ob both hashes are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SHA1Hash)) {
            return false;
        }

        SHA1Hash other = (SHA1Hash) o;

        return hexString.equals(other.hexString);
    }

    /**
     * Return hex string of this hash.
     */
    public @NotNull String getHex() {
        return hexString;
    }

    /**
     * Return hex string of this hash.
     */
    @Override
    public @NotNull String toString() {
        return getHex();
    }
}
