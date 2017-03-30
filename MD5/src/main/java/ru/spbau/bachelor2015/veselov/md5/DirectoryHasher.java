package ru.spbau.bachelor2015.veselov.md5;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.List;

/**
 * Objects of this class allows to calculate MD5 hash of a directory.
 */
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

        updateMessageDigest(message, path.toFile(), recursiveCaller);

        return new MD5Hash(message.digest());
    }

    public @NotNull MD5Hash getHashConcurrently(final @NotNull Path path) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new DirectoryHashTask(path.toFile()));
    }

    private static void updateMessageDigest(final @NotNull MessageDigest message,
                                            final @NotNull File file,
                                            final @NotNull BiConsumer<MessageDigest, File> action)
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
            action.accept(message, subfile);
        }
    }

    private static void updateMessageDigest(final @NotNull MessageDigest message, final @NotNull InputStream stream)
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

        public @NotNull byte[] getBytes() {
            return bytes;
        }
    }

    private static BiConsumer<MessageDigest, File> recursiveCaller = new BiConsumer<MessageDigest, File>() {
        @Override
        public void accept(final @NotNull MessageDigest message, final @NotNull File file) {
            try {
                updateMessageDigest(message, file, this);
            } catch (IOException | IrregularFileException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static class DirectoryHashTask extends RecursiveTask<MD5Hash> {
        final @NotNull MessageDigest message;

        final @NotNull File file;

        public DirectoryHashTask(final @NotNull File file) {
            try {
                this.message = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            this.file = file;
        }

        @Override
        protected MD5Hash compute() {
            final List<ForkJoinTask<MD5Hash>> taskList = new ArrayList<>();

            try {
                updateMessageDigest(
                    message,
                    file,
                    (messageDigest, file) -> {
                        DirectoryHashTask task = new DirectoryHashTask(file);
                        task.fork();
                        taskList.add(task);
                    }
                );
            } catch (IOException | IrregularFileException e) {
                throw new RuntimeException(e);
            }

            for (ForkJoinTask<MD5Hash> task : taskList) {
                message.update(task.join().getBytes());
            }

            return new MD5Hash(message.digest());
        }
    }
}
