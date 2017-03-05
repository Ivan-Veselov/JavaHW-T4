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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        assertThat(FileUtils.contentEquals(pathToFile.toFile(), blob.getPathToData().toFile()), is(true));
    }

    @Test(expected = RegularFileExpected.class)
    public void blobConstructionFromFolder() throws Exception {
        File folder = rootDirectory.newFolder();
        repository.new Blob(folder.toPath());
    }
}