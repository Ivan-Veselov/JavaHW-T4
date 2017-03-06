package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
         *
         * TODO: add sorting of child nodes
         */
        public final class Tree extends StoredObject {
            private final @NotNull String sha1Hash;

            private final @NotNull List<Named<Tree>> treeChildren;

            private final @NotNull List<Named<Blob>> blobChildren;

            /**
             * Constructs a Tree object. A file for this object in VCS inner storage will be created.
             *
             * @param treeList a list of named child trees.
             * @param blobList a list of named child blobs.
             * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
             */
            public Tree(final @NotNull List<Named<Tree>> treeList,
                        final @NotNull List<Named<Blob>> blobList) throws IOException {
                treeChildren = new ArrayList<>(treeList);
                blobChildren = new ArrayList<>(blobList);

                byte[] data;
                try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                     ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {

                    Function<Named<? extends StoredObject>, Named<String>> mapper =
                            namedObject -> new Named<>(namedObject.getObject().getSha1Hash(), namedObject.getName());

                    objectStream.writeObject(treeChildren.stream()
                                                         .map(mapper)
                                                         .collect(Collectors.toList()));
                    objectStream.writeObject(blobChildren.stream()
                                                         .map(mapper)
                                                         .collect(Collectors.toList()));
                    objectStream.flush();

                    data = byteStream.toByteArray();
                }

                sha1Hash = DigestUtils.sha1Hex(data);
                Files.write(getPathInStorage(), data);
            }

            /**
             * Returns SHA1 hash of data represented by this tree.
             */
            public @NotNull String getSha1Hash() {
                return sha1Hash;
            }
        }
    }
}
