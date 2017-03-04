package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * VCS Manager which can create VCS repository interfaces. Repository interface can be created for already existed
 * system or for a new one.
 */
public final class VCSManager {
    /**
     * Name of a vcs directory which will be created in folder where VCS is initialized.
     */
    public static final @NotNull String vcsDirectoryName = ".vcs";

    /**
     * Initializes new VCS in a given folder.
     *
     * @param directory a path to a folder in which VCS should be initialized.
     * @return a repository interface for newly created VCS.
     * @throws DirectoryExpected if given path does not represent a directory.
     * @throws VCSIsAlreadyInitialized if VCS is already initialized in a given directory.
     * @throws IOException if any IO exception occurs during VCS folder initialization.
     */
    public static @NotNull Repository initializeVCS(final @NotNull Path directory)
            throws DirectoryExpected, VCSIsAlreadyInitialized, IOException {
        if (!Files.isDirectory(directory)) {
            throw new DirectoryExpected();
        }

        Path vcsDirectoryPath = directory.resolve(vcsDirectoryName);

        try {
            Files.createDirectory(vcsDirectoryPath);
        } catch (FileAlreadyExistsException caught) {
            throw new VCSIsAlreadyInitialized(caught);
        }

        return new Repository(directory);
    }

    /**
     * Returns repository interface for a given directory.
     *
     * @param directory a path to a directory with repository inside it.
     * @return repository interface for a given directory.
     * @throws DirectoryExpected if given path does not represent a directory.
     * @throws VCSWasNotInitialized if there is no repository in a given directory.
     */
    public static @NotNull Repository getRepository(final @NotNull Path directory)
            throws DirectoryExpected, VCSWasNotInitialized {
        if (!Files.isDirectory(directory)) {
            throw new DirectoryExpected();
        }

        Path vcsDirectoryPath = directory.resolve(vcsDirectoryName);

        if (!Files.exists(vcsDirectoryPath)) {
            throw new VCSWasNotInitialized();
        }

        return new Repository(directory);
    }

    /**
     * An interface which allows modify and inspect files inside VCS.
     */
    public static final class Repository {
        private final @NotNull Path rootDirectory;

        private Repository(final @NotNull Path rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        /**
         * Returns a path to a root directory of this repository.
         */
        public @NotNull Path getRootDirectory() {
            return rootDirectory;
        }
    }
}
