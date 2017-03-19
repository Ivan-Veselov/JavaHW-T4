package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RepositoryTest {
    @Rule
    public final @NotNull TemporaryFolder rootDirectory = new TemporaryFolder();

    // TODO: Maybe add some kind of a special rule
    private VCSManager.Repository repository;

    @Before
    public void initializeEnvironment() throws Exception {
        repository = VCSManager.initializeVCS(rootDirectory.getRoot().toPath());
    }

    @Test
    public void blobConstruction() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob.getPathInStorage().toFile()), is(true));
    }

    @Test(expected = RegularFileExpected.class)
    public void blobConstructionFromFolder() throws Exception {
        File folder = rootDirectory.newFolder();
        repository.new Blob(folder.toPath());
    }

    @Test
    public void multipleBlobsForSingleFile() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        repository.new Blob(pathToFile);
        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob.getPathInStorage().toFile()), is(true));
    }

    @Test
    public void blobConstructionFromHash() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        VCSManager.Repository.Blob blob1 = repository.new Blob(pathToFile);
        VCSManager.Repository.Blob blob2 = repository.new Blob(blob1.getSha1Hash());

        assertThat(blob1.getSha1Hash(), is(equalTo(blob2.getSha1Hash())));
        assertThat(blob1.getPathInStorage(), is(equalTo(blob2.getPathInStorage())));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void treeConstruction() throws Exception {
        final String fileName = "file";

        Path pathToFolder = rootDirectory.newFolder().toPath();
        Path pathToFile = Files.createFile(pathToFolder.resolve(fileName));

        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);
        repository.new Tree(Collections.emptyList(),
                            Collections.singletonList(new Named<>(blob, fileName)));

        // blob for file and tree for folder
        assertFilesInFolder(repository.getObjectsDirectory(), 2);
    }

    @Test
    public void multipleTreesForSingleFolder() throws Exception {
        repository.new Tree(Collections.emptyList(), Collections.emptyList());
        repository.new Tree(Collections.emptyList(), Collections.emptyList());

        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test(expected = NamesContainsDuplicates.class)
    public void testTreeConstructionWithNameDuplicates() throws Exception {
        final String fileName = "file";

        Path pathToFolder = rootDirectory.newFolder().toPath();
        Path pathToFile = Files.createFile(pathToFolder.resolve(fileName));

        VCSManager.Repository.Blob blob = repository.new Blob(pathToFile);

        repository.new Tree(Collections.emptyList(),
                Arrays.asList(new Named<>(blob, fileName), new Named<>(blob, fileName)));
    }

    @Test
    public void treeConstructionFromHash() throws Exception {
        VCSManager.Repository.Tree tree1 = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Tree tree2 = repository.new Tree(tree1.getSha1Hash());

        assertThat(tree1.getSha1Hash(), is(equalTo(tree2.getSha1Hash())));
        assertThat(tree1.getPathInStorage(), is(equalTo(tree2.getPathInStorage())));
        assertFilesInFolder(repository.getObjectsDirectory(), 1);
    }

    @Test
    public void commitConstruction() throws Exception {
        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        repository.new Commit("author", "message", Collections.emptyList(), tree);

        assertFilesInFolder(repository.getObjectsDirectory(), 2);
    }

    @Test
    public void commitConstructionFromHash() throws Exception {
        final String author = "author";
        final String message = "message";

        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Commit commit1 = repository.new Commit(author, message, Collections.emptyList(), tree);
        VCSManager.Repository.Commit commit2 = repository.new Commit(commit1.getSha1Hash());

        assertThat(commit1.getSha1Hash(), is(equalTo(commit2.getSha1Hash())));
        assertThat(commit1.getPathInStorage(), is(equalTo(commit2.getPathInStorage())));
        assertThat(commit1.getAuthor(), is(equalTo(commit2.getAuthor())));
        assertThat(commit1.getMessage(), is(equalTo(commit2.getMessage())));
        assertThat(commit1.getDate(), is(equalTo(commit2.getDate())));

        // one for Tree and another for commit
        assertFilesInFolder(repository.getObjectsDirectory(), 2);
    }

    @Test
    public void referenceConstruction() throws Exception {
        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Commit commit = repository.new Commit("author",
                                                                    "message",
                                                                    Collections.emptyList(), tree);
        repository.new Reference("reference", commit);

        assertFilesInFolder(repository.getObjectsDirectory(), 2);
        assertFilesInFolder(repository.getHeadsDirectory(), 1);
    }

    @Test
    public void referenceConstructionFromName() throws Exception {
        final String referenceName = "reference";

        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        VCSManager.Repository.Commit commit = repository.new Commit("author",
                "message",
                Collections.emptyList(), tree);
        VCSManager.Repository.Reference reference1 = repository.new Reference(referenceName, commit);
        VCSManager.Repository.Reference reference2 = repository.new Reference(referenceName);

        assertThat(reference1.getPathInStorage(), is(equalTo(reference2.getPathInStorage())));
        assertThat(reference1.getName(), is(equalTo(reference2.getName())));
    }

    private void assertFilesInFolder(final @NotNull Path pathToFolder, final int expected) {
        File[] filesInFolder = pathToFolder.toFile().listFiles();
        assertThat(filesInFolder, notNullValue());

        // NPE is checked on a previous line
        assertThat(filesInFolder.length, is(equalTo(expected)));
    }
}