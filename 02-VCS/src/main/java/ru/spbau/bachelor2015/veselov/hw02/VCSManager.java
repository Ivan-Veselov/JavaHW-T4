package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
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
     * Name of a directory in which VCS's objects namely, blobs, trees and commits, will be stored.
     */
    public static final @NotNull String objectsDirectoryName = "objects";

    /**
     * Name of a directory in which VCS's references will be stored.
     */
    public static final @NotNull String referencesDirectoryName = "refs";

    /**
     * Name of a directory in which VCS's head references will be stored.
     */
    public static final @NotNull String headsDirectoryName = "heads";

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
            Files.createDirectory(vcsDirectoryPath.resolve(referencesDirectoryName));
            Files.createDirectory(vcsDirectoryPath.resolve(referencesDirectoryName).resolve(headsDirectoryName));
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
         * Returns a path to the VCS's references directory.
         */
        public @NotNull Path getReferencesDirectory() {
            return getVCSDirectory().resolve(referencesDirectoryName);
        }

        /**
         * Returns a path to the VCS's head references directory.
         */
        public @NotNull Path getHeadsDirectory() {
            return getReferencesDirectory().resolve(headsDirectoryName);
        }

        private interface VCSElement {
            @NotNull Path getPathInStorage();
        }

        private abstract class StoredObject implements VCSElement {
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

        private interface StoredObjectLoader<T extends StoredObject> {
            @NotNull T load(final @NotNull String hash) throws NoSuchElement, InvalidDataInStorage, IOException;
        }

        private final static class StoredObjectIterator<I, E, T extends StoredObject> implements Iterator<E> {
            private final @NotNull Iterator<I> iterator;

            private final @NotNull StoredObjectLoader<T> loader;

            private final @NotNull Function<? super I, ? extends String> innerConverter;

            private final @NotNull BiFunction<? super I, ? super T, ? extends E> outerConverter;

            public static <U extends StoredObject> StoredObjectIterator<String, U, U> fromHashIterator(
                                                                        final @NotNull Iterator<String> iterator,
                                                                        final @NotNull StoredObjectLoader<U> loader) {
                return new StoredObjectIterator<>(iterator, loader, Function.identity(), (x, y) -> y);
            }

            public StoredObjectIterator(final @NotNull Iterator<I> iterator,
                                        final @NotNull StoredObjectLoader<T> loader,
                                        final @NotNull Function<? super I, ? extends String> innerConverter,
                                        final @NotNull BiFunction<? super I, ? super T, ? extends E> outerConverter) {
                this.iterator = iterator;
                this.loader = loader;
                this.innerConverter = innerConverter;
                this.outerConverter = outerConverter;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                I element = iterator.next();

                try {
                    return outerConverter.apply(element, loader.load(innerConverter.apply(element)));
                } catch (NoSuchElement | InvalidDataInStorage | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Blob object represents a copy of real file. Each blob object associated with such copy which is stored in a
         * VCS inner storage.
         */
        public final class Blob extends StoredObject {
            private final @NotNull String contentSha1Hash;

            /**
             * TODO: check that file is inside repository and not inside hidden folder
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
             * Creates Blob object for previously existed data by its hash.
             *
             * @param hash hash of data.
             * @throws NoSuchElement if there is no data with a given hash.
             */
            public Blob(final @NotNull String hash) throws NoSuchElement {
                contentSha1Hash = hash;

                if (!Files.exists(getPathInStorage())) {
                    throw new NoSuchElement();
                }
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
             * Creates Tree object for previously existed data by its hash.
             *
             * @param hash hash of data.
             * @throws NoSuchElement if there is no data with a given hash.
             * @throws InvalidDataInStorage if it is an attempt to read an invalid data from storage.
             * @throws IOException if any IO error occurs during data reading.
             */
            @SuppressWarnings("unchecked")
            public Tree(final @NotNull String hash) throws NoSuchElement, InvalidDataInStorage, IOException {
                sha1Hash = hash;

                if (!Files.exists(getPathInStorage())) {
                    throw new NoSuchElement();
                }

                try (InputStream inputStream = Files.newInputStream(getPathInStorage());
                     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    treeChildrenHashes = (List<Named<String>>) objectInputStream.readObject();
                    blobChildrenHashes = (List<Named<String>>) objectInputStream.readObject();
                } catch (ClassNotFoundException | ClassCastException e ) {
                    throw new InvalidDataInStorage(e);
                }
            }

            /**
             * Returns SHA1 hash of data represented by this tree.
             */
            @Override
            public @NotNull String getSha1Hash() {
                return sha1Hash;
            }

            /**
             * Returns an iterator on named Tree children of this Tree.
             */
            public @NotNull Iterator<Named<Tree>> treeChildrenIterator() {
                return new StoredObjectIterator<>(treeChildrenHashes.iterator(),
                                                  Tree::new,
                                                  Named::getObject,
                                                  Named::replace);
            }

            /**
             * Returns an iterator on named Blob children of this Tree.
             */
            public @NotNull Iterator<Named<Blob>> blobChildrenIterator() {
                return new StoredObjectIterator<>(blobChildrenHashes.iterator(),
                                                  Blob::new,
                                                  Named::getObject,
                                                  Named::replace);
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
                    objectStream.writeObject(underlyingTreeHash);
                    objectStream.writeObject(parentCommitsHashes);

                    objectStream.flush();

                    data = byteStream.toByteArray();
                }

                sha1Hash = DigestUtils.sha1Hex(data);
                Files.write(getPathInStorage(), data);
            }

            /**
             * Creates Commit object for previously existed data by its hash.
             *
             * @param hash hash of data.
             * @throws NoSuchElement if there is no data with a given hash.
             * @throws InvalidDataInStorage if it is an attempt to read an invalid data from storage.
             * @throws IOException if any IO error occurs during data reading.
             */
            @SuppressWarnings("unchecked")
            public Commit(final @NotNull String hash) throws NoSuchElement, InvalidDataInStorage, IOException {
                sha1Hash = hash;

                if (!Files.exists(getPathInStorage())) {
                    throw new NoSuchElement();
                }

                try (InputStream inputStream = Files.newInputStream(getPathInStorage());
                     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    author = (String) objectInputStream.readObject();
                    message = (String) objectInputStream.readObject();
                    date = (Date) objectInputStream.readObject();
                    underlyingTreeHash = (String) objectInputStream.readObject();
                    parentCommitsHashes = (List<String>) objectInputStream.readObject();
                } catch (ClassNotFoundException | ClassCastException e ) {
                    throw new InvalidDataInStorage(e);
                }
            }

            /**
             * Returns SHA1 hash of data represented by this commit.
             */
            @Override
            public @NotNull String getSha1Hash() {
                return sha1Hash;
            }

            /**
             * Returns an author of this commit.
             */
            public @NotNull String getAuthor() {
                return author;
            }

            /**
             * Returns this commit's message.
             */
            public @NotNull String getMessage() {
                return message;
            }

            /**
             * Returns creation date of this commit.
             */
            public @NotNull Date getDate() {
                return date;
            }

            /**
             * Returns a Tree object which is referenced by this commit.
             *
             * @throws InvalidDataInStorage if this commit referencing an invalid Tree or data was corrupted.
             * @throws IOException if any IO exception occurs during reading data for referenced Tree.
             * @throws NoSuchElement if there is no Tree with a hash that this commit stores.
             */
            public @NotNull Tree getTree() throws InvalidDataInStorage, IOException, NoSuchElement {
                return new Tree(underlyingTreeHash);
            }

            /**
             * Returns an iterator on parent commits.
             */
            public @NotNull Iterator<Commit> parentCommitsIterator() {
                return StoredObjectIterator.fromHashIterator(parentCommitsHashes.iterator(), Commit::new);
            }
        }

        /**
         * Reference is a named object which references some commit.
         */
        public final class Reference implements VCSElement {
            private final @NotNull String name;

            private final @NotNull String commitHash;

            /**
             * Creates a reference object. A file for this object in VCS inner storage will be created.
             *
             * @param name a name of new reference
             * @param commit a commit which will be referenced
             * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
             */
            public Reference(final @NotNull String name, final @NotNull Commit commit) throws IOException {
                this.name = name;
                commitHash = commit.getSha1Hash();

                try (OutputStream fileStream = Files.newOutputStream(getPathInStorage());
                     ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
                    objectStream.writeObject(commitHash);
                }
            }

            /**
             * Creates Reference element for previously existed data by its name.
             *
             * @param name name of reference.
             * @throws NoSuchElement if there is no reference with a given name.
             * @throws InvalidDataInStorage if it is an attempt to read an invalid data from storage.
             * @throws IOException if any IO error occurs during data reading.
             */
            @SuppressWarnings("unchecked")
            public Reference(final @NotNull String name) throws NoSuchElement, IOException, InvalidDataInStorage {
                this.name = name;

                if (!Files.exists(getPathInStorage())) {
                    throw new NoSuchElement();
                }

                try (InputStream inputStream = Files.newInputStream(getPathInStorage());
                     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    commitHash = (String) objectInputStream.readObject();
                } catch (ClassNotFoundException | ClassCastException e ) {
                    throw new InvalidDataInStorage(e);
                }
            }

            /**
             * Returns a path to data represented by this object.
             */
            @Override
            public @NotNull Path getPathInStorage() {
                return getHeadsDirectory().resolve(name);
            }

            /**
             * Returns a name of this reference.
             */
            public @NotNull String getName() {
                return name;
            }

            /**
             * Returns a commit which is referenced.
             *
             * @throws InvalidDataInStorage if referenced commit is invalid or data was corrupted.
             * @throws IOException if any IO exception occurs during reading data for referenced commit.
             * @throws NoSuchElement if there is no commit with a hash that this reference stores.
             */
            public @NotNull Commit getCommit() throws InvalidDataInStorage, IOException, NoSuchElement {
                return new Commit(commitHash);
            }
        }
    }
}
