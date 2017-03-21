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

public class RepositoryTest {
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
        Repository repository = Repository.initializeVCS(rootDirectoryPath);
        checkRepositoryStructure(repository);
    }

    @Test(expected = VCSIsAlreadyInitialized.class)
    public void doubleInitialization() throws Exception {
        Repository.initializeVCS(rootDirectoryPath);
        Repository.initializeVCS(rootDirectoryPath);
    }

    @Test(expected = DirectoryExpected.class)
    public void initializationInFile() throws Exception {
        File file = rootDirectory.newFile();

        Repository.initializeVCS(file.toPath());
    }

    @Test
    public void testGetRepository() throws Exception {
        Repository.initializeVCS(rootDirectoryPath);
        Repository repository = Repository.getRepository(rootDirectoryPath);

        checkRepositoryStructure(repository);
    }

    @Test(expected = DirectoryExpected.class)
    public void getRepositoryForFile() throws Exception {
        File file = rootDirectory.newFile();
        Repository.getRepository(file.toPath());
    }

    @Test(expected = VCSWasNotInitialized.class)
    public void getNonExistingRepository() throws Exception {
        Repository.getRepository(rootDirectoryPath);
    }

    private void checkRepositoryStructure(Repository repository) throws Exception {
        // TODO: Find or write matchers
        assertThat(Files.isSameFile(repository.getRootDirectory(), rootDirectoryPath), is(true));
        assertThat(Files.isSameFile(repository.getVCSDirectory(),
                                    rootDirectoryPath.resolve(Repository.vcsDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getObjectsDirectory(),
                                    rootDirectoryPath.resolve(Repository.vcsDirectoryName)
                                                     .resolve(Repository.objectsDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getReferencesDirectory(),
                                    rootDirectoryPath.resolve(Repository.vcsDirectoryName)
                                                     .resolve(Repository.referencesDirectoryName)),
                   is(true));

        assertThat(Files.isSameFile(repository.getHeadsDirectory(),
                                    rootDirectoryPath.resolve(Repository.vcsDirectoryName)
                                                     .resolve(Repository.referencesDirectoryName)
                                                     .resolve(Repository.headsDirectoryName)),
                   is(true));

        assertThat(Files.exists(repository.getVCSDirectory()), is(true));
        assertThat(Files.exists(repository.getObjectsDirectory()), is(true));
        assertThat(Files.exists(repository.getReferencesDirectory()), is(true));
        assertThat(Files.exists(repository.getHeadsDirectory()), is(true));
    }
}