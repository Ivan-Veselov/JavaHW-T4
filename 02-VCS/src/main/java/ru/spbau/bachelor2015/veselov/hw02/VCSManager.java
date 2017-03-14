package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        private abstract class StoredObject {
            /**
             * Returns SHA1 hash of data represented by this object.
             */
            public abstract @NotNull String getSha1Hash();

            /**
             * Returns a path to data represented by this object.
             */
            public @NotNull Path getPathInStorage() {
                return getObjectsDirectory().resolve(getSha1Hash());
            }
        }

        /**
         * Blob object represents a copy of real file. Each blob object associated with such copy which is stored in a
         * VCS inner storage.
         */
        public final class Blob extends StoredObject {
            private final @NotNull String contentSha1Hash;

            /**
             * Creates Blob object. Given file will be copied into VCS inner storage.
             *
             * @param path a path to a file which should be copied.
             * @throws RegularFileExpected if given path does not represent a regular file.
             * @throws IOException if any IO exception occurs during process of copying.
             */
            public Blob(final @NotNull Path path) throws RegularFileExpected, IOException {
                if (!Files.isRegularFile(path)) {
                    throw new RegularFileExpected();
                }

                contentSha1Hash = DigestUtils.sha1Hex(Files.readAllBytes(path));
                Files.copy(path, getPathInStorage(), REPLACE_EXISTING, NOFOLLOW_LINKS);
            }

            /**
             * Returns SHA1 hash of data represented by this blob.
             */
            public @NotNull String getSha1Hash() {
                return contentSha1Hash;
            }
        }

        /**
         * Tree object represent a node in a folder structure. Objects of this type contain references for other Tree
         * objects and Blob objects. Each reference supplied with a name of a referenced object.
         */
        public final class Tree extends StoredObject {
            private final @NotNull String sha1Hash;

            private final @NotNull List<Named<String>> treeChildrenHashes;

            private final @NotNull List<Named<String>> blobChildrenHashes;

            /**
             * Constructs a Tree object. A file for this object in VCS inner storage will be created.
             *
             * @param treeList a list of named child trees.
             * @param blobList a list of named child blobs.
             * @throws NamesContainsDuplicates if there are two equal names among given objects.
             * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
             */
            public Tree(final @NotNull List<Named<Tree>> treeList,
                        final @NotNull List<Named<Blob>> blobList) throws NamesContainsDuplicates, IOException {
                if (namesContainsDuplicates(treeList, blobList)) {
                    throw new NamesContainsDuplicates();
                }

                Function<Named<? extends StoredObject>, Named<String>> mapper =
                        namedObject -> new Named<>(namedObject.getObject().getSha1Hash(), namedObject.getName());

                treeChildrenHashes = new ArrayList<>(treeList.stream()
                                                       .map(mapper)
                                                       .collect(Collectors.toList()));
                blobChildrenHashes = new ArrayList<>(blobList.stream()
                                                       .map(mapper)
                                                       .collect(Collectors.toList()));

                Collections.sort(treeChildrenHashes);
                Collections.sort(blobChildrenHashes);

                byte[] data;
                try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                     ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
                    objectStream.writeObject(treeChildrenHashes);
                    objectStream.writeObject(blobChildrenHashes);

                    objectStream.flush();

                    data = byteStream.toByteArray();
                }

                sha1Hash = DigestUtils.sha1Hex(data);
                Files.write(getPathInStorage(), data);
            }

            /**
             * Returns SHA1 hash of data represented by this tree.
             */
            @Override
            public @NotNull String getSha1Hash() {
                return sha1Hash;
            }

            private boolean namesContainsDuplicates(final @NotNull List<Named<Tree>> treeList,
                                                    final @NotNull List<Named<Blob>> blobList) {
                Set<String> namesSet = new HashSet<>();

                namesSet.addAll(treeList.stream().map(Named::getName).collect(Collectors.toList()));
                namesSet.addAll(blobList.stream().map(Named::getName).collect(Collectors.toList()));

                return namesSet.size() < treeList.size() + blobList.size();
            }
        }

        /**
         * Commit object represents a meta information associated with a Tree structure. Commit consist of
         * author name, message, date of creation which initialized automatically and list of parent commits
         * that produced this one.
         *
         * TODO: add tests.
         */
        public final class Commit extends StoredObject {
            private final @NotNull String sha1Hash;

            private final @NotNull String author;

            private final @NotNull String message;

            private final @NotNull Date date;

            private final @NotNull String underlyingTreeHash;

            private final @NotNull List<String> parentCommitsHashes;

            /**
             * Constructs a Commit object. A file for this object in VCS inner storage will be created.
             *
             * @param author a name of an author of this commit.
             * @param message a message of this commit.
             * @param parentCommits a list of parent commits.
             * @param tree a file structure which is referenced by this commit.
             * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
             */
            public Commit(final @NotNull String author,
                          final @NotNull String message,
                          final @NotNull List<Commit> parentCommits,
                          final @NotNull Tree tree) throws IOException {
                this.author = author;
                this.message = message;

                date = new Date();

                parentCommitsHashes = new ArrayList<>(parentCommits.stream()
                                                                   .map(Commit::getSha1Hash)
                                                                   .collect(Collectors.toList()));

                underlyingTreeHash = tree.getSha1Hash();

                byte[] data;
                try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                     ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
                    objectStream.writeObject(this.author);
                    objectStream.writeObject(this.message);
                    objectStream.writeObject(date);
                    objectStream.writeObject(parentCommitsHashes);
                    objectStream.writeObject(underlyingTreeHash);

                    objectStream.flush();

                    data = byteStream.toByteArray();
                }

                sha1Hash = DigestUtils.sha1Hex(data);
                Files.write(getPathInStorage(), data);
            }

            /**
             * Returns SHA1 hash of data represented by this commit.
             */
            @Override
            public @NotNull String getSha1Hash() {
                return sha1Hash;
            }
        }
    }
}
