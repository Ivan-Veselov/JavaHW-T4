package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * VCS Manager which can create VCS repository interfaces. Repository interface can be created for already existed
 * system or for a new one.
 */
public final class VCSManager {
    /**
     * Name of a VCS directory which will be created in folder where VCS is initialized.
     */
    public static final @NotNull String vcsDirectoryName = ".vcs";

    /**
     * Name of directory in which VCS's objects namely, blobs, trees and commits, will be stored.
     */
    public static final @NotNull String objectsDirectoryName = "objects";

    /**
     * Initializes new VCS in a given folder.
     *
     * @param path a path to a folder in which VCS should be initialized.
     * @return a repository interface for newly created VCS.
     * @throws DirectoryExpected if given path does not represent a directory.
     * @throws VCSIsAlreadyInitialized if VCS is already initialized in a given directory.
     * @throws IOException if any IO exception occurs during VCS folder initialization.
     */
    public static @NotNull Repository initializeVCS(final @NotNull Path path)
            throws DirectoryExpected, VCSIsAlreadyInitialized, IOException {
        if (!Files.isDirectory(path)) {
            throw new DirectoryExpected();
        }

        Path vcsDirectoryPath = path.resolve(vcsDirectoryName);

        try {
            Files.createDirectory(vcsDirectoryPath);
            Files.createDirectory(vcsDirectoryPath.resolve(objectsDirectoryName));
        } catch (FileAlreadyExistsException caught) {
            throw new VCSIsAlreadyInitialized(caught);
        }

        return new Repository(path);
    }

    /**
     * Returns repository interface for a given directory.
     *
     * @param path a path to a directory with repository inside it.
     * @return repository interface for a given directory.
     * @throws DirectoryExpected if given path does not represent a directory.
     * @throws VCSWasNotInitialized if there is no repository in a given directory.
     */
    public static @NotNull Repository getRepository(final @NotNull Path path)
            throws DirectoryExpected, VCSWasNotInitialized {
        if (!Files.isDirectory(path)) {
            throw new DirectoryExpected();
        }

        Path vcsDirectoryPath = path.resolve(vcsDirectoryName);

        if (!Files.exists(vcsDirectoryPath)) {
            throw new VCSWasNotInitialized();
        }

        return new Repository(path);
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
         * Returns a path to root directory of this repository.
         */
        public @NotNull Path getRootDirectory() {
            return rootDirectory;
        }

        /**
         * Returns a path to the VCS directory inside root directory of the repository.
         */
        public @NotNull Path getVCSDirectory() {
            return getRootDirectory().resolve(vcsDirectoryName);
        }

        /**
         * Returns a path to the VCS's objects directory.
         */
        public @NotNull Path getObjectsDirectory() {
            return getVCSDirectory().resolve(objectsDirectoryName);
        }

        /**
         * Copies given file into VCS inner storage.
         *
         * @param path a path to a file which should be copied.
         * @return a path to a copy of a given file in VCS's storage.
         * @throws RegularFileExpected if given path does not represent a regular file.
         * @throws IOException if any IO exception occurs during process of copying.
         */
        public @NotNull Path addFileToStorage(final @NotNull Path path) throws RegularFileExpected, IOException {
            if (!Files.isRegularFile(path)) {
                throw new RegularFileExpected();
            }

            String sha1HexString = DigestUtils.sha1Hex(Files.readAllBytes(path));
            return Files.copy(path, getObjectsDirectory().resolve(sha1HexString), REPLACE_EXISTING, NOFOLLOW_LINKS);
        }
    }
}
