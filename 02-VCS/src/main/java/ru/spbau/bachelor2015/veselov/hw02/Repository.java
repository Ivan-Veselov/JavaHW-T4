package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.*;

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
 * TODO: make base class for VCS exceptions
 * TODO: javadocs for exceptions
 * TODO: javadocs for SHA1Hash
 * TODO: javadocs for FileEntity
 * TODO: javadocs for NormalRelativePath
 *
 * An interface which allows modify and inspect files inside VCS.
 */
public final class Repository {
    /**
     * Name of a VCS directory which will be created in folder where VCS is initialized.
     */
    private static final @NotNull String vcsDirectoryName = ".vcs";

    /**
     * Name of a directory in which VCS's objects namely, blobs, trees and commits, will be stored.
     */
    private static final @NotNull String objectsDirectoryName = "objects";

    /**
     * Name of a directory in which VCS's references will be stored.
     */
    private static final @NotNull String referencesDirectoryName = "refs";

    /**
     * Name of a directory in which VCS's head references will be stored.
     */
    private static final @NotNull String headsDirectoryName = "heads";

    /**
     * Name of an index file which stores information about a commit that is created.
     */
    private static final @NotNull String indexFileName = "index";

    /**
     * Name of a HEAD file which stores current commit.
     */
    private static final @NotNull String headFileName = "HEAD";

    private static final @NotNull String initialCommitMessage = "Initial commit";

    private static final @NotNull String masterBranchName = "master";

    private final @NotNull NormalRelativePath rootDirectory;

    private Repository(final @NotNull Path rootDirectory) {
        this.rootDirectory = new NormalRelativePath(rootDirectory);
    }

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

        Repository repository = new Repository(path);

        // System.out.println(path);
        // System.out.println(repository.getVCSDirectory().realPath());

        try {
            Files.createDirectory(repository.getVCSDirectory().realPath());
            Files.createDirectory(repository.getObjectsDirectory().realPath());
            Files.createDirectory(repository.getReferencesDirectory().realPath());
            Files.createDirectory(repository.getHeadsDirectory().realPath());
            Files.createFile(repository.getIndexFile().realPath());
            Files.createFile(repository.getHeadFile().realPath());
        } catch (FileAlreadyExistsException caught) {
            throw new VCSIsAlreadyInitialized(caught);
        }

        try {
            Tree emptyTree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
            Commit initialCommit = repository.new Commit(initialCommitMessage, Collections.emptyList(), emptyTree);

            repository.createReference(masterBranchName, initialCommit);
            repository.writeToHead(masterBranchName);

            repository.writeToIndex(new ArrayList<>());
        } catch (NamesContainsDuplicates | AlreadyExists | FileFromWorkingDirectoryExpected | NoSuchElement e) {
            throw new RuntimeException(e);
        }

        return repository;
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
     * Checks whether or not given path lies inside repository root folder.
     *
     * @param path a path to check.
     * @return true if path lies inside repository folder, false otherwise.
     */
    public boolean isInsideRepository(final @NotNull Path path) {
        return getRootDirectory().relativePath(path).isInner();
    }

    /**
     * Checks whether or not given path lies inside vcs inner storage.
     *
     * @param path a path to check.
     * @return true if path lies inside repository inner storage, false otherwise.
     */
    public boolean isInsideStorage(final @NotNull Path path) {
        return getVCSDirectory().relativePath(path).isInner();
    }

    /**
     * Checks whether or not given path lies inside working directory.
     *
     * @param path a path to check.
     * @return true if path lies inside working directory, false otherwise.
     */
    public boolean isInsideWorkingDirectory(final @NotNull Path path) {
        return isInsideRepository(path) && !isInsideStorage(path);
    }

    /**
     * Creates a new reference inside VCS inner storage.
     *
     * @param name a name of a new reference.
     * @param commit a commit which will be referenced.
     * @throws AlreadyExists if reference with such name already exists.
     * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
     */
    public void createReference(final @NotNull String name, final @NotNull Commit commit)
            throws AlreadyExists, IOException {
        NormalRelativePath pathToReference = getReferencePath(name);

        if (Files.exists(pathToReference.realPath())) {
            throw new AlreadyExists();
        }

        try (OutputStream fileStream = Files.newOutputStream(pathToReference.realPath());
             ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
            objectStream.writeObject(commit.getVCSHash());
        }
    }

    /**
     * Returns a commit which is referenced by a reference with a given name.
     *
     * @param name name of reference.
     * @throws NoSuchElement if there is no reference with a given name or there is no commit with a hash that this
     *                       reference stores
     * @throws InvalidDataInStorage if it is an attempt to read an invalid data from storage.
     * @throws IOException if any IO error occurs during data reading.
     */
    @SuppressWarnings("unchecked")
    public Commit getCommitByReference(final @NotNull String name)
            throws NoSuchElement, IOException, InvalidDataInStorage {
        NormalRelativePath pathToReference = getReferencePath(name);

        if (!Files.exists(pathToReference.realPath())) {
            throw new NoSuchElement();
        }

        SHA1Hash commitHash;
        try (InputStream inputStream = Files.newInputStream(pathToReference.realPath());
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            commitHash = (SHA1Hash) objectInputStream.readObject();
        } catch (ClassNotFoundException | ClassCastException e ) {
            throw new InvalidDataInStorage(e);
        }

        return new Commit(commitHash);
    }

    /**
     * Updates an entry for a given file in an index of this repository.
     *
     * @param path path to file which entry will be updated.
     * @throws RegularFileExpected if given path doesn't lead to a regular file.
     * @throws FileFromWorkingDirectoryExpected if given path doesn't lead to file which lies in a working directory of
     *                                          this repository.
     * @throws IOException if any IO exception occurs during reading of data from vcs storage.
     * @throws InvalidDataInStorage if any logical error occurs during reading of data from vcs storage.
     */
    public void updateFileStateInIndex(final @NotNull Path path)
            throws RegularFileExpected, FileFromWorkingDirectoryExpected, IOException, InvalidDataInStorage {
        if (Files.exists(path) && !Files.isRegularFile(path)) {
            throw new RegularFileExpected();
        }

        if (!isInsideWorkingDirectory(path)) {
            throw new FileFromWorkingDirectoryExpected();
        }

        List<FileEntity> entities = readFromIndex();
        for (int i = 0; i < entities.size(); i++) {
            FileEntity entity = entities.get(i);
            if (Files.isSameFile(path, entity.getPathToFile().realPath())) {
                if (Files.exists(path)) {
                    entities.set(i, new FileEntity(getRootDirectory().relativePath(path), new Blob(path).getVCSHash()));
                } else {
                    entities.remove(i);
                }

                writeToIndex(entities);
                return;
            }
        }

        if (Files.exists(path)) {
            entities.add(new FileEntity(getRootDirectory().relativePath(path), new Blob(path).getVCSHash()));
        }

        writeToIndex(entities);
    }

    // TODO: javadocs
    // TODO: add check for nothing to commit
    public @NotNull Commit newCommitFromIndex(final @NotNull String message)
            throws IOException, InvalidDataInStorage, NoSuchElement {
        Commit commit =  new Commit(message, Collections.singletonList(getCurrentCommit()), buildTreeFromIndex());
        if (!isHeadContainReference()) {
            writeToHead(commit);
        }

        return commit;
    }

    /**
     * Returns a path to root directory of this repository.
     */
    private @NotNull NormalRelativePath getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Returns a path to the VCS directory inside root directory of the repository.
     */
    private @NotNull NormalRelativePath getVCSDirectory() {
        return getRootDirectory().resolve(vcsDirectoryName);
    }

    /**
     * Returns a path to the VCS's objects directory.
     */
    private @NotNull NormalRelativePath getObjectsDirectory() {
        return getVCSDirectory().resolve(objectsDirectoryName);
    }

    /**
     * Returns a path to the VCS's references directory.
     */
    private @NotNull NormalRelativePath getReferencesDirectory() {
        return getVCSDirectory().resolve(referencesDirectoryName);
    }

    /**
     * Returns a path to the VCS's head references directory.
     */
    private @NotNull NormalRelativePath getHeadsDirectory() {
        return getReferencesDirectory().resolve(headsDirectoryName);
    }

    /**
     * Returns a path to the vcs index file.
     */
    private @NotNull NormalRelativePath getIndexFile() {
        return getVCSDirectory().resolve(indexFileName);
    }

    /**
     * Returns a path to the vcs index file.
     */
    private @NotNull NormalRelativePath getHeadFile() {
        return getVCSDirectory().resolve(headFileName);
    }

    private @NotNull NormalRelativePath getReferencePath(final @NotNull String name) {
        return getHeadsDirectory().resolve(name);
    }

    private void writeToIndex(final @NotNull List<FileEntity> fileEntities)
            throws FileFromWorkingDirectoryExpected, IOException {
        for (FileEntity entity : fileEntities) {
            if (!isInsideWorkingDirectory(entity.getPathToFile().realPath())) {
                throw new FileFromWorkingDirectoryExpected();
            }
        }

        try (OutputStream outputStream = Files.newOutputStream(getIndexFile().realPath());
             ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
            objectStream.writeObject(fileEntities);
        }
    }

    @SuppressWarnings("unchecked")
    private List<FileEntity> readFromIndex() throws IOException, InvalidDataInStorage {
        List<FileEntity> entities;

        try (InputStream inputStream = Files.newInputStream(getIndexFile().realPath());
             ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
            entities = (List<FileEntity>) objectStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new InvalidDataInStorage(e);
        }

        for (FileEntity entity : entities) {
            if (!isInsideWorkingDirectory(entity.getPathToFile().realPath())) {
                throw new InvalidDataInStorage();
            }
        }

        return entities;
    }

    private void writeToHead(final @NotNull String referenceName) throws NoSuchElement, IOException {
        if (!Files.exists(getReferencePath(referenceName).realPath())) {
            throw new NoSuchElement();
        }

        try (OutputStream outputStream = Files.newOutputStream(getHeadFile().realPath());
             ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
            objectStream.writeObject(referenceName);
        }
    }

    private void writeToHead(final @NotNull Commit commit) throws NoSuchElement, IOException {
        try (OutputStream outputStream = Files.newOutputStream(getHeadFile().realPath());
             ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
            objectStream.writeObject(commit.getVCSHash());
        }
    }

    private @NotNull Object readFromHead() throws IOException, InvalidDataInStorage {
        try (InputStream inputStream = Files.newInputStream(getHeadFile().realPath());
             ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
            return objectStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new InvalidDataInStorage(e);
        }
    }

    private boolean isHeadContainReference() throws IOException, InvalidDataInStorage {
        return readFromHead() instanceof String;
    }

    private @NotNull Commit getCurrentCommit() throws IOException, InvalidDataInStorage {
        Object o = readFromHead();

        if (o instanceof SHA1Hash) {
            try {
                return new Commit((SHA1Hash) o);
            } catch (NoSuchElement e) {
                throw new RuntimeException(e);
            }
        }

        if (o instanceof String) {
            try {
                return getCommitByReference((String) o);
            } catch (NoSuchElement e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException();
    }

    private @NotNull Tree buildTreeFromIndex() throws IOException, InvalidDataInStorage {
        return buildTreeFromIndex(readFromIndex());
    }

    private @NotNull Tree buildTreeFromIndex(final @NotNull List<FileEntity> entities)
            throws IOException, InvalidDataInStorage {
        List<Named<Blob>> blobs = new ArrayList<>();
        List<Named<Tree>> trees = new ArrayList<>();

        Map<String, List<FileEntity>> subdirectories = new HashMap<>();
        for (FileEntity entity : entities) {
            Path relativeComponent = entity.getPathToFile().relativePath();
            if (relativeComponent.getNameCount() == 1) {
                try {
                    blobs.add(new Named<>(new Blob(entity.getContentHash()), relativeComponent.getName(0).toString()));
                } catch (NoSuchElement e) {
                    throw new InvalidDataInStorage(e);
                }

                continue;
            }

            String name = relativeComponent.getName(0).toString();
            if (!subdirectories.containsKey(name)) {
                subdirectories.put(name, new ArrayList<>());
            }

            subdirectories.get(name).add(new FileEntity(entity.getPathToFile().shifted(), entity.getContentHash()));
        }

        for (Map.Entry<String, List<FileEntity>> entry : subdirectories.entrySet()) {
            trees.add(new Named<>(buildTreeFromIndex(entry.getValue()), entry.getKey()));
        }

        try {
            return new Tree(trees, blobs);
        } catch (NamesContainsDuplicates e) {
            throw new InvalidDataInStorage(e);
        }
    }

    /**
     * An abstract class which represents an arbitrary vcs object that stored in an 'objects' folder of vcs storage
     * and has a SHA1 hash.
     */
    public abstract class StoredObject {
        /**
         * Returns SHA1 hash of data represented by this object.
         */
        public abstract @NotNull SHA1Hash getVCSHash();

        /**
         * Returns a path to data represented by this object.
         */
        public @NotNull Path getPathInStorage() {
            return getObjectsDirectory().resolve(getVCSHash().getHex()).realPath();
        }
    }

    private interface StoredObjectLoader<T extends StoredObject> {
        @NotNull T load(final @NotNull SHA1Hash hash) throws NoSuchElement, InvalidDataInStorage, IOException;
    }

    private final static class StoredObjectIterator<I, E, T extends StoredObject> implements Iterator<E> {
        private final @NotNull Iterator<I> iterator;

        private final @NotNull StoredObjectLoader<T> loader;

        private final @NotNull Function<? super I, ? extends SHA1Hash> innerConverter;

        private final @NotNull BiFunction<? super I, ? super T, ? extends E> outerConverter;

        public static <U extends StoredObject> StoredObjectIterator<SHA1Hash, U, U> fromHashIterator(
                                                                    final @NotNull Iterator<SHA1Hash> iterator,
                                                                    final @NotNull StoredObjectLoader<U> loader) {
            return new StoredObjectIterator<>(iterator, loader, Function.identity(), (x, y) -> y);
        }

        public StoredObjectIterator(final @NotNull Iterator<I> iterator,
                                    final @NotNull StoredObjectLoader<T> loader,
                                    final @NotNull Function<? super I, ? extends SHA1Hash> innerConverter,
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
    public class Blob extends StoredObject {
        private final @NotNull SHA1Hash contentHash;

        /**
         * Creates Blob object. Given file will be copied into VCS inner storage.
         *
         * @param path a path to a file which should be copied.
         * @throws FileFromWorkingDirectoryExpected if given file doesn't lie in working directory.
         * @throws RegularFileExpected if given path does not represent a regular file.
         * @throws IOException if any IO exception occurs during process of copying.
         */
        public Blob(final @NotNull Path path)
                throws FileFromWorkingDirectoryExpected, RegularFileExpected, IOException {
            if (!isInsideWorkingDirectory(path)) {
                throw new FileFromWorkingDirectoryExpected();
            }

            if (!Files.isRegularFile(path)) {
                throw new RegularFileExpected();
            }

            contentHash = new SHA1Hash(path);
            Files.copy(path, getPathInStorage(), REPLACE_EXISTING, NOFOLLOW_LINKS);
        }

        /**
         * Creates Blob object for previously existed data by its hash.
         *
         * @param hash hash of data.
         * @throws NoSuchElement if there is no data with a given hash.
         */
        public Blob(final @NotNull SHA1Hash hash) throws NoSuchElement {
            contentHash = hash;

            if (!Files.exists(getPathInStorage())) {
                throw new NoSuchElement();
            }
        }

        /**
         * Returns SHA1 hash of data represented by this blob.
         */
        public @NotNull SHA1Hash getVCSHash() {
            return contentHash;
        }
    }

    /**
     * Tree object represent a node in a folder structure. Objects of this type contain references for other Tree
     * objects and Blob objects. Each reference supplied with a name of a referenced object.
     */
    public class Tree extends StoredObject {
        private final @NotNull SHA1Hash hash;

        private final @NotNull List<Named<SHA1Hash>> treeChildrenHashes;

        private final @NotNull List<Named<SHA1Hash>> blobChildrenHashes;

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

            Function<Named<? extends StoredObject>, Named<SHA1Hash>> mapper =
                    namedObject -> new Named<>(namedObject.getObject().getVCSHash(), namedObject.getName());

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

            hash = new SHA1Hash(data);
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
        public Tree(final @NotNull SHA1Hash hash) throws NoSuchElement, InvalidDataInStorage, IOException {
            this.hash = hash;

            if (!Files.exists(getPathInStorage())) {
                throw new NoSuchElement();
            }

            try (InputStream inputStream = Files.newInputStream(getPathInStorage());
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                treeChildrenHashes = (List<Named<SHA1Hash>>) objectInputStream.readObject();
                blobChildrenHashes = (List<Named<SHA1Hash>>) objectInputStream.readObject();
            } catch (ClassNotFoundException | ClassCastException e ) {
                throw new InvalidDataInStorage(e);
            }
        }

        /**
         * Returns SHA1 hash of data represented by this tree.
         */
        @Override
        public @NotNull SHA1Hash getVCSHash() {
            return hash;
        }

        /**
         * Returns an iterable over named Tree children of this Tree.
         */
        public @NotNull Iterable<Named<Tree>> treeChildren() {
            return () -> new StoredObjectIterator<>(treeChildrenHashes.iterator(),
                                                    Tree::new,
                                                    Named::getObject,
                                                    Named::replace);
        }

        /**
         * Returns an iterable over named Blob children of this Tree.
         */
        public @NotNull Iterable<Named<Blob>> blobChildren() {
            return () -> new StoredObjectIterator<>(blobChildrenHashes.iterator(),
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
    public class Commit extends StoredObject {
        private final @NotNull SHA1Hash hash;

        private final @NotNull String author;

        private final @NotNull String message;

        private final @NotNull Date date;

        private final @NotNull SHA1Hash underlyingTreeHash;

        private final @NotNull List<SHA1Hash> parentCommitsHashes;

        /**
         * Constructs a Commit object. A file for this object in VCS inner storage will be created.
         *
         * @param message a message of this commit.
         * @param parentCommits a list of parent commits.
         * @param tree a file structure which is referenced by this commit.
         * @throws IOException if any IO exception occurs during a creation of file for this object in storage.
         */
        public Commit(final @NotNull String message,
                      final @NotNull List<Commit> parentCommits,
                      final @NotNull Tree tree) throws IOException {
            this.author = System.getProperty("user.name");
            this.message = message;

            date = new Date();

            parentCommitsHashes = new ArrayList<>(parentCommits.stream()
                                                               .map(Commit::getVCSHash)
                                                               .collect(Collectors.toList()));

            underlyingTreeHash = tree.getVCSHash();

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

            hash = new SHA1Hash(data);
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
        public Commit(final @NotNull SHA1Hash hash) throws NoSuchElement, InvalidDataInStorage, IOException {
            this.hash = hash;

            if (!Files.exists(getPathInStorage())) {
                throw new NoSuchElement();
            }

            try (InputStream inputStream = Files.newInputStream(getPathInStorage());
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                author = (String) objectInputStream.readObject();
                message = (String) objectInputStream.readObject();
                date = (Date) objectInputStream.readObject();
                underlyingTreeHash = (SHA1Hash) objectInputStream.readObject();
                parentCommitsHashes = (List<SHA1Hash>) objectInputStream.readObject();
            } catch (ClassNotFoundException | ClassCastException e ) {
                throw new InvalidDataInStorage(e);
            }
        }

        /**
         * Returns SHA1 hash of data represented by this commit.
         */
        @Override
        public @NotNull SHA1Hash getVCSHash() {
            return hash;
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
         * Returns an iterable over parent commits.
         */
        public @NotNull Iterable<Commit> parentCommits() {
            return () -> StoredObjectIterator.fromHashIterator(parentCommitsHashes.iterator(), Commit::new);
        }
    }
}
