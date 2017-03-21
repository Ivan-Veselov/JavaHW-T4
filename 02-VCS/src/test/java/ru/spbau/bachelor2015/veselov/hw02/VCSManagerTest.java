package ru.spbau.bachelor2015.veselov.hw02;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.DirectoryExpected;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.VCSIsAlreadyInitialized;
import ru.spbau.bachelor2015.veselov.hw02.exceptions.VCSWasNotInitialized;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VCSManagerTest {
    @Rule
    public final @NotNull TemporaryFolder rootDirectory = new TemporaryFolder();

    // TODO: Maybe add some kind of a special rule
    private Path rootDirectoryPath;

    @Before
    public void initializeEnvironment() {
        rootDirectoryPath = rootDirectory.getRoot().toPath();
    }

    @Test
    public void simpleInitialization() throws Exception {
        VCSManager.Repository repository = VCSManager.initializeVCS(rootDirectoryPath);
        checkRepositoryStructure(repository);
    }

    @Test(expected = VCSIsAlreadyInitialized.class)
    public void doubleInitialization() throws Exception {
        VCSManager.initializeVCS(rootDirectoryPath);
        VCSManager.initializeVCS(rootDirectoryPath);
    }

    @Test(expected = DirectoryExpected.class)
    public void initializationInFile() throws Exception {
        File file = rootDirectory.newFile();

        VCSManager.initializeVCS(file.toPath());
    }

    @Test
    public void testGetRepository() throws Exception {
        VCSManager.initializeVCS(rootDirectoryPath);
        VCSManager.Repository repository = VCSManager.getRepository(rootDirectoryPath);

        checkRepositoryStructure(repository);
    }

    @Test(expected = DirectoryExpected.class)
    public void getRepositoryForFile() throws Exception {
        File file = rootDirectory.newFile();
        VCSManager.getRepository(file.toPath());
    }

    @Test(expected = VCSWasNotInitialized.class)
    public void getNonExistingRepository() throws Exception {
        VCSManager.getRepository(rootDirectoryPath);
    }

    private void checkRepositoryStructure(VCSManager.Repository repository) throws Exception {
        // TODO: Find or write matchers
        assertThat(Files.isSameFile(repository.getRootDirectory(), rootDirectoryPath), is(true));
        assertThat(Files.isSameFile(repository.getVCSDirectory(),
                                    rootDirectoryPath.resolve(VCSManager.vcsDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getObjectsDirectory(),
                                    rootDirectoryPath.resolve(VCSManager.vcsDirectoryName)
                                                     .resolve(VCSManager.objectsDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getReferencesDirectory(),
                                    rootDirectoryPath.resolve(VCSManager.vcsDirectoryName)
                                                     .resolve(VCSManager.referencesDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getHeadsDirectory(),
                                    rootDirectoryPath.resolve(VCSManager.vcsDirectoryName)
                                                     .resolve(VCSManager.referencesDirectoryName)
                                                     .resolve(VCSManager.headsDirectoryName)),
                   is(true));

        assertThat(Files.exists(repository.getVCSDirectory()), is(true));
        assertThat(Files.exists(repository.getObjectsDirectory()), is(true));
        assertThat(Files.exists(repository.getReferencesDirectory()), is(true));
        assertThat(Files.exists(repository.getHeadsDirectory()), is(true));
    }
}