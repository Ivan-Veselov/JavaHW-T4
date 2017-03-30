package ru.spbau.bachelor2015.veselov.md5;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class DirectoryHasher {
    public DirectoryHasher() {
    }

    public @NotNull MD5Hash getHash(final @NotNull Path path) throws IOException, IrregularFileException {
        MessageDigest message;

        try {
            message = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        updateMessageDigest(message, path.toFile());

        return new MD5Hash(message.digest());
    }

    public @NotNull MD5Hash getHashConcurrently(final @NotNull Path path) {
        throw new UnsupportedOperationException();
    }

    private void updateMessageDigest(final @NotNull MessageDigest message, final @NotNull File file)
            throws IOException, IrregularFileException {
        if (file.isFile()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                updateMessageDigest(message, stream);
            }

            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            throw new IrregularFileException();
        }

        message.update(file.getName().getBytes());

        for (File subfile : files) {
            updateMessageDigest(message, subfile);
        }
    }

    private void updateMessageDigest(final @NotNull MessageDigest message, final @NotNull InputStream stream)
            throws IOException {
        final int chunkSize = 4096;
        final byte[] chunk = new byte[chunkSize];

        try (BufferedInputStream bufferedStream = new BufferedInputStream(stream)) {
            while (true) {
                int bytesRead = bufferedStream.read(chunk, 0, chunkSize);
                if (bytesRead == -1) {
                    break;
                }

                message.update(chunk, 0, bytesRead);
            }
        }
    }

    public static class MD5Hash {
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
