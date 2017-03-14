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
        Path pathToFolder = rootDirectory.newFolder().toPath();

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
    public void commitConstruction() throws Exception {
        VCSManager.Repository.Tree tree = repository.new Tree(Collections.emptyList(), Collections.emptyList());
        repository.new Commit("author", "message", Collections.emptyList(), tree);

        assertFilesInFolder(repository.getObjectsDirectory(), 2);
    }

    private void assertFilesInFolder(final @NotNull Path pathToFolder, final int expected) {
        File[] filesInFolder = pathToFolder.toFile().listFiles();
        assertThat(filesInFolder, notNullValue());

        // NPE is checked on a previous line
        assertThat(filesInFolder.length, is(equalTo(expected)));
    }
}