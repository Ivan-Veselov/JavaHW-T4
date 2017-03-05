package ru.spbau.bachelor2015.veselov.hw02;

import org.apache.commons.codec.digest.DigestUtils;
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
    public void testAdditionOfFileInStorage() throws Exception {
        Path pathToFile = rootDirectory.newFile().toPath();
        Files.write(pathToFile, new byte[] {1, 2, 3});

        repository.addFileToStorage(pathToFile);

        String sha1HexString = DigestUtils.sha1Hex(Files.readAllBytes(pathToFile));
        // TODO: Find or write matcher
        assertThat(FileUtils.contentEquals(pathToFile.toFile(),
                                           repository.getObjectsDirectory().resolve(sha1HexString).toFile()),
                   is(true));
    }

    @Test(expected = RegularFileExpected.class)
    public void addFolderToStorage() throws Exception {
        File folder = rootDirectory.newFolder();
        repository.addFileToStorage(folder.toPath());
    }
}