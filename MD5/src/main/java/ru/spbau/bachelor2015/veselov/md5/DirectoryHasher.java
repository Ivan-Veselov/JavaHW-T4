package ru.spbau.bachelor2015.veselov.md5;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Arrays;

public final class DirectoryHasher {
    public static @NotNull MD5Hash getHash(final @NotNull Path path) {
        throw new UnsupportedOperationException();
    }

    public static @NotNull MD5Hash getHashConcurrent(final @NotNull Path path) {
        throw new UnsupportedOperationException();
    }

    public class MD5Hash {
        private final @NotNull byte[] bytes;

        private MD5Hash(final @NotNull byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MD5Hash)) {
                return false;
            }

            MD5Hash other = (MD5Hash) o;
            return Arrays.equals(this.bytes, other.bytes);
        }
    }
}
